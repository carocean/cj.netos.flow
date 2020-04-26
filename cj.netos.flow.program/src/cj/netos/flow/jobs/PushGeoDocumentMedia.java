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
public class PushGeoDocumentMedia implements IFlowJob {
    @CjServiceRef(refByName = "receptor")
    IGeoReceptor receptor;

    @Override
    public void flow(EventTask e, INetworkBroadcast broadcast) throws CircuitException {
        Map<String,Object> media = (Map<String, Object>) e.parameter("media");
        String category =(String) media.get("category");
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
        String creator=doc.getCreator();
        frame.parameter("docid", docid);
        frame.parameter("category", category);
        frame.parameter("receptor", receptor);
        frame.parameter("creator",creator);
        frame.head("sender", mediacreator);

        List<String> sendedPersons = new ArrayList<>();
        //先推送给创建者
        broadcastToCreator(broadcast, frame.copy(), creator,  e.interval());
        sendedPersons.add(creator);

        long limit = 100;
        long skip = 0;
        while (true) {
            Map<String, List<String>> outputPersons = this.receptor.searchAroundReceptors(category, receptor,null, limit, skip);
            if (outputPersons.isEmpty()) {
                break;
            }
            skip += outputPersons.size();
            Set<String> creators=outputPersons.keySet();
            for (String person : creators) {
                if (sendedPersons.contains(person)) {
                    continue;
                }
                List<String> receptorids = outputPersons.get(person);
                frame.head("to-receptors",new Gson().toJson(receptorids));
                frame.head("to-person", person);
                broadcast.broadcast(frame.copy());
                sendedPersons.add(person);
                if (e.interval() > 0) {
                    try {
                        Thread.sleep(e.interval());
                    } catch (InterruptedException ex) {
                        CJSystem.logging().warn(getClass(), ex);
                    }
                }
            }
        }

        skip=0;
        while (true) {
            List<String> outputPersons = this.receptor.pageReceptorFans(category, receptor, limit, skip);
            if (outputPersons.isEmpty()) {
                break;
            }
            skip += outputPersons.size();
            for (String person : outputPersons) {
                if (sendedPersons.contains(person)) {
                    continue;
                }
                List<String> receptorids = new ArrayList<>();
                receptorids.add(String.format("%s/%s", category, receptor));
                frame.head("to-receptors", new Gson().toJson(receptorids));
                frame.head("to-person", person);
                broadcast.broadcast(frame.copy());
                sendedPersons.add(person);
                if (e.interval() > 0) {
                    try {
                        Thread.sleep(e.interval());
                    } catch (InterruptedException ex) {
                        CJSystem.logging().warn(getClass(), ex);
                    }
                }
            }
        }

        frame.dispose();
        sendedPersons.clear();
    }

    private void broadcastToCreator(INetworkBroadcast broadcast, NetworkFrame frame, String creator, long interval) throws CircuitException {
        List<String> receptorids = new ArrayList<>();
        String category=frame.parameter("category");
        String receptor=frame.parameter("receptor");
        receptorids.add(String.format("%s/%s", category, receptor));
        frame.head("to-receptors", new Gson().toJson(receptorids));
        frame.head("to-person", creator);
        broadcast.broadcast(frame);
        if (interval > 0) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
                CJSystem.logging().warn(getClass(), ex);
            }
        }
    }
}
