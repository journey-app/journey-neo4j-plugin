package com.thoughtworks.studios.journey.models;

import com.thoughtworks.studios.journey.ModelTestCase;
import com.thoughtworks.studios.journey.jql.JourneyQuery;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.util.List;
import java.util.Map;

import static com.thoughtworks.studios.journey.TestHelper.assertIterableEquals;
import static com.thoughtworks.studios.journey.TestHelper.dateToMillis;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.listO;
import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;
import static org.neo4j.helpers.collection.Iterables.iterable;

public class JourneyQueryTest extends ModelTestCase {

    private Node j1;
    private Node j2;
    private Node j3;
    private Node j4;

    @Before
    public void setup() {
        j1 = setupJourney(iterable("a0", "a1", "a1"), dateToMillis(2015, 1, 1), 100L, "u0", "s0");
        j2 = setupJourney(iterable("a1", "a2"), dateToMillis(2015, 1, 2), 100L, "u1", "s1");
        j3 = setupJourney(iterable("a2", "a3", "a0"), dateToMillis(2015, 1, 3),100L, null, "s2");
        j4 = setupJourney(iterable("a0", "a1"), dateToMillis(2015, 1, 4), 100L, "u1", "s1");
    }

    @Test
    public void shouldReturnAllWithoutCondition() {
        assertIterableEquals(list(j1, j2, j3, j4), query().journeys());
    }


    @Test
    public void queryDescOrder() {
        assertIterableEquals(list(j4, j3, j2, j1), builder().desc().build().journeys());
    }

    @Test
    public void testQueryJourneyWithUID() {
        assertIterableEquals(list(j2, j4), query(listO("User", "name is", "u1")).journeys());
    }

    @Test
    public void testQueryJourneyWithStartBeforeDate() {
        assertIterableEquals(list(j1, j2), query(listO("Journey", "starts before", String.valueOf(dateToMillis(2015, 1, 3)))).journeys());
    }

    @Test
    public void testQueryJourneyWithStartAfterDate() {
        assertIterableEquals(list(j2, j3, j4), query(listO("Journey", "starts after", String.valueOf(dateToMillis(2015, 1, 2)))).journeys());
    }

    @Test
    public void queryWithLimit() {
        assertIterableEquals(iterable(j1, j2, j3), builder().limit(3).build().journeys());
        assertIterableEquals(list(), builder().limit(0).build().journeys());
    }

    @Test
    public void queryWithUserActivatedBetween() {
        JourneyQuery query = query(listO("User",
                "activated between",
                String.valueOf(dateToMillis(2015, 1, 1)) + "," + String.valueOf(dateToMillis(2015, 1, 2))));
        assertIterableEquals(list(j1), query.journeys());
    }

    @Test
    public void queryJourneyWithStartWithAction() {
        actions.ignore(actions.findByActionLabel("a1"));
        actions.ignore(actions.findByActionLabel("a2"));
        assertIterableEquals(list(j1, j4), query(listO("Journey", "first action is", "a0")).journeys());
    }

    @Test
    public void queryIgnoredEmptyJourneyWithStartWith() {
        assertIterableEquals(list(j1, j4), query(listO("Journey", "first action is", "a0")).journeys());
        assertIterableEquals(list(j1, j4), query(listO("User", "journey start with", "a0")).journeys());

        assertIterableEquals(list(j2), query(listO("Journey", "first action is", "a1")).journeys());
        assertIterableEquals(list(), query(listO("Journey", "first action is", "a3")).journeys());
    }

    @Test
    public void queryCanBeLimitedToFirstJourneys() {
        assertIterableEquals(list(j1, j2, j3), query(listO("Journey", "is first", null)).journeys());
    }

    @Test
    public void queryJourneyForUserReturned() {
        assertIterableEquals(list(j2, j4), query(listO("User", "returned after", 24)).journeys());
        assertIterableEquals(list(), query(listO("User", "returned after", 72)).journeys());
    }

    @Test
    public void queryJourneyForUserNotReturned() {
        assertIterableEquals(list(j1, j3), query(listO("User", "not returned after", 24)).journeys());
        assertIterableEquals(list(j1, j2, j3, j4), query(listO("User", "not returned after", 72)).journeys());
    }

    @Test
    public void queryIgnoresJourneyStartTooFarAwayInTheFuture() {
        assertIterableEquals(list(j1, j2, j3, j4), query().journeys());
    }

    @Test
    public void queryJourneyIncludeCertainAction() {
        assertIterableEquals(list(j1, j2, j4), query(listO("Journey", "includes action", "a1")).journeys());
        assertIterableEquals(list(j2, j3), query(listO("Journey", "includes action", "a2")).journeys());
        assertIterableEquals(list(), query(listO("Journey", "includes action", "a4")).journeys());
    }

    @Test
    public void queryJourneyAtLeastActionCount() {
        assertIterableEquals(list(j1, j2, j3, j4), query(listO("Journey", "at least N actions", 0)).journeys());
        assertIterableEquals(list(j1, j2, j3, j4), query(listO("Journey", "at least N actions", 1)).journeys());
        assertIterableEquals(list(j1, j2, j3, j4), query(listO("Journey", "at least N actions", 2)).journeys());
        assertIterableEquals(list(j1, j3), query(listO("Journey", "at least N actions", 3)).journeys());
        assertIterableEquals(list(), query(listO("Journey", "at least N actions", 4)).journeys());
    }

    @Test
    public void queryJourneysWithUniqueSessionId() {
        assertIterableEquals(list(j1, j2), query(listO("Journey", "includes action", "a1")).uniqueJourneys());
    }


    @SafeVarargs
    private final JourneyQuery query(List<Object>... conditionTriples) {
        JourneyQuery.Builder builder = builder(conditionTriples);
        return builder.build();
    }

    @SafeVarargs
    private final JourneyQuery.Builder builder(List... conditionTriples) {
        List<Map> conditions = buildConditions(conditionTriples);
        return JourneyQuery.Builder.query(app).conditions(conditions);
    }
}
