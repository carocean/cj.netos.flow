package cj.netos.flow.jobs;

import cj.netos.flow.EventTask;
import cj.netos.flow.IFlowJob;
import cj.netos.flow.IGeoReceptor;
import cj.netos.flow.INetworkBroadcast;
import cj.netos.flow.openports.entities.GeoDocument;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.*;

@CjService(name = "/geosphere/document/comment")
public class PushGeoDocumentComment extends PushGeoFlowJobBase {

    @Override
    public void flow(EventTask e, INetworkBroadcast broadcast) throws CircuitException {
        String category = (String) e.parameter("category");
        String receptor = (String) e.parameter("receptor");
        String docid = (String) e.parameter("docid");
        String commenter = (String) e.parameter("commenter");
        String commentid = (String) e.parameter("commentid");
        String comments = (String) e.parameter("comments");

        GeoDocument doc = this.receptor.getDocument(category, receptor, docid);
        if (doc == null) {
            CJSystem.logging().warn(getClass(), String.format("文档不存在:%s/%s", receptor, docid));
            return;
        }
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> map = new HashMap<>();
        map.put("doc", doc);
        map.put("comments", comments);

        bb.writeBytes(new Gson().toJson(map).getBytes());
        NetworkFrame frame = new NetworkFrame("commentDocument /geosphere/receptor gbera/1.0", bb);
        String creator = doc.getCreator();
        frame.parameter("docid", docid);
        frame.parameter("category", category);
        frame.parameter("receptor", receptor);
        frame.parameter("creator", creator);
        frame.parameter("commentid", commentid);
        frame.head("sender", commenter);

        Map<String, List<String>> destinations = getDestinations(category, receptor, creator);
//        CJSystem.logging().warn(getClass(), String.format("推送目标:%s", new Gson().toJson(destinations)));
        broadcast(broadcast, destinations, frame, e.interval());

        frame.dispose();
        destinations.clear();
    }
}
