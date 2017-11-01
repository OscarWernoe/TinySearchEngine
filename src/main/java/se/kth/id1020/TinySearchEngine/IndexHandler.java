package se.kth.id1020.TinySearchEngine;

import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Word;

import java.util.*;

public class IndexHandler {
    private Map<String, List<Document>> index = new HashMap<>();

    public void insert(Word word, Attributes attr) {
        String key = word.word;
        if(!index.containsKey(key)) {
            index.put(key, new LinkedList<>());
        }

        index.get(key).add(attr.document);
    }
}
