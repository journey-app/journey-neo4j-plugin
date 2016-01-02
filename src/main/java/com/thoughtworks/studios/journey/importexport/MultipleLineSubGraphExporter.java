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

import org.neo4j.cypher.export.SubGraph;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterables;

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MultipleLineSubGraphExporter {
    private SubGraph graph;

    public MultipleLineSubGraphExporter(SubGraph subGraph) {
        this.graph = subGraph;
    }

    public void export(PrintWriter out) {
        appendNodes(out);
        appendRelationships(out);
    }


    private String quote(String id) {
        return "`" + id + "`";
    }

    private String labelString(Node node) {
        Iterator<Label> labels = node.getLabels().iterator();
        if (!labels.hasNext()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        while (labels.hasNext()) {
            Label next = labels.next();
            result.append(":").append(quote(next.name()));
        }
        return result.toString();
    }

    private String identifier(Node node) {
        return "_" + node.getId();
    }


    private void appendRelationships(PrintWriter out) {
        for (Node node : graph.getNodes()) {
            for (Relationship rel : node.getRelationships(Direction.OUTGOING)) {
                appendRelationship(out, rel);
            }
        }
    }

    private void appendRelationship(PrintWriter out, Relationship rel) {
        String startIdentifier = identifier(rel.getStartNode());
        String endIdentifier = identifier(rel.getEndNode());

        out.print("match ");
        out.print(startIdentifier);
        out.print(", ");
        out.print(endIdentifier);
        out.print(" where id(");
        out.print(startIdentifier);
        out.print(")={");
        out.print(startIdentifier);
        out.print("} and id(");
        out.print(endIdentifier);
        out.print(")={");
        out.print(endIdentifier);
        out.print("} create ");
        out.print(startIdentifier);
        out.print("-[:");
        out.print(quote(rel.getType().name()));
        formatProperties(out, rel);
        out.print("]->");
        out.print(endIdentifier);
        out.print(";//in:");
        out.print(startIdentifier);
        out.print(",");
        out.print(endIdentifier);
        out.println();
    }

    private void appendNodes(PrintWriter out) {
        for (Node node : graph.getNodes()) {
            appendNode(out, node);
        }
    }

    private void appendNode(PrintWriter out, Node node) {
        out.print("create (");
        out.print(identifier(node));
        String labels = labelString(node);
        if (!labels.isEmpty()) {
            out.print(labels);
        }
        formatProperties(out, node);
        out.print(") return id(");
        out.print(identifier(node));
        out.print(") as ");
        out.print(identifier(node));
        out.println(";//out");
    }

    private void formatProperties(PrintWriter out, PropertyContainer pc) {
        if (!pc.getPropertyKeys().iterator().hasNext()) {
            return;
        }
        out.print(" ");
        final String propertyString = formatProperties(pc);
        out.print(propertyString);
    }

    private String formatProperties(PropertyContainer pc) {
        StringBuilder result = new StringBuilder();
        List<String> keys = Iterables.toList(pc.getPropertyKeys());
        Collections.sort(keys);
        for (String prop : keys) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(quote(prop)).append(":");
            Object value = pc.getProperty(prop);
            result.append(toString(value));
        }
        return "{" + result + "}";
    }

    private String toString(Iterator<?> iterator) {
        StringBuilder result = new StringBuilder();
        while (iterator.hasNext()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            Object value = iterator.next();
            result.append(toString(value));
        }
        return "[" + result + "]";
    }

    private String arrayToString(Object value) {
        StringBuilder result = new StringBuilder();
        int length = Array.getLength(value);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(toString(Array.get(value, i)));
        }
        return "[" + result + "]";
    }

    private String escapeString(String value) {
        return "\"" +
                value.replaceAll("\\\\", "\\\\\\\\")
                        .replaceAll("\"", "\\\\\"")
                        .replaceAll("\n", "\\\\n")
                + "\"";
    }

    private String toString(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return escapeString((String) value);
        }
        if (value instanceof Float || value instanceof Double) {
            return String.format(Locale.ENGLISH, "%f", value);
        }
        if (value instanceof Iterator) {
            return toString(((Iterator) value));
        }
        if (value instanceof Iterable) {
            return toString(((Iterable) value).iterator());
        }
        if (value.getClass().isArray()) {
            return arrayToString(value);
        }
        return value.toString();
    }

}
