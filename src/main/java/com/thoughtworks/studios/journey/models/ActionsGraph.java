/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provids out-of-box action path analysis features on top of the graph database.
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
package com.thoughtworks.studios.journey.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.graphdb.Node;

import java.util.*;

import static org.neo4j.helpers.collection.Iterables.limit;

public class ActionsGraph {
    private final HashMap<Integer, Link> links;
    private Map<String, GraphNode> nodes;
    private Application app;
    private int stepLimit;

    public ActionsGraph(Application app, int stepLimit) {
        this.app = app;
        this.stepLimit = stepLimit;
        this.nodes = new HashMap<>();
        this.links = new HashMap<>();

        findOrCreateNode("$start", 0);
    }

    public void add(Iterable<Node> requests) {
        Iterable<Node> limited = limit(stepLimit, requests);
        int step = 0;
        GraphNode previous = findOrCreateNode("$start", step);
        for (Node request : limited) {
            step ++;
            String actionLabel = app.requests().getActionLabel(request);
            GraphNode current = findOrCreateNode(actionLabel, step);
            if (previous != null) {
                Link link = findOrCreateLink(previous, current);
                link.increaseWeight();
            }
            previous = current;
        }
    }

    private Link findOrCreateLink(GraphNode src, GraphNode target) {
        int key = cantorpi(src.index + 1, target.index + 1); // plus on to make sure nature number

        Link existing = links.get(key);
        if (existing != null) {
            return existing;
        }

        Link fresh = new Link(src, target);
        links.put(key, fresh);
        return fresh;
    }

    // pair function: http://en.wikipedia.org/wiki/Pairing_function#Cantor_pairing_function
    private int cantorpi(int k1, int k2) {
        return (k1 + k2) * (k1 + k2 + 1) / 2 + k2;
    }

    private GraphNode findOrCreateNode(String actionLabel, int step) {
        String key = String.valueOf(step) + "$" + actionLabel;

        GraphNode existing = nodes.get(key);
        if (existing != null) {
            return existing;
        }

        GraphNode fresh = new GraphNode(actionLabel, nodes.size(), step);
        nodes.put(key, fresh);

        return fresh;
    }

    @JsonProperty("nodes")
    public List<GraphNode> nodes() {
        ArrayList<GraphNode> result = new ArrayList<>(nodes.size());
        result.addAll(nodes.values());
        Collections.sort(result);
        return result;
    }

    @JsonProperty("links")
    public Collection<Link> links() {
        ArrayList<Link> result = new ArrayList<>(links.size());
        result.addAll(links.values());
        Collections.sort(result);
        return result;
    }

    public static class GraphNode implements Comparable<GraphNode> {
        private String name;
        private final int index;
        private int step;

        public GraphNode(String name, int index, int step) {
            this.name = name;
            this.index = index;
            this.step = step;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphNode graphNode = (GraphNode) o;
            return name.equals(graphNode.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public int compareTo(GraphNode another) {
            return this.index - another.index;
        }

        public int getStep() {
            return step;
        }
    }

    public static class Link implements Comparable<Link>{
        private int source;
        private int target;
        private int weight = 0;

        public Link(GraphNode src, GraphNode target) {
            this.source = src.getIndex();
            this.target = target.getIndex();

        }

        public int getSource() {
            return source;
        }

        public int getTarget() {
            return target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Link link = (Link) o;

            if (source != link.source) return false;
            if (target != link.target) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = source;
            result = 31 * result + target;
            return result;
        }

        public int getWeight() {
            return weight;
        }

        public int getValue() {
            return weight;
        }

        public void increaseWeight() {
            this.weight++;
        }

        @Override
        public int compareTo(Link other) {
            return other.weight - this.weight;
        }
    }
}
