package cj.netos.flow.ports;

import cj.netos.flow.*;
import cj.netos.flow.openports.entities.NetworkNode;
import cj.netos.flow.openports.ports.IFlowPorts;
import cj.netos.network.ListenMode;
import cj.netos.network.peer.ILogicNetwork;
import cj.netos.network.peer.IPeer;
import cj.netos.network.peer.Peer;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecuritySession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@CjService(name = "/flow.service")
public class DefaultFlowPorts implements IFlowPorts {
    @CjServiceSite
    IServiceSite site;
    ExecutorService exec;
    List<Future> futureList;
    @CjServiceRef(refByName = "taskQueue")
    ITaskQueue taskQueue;
    private boolean isRunning;
    @CjServiceRef(refByName = "networkNodeService")
    INetworkNodeService networkNodeService;
    Map<String, ILogicNetwork> networks;
    Map<String, IPeer> peers;

    private void checkRuning() throws CircuitException {
        if (!isRunning) {
            throw new CircuitException("500", "没有运行");
        }
    }

    private void checkRights(ISecuritySession securitySession) throws CircuitException {
        boolean hasRights = false;
        for (int i = 0; i < securitySession.roleCount(); i++) {
            if (securitySession.role(i).startsWith("app:administrators") ||
                    securitySession.role(i).startsWith("tenant:administrators") ||
                    securitySession.role(i).startsWith("platform:administrators")) {
                hasRights = true;
                break;
            }
        }
        if (!hasRights) {
            throw new CircuitException("801", "无权访问");
        }
    }

    @Override
    public void start(ISecuritySession securitySession, int workThreadCount) throws CircuitException {
        if (isRunning) {
            return;
        }
        checkRights(securitySession);
        if (workThreadCount < 1) {
            throw new CircuitException("500", "没有指定线程数");
        }
        peers = new HashMap<>();
        networks = new ConcurrentHashMap<>();

        ServiceCollection<IFlowJob> jobs = site.getServices(IFlowJob.class);
        Map<String, IFlowJob> registry = new HashMap<>();
        for (IFlowJob job : jobs) {
            CjService service = job.getClass().getAnnotation(CjService.class);
            registry.put(service.name(), job);
        }
        exec = Executors.newFixedThreadPool(workThreadCount);

        startPeers();
        INetworkBroadcast broadcast = new DefaultNetworkBroadcast(networks);

        futureList = new ArrayList<>();
        for (int i = 0; i < workThreadCount; i++) {
            IEventloop eventloop = new Eventloop(taskQueue, registry, broadcast);
            Future future = exec.submit(eventloop);
            futureList.add(future);
        }
        isRunning = true;
    }

    private void startPeers() {
        List<NetworkNode> networkNodes = networkNodeService.getAll();
        for (NetworkNode node : networkNodes) {
            try {
                _connectNetworkNode(node.getPeerName(), node.getNetworkName(), node.getUrl(), node.getOperator(), node.getPassword());
            } catch (Exception e) {
                CJSystem.logging().error(getClass(), e);
            }
        }
    }


    @Override
    public void stop(ISecuritySession securitySession) throws CircuitException {
        if (!isRunning) {
            return;
        }
        checkRights(securitySession);
        for (Future future : futureList) {
            future.cancel(true);
        }
        futureList.clear();
        exec.shutdownNow();
        taskQueue.close();
        for (String peerName : this.networks.keySet()) {
            disConnectNetworkNode(securitySession, peerName, false);
        }
        isRunning = false;
    }

    @Override
    public boolean isRunning(ISecuritySession securitySession) throws CircuitException {
        return this.isRunning;
    }

    @Override
    public void connectNetworkNode(ISecuritySession securitySession, String peerName, String networkName, String url, String operator, String password) throws CircuitException {
        checkRights(securitySession);
        checkRuning();
        if (networks.containsKey(peerName)) {
            throw new CircuitException("500", "已连接peer");
        }
        if (peerName.indexOf("/") < 0) {
            throw new CircuitException("500", "peerName格式错误，应为：flowName/nodeName");
        }
        _connectNetworkNode(peerName, networkName, url, operator, password);
    }

    private void _connectNetworkNode(String peerName, String networkName, String url, String operator, String password) {
        NetworkNode node = networkNodeService.get(peerName);
        if (node != null) {
            if (!node.getNetworkName().equals(networkName)) {
                networkNodeService.updateNetworkName(peerName, networkName);
            }
            if (!node.getUrl().equals(url)) {
                networkNodeService.updateUrl(peerName, url);
            }
            if (!node.getOperator().equals(operator)) {
                networkNodeService.updateOperator(peerName, operator);
            }
            if (!node.getPassword().equals(password)) {
                networkNodeService.updatePassword(peerName, password);
            }
        }else {
            node = new NetworkNode(peerName, networkName, url, operator, password);
            networkNodeService.save(node);
        }

        PeerEvent event = new PeerEvent(peerName, networkName, url, this.networks, this.peers, this.networkNodeService);
        IPeer peer = Peer.connect(url, event, event, event, event);
        peer.authByPassword(peerName, operator, password);

        ILogicNetwork logicNetwork = peer.listen(networkName, false, ListenMode.upstream);
        networks.put(peerName, logicNetwork);
        peers.put(peerName, peer);
        peer.viewServer();
    }

    @Override
    public void disConnectNetworkNode(ISecuritySession securitySession, String peerName, boolean isRemove) throws CircuitException {
        checkRights(securitySession);
        checkRuning();
        ILogicNetwork network = networks.get(peerName);
        if (network != null) {
            network.leave();
        }
        IPeer peer = peers.get(peerName);
        if (peer != null) {
            peer.close();
        }
        if (isRemove) {
            networkNodeService.remove(peerName);
        }
        networks.remove(peerName);
        peers.remove(peerName);
    }

    @Override
    public List<Map<String, Object>> listNetworkNode(ISecuritySession securitySession) throws CircuitException {
        checkRights(securitySession);
        List<NetworkNode> list = networkNodeService.getAll();
        List<Map<String, Object>> infoList = new ArrayList<>();
        for (NetworkNode node : list) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("networkName", node.getNetworkName());
            obj.put("operator", node.getOperator());
            obj.put("peerName", node.getPeerName());
            obj.put("url", node.getUrl());
            obj.put("ports", node.getPorts());
            obj.put("isOpened", node.isOpened());
            infoList.add(obj);
        }
        return infoList;
    }
}
