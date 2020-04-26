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

@CjService(name = "/geosphere/document/uncomment")
public class PushGeoDocumentUncomment implements IFlowJob {
    @CjServiceRef(refByName = "receptor")
    IGeoReceptor receptor;

    @Override
    public void flow(EventTask e, INetworkBroadcast broadcast) throws CircuitException {
        String category = (String) e.parameter("category");
        String receptor = (String) e.parameter("receptor");
        String docid = (String) e.parameter("docid");
        String uncommenter = (String) e.parameter("uncommenter");
        String commentid = (String) e.parameter("commentid");

        GeoDocument doc = this.receptor.getDocument(category, receptor, docid);
        if (doc == null) {
            CJSystem.logging().warn(getClass(), String.format("文档不存在:%s/%s", receptor, docid));
            return;
        }
        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(new Gson().toJson(doc).getBytes());
        NetworkFrame frame = new NetworkFrame("uncommentDocument /geosphere/receptor gbera/1.0", bb);
        String creator=doc.getCreator();
        frame.parameter("docid", docid);
        frame.parameter("category", category);
        frame.parameter("receptor", receptor);
        frame.parameter("creator",creator);
        frame.parameter("commentid", commentid);
        frame.head("sender", uncommenter);

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
