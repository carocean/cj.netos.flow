package cj.netos.flow;

import cj.netos.network.ListenMode;
import cj.netos.network.NetworkFrame;
import cj.netos.network.peer.*;
import cj.studio.ecm.CJSystem;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class PeerEvent implements IOnopen, IOnreconnection, IOnnotify, IOnclose {
    private final INetworkNodeService networkNodeService;
    private final String networkName;
    private final Map<String, ILogicNetwork> networks;
    private final Map<String, IPeer> peers;
    String peerName;
    String url;

    public PeerEvent(String peerName, String networkName, String url, Map<String, ILogicNetwork> networks, Map<String, IPeer> peers, INetworkNodeService networkNodeService) {
        this.peerName = peerName;
        this.url = url;
        this.networkNodeService = networkNodeService;
        this.networkName = networkName;
        this.networks = networks;
        this.peers = peers;
    }

    @Override
    public void onopen() {
        CJSystem.logging().info(getClass(), String.format("连接已打开。%s %s", peerName, url));
        networkNodeService.onopen(peerName);
    }

    @Override
    public void onclose() {
        CJSystem.logging().info(getClass(), String.format("连接已关闭。%s %s", peerName, url));
        networkNodeService.onclose(peerName);
    }

    @Override
    public void onreconnected(String protocol, String host, int port, Map<String, String> props) {
        CJSystem.logging().info(getClass(), String.format("重连... %s %s", peerName, url));
        IPeer peer = peers.get(peerName);
        try {
            ILogicNetwork network = networks.get(peerName);
            if (network != null) {
                network.leave();
                networks.remove(peerName);
            }
            network = peer.listen(networkName, false, ListenMode.upstream);
            networks.put(peerName, network);
            peer.viewServer();
        } catch (Exception e) {
            CJSystem.logging().error(getClass(), e);
        }
        networkNodeService.onreconnect(peerName);
    }

    @Override
    public void onevent(NetworkFrame frame) {
        CJSystem.logging().info(getClass(), String.format("系统事件。%s %s %s", peerName, url, frame));
        Map<String, Object> map = new Gson().fromJson(frame.toJson(), HashMap.class);
        if (frame.command().equals("viewServer")) {
            networkNodeService.reportNodeServerInfo(peerName, map);
        }
        networkNodeService.onevent(peerName, map);
    }

    @Override
    public void onerror(NetworkFrame frame) {
        Map<String, Object> map = new Gson().fromJson(frame.toJson(), HashMap.class);
        CJSystem.logging().info(getClass(), String.format("系统错误。%s %s %s", peerName, url, map));
        networkNodeService.onerror(peerName, map);
    }
}
