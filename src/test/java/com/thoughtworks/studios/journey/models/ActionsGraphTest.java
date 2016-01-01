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

import com.thoughtworks.studios.journey.ModelTestCase;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.util.*;

import static com.thoughtworks.studios.journey.TestHelper.dateToMillis;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.Iterables.iterable;

public class ActionsGraphTest extends ModelTestCase {
    @Test
    public void forOneStepSingleJourneyGraph() {
        Node j1 = setupJourney(iterable("a0"), dateToMillis(2015, 1, 1));
        ActionsGraph graph = new ActionsGraph(app, 100);
        graph.add(journeys.userRequests(j1));
        assertEquals(list("$start", "a0"), getNodeNames(graph.nodes()));
        assertEquals(set(list("$start", "a0", "1")), getLinkage(graph));
    }

    @Test
    public void forMultiStepSingleJourneyGraph() {
        Node j1 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2015, 1, 1));
        ActionsGraph graph = new ActionsGraph(app, 100);
        graph.add(journeys.userRequests(j1));
        assertEquals(list("$start", "a0", "a1", "a2"), getNodeNames(graph.nodes()));
        assertEquals(set(list("$start", "a0", "1"), list("a0", "a1", "1"), list("a1", "a2", "1")), getLinkage(graph));
    }


    @Test
    public void shouldMergeMultiStepMultipleJourneyGraph() {
        Node j1 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2015, 1, 1));
        Node j2 = setupJourney(iterable("a0", "a1", "a3"), dateToMillis(2015, 1, 1));
        ActionsGraph graph = new ActionsGraph(app, 100);
        graph.add(journeys.userRequests(j1));
        graph.add(journeys.userRequests(j2));
        assertEquals(list("$start", "a0", "a1", "a2", "a3"), getNodeNames(graph.nodes()));
        assertEquals(set(list("$start", "a0", "2"), list("a0", "a1", "2"), list("a1", "a2", "1"), list("a1", "a3", "1")), getLinkage(graph));
    }

    @Test
    public void shouldNotGenerateBackLoop() {
        Node j1 = setupJourney(iterable("a0", "a1", "a2", "a0"), dateToMillis(2015, 1, 1));
        ActionsGraph graph = new ActionsGraph(app, 100);
        graph.add(journeys.userRequests(j1));
        assertEquals(list("$start", "a0", "a1", "a2", "a0"), getNodeNames(graph.nodes()));
        assertEquals(set(list(0, 1, 1), list(1, 2, 1), list(2, 3, 1), list(3, 4, 1)), getLinkage2(graph));
    }

    private Set<List<String>> getLinkage(ActionsGraph graph) {
        Collection<ActionsGraph.Link> links = graph.links();
        List<ActionsGraph.GraphNode> nodes = graph.nodes();
        Set<List<String>> result = new HashSet<>(links.size());

        for (ActionsGraph.Link link : links) {
            ActionsGraph.GraphNode source = nodes.get(link.getSource());
            ActionsGraph.GraphNode target = nodes.get(link.getTarget());
            result.add(list(source.getName(), target.getName(), String.valueOf(link.getWeight())));
        }
        return result;
    }

    private Set<List<Integer>> getLinkage2(ActionsGraph graph) {
        Collection<ActionsGraph.Link> links = graph.links();
        Set<List<Integer>> result = new HashSet<>(links.size());

        for (ActionsGraph.Link link : links) {
            result.add(list(link.getSource(), link.getTarget(), link.getValue()));
        }
        return result;
    }

    private List<String> getNodeNames(List<ActionsGraph.GraphNode> nodes) {
        ArrayList<String> result = new ArrayList<>(nodes.size());
        for (ActionsGraph.GraphNode node : nodes) {
            result.add(node.getName());
        }
        return result;
    }

}
