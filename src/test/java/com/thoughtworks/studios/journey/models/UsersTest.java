package com.thoughtworks.studios.journey.models;

import com.thoughtworks.studios.journey.ModelTestCase;
import org.junit.Test;
import org.neo4j.function.Function;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.Iterables;

import static com.thoughtworks.studios.journey.TestHelper.*;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.set;
import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;
import static org.junit.Assert.*;
import static org.neo4j.helpers.collection.Iterables.iterable;

public class UsersTest extends ModelTestCase {

    @Test
    public void importShouldCreateAndConnectJourneyWithUserNode() {
        Node req1 = requests.add(createRequestAttributes("s1", "a0", dateToMillis(2014, 7, 7, 10), "u1"));
        Node req2 = requests.add(createRequestAttributes("s1", "a1", dateToMillis(2014, 7, 7, 9), "u1"));
        Node req3 = requests.add(createRequestAttributes("s2", "a1", dateToMillis(2014, 7, 7, 11), "u2"));
        assertIterableEquals(iterable("u1", "u2"), Iterables.map(pluckIdentifier(), users.findAll()));

        assertEquals(users.findByIdentifier("u1"), journeys.user(requests.journeyOf(req1)));
        assertEquals(users.findByIdentifier("u1"), journeys.user(requests.journeyOf(req2)));
        assertEquals(users.findByIdentifier("u2"), journeys.user(requests.journeyOf(req3)));
    }

    @Test
    public void startActiveFieldRecordFirstRequestStartAt() {
        Node journey1 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 7, 10), "u1");
        Node journey2 = setupJourney(iterable("a2", "a3", "a4"), dateToMillis(2014, 7, 7, 14), "u1");
        Node user = users.findByIdentifier("u1");
        assertEquals(journeys.getStartAt(journey1), users.getStartActiveAt(user));
        assertEquals(journeys.getFinishAt(journey2), users.getLastActiveAt(user));
    }

    @Test
    public void getJourneysForAUserInSpecifiedTimeOrder() {
        Node journey1 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 7, 10), "u1");
        Node journey2 = setupJourney(iterable("a2", "a3", "a4"), dateToMillis(2014, 7, 7, 14), "u1");
        Node journey3 = setupJourney(iterable("a0", "a1", "a3"), dateToMillis(2014, 7, 6, 14), "u1");
        Node u1 = users.findByIdentifier("u1");
        assertIterableEquals(iterable(journey2, journey1, journey3), users.journeys(u1, true));
        assertIterableEquals(iterable(journey3, journey1, journey2), users.journeys(u1, false));
    }

    @Test
    public void shouldCreateAnonymousUserIfUidIsNull() {
        Node journey = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 7, 10), null);
        Node user = journeys.user(journey);
        assertNotNull(user);
        assertNull(users.getIdentifier(user));
        assertTrue(users.isAnonymous(user));
    }

    @Test
    public void anonymousJourneyWithSameSessionIdShouldBeAddedToSameAnonymousUser() {
        Node j0 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 7, 10), 2000, null, "s1");
        Node j1 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 8, 10), 2000, null, "s2");
        Node j2 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 9, 10), 2000, null, "s1");
        Node user = journeys.user(j0);
        assertTrue(users.isAnonymous(user));
        assertNotEquals(user, journeys.user(j1));
        assertEquals(user, journeys.user(j2));
    }

    @Test
    public void testShouldIdentifyAnonymousUserForNewUserId() {
        Node r1 = requests.add(createRequestAttributes("s0", "a0", dateToMillis(2014, 7, 7), (String) null));
        Node theAnonymousUser = journeys.user(requests.journeyOf(r1));
        Node r2 = requests.add(createRequestAttributes("s0", "a1", dateToMillis(2014, 7, 7) + 1000, "u1"));

        assertEquals(requests.journeyOf(r1), requests.journeyOf(r2));
        Node identified = journeys.user(requests.journeyOf(r1));
        assertFalse(users.isAnonymous(identified));
        assertEquals("u1", users.getIdentifier(identified));
        assertNodeDeleted(theAnonymousUser);
    }

    @Test
    public void testShouldLinkPreviousAnonymousJourneysToNewlyIdentifiedUser() {
        Node j0 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 4, 10), 2000, null, "s0");
        Node j1 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 5, 10), 2000, null, "s0");
        requests.add(createRequestAttributes("s0", "a0", dateToMillis(2014, 7, 7), (String) null));
        Node request = requests.add(createRequestAttributes("s0", "a1", dateToMillis(2014, 7, 7) + 1000, "u1"));
        assertEquals("u1", users.getIdentifier(journeys.user(j0)));
        assertEquals("u1", users.getIdentifier(journeys.user(j1)));
        assertTrue(journeys.isFirstJourney(j0));
        assertFalse(journeys.isFirstJourney(requests.journeyOf(request)));
    }

    @Test
    public void testShouldLinkPreviousAnonymousJourneysToExistingIdentifiedUser() {
        Node j0 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 4, 10), 2000, null, "s0");
        Node j1 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 5, 10), 2000, null, "s0");
        Node j2 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 6, 10), 2000, "u1", "s1");
        requests.add(createRequestAttributes("s0", "a0", dateToMillis(2014, 7, 7), (String) null));
        Node request = requests.add(createRequestAttributes("s0", "a1", dateToMillis(2014, 7, 7) + 1000, "u1"));
        assertEquals("u1", users.getIdentifier(journeys.user(j0)));
        assertEquals("u1", users.getIdentifier(journeys.user(j1)));
        assertTrue(journeys.isFirstJourney(j0));
        assertFalse(journeys.isFirstJourney(j2));
    }

    @Test
    public void testShouldIdentifyAnonymousJourneysWithSessionCollision() {
        Node j0 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 4, 10), 2000, null, "s0");
        Node j1 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 5, 10), 2000, null, "s0");
        Node j2 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 6, 10), 2000, "u1", "s0");

        assertEquals("u1", users.getIdentifier(journeys.user(j0)));
        assertEquals("u1", users.getIdentifier(journeys.user(j1)));
        assertEquals("u1", users.getIdentifier(journeys.user(j2)));
        assertTrue(journeys.isFirstJourney(j0));
        assertFalse(journeys.isFirstJourney(j2));
    }


    @Test
    public void testShouldRemoveAnonymousUserThatIdentifiedToExistingUser() {
        setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 4, 10), 2000, "u1", "s0");
        Node r1 = requests.add(createRequestAttributes("s1", "a0", dateToMillis(2014, 7, 7), (String) null));
        Node theAnonymousUser = journeys.user(requests.journeyOf(r1));
        requests.add(createRequestAttributes("s1", "a1", dateToMillis(2014, 7, 7) + 1000, "u1"));
        assertEquals("u1", users.getIdentifier(journeys.user(requests.journeyOf(r1))));
        assertNodeDeleted(theAnonymousUser);
    }

    @Test
    public void testShouldNotUnIdentifyUserToAnonymous() {
        setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 4, 10), 2000, "u1", "s0");
        requests.add(createRequestAttributes("s1", "a0", dateToMillis(2014, 7, 7), "u1"));
        Node r = requests.add(createRequestAttributes("s1", "a1", dateToMillis(2014, 7, 7) + 1000, (String) null));
        Node user = journeys.user(requests.journeyOf(r));
        assertEquals("u1", users.getIdentifier(user));
        assertFalse(users.isAnonymous(user));
    }

    @Test
    public void testIdentifyAnonymousUserWithNewUser() {
        Node journey = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 4, 10), 2000, null, "s0");
        Node u1 = users.identify("u1", "s0");
        assertEquals(u1, journeys.user(journey));
        assertEquals("u1", users.getIdentifier(u1));
    }

    @Test
    public void testIdentifyAnonymousUserWithExistingUser() {
        Node j0 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 3, 10), 2000, "u1", "s0");
        Node u1 = journeys.user(j0);
        Node j1 = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 4, 10), 2000, null, "s0");
        assertEquals(u1, users.identify("u1", "s0"));
        assertIterableEquals(list(j0, j1), users.journeys(u1, false));
        assertNull(users.findByAnonymousId("s0"));
    }

    @Test
    public void testShouldCreateUserWhenAnonymousNotFound() {
        Node u1 = users.identify("u1", "s1");
        assertNotNull(u1);
        assertNull(users.findByAnonymousId("s1"));
        assertEquals("u1", users.getIdentifier(u1));
    }

    @Test
    public void testShouldJustReturnAnonymousUserIfUidIsNull () {
        Node journey = setupJourney(iterable("a0", "a1", "a2"), dateToMillis(2014, 7, 4, 10), 2000, null, "s0");
        Node u = users.identify(null, "s0");
        assertEquals(u, journeys.user(journey));
        assertEquals(u, users.findByAnonymousId("s0"));
    }

    @Test
    public void testShouldCreateAnonymousUserIfUidIsNullAndAnonymousIdNotExists() {
        Node u = users.identify(null, "s0");
        assertTrue(users.isAnonymous(u));
        assertEquals(u, users.findByAnonymousId("s0"));
    }

    @Test
    public void testAddAndRetrieveTraitsForIdentifiedUser() {
        Node user = users.identify("foo", null);
        users.addTrait(user,  "company size", 30);
        assertIterableEquals(list(30), users.getTraitValue(user, "company size"));
    }

    @Test
    public void testAddAndRetrieveTraitsForAnonymousUser() {
        Node user = users.identify(null, "anonymous-id");
        users.addTrait(user,  "company size", 30);
        assertIterableEquals(list(30), users.getTraitValue(user, "company size"));
    }

    @Test
    public void testAddTraitsMultipleTimeWithSameValueShouldResultInSingleValue() {
        Node user = users.identify("foo", null);
        users.addTrait(user,  "company size", 30);
        users.addTrait(user,  "company size", 30);
        assertIterableEquals(list(30), users.getTraitValue(user, "company size"));
    }

    @Test
    public void testIdentifyAnAnonymousUserToNewUserShouldTransferTraits() {
        Node user = users.identify(null, "anon-01");
        users.addTrait(user, "company size", 30);
        Node identified = users.identify("u1", "anon-01");
        assertEquals(mapOf("company size", set(30)), users.traits(identified));
    }

    @Test
    public void testIdentifyAnAnonymousUserToExistingUserShouldTransferTraits() {
        Node identified = users.identify("u0", null);
        users.addTrait(identified, "industry", "IT");
        Node user = users.identify(null, "anon-01");
        users.addTrait(user, "company size", 30);
        users.addTrait(user, "industry", "computer");
        assertEquals(identified, users.identify("u0", "anon-01"));
        assertEquals(mapOf("company size", set(30), "industry", set("IT", "computer")), users.traits(identified));
    }


    private Function<Node, String> pluckIdentifier() {
        return new Function<Node, String>() {
            public String apply(Node user) {
                return users.getIdentifier(user);
            }
        };
    }
}
