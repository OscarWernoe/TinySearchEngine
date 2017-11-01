package se.kth.id1020.TinySearchEngine;

import se.kth.id1020.TinySearchEngineBase;
import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Sentence;
import se.kth.id1020.util.Word;

import java.util.List;

public class TinySearchEngine implements TinySearchEngineBase {
    private IndexHandler indexHandler = new IndexHandler();

    @Override
    public void preInserts() {}

    @Override
    public void insert(Sentence sentence, Attributes attr) {
        for(Word word : sentence.getWords()) {
            indexHandler.insert(word, attr);
        }
    }

    @Override
    public void postInserts() {

    }

    @Override
    public List<Document> search(String s) {
        try {
            return QueryParser.parse(s, indexHandler);
        } catch(Exception e) {
            System.out.println(e.getMessage());

            return null;
        }
    }

    @Override
    public String infix(String s) {
        return null;
    }
}
