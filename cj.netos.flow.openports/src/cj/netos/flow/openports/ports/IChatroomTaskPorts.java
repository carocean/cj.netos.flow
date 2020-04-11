package cj.netos.flow.openports.ports;

import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.PKeyInRequest;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

@CjOpenports(usage = "聊天室任务开放服务")
public interface IChatroomTaskPorts extends IOpenportService {
    @CjOpenport(usage = "推文.当前令牌持有者要推送指定的文档，该文档必须是持有者发布的文档",command = "post")
    void pushMessage(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "聊天室创建者", name = "creator")
                    String creator,
            @CjOpenportParameter(usage = "聊天室", name = "room")
                    String room,
            @CjOpenportParameter(usage = "消息标识", name = "msgid")
                    String msgid,
            @CjOpenportParameter(usage = "消息类型。text|file|audio|video|image|share|email|等等", name = "contentType")
                    String contentType,
            @CjOpenportParameter(usage = "消息内容，是具有格式的内容，不同的消息类型格式不同", name = "content",in = PKeyInRequest.content)
                    String content,
            @CjOpenportParameter(usage = "每侦推送的时间间隔，0为立即发送", name = "interval", defaultValue = "10") long interval
    ) throws CircuitException;

}
