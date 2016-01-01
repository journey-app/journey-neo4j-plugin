package com.thoughtworks.studios.journey.models;

import com.thoughtworks.studios.journey.ModelTestCase;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import static com.thoughtworks.studios.journey.TestHelper.dateToMillis;
import static org.junit.Assert.*;
import static org.neo4j.helpers.collection.Iterables.iterable;

public class StoppingConditionTest extends ModelTestCase {

    @Test
    public void testStopWithAnAction() {
        StoppingCondition.StopMatchResult match = matchResult(iterable("a0", "a1", "a2", "a3"), "a2");
        assertTrue(match.matched());
        assertEquals("a2", requests.getActionLabel(match.last()));
        assertEquals("a3", requests.getActionLabel(match.iterator().next()));
    }

    @Test
    public void testNoMatchingWhenActionNotFound() {
        StoppingCondition.StopMatchResult failedMatch = matchResult(iterable("a2", "a3"), "a0");
        assertFalse(failedMatch.matched());
        assertNull(failedMatch.last());
    }

    @Test
    public void testNoMatchingWhenActionExistFromOtherJourney() {
        setupJourney(iterable("a0"), 0L);
        StoppingCondition.StopMatchResult failedMatch = matchResult(iterable("a2", "a3"), "a0");
        assertFalse(failedMatch.matched());
        assertNull(failedMatch.last());
    }


    @Test
    public void testStopWithAnyAction() {
        StoppingCondition.StopMatchResult match = matchResult(iterable("a0", "a1", "a2", "a3"), "*");
        assertTrue(match.matched());
        assertEquals("a0", requests.getActionLabel(match.last()));
        assertEquals("a1", requests.getActionLabel(match.iterator().next()));
    }

    @Test
    public void testStopWithQualifier() {
        StoppingCondition.StopMatchResult match = matchResult(iterable("a0", "a1", "a2"), "*:2");
        assertTrue(match.matched());
        assertEquals("a1", requests.getActionLabel(match.last()));
        assertEquals("a2", requests.getActionLabel(match.iterator().next()));

        assertTrue(matchResult(iterable("a0", "a1"), "*:2").matched());
        assertFalse(matchResult(iterable("a0"), "*:2").matched());
    }

    @Test
    public void testStopWithQualifierAndRewind() {
        StoppingCondition.StopMatchResult match = matchResult(iterable("a0", "a1", "a2"), "*:2:<<");
        assertTrue(match.matched());
        assertEquals("a1", requests.getActionLabel(match.last()));
        assertEquals("a0", requests.getActionLabel(match.iterator().next()));
    }


    private StoppingCondition.StopMatchResult matchResult(Iterable<Object> actions, String stoppingExpression) {
        Node journey = setupJourney(actions, dateToMillis(2014, 7, 7, 10));
        StoppingCondition stop = StoppingCondition.eval(app, stoppingExpression);
        return stop.match(journeys.userRequests(journey));
    }

}