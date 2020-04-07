package cj.netos.flow;

import cj.netos.flow.openports.entities.Chatroom;

import java.util.List;

public interface IChatroom {
    Chatroom getRoom(String msgcreator, String room);

    List<String> pageMember(String creator, String room, long limit, long skip);

}
