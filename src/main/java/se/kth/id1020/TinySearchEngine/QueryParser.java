package se.kth.id1020.TinySearchEngine;

import se.kth.id1020.util.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryParser {
    private Map<String, Map<Document, Double>> cache = new HashMap<>();

    public static List<Document> parse(String s, IndexHandler indexHandler) {
        return null;
    }
}
