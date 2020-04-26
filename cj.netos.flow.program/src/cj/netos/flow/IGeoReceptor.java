package cj.netos.flow;

import cj.netos.flow.openports.entities.GeoDocument;
import cj.netos.flow.openports.entities.GeoReceptor;

import java.util.List;
import java.util.Map;

public interface IGeoReceptor {
    GeoDocument getDocument(String category, String receptor,String docid);
    Map<String, List<String>> searchAroundReceptors(String category, String receptor, String geoType, long limit, long skip);
    List<String> pageReceptorFans( String category, String receptor, long limit, long skip);
}
