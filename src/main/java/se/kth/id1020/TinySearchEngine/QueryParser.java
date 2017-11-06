package se.kth.id1020.TinySearchEngine;

import se.kth.id1020.util.Document;

import java.util.*;
import java.util.stream.Collectors;

public class QueryParser {
    private static final ArrayList<String> OPERATORS = new ArrayList<String>() {{
        add("+");
        add("|");
        add("-");
        }};
    private IndexHandler indexHandler;
    private Map<String, Map<Document, Double>> cache = new HashMap<>();
    private Map<String, String> infixCache = new HashMap<>();
    private String infixExpression;
    private StringBuilder infixStringBuilder;

    public QueryParser(IndexHandler indexHandler) {
        this.indexHandler = indexHandler;
    }

    public List<Document> parse(String string) throws Exception {
        if(string == null || string.length() == 0) {
            return null;
        }

        String[] s = string.split("\\s+orderby\\s+");
        String[] expression = s[0].split("\\s+");
        String[] ordering = null;
        if(s.length > 1) {
            ordering = s[1].split("\\s+");
        }

        Map<Document, Double> result;
        if(expression.length == 1) {
            infixExpression = expression[0];
            result = Relevance.calculate(new LinkedList<>(indexHandler.search(expression[0])), indexHandler);
        } else {
            result = checkPrefix(expression);
        }

        return sortDocuments(result, ordering);
    }

    private Map<Document, Double> checkPrefix(String[] expression) throws Exception {
        Deque<String> query = new ArrayDeque<>();
        Deque<String> operands = new ArrayDeque<>();
        for(String item : expression) {
            query.push(item);
        }

        if(Collections.disjoint(query, OPERATORS)) {
            throw new Exception("Missing prefix!");
        }

        while(query.size() > 1 || OPERATORS.contains(query.peek())) {
            String item = query.pop();
            if(OPERATORS.contains(item)) {
                query.push(evaluateExpression(item, operands.pop(), operands.pop()));
            } else {
                operands.push(item);
            }
        }

        return cache.get(query.pop());
    }

    private String evaluateExpression(String operator, String firstTerm, String secondTerm) {
        String expression = String.format("%s %s %s", operator, firstTerm, secondTerm);
        if(operator.equals("+") || operator.equals("|")) {
            if(firstTerm.compareTo(secondTerm) > 0) {
                expression = String.format("%s %s %s", operator, secondTerm, firstTerm);
            }
        }

        String firstInfixTerm = firstTerm;
        if(infixCache.containsKey(firstTerm)) {
            firstInfixTerm = infixCache.get(firstTerm);
        }

        String secondInfixTerm = secondTerm;
        if(infixCache.containsKey(secondTerm)) {
            secondInfixTerm = infixCache.get(secondTerm);
        }

        infixExpression = String.format("(%s %s %s)", firstInfixTerm, operator, secondInfixTerm);
        infixCache.put(expression, infixExpression);

        if(cache.containsKey(expression)) {
            return expression;
        }

        Map<Document, Double> firstTermDocuments;
        Map<Document, Double> secondTermDocuments;
        if(cache.containsKey(firstTerm)) {
            firstTermDocuments = cache.get(firstTerm);
        } else {
            firstTermDocuments = Relevance.calculate(indexHandler.search(firstTerm), indexHandler);
            cache.put(firstTerm, firstTermDocuments);
        }

        if(cache.containsKey(secondTerm)) {
            secondTermDocuments = cache.get(secondTerm);
        } else {
            secondTermDocuments = Relevance.calculate(indexHandler.search(secondTerm), indexHandler);
            cache.put(secondTerm, secondTermDocuments);
        }

        switch(operator) {
            case "+":
                cache.put(expression, getIntersection(firstTermDocuments, secondTermDocuments));
                break;
            case "|":
                cache.put(expression, getUnion(firstTermDocuments, secondTermDocuments));
                break;
            case "-":
                cache.put(expression, getDifference(firstTermDocuments, secondTermDocuments));
                break;
        }

        return expression;
    }

    private List<Document> sortDocuments(Map<Document, Double> list, String[] ordering) {
        List<Document> result = new LinkedList<>(list.keySet());
        infixStringBuilder = new StringBuilder("Query(").append(infixExpression);
        if(ordering == null) {
            infixStringBuilder.append(")");
            return result;
        }

        infixStringBuilder.append(" ORDERBY");

        if(ordering[0].equalsIgnoreCase("popularity")) {
            infixStringBuilder.append(" " + ordering[0].toUpperCase());

            if(ordering[1].equalsIgnoreCase("asc")) {
                Collections.sort(result, new CompareByPopularity());
                infixStringBuilder.append(" " + ordering[1].toUpperCase());
            }

            else if(ordering[1].equalsIgnoreCase("desc")) {
                Collections.sort(result, new CompareByPopularity().reversed());
                infixStringBuilder.append(" " + ordering[1].toUpperCase());
            }
        }

        else if(ordering[0].equalsIgnoreCase("relevance")) {
            infixStringBuilder.append(" " + ordering[0].toUpperCase());

            if(ordering[1].equalsIgnoreCase("asc")) {
                result = list.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).collect(Collectors.toList());
                infixStringBuilder.append(" " + ordering[1].toUpperCase());
            }

            else if(ordering[1].equalsIgnoreCase("desc")) {
                result = list.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).map(Map.Entry::getKey).collect(Collectors.toList());
                infixStringBuilder.append(" " + ordering[1].toUpperCase());
            }
        }

        infixStringBuilder.append(")");

        return result;
    }

    private Map<Document, Double> getIntersection(Map<Document, Double> first, Map<Document, Double> second) {
        Map<Document, Double> result = new HashMap<>();
        for(Map.Entry<Document, Double> entry : first.entrySet()) {
            if(second.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue() + second.get(entry.getKey()));
            }
        }

        return result;
    }

    private Map<Document, Double> getUnion(Map<Document, Double> first, Map<Document, Double> second) {
        Map<Document, Double> result = new HashMap<>(first);
        for (Map.Entry<Document, Double> entry : second.entrySet()) {
            if (!result.containsKey(entry.getKey()))
                result.put(entry.getKey(), entry.getValue());
            else
                result.put(entry.getKey(), result.get(entry.getKey()) + entry.getValue());
        }

        return result;
    }

    private Map<Document, Double> getDifference(Map<Document, Double> first, Map<Document, Double> second) {
        Map<Document, Double> result = new HashMap<>();
        for (Map.Entry<Document, Double> entry : first.entrySet()) {
            if (!second.containsKey(entry.getKey()))
                result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public String getInfix() {
        if(infixStringBuilder != null) {
            String infix = infixStringBuilder.toString();
            infixStringBuilder = null;

            return infix;
        }

        return null;
    }

    private static class CompareByPopularity implements Comparator<Document> {

        @Override
        public int compare(Document o1, Document o2) {
            return o1.popularity - o2.popularity;
        }
    }
}
