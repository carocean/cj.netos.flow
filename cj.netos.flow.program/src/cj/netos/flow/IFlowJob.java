package cj.netos.flow;

import cj.studio.ecm.net.CircuitException;

public interface IFlowJob {
    void flow(EventTask e, INetworkBroadcast broadcast) throws CircuitException;
}
