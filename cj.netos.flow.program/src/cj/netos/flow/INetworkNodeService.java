package cj.netos.flow;

import cj.netos.flow.openports.entities.NetworkNode;

import java.util.List;
import java.util.Map;

public interface INetworkNodeService {
    NetworkNode get(String peerName);


    void remove(String peerName);

    List<NetworkNode> getAll();

    void save(NetworkNode node);

    void reportNodeServerInfo(String peerName, Map<String, Object> map);

    void onopen(String peerName);

    void updateState(String peerName, boolean isOpened);

    void onclose(String peerName);

    void onreconnect(String peerName);

    void onerror(String peerName, Map<String, Object> map);

    void onevent(String peerName, Map<String, Object> map);


    void updateNetworkName(String peerName, String networkName);

    void updateUrl(String peerName, String url);

    void updateOperator(String peerName, String operator);

    void updatePassword(String peerName, String password);

}
