package cj.netos.flow.ports;

import cj.netos.flow.openports.entities.ChannelDocumentMedia;
import cj.netos.flow.EventTask;
import cj.netos.flow.ITaskQueue;
import cj.netos.flow.openports.ports.IChannelTaskPorts;
import cj.studio.ecm.CJSystem;
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
        task.parameter("creator", securitySession.principal());
        task.parameter("channel", channel);
        task.parameter("docid", docid);
        queue.append(task);
    }

    @Override
    public void pushChannelDocumentLike(ISecuritySession securitySession, String channel, String docid, String creator,long interval) throws CircuitException {
        EventTask task = new EventTask("/channel/document/like",interval);
        task.parameter("liker", securitySession.principal());
        task.parameter("creator", creator);
        task.parameter("channel", channel);
        task.parameter("docid", docid);
        queue.append(task);
    }

    @Override
    public void pushChannelDocumentUnlike(ISecuritySession securitySession, String channel, String docid, String creator, long interval) throws CircuitException {
        EventTask task = new EventTask("/channel/document/unlike",interval);
        task.parameter("unliker", securitySession.principal());
        task.parameter("creator", creator);
        task.parameter("channel", channel);
        task.parameter("docid", docid);
        queue.append(task);
    }

    @Override
    public void pushChannelDocumentComment(ISecuritySession securitySession, String channel, String docid, String creator, String commentid,String comments,long interval) throws CircuitException {
        EventTask task = new EventTask("/channel/document/comment",interval);
        task.parameter("commenter", securitySession.principal());
        task.parameter("creator", creator);
        task.parameter("channel", channel);
        task.parameter("docid", docid);
        task.parameter("commentid", commentid);
        task.parameter("comments",comments);
        queue.append(task);
    }

    @Override
    public void pushChannelDocumentUncomment(ISecuritySession securitySession, String channel, String docid, String creator, String commentid, long interval) throws CircuitException {
        EventTask task = new EventTask("/channel/document/uncomment",interval);
        task.parameter("uncommenter", securitySession.principal());
        task.parameter("creator", creator);
        task.parameter("channel", channel);
        task.parameter("docid", docid);
        task.parameter("commentid", commentid);
        queue.append(task);
    }

    @Override
    public void pushChannelDocumentMedia(ISecuritySession securitySession, ChannelDocumentMedia media, long interval) throws CircuitException {
        EventTask task = new EventTask("/channel/document/media",interval);
        task.parameter("creator", securitySession.principal());
        task.parameter("media", media);
        queue.append(task);
    }
}
