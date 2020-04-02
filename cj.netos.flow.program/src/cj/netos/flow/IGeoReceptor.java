package cj.netos.flow;

import cj.netos.flow.openports.entities.GeoDocument;

import java.util.List;

public interface IGeoReceptor {
    GeoDocument getDocument(String category, String receptor,String docid);
    List<String> searchAroundReceptors(String category, String receptor, String geoType, long limit, long skip);
    List<String> pageReceptorFans( String category, String receptor, long limit, long skip);
}
