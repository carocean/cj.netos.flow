package cj.netos.flow;

public class DocMedia {
    String type;
    String src;
    long ctime;
    String docid;

    public DocMedia(String type, String src, long ctime, String docid) {
        this.type = type;
        this.src = src;
        this.ctime = ctime;
        this.docid = docid;
    }
}
