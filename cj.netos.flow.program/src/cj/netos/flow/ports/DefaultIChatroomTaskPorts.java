package cj.netos.flow.ports;

import cj.netos.flow.EventTask;
import cj.netos.flow.ITaskQueue;
import cj.netos.flow.openports.ports.IChatroomTaskPorts;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecuritySession;

@CjService(name = "/chat.service")
public class DefaultIChatroomTaskPorts implements IChatroomTaskPorts {
    @CjServiceRef(refByName = "taskQueue")
    ITaskQueue queue;

    @Override
    public void pushMessage(ISecuritySession securitySession, String room, String msgid, String contentType, String content, long interval) throws CircuitException {
        EventTask task = new EventTask("/chat/message", interval);
        task.parameter("creator", securitySession.principal());
        task.parameter("room", room);
        task.parameter("msgid", msgid);
        task.parameter("contentType", contentType);
        task.parameter("content", content);
        queue.append(task);
    }
}
