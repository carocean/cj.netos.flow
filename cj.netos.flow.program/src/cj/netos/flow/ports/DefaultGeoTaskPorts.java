package cj.netos.flow.ports;

import cj.netos.flow.EventTask;
import cj.netos.flow.ITaskQueue;
import cj.netos.flow.openports.entities.GeoDocumentMedia;
import cj.netos.flow.openports.ports.IGeosphereTaskPorts;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecuritySession;
@CjService(name = "/geosphere.service")
public class DefaultGeoTaskPorts implements IGeosphereTaskPorts {
    @CjServiceRef(refByName = "taskQueue")
    ITaskQueue queue;
    @Override
    public void pushGeoDocument(ISecuritySession securitySession, String category, String receptor, String docid, long interval) throws CircuitException {
        EventTask task = new EventTask("/geosphere/document",interval);
        task.parameter("creator", securitySession.principal());
        task.parameter("category", category);
        task.parameter("receptor", receptor);
        task.parameter("docid", docid);
        task.parameter("sender", securitySession.principal());
        queue.append(task);
    }

    @Override
    public void pushGeoDocumentOfPerson(ISecuritySession securitySession, String category, String receptor, String docid, String creator, long interval) throws CircuitException {
        EventTask task = new EventTask("/geosphere/document",interval);
        task.parameter("creator", creator);
        task.parameter("category", category);
        task.parameter("receptor", receptor);
        task.parameter("docid", docid);
        task.parameter("sender", securitySession.principal());
        queue.append(task);
    }

    @Override
    public void pushGeoDocumentLike(ISecuritySession securitySession, String category, String receptor, String docid, String creator, long interval) throws CircuitException {
        EventTask task = new EventTask("/geosphere/document/like",interval);
        task.parameter("liker", securitySession.principal());
        task.parameter("creator", creator);
        task.parameter("category", category);
        task.parameter("receptor", receptor);
        task.parameter("docid", docid);
        queue.append(task);
    }

    @Override
    public void pushGeoDocumentUnlike(ISecuritySession securitySession, String category, String receptor, String docid, String creator, long interval) throws CircuitException {
        EventTask task = new EventTask("/geosphere/document/unlike",interval);
        task.parameter("unliker", securitySession.principal());
        task.parameter("creator", creator);
        task.parameter("category", category);
        task.parameter("receptor", receptor);
        task.parameter("docid", docid);
        queue.append(task);
    }

    @Override
    public void pushGeoDocumentComment(ISecuritySession securitySession, String category, String receptor, String docid, String creator, String commentid, String comments, long interval) throws CircuitException {
        EventTask task = new EventTask("/geosphere/document/comment",interval);
        task.parameter("commenter", securitySession.principal());
        task.parameter("creator", creator);
        task.parameter("category", category);
        task.parameter("receptor", receptor);
        task.parameter("docid", docid);
        task.parameter("commentid", commentid);
        task.parameter("comments",comments);
        queue.append(task);
    }

    @Override
    public void pushGeoDocumentUncomment(ISecuritySession securitySession, String category, String receptor, String docid, String creator, String commentid, long interval) throws CircuitException {
        EventTask task = new EventTask("/geosphere/document/uncomment",interval);
        task.parameter("uncommenter", securitySession.principal());
        task.parameter("creator", creator);
        task.parameter("category", category);
        task.parameter("receptor", receptor);
        task.parameter("docid", docid);
        task.parameter("commentid", commentid);
        queue.append(task);
    }

    @Override
    public void pushGeoDocumentMedia(ISecuritySession securitySession, GeoDocumentMedia media, long interval) throws CircuitException {
        EventTask task = new EventTask("/geosphere/document/media",interval);
        task.parameter("creator", securitySession.principal());
        task.parameter("media", media);
        queue.append(task);
    }
}
