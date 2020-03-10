package cj.netos.flow.openports.entities;

import java.util.Map;

public class NetworkNode {
    private String peerName;
    private String networkName;
    private String url;
    private String operator;
    private String password;
    Map<String,Object> ports;
    boolean isOpened;

    public NetworkNode() {
    }

    public NetworkNode(String peerName, String networkName, String url, String operator, String password) {
        this.peerName = peerName;
        this.networkName = networkName;
        this.url = url;
        this.operator = operator;
        this.password = password;
    }

    public Map<String, Object> getPorts() {
        return ports;
    }

    public void setPorts(Map<String, Object> ports) {
        this.ports = ports;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOperator() {

        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
