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
package com.thoughtworks.studios.journey.models;

import com.thoughtworks.studios.journey.utils.GraphDbUtils;
import org.neo4j.function.Function;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.index.lucene.QueryContext;
import org.neo4j.index.lucene.ValueContext;

import java.util.*;

import static com.thoughtworks.studios.journey.utils.GraphDbUtils.getSingleEndNode;
import static org.neo4j.helpers.collection.Iterables.*;

public class Journeys implements Models {
    public static final String PROP_START_AT = "start_at";
    public static final String PROP_FINISH_AT = "finish_at";
    public static final String PROP_SESSION_ID = "session_id";
    public static final String IDX_PROP_UID = "uid";
    public static final Long CUT_TOLERANT = 2 * 60 * 60 * 1000L; // 2 hours
    public static final String IDX_PROP_ACTION_IDS = "action_ids";
    public static final String PROP_LENGTH = "length";
    public static final Set<String> INDEXED_FIELDS = new HashSet<>(3);

    private final Application app;
    private final Map<String, LinkedList<Node>> journeysCache;
    private final ChronologicalChain chainHelper;
    private final Events events;
    private GraphDatabaseService graphDb;

    public Journeys(Application application) {
        this.app = application;
        this.graphDb = application.graphDB();
        this.journeysCache = new HashMap<>();
        chainHelper = new ChronologicalChain(Events.PROP_START_AT);
        this.events = app.events();
    }

    public void setupSchema() {
        GraphDbUtils.createIndexIfNotExists(graphDb, getLabel(), PROP_SESSION_ID);
    }

    private Index<Node> legacyIndex() {
        return GraphDbUtils.legacyIndex(graphDb, getLabel());
    }

    public Label getLabel() {
        return app.nameSpacedLabel("Journey");
    }

    public ResourceIterator<Node> findAllBySessionId(String sessionId) {
        return graphDb.findNodes(getLabel(), PROP_SESSION_ID, sessionId);
    }

    public Long getStartAt(Node node) {
        return (Long) node.getProperty(PROP_START_AT);
    }

    public Long getFinishAt(Node node) {
        return (Long) node.getProperty(PROP_FINISH_AT);
    }

    public Node addEvent(String sessionId, String userIdentifier, Node event, Node action) {
        Node journey = findOrCreateCoveredBySessionId(sessionId, event, userIdentifier);
        GraphDbUtils.connectUnique(journey, RelTypes.JOURNEY_ACTIONS, action);
        expandTimeRange(journey, events.getStartAt(event));
        event.createRelationshipTo(journey, RelTypes.BELONGS_TO);
        legacyIndex().add(journey, IDX_PROP_ACTION_IDS, new ValueContext(action.getId()).indexNumeric());
        chainHelper.insert(journey, event);
        incrementLength(journey);
        return journey;
    }

    private void incrementLength(Node journey) {
        journey.setProperty(PROP_LENGTH, length(journey) + 1);
        legacyIndex().remove(journey, PROP_LENGTH);
        legacyIndex().add(journey, PROP_LENGTH, new ValueContext(length(journey)).indexNumeric());
    }

    private void expandTimeRange(Node journey, Long eventAt) {
        Index<Node> index = legacyIndex();
        ValueContext valueContext = new ValueContext(eventAt).indexNumeric();
        if (chainHelper.isEmpty(journey)) {
            journey.setProperty(PROP_START_AT, eventAt);
            journey.setProperty(PROP_FINISH_AT, eventAt);

            index.add(journey, PROP_START_AT, valueContext);
            index.add(journey, PROP_FINISH_AT, valueContext);
        } else {
            if (eventAt < getStartAt(journey)) {
                journey.setProperty(PROP_START_AT, eventAt);
                index.remove(journey, PROP_START_AT);
                index.add(journey, PROP_START_AT, valueContext);
            }

            if (eventAt > getFinishAt(journey)) {
                journey.setProperty(PROP_FINISH_AT, eventAt);
                index.remove(journey, PROP_FINISH_AT);
                index.add(journey, PROP_FINISH_AT, valueContext);
            }
        }
    }


    // test only
    public long count() {
        return IteratorUtil.count(graphDb.findNodes(getLabel()));
    }

    /**
     * User events that belongs to the journey, following next link
     *
     * @param journey the journey neo4j node
     * @return nodes belongs to the journey in earliest first order
     */
    public Iterable<Node> events(final Node journey) {
        return rejectIgnored(chainHelper.nodes(journey));
    }

    private Iterable<Node> rejectIgnored(Iterable<Node> evs) {
        return filter(new Predicate<Node>() {
            @Override
            public boolean accept(Node event) {
                return !app.actions().isIgnored(events.action(event));
            }
        }, evs);
    }

    private Node findOrCreateCoveredBySessionId(String sessionId, Node event, String userIdentifier) {
        if (journeysCache.containsKey(sessionId)) {
            LinkedList<Node> journeys = journeysCache.get(sessionId);
            for (Node journey : journeys) {
                if (covers(journey, event, userIdentifier)) {
                    return journey;
                }
            }
        }

        ResourceIterator<Node> journeys = findAllBySessionId(sessionId);

        while (journeys.hasNext()) {
            Node journey = journeys.next();
            if (covers(journey, event, userIdentifier)) {
                putIntoCache(sessionId, journey);
                return journey;
            }
        }

        Node journey = graphDb.createNode(getLabel());
        journey.setProperty(PROP_SESSION_ID, sessionId);
        putIntoCache(sessionId, journey);
        return journey;
    }

    private void putIntoCache(String sessionId, Node journey) {
        if (!journeysCache.containsKey(sessionId)) {
            journeysCache.put(sessionId, new LinkedList<Node>());
        }

        journeysCache.get(sessionId).addFirst(journey);
    }

    private boolean covers(Node journey, Node event, String userIdentifier) {
        String journeyUserId = userIdentifier(journey);
        return (journeyUserId == null || userIdentifier == null || journeyUserId.equals(userIdentifier)) &&
                events.getStartAt(event) >= getStartAt(journey) - CUT_TOLERANT &&
                events.getStartAt(event) <= getFinishAt(journey) + CUT_TOLERANT;

    }

    @Override
    public Map<String, Object> toHash(Node journey) {
        return toHash(journey, Integer.MAX_VALUE, 0);
    }

    public Map<String, Object> toHash(Node journey, int eventsLimit, int eventsOffset) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", journey.getId());
        result.put("session_id", getSessionId(journey));
        result.put("start_at", getStartAt(journey));
        result.put("finish_at", getFinishAt(journey));
        result.put("user", app.users().toHash(user(journey)));

        List<Map<String, Object>> evs = new ArrayList<>();
        Node lastEvent = null;
        for (Node event : limit(eventsLimit, skip(eventsOffset, events(journey)))) {
            lastEvent = event;
            evs.add(events.toHash(event));
        }
        result.put("events", evs);
        boolean reachedLast = lastEvent == null
                || !rejectIgnored(skip(1, chainHelper.nodesAfter(lastEvent))).iterator().hasNext();
        result.put("reached_last", reachedLast);
        return result;
    }

    private String userIdentifier(Node journey) {
        Node user = user(journey);
        return app.users().getIdentifier(user);
    }

    public String getSessionId(Node journey) {
        return (String) journey.getProperty(PROP_SESSION_ID);
    }

    public Node user(Node journey) {
        return getSingleEndNode(journey, RelTypes.JOURNEY_USER);
    }

    public void setUser(Node journey, Node user) {
        if (GraphDbUtils.connectSingle(journey, RelTypes.JOURNEY_USER, user)) {
            Index<Node> index = legacyIndex();
            index.remove(journey, IDX_PROP_UID);
            String uid = userIdentifier(journey);
            if (uid != null) {
                index.add(journey, IDX_PROP_UID, uid);
            }
        }
    }


    public IndexHits<Node> query(QueryContext queryContext) {
        return legacyIndex().query(queryContext);
    }

    public Iterable<Node> reversedPrefixFor(Node event) {
        return rejectIgnored(skip(1, chainHelper.reverseNodesFrom(event)));
    }

    public Iterable<Node> reversedPrefixFor(Node journey, String label) {
        return reversedPrefixFor(firstEventForAction(journey, label));
    }


    public Iterable<Node> suffixFor(Node event) {
        return rejectIgnored(skip(1, chainHelper.nodesAfter(event)));
    }

    public Iterable<Node> findByIds(String[] ids) {
        ArrayList<Node> result = new ArrayList<>(ids.length);
        for (String id : ids) {
            result.add(graphDb.getNodeById(Long.valueOf(id)));
        }
        return result;
    }

    public boolean isFirstJourney(Node journey) {
        return journey.getDegree(RelTypes.FIRST_JOURNEY, Direction.INCOMING) > 0;
    }

    public void reindex(Node journey) {
        Index<Node> index = legacyIndex();
        if (!journey.hasLabel(getLabel())) {
            throw new RuntimeException("Illegal access to index: " +
                    index.getName() + " . Except node[" + journey.getId() + "] has label: " + getLabel().name());
        }

        index.remove(journey, IDX_PROP_ACTION_IDS);
        index.remove(journey, IDX_PROP_UID);
        index.remove(journey, PROP_FINISH_AT);
        index.remove(journey, PROP_START_AT);

        String uid = userIdentifier(journey);
        if (uid != null) {
            index.add(journey, IDX_PROP_UID, uid);
        }
        index.add(journey, PROP_START_AT, new ValueContext(getStartAt(journey)).indexNumeric());
        index.add(journey, PROP_FINISH_AT, new ValueContext(getFinishAt(journey)).indexNumeric());

        for (Node event : this.events(journey)) {
            Node action = app.events().action(event);
            index.add(journey, IDX_PROP_ACTION_IDS, new ValueContext(action.getId()).indexNumeric());
        }
    }

    public void tearDownLegacyIndex() {
        legacyIndex().delete();
    }

    public Node firstEventForAction(Node journey, String actionLabel) {
        Node action = app.actions().findByActionLabel(actionLabel);
        for (Node event : events(journey)) {
            if (app.events().action(event).equals(action)) {
                return event;
            }
        }
        return null;
    }

    public Node next(Node journey) {
        return GraphDbUtils.getSingleEndNode(journey, RelTypes.NEXT);
    }

    public Iterator<Node> eventsCrossJourney(Node journey) {
        Iterable<Node> journeys = app.users().journeysFrom(journey);
        return Iterables.flatMap(new Function<Node, Iterator<Node>>() {
            @Override
            public Iterator<Node> apply(Node node) throws RuntimeException {
                return events(node).iterator();
            }
        }, journeys.iterator());
    }

    public Set<String> actions(Node journey) {
        Iterable<Node> actions = GraphDbUtils.getEndNodes(journey, RelTypes.JOURNEY_ACTIONS);
        HashSet<String> labels = new HashSet<>();
        for (Node action : actions) {
            labels.add(app.actions().getActionLabel(action));
        }
        return labels;
    }

    public Set<String> crossActions(Node journey) {
        Iterable<Node> journeys = app.users().journeysFrom(journey);
        HashSet<String> labels = new HashSet<>();
        for (Node j : journeys) {
            labels.addAll(actions(j));
        }
        return labels;
    }

    public Integer length(Node journey) {
        return journey.hasProperty(PROP_LENGTH) ? (Integer) journey.getProperty(PROP_LENGTH) : 0;
    }
}
