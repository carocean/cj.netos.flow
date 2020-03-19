package cj.netos.flow.openports.ports;

import cj.netos.flow.openports.entities.ChannelDocumentMedia;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.PKeyInRequest;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

@CjOpenports(usage = "管道推文任务")
public interface IChannelTaskPorts extends IOpenportService {
    @CjOpenport(usage = "推文.当前令牌持有者要推送指定的文档，该文档必须是持有者发布的文档")
    void pushChannelDocument(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "管道号", name = "channel")
                    String channel,
            @CjOpenportParameter(usage = "文档号", name = "docid")
                    String docid,
            @CjOpenportParameter(usage = "每侦推送的时间间隔，0为立即发送", name = "interval", defaultValue = "10") long interval
    ) throws CircuitException;

    @CjOpenport(usage = "推赞。当前令牌持有者赞了指定的文档")
    void pushChannelDocumentLike(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "管道号", name = "channel")
                    String channel,
            @CjOpenportParameter(usage = "文档号", name = "docid")
                    String docid,
            @CjOpenportParameter(usage = "文档所有者", name = "creator")
                    String creator,
            @CjOpenportParameter(usage = "每侦推送的时间间隔，0为立即发送", name = "interval", defaultValue = "10") long interval
    ) throws CircuitException;

    @CjOpenport(usage = "推取消赞。当前令牌持有者赞了指定的文档")
    void pushChannelDocumentUnlike(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "管道号", name = "channel")
                    String channel,
            @CjOpenportParameter(usage = "文档号", name = "docid")
                    String docid,
            @CjOpenportParameter(usage = "文档所有者", name = "creator")
                    String creator,
            @CjOpenportParameter(usage = "每侦推送的时间间隔，0为立即发送", name = "interval", defaultValue = "10") long interval
    ) throws CircuitException;

    @CjOpenport(usage = "推评论.当前令牌持有者评论了指定的文档")
    void pushChannelDocumentComment(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "管道号", name = "channel")
                    String channel,
            @CjOpenportParameter(usage = "文档号", name = "docid")
                    String docid,
            @CjOpenportParameter(usage = "文档所有者", name = "creator")
                    String creator,
            @CjOpenportParameter(usage = "评论号", name = "commentid")
                    String commentid,
            @CjOpenportParameter(usage = "评论内容", name = "comments")
                    String comments,
            @CjOpenportParameter(usage = "每侦推送的时间间隔，0为立即发送", name = "interval", defaultValue = "10") long interval
    ) throws CircuitException;

    @CjOpenport(usage = "推取消评论.当前令牌持有者评论了指定的文档")
    void pushChannelDocumentUncomment(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "管道号", name = "channel")
                    String channel,
            @CjOpenportParameter(usage = "文档号", name = "docid")
                    String docid,
            @CjOpenportParameter(usage = "文档所有者", name = "creator")
                    String creator,
            @CjOpenportParameter(usage = "评论号", name = "commentid")
                    String commentid,
            @CjOpenportParameter(usage = "每侦推送的时间间隔，0为立即发送", name = "interval", defaultValue = "10") long interval
    ) throws CircuitException;

    @CjOpenport(usage = "推送文档的媒体文件",command = "post")
    void pushChannelDocumentMedia(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "多媒体内容", name = "media",in = PKeyInRequest.content)
                    ChannelDocumentMedia media,
            @CjOpenportParameter(usage = "每侦推送的时间间隔，0为立即发送", name = "interval", defaultValue = "10") long interval
    ) throws CircuitException;

}
