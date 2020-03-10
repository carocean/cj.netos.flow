package cj.netos.flow;

import java.util.Map;

public class PeerLogger {
    String event;
    String peerName;
    long ctime;
    Map<String,Object> log;

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public Map<String, Object> getLog() {
        return log;
    }

    public void setLog(Map<String, Object> log) {
        this.log = log;
    }
}
