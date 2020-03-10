package cj.netos.flow.ports;

import cj.netos.flow.EventTask;
import cj.netos.flow.ITaskQueue;
import cj.netos.flow.openports.ports.IChannelTaskPorts;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecuritySession;

@CjService(name = "/channel.service")
public class DefaultChannelTaskPorts implements IChannelTaskPorts {
    @CjServiceRef(refByName = "taskQueue")
    ITaskQueue queue;

    @Override
    public void pushChannelDocument(ISecuritySession securitySession, String channel, String docid,long interval) throws CircuitException {
        EventTask task = new EventTask("/channel/document",interval);
        task.parameter("docOwner", securitySession.principal());
        task.parameter("channel", channel);
        task.parameter("docid", docid);
        queue.append(task);
    }

    @Override
    public void pushChannelDocumentLike(ISecuritySession securitySession, String channel, String docid, String docOwner,long interval) throws CircuitException {
        EventTask task = new EventTask("/channel/document/like",interval);
        task.parameter("liker", securitySession.principal());
        task.parameter("docOwner", docOwner);
        task.parameter("channel", channel);
        task.parameter("docid", docid);
        queue.append(task);
    }

    @Override
    public void pushChannelDocumentComment(ISecuritySession securitySession, String channel, String docid, String docOwner, String commentid,long interval) throws CircuitException {
        EventTask task = new EventTask("/channel/document/comment",interval);
        task.parameter("commenter", securitySession.principal());
        task.parameter("docOwner", docOwner);
        task.parameter("channel", channel);
        task.parameter("docid", docid);
        task.parameter("commentid", commentid);
        queue.append(task);
    }
}
