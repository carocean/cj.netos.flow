package cj.netos.flow.jobs;

import cj.netos.flow.EventTask;
import cj.netos.flow.IChannel;
import cj.netos.flow.IFlowJob;
import cj.netos.flow.INetworkBroadcast;
import cj.netos.flow.openports.entities.ChannelDocumentMedia;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;
import java.util.Map;

@CjService(name = "/channel/document/media")
public class PushChannelDocumentMedia implements IFlowJob {
    @CjServiceRef(refByName = "channel")
    IChannel channel;

    @Override
    public void flow(EventTask e, INetworkBroadcast broadcast) throws CircuitException {
        String creator = (String) e.parameter("creator");
        Map media = (Map) e.parameter("media");

        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(new Gson().toJson(media).getBytes());
        NetworkFrame frame = new NetworkFrame("mediaDocument /netflow/channel gbera/1.0", bb);
        frame.parameter("creator", creator);
        long limit = 100;
        long skip = 0;
        while (true) {
            List<String> outputPersons = this.channel.findOutputPersons(creator, (String)media.get("channel"), limit, skip);
            if (outputPersons.isEmpty()) {
                break;
            }
            skip += outputPersons.size();
            frame.head("sender", creator);
            for (String person : outputPersons) {
                frame.head("to-person", person);
                broadcast.broadcast(frame.copy());
                if (e.interval() > 0) {
                    try {
                        Thread.sleep(e.interval());
                    } catch (InterruptedException ex) {
                        CJSystem.logging().warn(getClass(), ex);
                    }
                }
            }
        }
        frame.dispose();
    }
}
