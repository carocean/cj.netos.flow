package cj.netos.flow.jobs;

import cj.netos.flow.EventTask;
import cj.netos.flow.IChatroom;
import cj.netos.flow.IFlowJob;
import cj.netos.flow.INetworkBroadcast;
import cj.netos.flow.openports.entities.Chatroom;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

@CjService(name = "/chat/message")
public class PushChatMessage implements IFlowJob {
    @CjServiceRef
    IChatroom chatroom;

    @Override
    public void flow(EventTask e, INetworkBroadcast broadcast) throws CircuitException {
        String room = (String) e.parameter("room");
        String msgid = (String) e.parameter("msgid");
        String contentType = (String) e.parameter("contentType");
        String content = (String) e.parameter("content");
        String msgcreator = (String) e.parameter("creator");

        Chatroom chatroom = this.chatroom.getRoom(msgcreator, room);
        if (chatroom == null) {
            CJSystem.logging().warn(getClass(), String.format("聊天室不存在:%s/%s", msgcreator, room));
            return;
        }

        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(content.getBytes());
        NetworkFrame frame = new NetworkFrame("pushMessage /chat/room/message gbera/1.0", bb);
        frame.parameter("room", room);
        frame.parameter("contentType", contentType);
        frame.parameter("msgid", msgid);
        frame.head("sender", msgcreator);

        List<String> sendedPersons = new ArrayList<>();

        long limit = 100;
        long skip = 0;
        while (true) {
            List<String> members = this.chatroom.pageMember(chatroom.getCreator(), room, limit, skip);
            if (members.isEmpty()) {
                break;
            }
            skip += members.size();
            for (String person : members) {
                if (sendedPersons.contains(person)) {
                    continue;
                }
                frame.head("to-person", person);
                broadcast.broadcast(frame.copy());
                sendedPersons.add(person);
                if (e.interval() > 0) {
                    try {
                        Thread.sleep(e.interval());
                    } catch (InterruptedException ex) {
                        CJSystem.logging().warn(getClass(), ex);
                    }
                }
            }
        }
    }

}
