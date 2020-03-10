package cj.netos.flow;

import cj.netos.network.NetworkFrame;
import cj.studio.ecm.net.CircuitException;

public interface INetworkBroadcast {
    void broadcast(NetworkFrame frame) throws CircuitException;
}
