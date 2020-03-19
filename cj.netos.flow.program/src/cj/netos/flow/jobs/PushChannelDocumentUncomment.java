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

@CjService(name = "/channel/document/uncomment")
public class PushChannelDocumentUncomment implements IFlowJob {
    @CjServiceRef(refByName = "channel")
    IChannel channel;

    @Override
    public void flow(EventTask e, INetworkBroadcast broadcast) throws CircuitException {
        String creator = (String) e.parameter("creator");
        String channel = (String) e.parameter("channel");
        String docid = (String) e.parameter("docid");
        String uncommenter = (String) e.parameter("uncommenter");
        String commentid = (String) e.parameter("commentid");

        ChannelDocument doc = this.channel.getDocument(creator, channel, docid);
        if (doc == null) {
            CJSystem.logging().warn(getClass(), String.format("文档不存在:%s/%s", channel, docid));
            return;
        }
        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(new Gson().toJson(doc).getBytes());
        NetworkFrame frame = new NetworkFrame("uncommentDocument /netflow/channel gbera/1.0", bb);
        frame.parameter("uncommenter", uncommenter);
        frame.parameter("docid", docid);
        frame.parameter("channel", channel);
        frame.parameter("creator", creator);
        frame.parameter("commentid", commentid);


        List<String> sendedPerson = new ArrayList<>();
        //先推送给创建者
        broadcastToCreator(broadcast, frame.copy(), creator, uncommenter, e.interval());
        sendedPerson.add(creator);


        long limit = 100;
        long skip = 0;
        CJSystem.logging().debug(getClass(), String.format("开始推送输出公众"));
        while (true) {
            List<String> outputPersons = this.channel.findOutputPersons(uncommenter, channel, limit, skip);
            if (outputPersons.isEmpty()) {
                break;
            }
            skip += outputPersons.size();
            frame.head("sender", uncommenter);
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
        CJSystem.logging().debug(getClass(), String.format("开始推送流转用户"));
        skip = 0;
        while (true) {
            List<String> activities = this.channel.findFlowActivities(creator, docid, channel, limit, skip);
            if (activities.isEmpty()) {
                break;
            }
            skip += activities.size();
            frame.head("sender", uncommenter);
            for (String person : activities) {
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

    private void broadcastToCreator(INetworkBroadcast broadcast, NetworkFrame frame, String creator, String liker, long interval) throws CircuitException {
        frame.head("sender", liker);
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
