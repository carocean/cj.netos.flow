package cj.netos.flow;

import java.util.List;

public interface IChannel {
    List<String> findOutputPersons(String person, String channel, long limit, long skip);

    ChannelDocument getDocument(String person, String channel, String docid);

}
