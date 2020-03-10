package cj.netos.flow;

import cj.netos.network.NetworkFrame;
import cj.netos.network.peer.ILogicNetwork;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;

import java.util.Map;

public class DefaultNetworkBroadcast implements INetworkBroadcast {
    Map<String, ILogicNetwork> networks;

    public DefaultNetworkBroadcast(Map<String, ILogicNetwork> networks) {
        this.networks = networks;
    }

    @Override
    public void broadcast(NetworkFrame frame) throws CircuitException {
        for (String key : networks.keySet()) {
            ILogicNetwork network = networks.get(key);
            if (network == null) {
                continue;
            }
            network.send(frame.copy());
            CJSystem.logging().debug(getClass(), String.format("分发给节点:%s 侦:%s 目标：%s", key, frame,frame.head("to-person")));
        }
        frame.dispose();
    }
}
