package cj.netos.flow;

import cj.studio.ecm.CJSystem;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Eventloop implements IEventloop {
    private final INetworkBroadcast broadcast;
    Map<String, IFlowJob> registry;
    private ITaskQueue queue;

    public Eventloop(ITaskQueue queue, Map<String, IFlowJob> registry, INetworkBroadcast broadcast) {
        this.queue = queue;
        this.registry = registry;
        this.broadcast = broadcast;
    }

    @Override
    public Object call() throws Exception {
        while (!Thread.interrupted()) {
            EventTask event = queue.selectOne(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            if (event == null) {
                continue;
            }
            synchronized (event.taskName) {
                IFlowJob job = registry.get(event.taskName);
                try {
                    job.flow(event, broadcast);
                } catch (Exception e) {
                    CJSystem.logging().error(getClass(), e);
                }
            }
        }
        return null;
    }
}
