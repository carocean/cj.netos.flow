package cj.netos.flow.jobs;

import cj.netos.flow.*;
import cj.netos.flow.openports.entities.ChannelDocument;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

@CjService(name = "/channel/document")
public class PushChannelDocument implements IFlowJob {
    @CjServiceRef(refByName = "channel")
    IChannel channel;

    @Override
    public void flow(EventTask e, INetworkBroadcast broadcast) throws CircuitException {
        String creator = (String) e.parameter("creator");
        String channel = (String) e.parameter("channel");
        String docid = (String) e.parameter("docid");
        String sender = (String) e.parameter("sender");

        ChannelDocument doc = this.channel.getDocument(creator, channel, docid);
        if (doc == null) {
            CJSystem.logging().warn(getClass(), String.format("文档不存在:%s/%s", channel, docid));
            return;
        }
        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(new Gson().toJson(doc).getBytes());
        NetworkFrame frame = new NetworkFrame("pushDocument /netflow/channel gbera/1.0", bb);
        frame.parameter("docid", docid);
        frame.parameter("channel", channel);
        frame.parameter("creator",creator);
        frame.head("sender", sender);

        List<String> sendedPerson = new ArrayList<>();
        //先推送给创建者
        broadcastToCreator(broadcast, frame.copy(), creator,  e.interval());
        sendedPerson.add(creator);

        long limit = 100;
        long skip = 0;
        while (true) {
            List<String> outputPersons = this.channel.findOutputPersons(sender, channel, limit, skip);
            if (outputPersons.isEmpty()) {
                break;
            }
            skip += outputPersons.size();
            for (String person : outputPersons) {
                if (sendedPerson.contains(person)) {
                    continue;
                }
                frame.head("to-person", person);
                broadcast.broadcast(frame.copy());
                sendedPerson.add(person);
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
        sendedPerson.clear();
    }

    private void broadcastToCreator(INetworkBroadcast broadcast, NetworkFrame frame, String creator, long interval) throws CircuitException {
        frame.head("to-person", creator);
        broadcast.broadcast(frame);
        if (interval > 0) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
                CJSystem.logging().warn(getClass(), ex);
            }
        }
    }
}
