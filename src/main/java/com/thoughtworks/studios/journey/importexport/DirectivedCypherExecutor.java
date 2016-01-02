/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provides out-of-box action path analysis features on top of the graph database.
 *
 * Copyright 2015 ThoughtWorks, Inc. and Pengchao Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
