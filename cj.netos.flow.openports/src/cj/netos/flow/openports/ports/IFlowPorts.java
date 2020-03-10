package cj.netos.flow.openports.ports;

import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

import java.util.List;
import java.util.Map;

@CjOpenports(usage = "流转服务控制器")
public interface IFlowPorts extends IOpenportService {
    @CjOpenport(usage = "启动")
    void start(ISecuritySession securitySession,
               @CjOpenportParameter(usage = "线程数", name = "workThreadCount")
                       int workThreadCount
    ) throws CircuitException;

    @CjOpenport(usage = "停止")
    void stop(ISecuritySession securitySession) throws CircuitException;

    @CjOpenport(usage = "是否在运行")
    boolean isRunning(ISecuritySession securitySession) throws CircuitException;

    @CjOpenport(usage = "连接网络节点。")
    void connectNetworkNode(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "节点名。命名规则：flow本地名/远程节点名。本地名与节点名可以随意起。如果重名会覆盖掉旧的配置", name = "peerName")
                    String peerName,
            @CjOpenportParameter(usage = "要侦听的逻辑网络名", name = "networkName")
                    String networkName,
            @CjOpenportParameter(usage = "连接地址", name = "url")
                    String url,
            @CjOpenportParameter(usage = "操作员", name = "operator")
                    String person,
            @CjOpenportParameter(usage = "密码", name = "password")
                    String password
    ) throws CircuitException;

    @CjOpenport(usage = "断开网络节点")
    void disConnectNetworkNode(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "节点名", name = "peer")
                    String peer,
            @CjOpenportParameter(usage = "是否从节点列表中移除", name = "isRemove", defaultValue = "false")
                    boolean isRemove
    ) throws CircuitException;

    @CjOpenport(usage = "查看网络节点，必须是管理员权限")
    List<Map<String, Object>> listNetworkNode(ISecuritySession securitySession) throws CircuitException;
}
