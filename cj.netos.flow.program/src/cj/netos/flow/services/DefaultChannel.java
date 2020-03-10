package cj.netos.flow.services;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.netos.flow.AbstractService;
import cj.netos.flow.ChannelDocument;
import cj.netos.flow.DocMedia;
import cj.netos.flow.IChannel;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.*;

@CjService(name = "channel")
public class DefaultChannel extends AbstractService implements IChannel {
    @Override
    public List<String> findOutputPersons(String person, String channel, long limit, long skip) {
        ICube cube = cube(person);
        String outputStrategy = _getOutputStrategy(cube, channel);
        if (StringUtil.isEmpty(outputStrategy)) {
            CJSystem.logging().warn(getClass(), String.format("没有发现用户输出策略:%s", channel));
            return new ArrayList<>();
        }
        switch (outputStrategy) {
            case "all_except":
                return _allExceptOutputPersons(cube, channel, limit, skip);
            case "only_select":
                return _onlySelectOutputPersons(cube, channel, limit, skip);
            default:
                return new ArrayList<>();
        }
    }

    private List<String> _onlySelectOutputPersons(ICube cube, String channel, long limit, long skip) {
        AggregateIterable aggregateIterable = cube.aggregate("output.persons", Arrays.asList(
                Document.parse(String.format("{'$match':{'tuple.channel':'%s'}}", channel)),
                Document.parse(String.format("{'$group':{'_id':'$tuple.person'}}")),
                Document.parse(String.format("{'$limit':%s}", limit)),
                Document.parse(String.format("{'$skip':%s}", skip)),
                Document.parse(String.format("{'$project':{'_id':0,'person':'$_id'}}"))));
        MongoCursor<Document> it = aggregateIterable.iterator();
        List<String> persons = new ArrayList<>();
        while (it.hasNext()) {
            Document document = it.next();
            persons.add((String)document.get("person"));
        }
        return persons;
    }

    private List<String> _allExceptOutputPersons(ICube cube, String channel, long limit, long skip) {
        List<String> selectPersons = _onlySelectOutputPersons(cube, channel, Integer.MAX_VALUE, 0);

        AggregateIterable aggregateIterable = cube.aggregate("persons", Arrays.asList(
                Document.parse(String.format("{'$match':{'tuple.official':{'$nin':%s}}}", new Gson().toJson(selectPersons))),
                Document.parse(String.format("{'$group':{'_id':'$tuple.official'}}")),
                Document.parse(String.format("{'$limit':%s}", limit)),
                Document.parse(String.format("{'$skip':%s}", skip)),
                Document.parse(String.format("{'$project':{'_id':0,'person':'$_id'}}"))));
        MongoCursor<Document> it = aggregateIterable.iterator();
        List<String> persons = new ArrayList<>();
        while (it.hasNext()) {
            Document document = it.next();
            persons.add((String)document.get("person"));
        }
        return persons;
    }

    private String _getOutputStrategy(ICube cube, String channel) {
        String cjql = String.format("select {'tuple':'*'}.limit(1) from tuple channels ?(clazz) where {'tuple.channel':'?(channel)'}");
        IQuery<Map<String, Object>> query = cube.createQuery(cjql);
        query.setParameter("clazz", HashMap.class.getName());
        query.setParameter("channel", channel);
        IDocument<Map<String, Object>> doc = query.getSingleResult();
        if (doc == null) {
            return null;
        }
        return doc.tuple().get("outPersonSelector") + "";
    }

    @Override
    public ChannelDocument getDocument(String person, String channel, String docid) {
        ICube cube = cube(person);
        String cjql = String.format("select {'tuple':'*'}.limit(1) from tuple network.channel.documents ?(clazz) where {'tuple.channel':'?(channel)','tuple.id':'?(docid)'}");
        IQuery<ChannelDocument> query = cube.createQuery(cjql);
        query.setParameter("clazz", ChannelDocument.class.getName());
        query.setParameter("channel", channel);
        query.setParameter("docid", docid);
        IDocument<ChannelDocument> doc = query.getSingleResult();
        if (doc == null) {
            return null;
        }
        cjql = String.format("select {'tuple':'*'}.limit(1) from tuple network.channel.documents.medias ?(clazz) where {'tuple.docid':'?(docid)'}");
        IQuery<DocMedia> query2 = cube.createQuery(cjql);
        query2.setParameter("clazz", DocMedia.class.getName());
        query2.setParameter("docid", docid);
        List<IDocument<DocMedia>> medias = query2.getResultList();
        List<DocMedia> _medias = new ArrayList<>();
        for (IDocument<DocMedia> media : medias) {
            _medias.add(media.tuple());
        }
        ChannelDocument channelDoc = doc.tuple();
        channelDoc.setMedias(_medias);
        return channelDoc;
    }
}
