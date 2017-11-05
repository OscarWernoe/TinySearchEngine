package se.kth.id1020.TinySearchEngine;

import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Word;

import java.util.*;

public class IndexHandler {
    private Map<String, List<Document>> index = new HashMap<>();
    private Map<Document, List<String>> termsInDocument = new HashMap<>();

    public void insert(Word word, Attributes attr) {
        String key = word.word;
        if(!index.containsKey(key)) {
            index.put(key, new LinkedList<>());
        }

        index.get(key).add(attr.document);
    }

    public List<Document> search(String key) {
        return index.get(key);
    }

    public void countTerms() {
        for(Map.Entry<String, List<Document>> entry : index.entrySet()) {
            for(Document doc : entry.getValue()) {
                if(!termsInDocument.containsKey(doc)) {
                    termsInDocument.put(doc, new LinkedList<>());
                }

                termsInDocument.get(doc).add(entry.getKey());
            }
        }
    }

    public double getNumTermsInDocument(Document doc) {
        return termsInDocument.get(doc).size();
    }

    public double getNumDocuments() {
        return termsInDocument.size();
    }
}
