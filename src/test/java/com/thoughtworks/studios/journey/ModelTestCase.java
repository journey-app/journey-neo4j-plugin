package com.thoughtworks.studios.journey;

import com.thoughtworks.studios.journey.cspmining.TreeNode;
import com.thoughtworks.studios.journey.models.*;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.IOException;
import java.util.*;

import static com.thoughtworks.studios.journey.TestHelper.assertIterableEquals;
import static com.thoughtworks.studios.journey.TestHelper.createRequestAttributes;
import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ModelTestCase {
    public static final String TEST_NAME_SPACE = "parsley";
    public static final String TEST_NAME_SPACE2 = "acme";

    protected Requests requests;
    protected Journeys journeys;
    protected Actions actions;
    private Transaction tx;
    protected Users users;
    protected GraphDatabaseService db;
    protected Application app;
    protected Application app2;

    @Before
    public void supperSetUp() throws IOException {
//        db = new TestGraphDatabaseFactory().newEmbeddedDatabase("/tmp/journey-test");
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        app = new Application(db, TEST_NAME_SPACE);
        app2 = new Application(db, TEST_NAME_SPACE2);

        requests = app.requests();
        journeys = app.journeys();
        actions = app.actions();
        users = app.users();
        try (Transaction ignored = db.beginTx()) {
            app.setupSchema();
            app2.setupSchema();
        }
        this.tx = db.beginTx();
    }

    @After
    public void supperTearDown() {
        tx.failure();
        tx.close();
    }

    protected Node setupJourney(Iterable actions, long startAt, String uid) {
        return setupJourney(actions, startAt, 10 * 1000L, uid);
    }

    protected Node setupJourney(Iterable actions, long startAt, long interval, String uid) {
        String sessionId = UUID.randomUUID().toString();
        return setupJourney(actions, startAt, interval, uid, sessionId);
    }

    protected Node setupJourney(Iterable actions, long startAt, long interval, String uid, String sessionId) {
        Node request = null;
        for (Object action : actions) {
            request = requests.add(createRequestAttributes(sessionId, (String) action, startAt, uid));
            startAt += interval;
        }
        return requests.journeyOf(request);

    }


    protected Node setupJourney(Iterable actions, long startAt) {
        return setupJourney(actions, startAt, null);
    }

    protected void assertTreeNode(TreeNode node, Set<Long> expectedJourneyIds, String... expectedNames) {
        assertIterableEquals(Arrays.asList(expectedNames), Arrays.asList(node.getNames()));
        assertEquals(expectedJourneyIds, node.getJourneyIdsSet());
    }

    protected void assertNodeDeleted(Node node) {
        try {
            app.graphDB().getNodeById(node.getId());
        } catch (NotFoundException e) {
            return;
        }
        fail("node should have been deleted");
    }


    protected List<Map> buildConditions(List... conditionTriples) {
        List<Map> conditions = new ArrayList<>();
        for (List conditionTriple : conditionTriples) {
            conditions.add(mapOf("subject", conditionTriple.get(0), "verb", conditionTriple.get(1), "object", conditionTriple.get(2)));
        }
        return conditions;
    }

    protected Node action(String actionLabel) {
        return actions.findByActionLabel(actionLabel);
    }
}
