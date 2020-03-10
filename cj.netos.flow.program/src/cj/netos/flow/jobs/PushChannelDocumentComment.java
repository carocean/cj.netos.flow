package cj.netos.flow.jobs;

import cj.netos.flow.*;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

@CjService(name = "/channel/document/comment")
public class PushChannelDocumentComment implements IFlowJob {
    @CjServiceRef(refByName = "channel")
    IChannel channel;
    @Override
    public void flow(EventTask e, INetworkBroadcast broadcast) throws CircuitException {
        String docOwner = (String) e.parameter("docOwner");
        String channel = (String) e.parameter("channel");
        String docid = (String) e.parameter("docid");
        String commenter = (String) e.parameter("commenter");
        String commentid = (String) e.parameter("commentid");

        ChannelDocument doc = this.channel.getDocument(docOwner, channel, docid);
        if (doc == null) {
            CJSystem.logging().warn(getClass(), String.format("文档不存在:%s/%s", channel, docid));
            return;
        }
        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(new Gson().toJson(doc).getBytes());
        NetworkFrame frame = new NetworkFrame("commentDocument /netflow/channel gbera/1.0", bb);
        frame.parameter("commenter",commenter);
        frame.parameter("docid", docid);
        frame.parameter("channel", channel);
        frame.parameter("owner",docOwner);
        frame.parameter("commentid",commentid);
        long limit = 100;
        long skip = 0;
        while (true) {
            List<String> outputPersons = this.channel.findOutputPersons(docOwner, channel, limit, skip);
            if (outputPersons.isEmpty()) {
                break;
            }
            skip += outputPersons.size();
            frame.head("sender", docOwner);
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
