package cj.netos.flow.services;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.flow.INetworkNodeService;
import cj.netos.flow.PeerLogger;
import cj.netos.flow.openports.entities.NetworkNode;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CjService(name = "networkNodeService")
public class DefaultNetworkNodeService implements INetworkNodeService {
    final String _KEY_COL_NAME = "flow.network.nodes";
    final String _KEY_LOG_NAME = "flow.network.logs";
    @CjServiceRef(refByName = "mongodb.netos.home")
    ICube home;

    @Override
    public NetworkNode get(String peerName) {
        String cjql = "select {'tuple':'*'}.limit(1) from tuple ?(colname) ?(clazz) where {'tuple.peerName':'?(peerName)'}";
        IQuery<NetworkNode> query = home.createQuery(cjql);
        query.setParameter("colname", _KEY_COL_NAME);
        query.setParameter("clazz", NetworkNode.class.getName());
        query.setParameter("peerName", peerName);
        IDocument<NetworkNode> doc = query.getSingleResult();
        if (doc == null) {
            return null;
        }
        return doc.tuple();
    }

    @Override
    public void remove(String peerName) {
        home.deleteDocOne(_KEY_COL_NAME, String.format("{'tuple.peerName':'%s'}", peerName));
    }

    @Override
    public List<NetworkNode> getAll() {
        String cjql = "select {'tuple':'*'} from tuple ?(colname) ?(clazz) where {}";
        IQuery<NetworkNode> query = home.createQuery(cjql);
        query.setParameter("colname", _KEY_COL_NAME);
        query.setParameter("clazz", NetworkNode.class.getName());
        List<NetworkNode> list = new ArrayList<>();
        List<IDocument<NetworkNode>> docs = query.getResultList();
        for (IDocument<NetworkNode> doc : docs) {
            list.add(doc.tuple());
        }
        return list;
    }


    @Override
    public void save(NetworkNode node) {
        home.saveDoc(_KEY_COL_NAME, new TupleDocument<>(node));
    }

    @Override
    public void updateNetworkName(String peerName, String networkName) {
        Document filter = Document.parse(String.format("{'tuple.peerName':'%s'}", peerName));
        Document update = Document.parse(String.format("{'$set':{'tuple.networkName':'%s'}}", networkName));
        home.updateDocOne(_KEY_COL_NAME, filter, update);
    }

    @Override
    public void updateUrl(String peerName, String url) {
        Document filter = Document.parse(String.format("{'tuple.peerName':'%s'}", peerName));
        Document update = Document.parse(String.format("{'$set':{'tuple.url':'%s'}}", url));
        home.updateDocOne(_KEY_COL_NAME, filter, update);
    }

    @Override
    public void updateOperator(String peerName, String operator) {
        Document filter = Document.parse(String.format("{'tuple.peerName':'%s'}", peerName));
        Document update = Document.parse(String.format("{'$set':{'tuple.operator':'%s'}}", operator));
        home.updateDocOne(_KEY_COL_NAME, filter, update);
    }

    @Override
    public void updatePassword(String peerName, String password) {
        Document filter = Document.parse(String.format("{'tuple.peerName':'%s'}", peerName));
        Document update = Document.parse(String.format("{'$set':{'tuple.password':'%s'}}", password));
        home.updateDocOne(_KEY_COL_NAME, filter, update);
    }

    @Override
    public void reportNodeServerInfo(String peerName, Map<String, Object> map) {
        String json = (String) map.get("content");
//        Map<String, Object> ports = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {
//        }.getType());
        Document filter = Document.parse(String.format("{'tuple.peerName':'%s'}", peerName));
        Document update = Document.parse(String.format("{'$set':{'tuple.ports':%s}}", json));
        home.updateDocOne(_KEY_COL_NAME, filter, update);
    }

    @Override
    public void onopen(String peerName) {
        PeerLogger logger = new PeerLogger();
        logger.setPeerName(peerName);
        logger.setCtime(System.currentTimeMillis());
        logger.setEvent("onopen");
        logger.setLog(new HashMap<>());
        home.saveDoc(_KEY_LOG_NAME, new TupleDocument<>(logger));

        updateState(peerName,true);
    }
    @Override
    public void updateState(String peerName, boolean isOpened) {
        Document filter = Document.parse(String.format("{'tuple.peerName':'%s'}", peerName));
        Document update = Document.parse(String.format("{'$set':{'tuple.isOpened':%s}}",isOpened));
        home.updateDocOne(_KEY_COL_NAME, filter, update);
    }
    @Override
    public void onclose(String peerName) {
        PeerLogger logger = new PeerLogger();
        logger.setPeerName(peerName);
        logger.setCtime(System.currentTimeMillis());
        logger.setEvent("onclose");
        logger.setLog(new HashMap<>());
        home.saveDoc(_KEY_LOG_NAME, new TupleDocument<>(logger));

        Document filter = Document.parse(String.format("{'tuple.peerName':'%s'}", peerName));
        Document update = Document.parse(String.format("{'$set':{'tuple.isOpened':false}}"));
        home.updateDocOne(_KEY_COL_NAME, filter, update);
    }

    @Override
    public void onreconnect(String peerName) {
        PeerLogger logger = new PeerLogger();
        logger.setPeerName(peerName);
        logger.setCtime(System.currentTimeMillis());
        logger.setEvent("onreconnect");
        logger.setLog(new HashMap<>());
        home.saveDoc(_KEY_LOG_NAME, new TupleDocument<>(logger));
    }

    @Override
    public void onerror(String peerName, Map<String, Object> map) {
        PeerLogger logger = new PeerLogger();
        logger.setPeerName(peerName);
        logger.setCtime(System.currentTimeMillis());
        logger.setEvent("onerror");
        logger.setLog(map);
        home.saveDoc(_KEY_LOG_NAME, new TupleDocument<>(logger));
    }

    @Override
    public void onevent(String peerName, Map<String, Object> map) {
        PeerLogger logger = new PeerLogger();
        logger.setPeerName(peerName);
        logger.setCtime(System.currentTimeMillis());
        logger.setEvent("onevent");
        logger.setLog(map);
        home.saveDoc(_KEY_LOG_NAME, new TupleDocument<>(logger));
    }
}
