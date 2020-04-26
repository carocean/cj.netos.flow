package cj.netos.flow.jobs;

import cj.netos.flow.EventTask;
import cj.netos.flow.IFlowJob;
import cj.netos.flow.IGeoReceptor;
import cj.netos.flow.INetworkBroadcast;
import cj.netos.flow.openports.entities.GeoDocument;
import cj.netos.flow.openports.entities.GeoDocumentMedia;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CjService(name = "/geosphere/document/media")
public class PushGeoDocumentMedia extends PushGeoFlowJobBase {

    @Override
    public void flow(EventTask e, INetworkBroadcast broadcast) throws CircuitException {
        Map<String, Object> media = (Map<String, Object>) e.parameter("media");
        String category = (String) media.get("category");
        String receptor = (String) media.get("receptor");
        String docid = (String) media.get("docid");
        String mediacreator = (String) e.parameter("creator");

        GeoDocument doc = this.receptor.getDocument(category, receptor, docid);
        if (doc == null) {
            CJSystem.logging().warn(getClass(), String.format("文档不存在:%s/%s", receptor, docid));
            return;
        }
        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(new Gson().toJson(media).getBytes());
        NetworkFrame frame = new NetworkFrame("mediaDocument /geosphere/receptor gbera/1.0", bb);
        String creator = doc.getCreator();
        frame.parameter("docid", docid);
        frame.parameter("category", category);
        frame.parameter("receptor", receptor);
        frame.parameter("creator", creator);
        frame.head("sender", mediacreator);

        Map<String, List<String>> destinations = getDestinations(category, receptor, creator);
//        CJSystem.logging().warn(getClass(), String.format("推送目标:%s", new Gson().toJson(destinations)));
        broadcast(broadcast, destinations, frame, e.interval());

        frame.dispose();
        destinations.clear();
    }

}
