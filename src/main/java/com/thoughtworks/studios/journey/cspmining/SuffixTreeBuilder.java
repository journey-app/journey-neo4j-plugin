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
package com.thoughtworks.studios.journey.cspmining;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.jql.JourneyQuery;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.*;

import static org.neo4j.helpers.collection.Iterables.limit;
import static org.neo4j.helpers.collection.Iterables.toList;

public class SuffixTreeBuilder {
    private Application app;
    private JourneyQuery query;

    private TreeCategory treeCategory;
    private int treeHeightLimit;

    public SuffixTreeBuilder(Application app, JourneyQuery query, TreeCategory treeCategory, int treeHeightLimit) {
        this.app = app;
        this.query = query;
        this.treeCategory = treeCategory;
        this.treeHeightLimit = treeHeightLimit;
    }

    public Map<String, SuffixTree> build() {
        Map<String, SuffixTree> forest = new HashMap<>();
        LongList journeyIds = collectJourneyIds();
        Map<String, Integer> actionJourneyCount = collectActionJourneyCount(journeyIds);
        Map<String, Set<Node>> lastAddedSuffixes = new HashMap<>();


        for (int i = 0; i < journeyIds.size(); i++) {
            long journeyId = journeyIds.getLong(i);
            try (Transaction tx = app.graphDB().beginTx()) {
                Node journey = app.graphDB().getNodeById(journeyId);
                for (Node event : app.journeys().events(journey)) {
                    String actionLabel = app.events().getActionLabel(event);

                    if (!forest.containsKey(actionLabel)) {
                        SuffixTree tree = new SuffixTree(app, actionLabel, journeyIds.size());
                        tree.setCategory(treeCategory);
                        tree.setJourneysCount(actionJourneyCount.get(actionLabel));
                        forest.put(actionLabel, tree);
                    }

                    SuffixTree tree = forest.get(actionLabel);
                    List<Node> suffix = toList(limit(treeHeightLimit, app.journeys().suffixFor(event)));

                    if (!hasOverlap(lastAddedSuffixes.get(actionLabel), suffix)) {
                        tree.addSuffix(getNames(suffix), journeyId);
                        lastAddedSuffixes.put(actionLabel, new HashSet<>(suffix));
                    }
                }

                tx.success();
            }
        }


        return forest;
    }

    private boolean hasOverlap(Set<Node> lastAdded, List<Node> sequence) {
        if (lastAdded == null) {
            return false;
        }

        for (Node event : sequence) {
            if (lastAdded.contains(event)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getNames(Iterable<Node> events) {
        ArrayList<String> names = new ArrayList<>(treeHeightLimit);
        for (Node event : events) {
            names.add(app.events().getActionLabel(event));
        }
        return names;
    }

    private HashMap<String, Integer> collectActionJourneyCount(LongList journeyIds) {
        Map<String, LongSet> involvedJourneyIds = new HashMap<>();
        try (Transaction ignored = app.graphDB().beginTx()) {
            for (int i = 0; i < journeyIds.size(); i++) {
                long journeyId = journeyIds.getLong(i);
                Node journey = app.graphDB().getNodeById(journeyId);

                for (Node event : app.journeys().events(journey)) {
                    String actionLabel = app.events().getActionLabel(event);

                    if (!involvedJourneyIds.containsKey(actionLabel)) {
                        involvedJourneyIds.put(actionLabel, new LongOpenHashSet());
                    }
                    involvedJourneyIds.get(actionLabel).add(journeyId);
                }
            }
        }

        HashMap<String, Integer> result = new HashMap<>();
        for (String actionLabel : involvedJourneyIds.keySet()) {
            result.put(actionLabel, involvedJourneyIds.get(actionLabel).size());
        }
        return result;
    }

    private LongList collectJourneyIds() {
        LongList journeyIds = new LongArrayList();

        try (Transaction ignored = app.graphDB().beginTx()) {
            for (Node journey : query.journeys()) {
                journeyIds.add(journey.getId());
            }
        }
        return journeyIds;
    }

}
