package se.kth.id1020.TinySearchEngine;

import se.kth.id1020.util.Document;

import java.util.*;

public class Relevance {
    public static Map<Document, Double> calculate(List<Document> list, IndexHandler indexHandler) {
        List<Document> distinctDocuments = new LinkedList<>(new HashSet<>(list));
        Map<Document, Double> result = new HashMap<>();
        for(Document doc : distinctDocuments) {
            result.put(doc, tfidf(getTermCount(doc, list), indexHandler.getNumTermsInDocument(doc), indexHandler.getNumDocuments(), distinctDocuments.size()));
        }

        return result;
    }

    private static double tfidf(double termCount, double numTermsInDocument, double numDocuments, double numDocumentsContainingTerm) {
        return getTermFrequency(termCount, numTermsInDocument) * getInverseDocumentFrequency(numDocuments, numDocumentsContainingTerm);
    }

    private static double getTermFrequency(double termCount, double numTermsInDocument) {
        return termCount / numTermsInDocument;
    }

    private static double getInverseDocumentFrequency(double numDocuments, double numDocumentsContainingTerm) {
        return Math.log10(numDocuments / numDocumentsContainingTerm);
    }

    private static int getTermCount(Document doc, List<Document> list) {
        return Collections.frequency(list, doc);
    }
}
