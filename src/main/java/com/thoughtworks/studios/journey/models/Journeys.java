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
    private static final String PROP_FINISH_AT = "finish_at";
    public static final String PROP_SESSION_ID = "session_id";
    public static final String IDX_PROP_UID = "uid";
    public static final Long CUT_TOLERANT = 2 * 60 * 60 * 1000L; // 2 hours
    public static final String IDX_PROP_ACTION_IDS = "action_ids";
    private final Application app;
    private final Map<String, LinkedList<Node>> journeysCache;
    private final ChronologicalChain chainHelper;
    private GraphDatabaseService graphDb;

    public Journeys(Application application) {
        this.app = application;
        this.graphDb = application.graphDB();
        this.journeysCache = new HashMap<>();
        chainHelper = new ChronologicalChain(Requests.PROP_START_AT);
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

    public Node addRequest(String sessionId, String userIdentifier, Node request, Node action) {
        Node journey = findOrCreateCoveredBySessionId(sessionId, request, userIdentifier);
        GraphDbUtils.connectUnique(journey, RelTypes.JOURNEY_ACTIONS, action);
        expandTimeRange(journey, requests().getStartAt(request));
        request.createRelationshipTo(journey, RelTypes.BELONGS_TO);
        legacyIndex().add(journey, IDX_PROP_ACTION_IDS, new ValueContext(action.getId()).indexNumeric());
        chainHelper.insert(journey, request);
        return journey;
    }

    private void expandTimeRange(Node journey, Long requestTime) {
        Index<Node> index = legacyIndex();
        ValueContext valueContext = new ValueContext(requestTime).indexNumeric();
        if (chainHelper.isEmpty(journey)) {
            journey.setProperty(PROP_START_AT, requestTime);
            journey.setProperty(PROP_FINISH_AT, requestTime);

            index.add(journey, PROP_START_AT, valueContext);
            index.add(journey, PROP_FINISH_AT, valueContext);
        } else {
            if (requestTime < getStartAt(journey)) {
                journey.setProperty(PROP_START_AT, requestTime);
                index.remove(journey, PROP_START_AT);
                index.add(journey, PROP_START_AT, valueContext);
            }

            if (requestTime > getFinishAt(journey)) {
                journey.setProperty(PROP_FINISH_AT, requestTime);
                index.remove(journey, PROP_FINISH_AT);
                index.add(journey, PROP_FINISH_AT, valueContext);
            }
        }
    }


    private Requests requests() {
        return app.requests();
    }

    // test only
    public long count() {
        return IteratorUtil.count(graphDb.findNodes(getLabel()));
    }

    /**
     * User requests that belongs to the journey, following next link
     *
     * @param journey the journey neo4j node
     * @return nodes belongs to the journey in earliest first order
     */
    public Iterable<Node> userRequests(final Node journey) {
        return rejectIgnored(chainHelper.nodes(journey));
    }

    private Iterable<Node> rejectIgnored(Iterable<Node> requests) {
        return filter(new Predicate<Node>() {
            @Override
            public boolean accept(Node request) {
                return !app.actions().isIgnored(requests().action(request));
            }
        }, requests);
    }

    private Node findOrCreateCoveredBySessionId(String sessionId, Node request, String userIdentifier) {
        if (journeysCache.containsKey(sessionId)) {
            LinkedList<Node> journeys = journeysCache.get(sessionId);
            for (Node journey : journeys) {
                if (covers(journey, request, userIdentifier)) {
                    return journey;
                }
            }
        }

        ResourceIterator<Node> journeys = findAllBySessionId(sessionId);

        while (journeys.hasNext()) {
            Node journey = journeys.next();
            if (covers(journey, request, userIdentifier)) {
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

    private boolean covers(Node journey, Node request, String userIdentifier) {
        String journeyUserId = userIdentifier(journey);
        return (journeyUserId == null || userIdentifier == null || journeyUserId.equals(userIdentifier)) &&
                requests().getStartAt(request) >= getStartAt(journey) - CUT_TOLERANT &&
                requests().getStartAt(request) <= getFinishAt(journey) + CUT_TOLERANT;

    }

    @Override
    public Map<String, Object> toHash(Node journey) {
        return toHash(journey, Integer.MAX_VALUE, 0);
    }

    public Map<String, Object> toHash(Node journey, int requestsLimit, int requestsOffset) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", journey.getId());
        result.put("session_id", getSessionId(journey));
        result.put("start_at", getStartAt(journey));
        result.put("finish_at", getFinishAt(journey));
        result.put("user", app.users().toHash(user(journey)));

        List<Map<String, Object>> reqs = new ArrayList<>();
        Node lastRequest = null;
        for (Node request : limit(requestsLimit, skip(requestsOffset, userRequests(journey)))) {
            lastRequest = request;
            reqs.add(requests().toHash(request));
        }
        result.put("requests", reqs);
        boolean reachedLast = lastRequest == null
                || !rejectIgnored(skip(1, chainHelper.nodesAfter(lastRequest))).iterator().hasNext();
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

    public Iterable<Node> reversedPrefixFor(Node request) {
        return rejectIgnored(skip(1, chainHelper.reverseNodesFrom(request)));
    }

    public Iterable<Node> reversedPrefixFor(Node journey, String label) {
        return reversedPrefixFor(firstRequestWithLabel(journey, label));
    }


    public Iterable<Node> suffixFor(Node request) {
        return rejectIgnored(skip(1, chainHelper.nodesAfter(request)));
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

        for (Node request : this.userRequests(journey)) {
            Node action = app.requests().action(request);
            index.add(journey, IDX_PROP_ACTION_IDS, new ValueContext(action.getId()).indexNumeric());
        }
    }

    public void tearDownLegacyIndex() {
        legacyIndex().delete();
    }

    public Node firstRequestWithLabel(Node journey, String actionLabel) {
        Node action = app.actions().findByActionLabel(actionLabel);
        for (Node request : userRequests(journey)) {
            if (app.requests().action(request).equals(action)) {
                return request;
            }
        }
        return null;
    }

    public Node next(Node journey) {
        return GraphDbUtils.getSingleEndNode(journey, RelTypes.NEXT);
    }

    public Iterator<Node> userRequestsCrossJourneys(Node journey) {
        Iterable<Node> journeys = app.users().journeysFrom(journey);
        return Iterables.flatMap(new Function<Node, Iterator<Node>>() {
            @Override
            public Iterator<Node> apply(Node node) throws RuntimeException {
                return userRequests(node).iterator();
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
}
