package se.kth.id1020;

import se.kth.id1020.TinySearchEngine.TinySearchEngine;

public class Main {

    public static void main(String[] args) throws Exception {
        TinySearchEngineBase searchEngine = new TinySearchEngine();
        Driver.run(searchEngine);
    }
}
