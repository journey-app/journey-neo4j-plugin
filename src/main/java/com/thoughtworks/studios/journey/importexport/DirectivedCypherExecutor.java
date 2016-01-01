package com.thoughtworks.studios.journey.importexport;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectivedCypherExecutor {

    private final HashMap<String, Object> bindings;
    private GraphDatabaseService db;

    public DirectivedCypherExecutor(GraphDatabaseService db) {
        this.db = db;
        bindings = new HashMap<>();
    }

    public void execute(String cypher) {
        Directive directive = parseDirective(extractDirective(cypher));
        directive.execute(cypher, bindings);
    }

    private Directive parseDirective(String directive) {
        if(directive == null) {
            return new NullDirective(db);
        }
        String[] parts = directive.split(":");

        if(parts[0].equals("in")) {
            return new ParamBindingDirective(db, parts[1].split(","));
        }

        if(parts[0].equals("out")) {
            return new ParamOutputDirective(db);
        }

        return new NullDirective(db);
    }

    private String extractDirective(String cypher) {
        String[] parts = cypher.split("//");
        if(parts.length < 2) {
            return null;
        }
        return parts[parts.length - 1];
    }

    private interface Directive {
        void execute(String cypher, HashMap<String, Object> bindings);
    }

    private static class NullDirective implements Directive {
        private GraphDatabaseService db;

        public NullDirective(GraphDatabaseService db) {
            this.db = db;
        }

        @Override
        public void execute(String cypher, HashMap<String, Object> bindings) {
            db.execute(cypher);
        }
    }

    private class ParamBindingDirective implements Directive {
        private final GraphDatabaseService db;
        private final String[] params;

        public ParamBindingDirective(GraphDatabaseService db, String[] params) {
            this.db = db;
            this.params = params;
        }

        @Override
        public void execute(String cypher, HashMap<String, Object> bindings) {
            HashMap<String, Object> parameters = new HashMap<>(params.length);
            for (String param : params) {
                parameters.put(param, bindings.get(param));
            }

            db.execute(cypher, parameters);
        }
    }

    private class ParamOutputDirective implements Directive {
        private final GraphDatabaseService db;

        public ParamOutputDirective(GraphDatabaseService db) {
            this.db = db;
        }

        @Override
        public void execute(String cypher, HashMap<String, Object> bindings) {
            Result result = db.execute(cypher);
            List<String> columns = result.columns();
            Map<String, Object> row = result.next();

            for (String column : columns) {
                bindings.put(column, row.get(column));
            }
        }
    }
}
