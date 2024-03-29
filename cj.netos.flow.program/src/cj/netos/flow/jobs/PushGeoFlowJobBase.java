package cj.netos.flow.jobs;

import cj.netos.flow.IFlowJob;
import cj.netos.flow.IGeoReceptor;
import cj.netos.flow.INetworkBroadcast;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.util.*;

public abstract class  PushGeoFlowJobBase   implements IFlowJob {
    @CjServiceRef(refByName = "receptor")
    IGeoReceptor receptor;

    protected void broadcast(INetworkBroadcast broadcast, Map<String, List<String>> destinations, NetworkFrame frame, long interval) throws CircuitException {
        Set<String> persons=destinations.keySet();
        for (String person : persons) {
            List<String> ids = destinations.get(person);
            frame.head("to-receptors", new Gson().toJson(ids));
            frame.head("to-person", person);
            broadcast.broadcast(frame.copy());
            if (interval > 0) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    CJSystem.logging().warn(getClass(), ex);
                }
            }
        }
    }

    protected Map<String, List<String>> getDestinations(String category, String receptor, String creator) {
        Map<String, List<String>> destinations = new HashMap<>();
        List<String> keypair = new ArrayList<>();
        //消息创建者发消息创建者的目标注释掉
//        keypair.add(String.format("%s/%s", category, receptor));
//        destinations.put(creator, keypair);

        long limit = 100;
        long skip = 0;
        while (true) {
            Map<String, List<String>> personReceptors = this.receptor.searchAroundReceptors(category, receptor, null, limit, skip);
            if (personReceptors.isEmpty()) {
                break;
            }
            CJSystem.logging().warn(getClass(), String.format("感知用户数:%s", personReceptors.size()));
            skip += personReceptors.size();
            Set<String> creators = personReceptors.keySet();
            for (String person : creators) {
                keypair = destinations.get(person);
                if (keypair == null) {
                    keypair = new ArrayList<>();
                    destinations.put(person, keypair);
                }
                List<String> _keypairs=personReceptors.get(person);
                for(String id:_keypairs) {
                    if (keypair.contains(id)) {
                        continue;
                    }
                    keypair.add(id);
                }
            }
        }
        skip = 0;
        while (true) {
            List<String> personReceptors = this.receptor.pageReceptorFans(category, receptor, limit, skip);
            if (personReceptors.isEmpty()) {
                break;
            }
            CJSystem.logging().warn(getClass(), String.format("粉丝数:%s", personReceptors.size()));
            skip += personReceptors.size();
            for (String person : personReceptors) {
                keypair = destinations.get(person);
                if (keypair == null) {
                    keypair = new ArrayList<>();
                    destinations.put(person, keypair);
                }
                String id = String.format("%s/%s", category, receptor);
                if (keypair.contains(id)) {
                    continue;
                }
                keypair.add(id);
            }
        }
        return destinations;
    }
}
