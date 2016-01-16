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
package com.thoughtworks.studios.journey.jql;

import com.thoughtworks.studios.journey.ModelTestCase;
import com.thoughtworks.studios.journey.TestHelper;
import com.thoughtworks.studios.journey.jql.values.JQLValue;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.Iterables;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.studios.journey.TestHelper.*;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;
import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.Iterables.iterable;

public class DataQueryTest extends ModelTestCase {

    private Node u0;
    private Node u1;
    private Node anonymous;
    private Node j1;
    private Node j2;
    private Node j3;
    private Node j4;

    @Before
    public void setup() {
        j1 = setupJourney(iterable("a0", "a1", "a1"), TestHelper.dateToMillis(2015, 1, 1), 100L, "u0", "s0");
        j2 = setupJourney(iterable("a1", "a2"), TestHelper.dateToMillis(2015, 1, 2), 100L, "u1", "s1");
        j3 = setupJourney(iterable("a2", "a3", "a0"), TestHelper.dateToMillis(2015, 1, 3), 100L, null, "s2");
        j4 = setupJourney(iterable("a0", "a1"), TestHelper.dateToMillis(2015, 1, 4), 100L, "u1", "s1");
        u0 = users.findByIdentifier("u0");
        u1 = users.findByIdentifier("u1");
        anonymous = users.findByAnonymousId("s2");
    }

    private JQLValue jv(Node journey) {
        return Values.wrapModel(journey, journeys);
    }

    private JQLValue uv(Node user) {
        return Values.wrapModel(user, users);
    }

    private JQLValue ev(Node event) {
        return Values.wrapModel(event, events);
    }


    @Test
    public void testSelectJourneys() throws IOException {
        DataQuery query = new DataQuery(app, false);
        query.select("journey");
        assertEquals(list(list(t(jv(j1)), t(jv(j2)), t(jv(j3)), t(jv(j4)))), query.execute().data());
        query.crossJourney(true);
        assertEquals(list(list(t(jv(j1)), t(jv(j2)), t(jv(j3)))), query.execute().data());
    }

    @Test
    public void testSelectUsers() throws IOException {
        DataQuery query = new DataQuery(app, false);
        query.select("user");
        assertEquals(list(list(t(uv(u0)), t(uv(u1)), t(uv(anonymous)), t(uv(u1)))), query.execute().data());
        query.crossJourney(true);
        assertEquals(list(list(t(uv(u0)), t(uv(u1)), t(uv(anonymous)))), query.execute().data());
    }

    @Test
    public void testDistinctFunction() throws IOException {
        DataQuery query = new DataQuery(app, false);
        query.select("user |> distinct");
        assertEquals(list(list(t(uv(u0)), t(uv(u1)), t(uv(anonymous)))), query.execute().data());
    }

    @Test
    public void testDistinctByFunction() throws IOException {
        DataQuery query = new DataQuery(app, false);
        query.select("(user, journey) |> distinct_by:0");
        assertEquals(list(list(t(uv(u0), jv(j1)), t(uv(u1), jv(j2)), t(uv(anonymous), jv(j3)))), query.execute().data());
    }

    @Test
    public void testSelectCountOfJourneys() throws IOException {
        DataQuery query = new DataQuery(app);
        query.select("journey |> count");
        assertEquals(list(list(t(v(3)))), query.execute().data());
    }

    @Test
    public void testSelectSingleStop() throws IOException {
        DataQuery query = new DataQuery(app);
        query.select("user |> count");
        query.addStop(stop("a1"));
        assertEquals(list(list(t(v(2)))), query.execute().data());
    }

    @Test
    public void testSelectMultipleStops() throws IOException {
        DataQuery query = new DataQuery(app);
        query.select("event |> compact |> count");
        query.addStop(stop("a0")).addStop(stop("a1"));
        DataQueryResult result = query.execute();
        assertEquals(list(list(t(v(3))), list(t(v(2)))), result.data());
    }

    @Test
    public void testSelectMultipleStopsWithBaseConditions() throws IOException {
        DataQuery query = new DataQuery(app);
        query.select("event |> compact |> count");
        query.addStop(stop("a0", list("start_at > " + (TestHelper.dateToMillis(2015, 1, 1) + 1))))
                .addStop(stop("a1"));
        assertEquals(list(list(t(v(2))), list(t(v(1)))), query.execute().data());
    }

    @Test
    public void testSelectEvent() throws IOException {
        DataQuery query = new DataQuery(app);
        query.select("event");
        query.addStop(stop("a1")).addStop(stop("a1"));
        Node r12 = Iterables.toList(journeys.events(j1)).get(1);
        Node r13 = Iterables.toList(journeys.events(j1)).get(2);
        Node r21 = Iterables.toList(journeys.events(j2)).get(0);
        Node r42 = Iterables.toList(journeys.events(j4)).get(1);
        assertEquals(list(list(t(ev(r12)), t(ev(r21))), list(t(ev(r13)), t(ev(r42)))), query.execute().data());
    }

    @Test
    public void testSelectEventProperty() throws IOException {
        Node r12 = Iterables.toList(journeys.events(j1)).get(1);
        Node r13 = Iterables.toList(journeys.events(j1)).get(2);
        Node r21 = Iterables.toList(journeys.events(j2)).get(0);
        Iterables.toList(journeys.events(j4)).get(1);

        events.addProperty(r12, "color", "blue");
        events.addProperty(r13, "color", "red");
        events.addProperty(r21, "color", "yellow");
        events.addProperty(r21, "color", "brown");

        DataQuery query = new DataQuery(app);
        query.select("event.color");
        query.addStop(stop("a1")).addStop(stop("a1"));
        assertEquals(list(list(t(v("blue")), t(v("yellow", "brown"))), list(t(v("red")), t(v()))), query.execute().data());
    }

    @Test
    public void testFlattenMultiplePropertyValue() throws IOException {
        Node r12 = Iterables.toList(journeys.events(j1)).get(1);
        Node r13 = Iterables.toList(journeys.events(j1)).get(2);
        Node r21 = Iterables.toList(journeys.events(j2)).get(0);
        Iterables.toList(journeys.events(j4)).get(1);

        events.addProperty(r12, "color", "blue");
        events.addProperty(r13, "color", "red");
        events.addProperty(r21, "color", "yellow");
        events.addProperty(r21, "color", "brown");

        DataQuery query = new DataQuery(app);
        query.select("event.color |> flatten");
        query.addStop(stop("a1")).addStop(stop("a1"));
        List<List<Tuple>> result = query.execute().data();
        assertSetEquals(list(t(v("blue")), t(v("brown")), t(v("yellow"))), result.get(0));
        assertSetEquals(list(t(v("red")), t(v())), result.get(1));
    }

    @Test
    public void testCompactNullValue() throws IOException {
        Node r12 = Iterables.toList(journeys.events(j1)).get(1);
        Node r13 = Iterables.toList(journeys.events(j1)).get(2);
        Node r21 = Iterables.toList(journeys.events(j2)).get(0);
        Iterables.toList(journeys.events(j4)).get(1);

        events.addProperty(r12, "color", "blue");
        events.addProperty(r13, "color", "red");
        events.addProperty(r21, "color", "yellow");
        events.addProperty(r21, "color", "brown");

        DataQuery query = new DataQuery(app);
        query.select("event.color |> compact |> flatten");
        query.addStop(stop("a1")).addStop(stop("a1"));
        List<List<Tuple>> result = query.execute().data();
        assertSetEquals(list(t(v("blue")), t(v("brown")), t(v("yellow"))), result.get(0));
        assertSetEquals(list(t(v("red"))), result.get(1));
    }

    @Test
    public void testGroupCounting() throws IOException {
        Node r12 = Iterables.toList(journeys.events(j1)).get(1);
        Node r13 = Iterables.toList(journeys.events(j1)).get(2);
        Node r21 = Iterables.toList(journeys.events(j2)).get(0);
        Iterables.toList(journeys.events(j4)).get(1);

        events.addProperty(r12, "color", "blue");
        events.addProperty(r12, "color", "brown");
        events.addProperty(r13, "color", "red");
        events.addProperty(r21, "color", "yellow");
        events.addProperty(r21, "color", "brown");

        DataQuery query = new DataQuery(app);
        query.select("event.color |> group_count");
        query.addStop(stop("a1")).addStop(stop("a1"));
        assertEquals(list(list(t(v("blue", "brown"), v(1)), t(v("brown", "yellow"), v(1))), list(t(v("red"), v(1)), t(v(), v(1)))), query.execute().data());
    }

    @Test
    public void testGroupCountingWithFlattening() throws IOException {
        Node r12 = Iterables.toList(journeys.events(j1)).get(1);
        Node r13 = Iterables.toList(journeys.events(j1)).get(2);
        Node r21 = Iterables.toList(journeys.events(j2)).get(0);
        Iterables.toList(journeys.events(j4)).get(1);

        events.addProperty(r12, "color", "blue");
        events.addProperty(r12, "color", "brown");
        events.addProperty(r13, "color", "red");
        events.addProperty(r21, "color", "yellow");
        events.addProperty(r21, "color", "brown");

        DataQuery query = new DataQuery(app);
        query.select("event.color |> flatten |> group_count");
        query.addStop(stop("a1")).addStop(stop("a1"));
        assertEquals(list(list(t(v("blue"), v(1)), t(v("brown"), v(2)), t(v("yellow"), v(1))), list(t(v("red"), v(1)), t(v(), v(1)))), query.execute().data());
    }

    @Test
    public void testSelectUserTrait() throws IOException {
        users.addTrait(users.findByIdentifier("u1"), "foo", "bar");
        DataQuery query = new DataQuery(app);
        query.select("user.foo");
        assertEquals(list(list(t(v()), t(v("bar")), t(v()))), query.execute().data());
    }

    @Test
    public void testSelectEventUrl() throws IOException {
        DataQuery query = new DataQuery(app);
        query.select("event.url |> distinct");
        query.addStop(stop("a1"));
        assertEquals(list(list(t(v("/url/a1")))), query.execute().data());
    }

    @Test
    public void testSelectUrlQueryValue() throws IOException {
        Node r12 = Iterables.toList(journeys.events(j1)).get(1);
        r12.setProperty("url", "/url/a1?foo=bar");
        Node r21 = Iterables.toList(journeys.events(j2)).get(0);
        r21.setProperty("url", "/url/a1?foo=baz");

        DataQuery query = new DataQuery(app);
        query.select("event.url |> url_query:foo");
        query.addStop(stop("a1"));
        assertEquals(list(list(t(v("bar")), t(v("baz")))), query.execute().data());
    }

    @Test
    public void testSelectReferrerDomain() throws IOException {
        Node r12 = Iterables.toList(journeys.events(j1)).get(1);
        r12.setProperty("referrer", "https://www.google.com/url/a1?foo=bar");
        Node r21 = Iterables.toList(journeys.events(j2)).get(0);
        r21.setProperty("referrer", "https://www.facebook.com/url/a1?foo=baz");

        DataQuery query = new DataQuery(app);
        query.select("event.referrer |> url_domain");
        query.addStop(stop("a1"));
        assertEquals(list(list(t(v("www.google.com")), t(v("www.facebook.com")))), query.execute().data());
    }

    @Test
    public void testSelectAction() throws IOException {
        DataQuery query = new DataQuery(app);
        query.select("event.action");
        query.addStop(stop("*", list("user.identifier = 'u1'")))
                .addStop(stop("*"))
                .addStop(stop("*"));
        assertEquals(list(list(t(v("a1"))), list(t(v("a2"))), list(t(v("a0")))), query.execute().data());
    }

    @Test
    public void testSelectJourneyActions() throws IOException {
        DataQuery query = new DataQuery(app);
        query.select("journey.actions");
        query.addStop(stop("*", list("user.identifier = 'u1'")));
        query.crossJourney(false);
        assertEquals(list(list(t(v("a1", "a2")), t(v("a0", "a1")))), query.execute().data());
        query.crossJourney(true);
        assertEquals(list(list(t(v("a1", "a2", "a0")))), query.execute().data());
    }

    @Test
    public void testSelectEventTimeStamp() throws IOException {
        DataQuery query = new DataQuery(app);
        query.select("event.timestamp");
        query.addStop(stop("a0", list("user.identifier = 'u1'")));
        assertEquals(list(list(t(v(TestHelper.dateToMillis(2015, 1, 4))))), query.execute().data());
    }

    @Test
    public void testTimeCeilingFunction() throws IOException {
        DataQuery query = new DataQuery(app);
        query.addStop(stop("a2", list("user.identifier = 'u1'")));

        query.select("event.timestamp |> time_floor:day");
        assertEquals(list(list(t(v(TestHelper.dateToMillis(2015, 1, 2))))), query.execute().data());

        query.select("event.timestamp |> time_floor:week");
        assertEquals(list(list(t(v(TestHelper.dateToMillis(2014, 12, 29))))), query.execute().data());

        query.select("event.timestamp |> time_floor:week:America/Los_Angeles");
        assertEquals(list(list(t(v(TestHelper.dateToMillis(2014, 12, 29) + 8 * 60 * 60 * 1000)))), query.execute().data());
    }

    @Test
    public void testFindDistinctUserVisitsCountGroupByDay() throws IOException {
        DataQuery query = new DataQuery(app, false);
        query.select("(user, event.timestamp |> time_floor:day) |> distinct |> take:1 |> group_count");
        assertIterableEquals(list(list(
                t(v(TestHelper.dateToMillis(2015, 1, 1)), v(1)),
                t(v(TestHelper.dateToMillis(2015, 1, 2)), v(1)),
                t(v(TestHelper.dateToMillis(2015, 1, 3)), v(1)),
                t(v(TestHelper.dateToMillis(2015, 1, 4)), v(1)))), query.execute().data());
    }

    @Test
    public void testLimitAndOffset() throws IOException {
        DataQuery query = new DataQuery(app, false);
        query.select("journey |> limit:2");
        assertEquals(list(list(t(jv(j1)), t(jv(j2)))), query.execute().data());

        query.select("journey |> offset:2 |> limit:2");
        assertEquals(list(list(t(jv(j3)), t(jv(j4)))), query.execute().data());
    }


    @Test
    public void testGetUserIdentifier() throws IOException {
        DataQuery query = new DataQuery(app, true);
        query.select("user.identifier");
        assertEquals(list(list(t(v("u0")), t(v("u1")), t(v()))), query.execute().data());
    }

    @Test
    public void testCompactBy() throws IOException {
        DataQuery query = new DataQuery(app, true);
        query.select("(user.identifier, journey) |> compact_by:0 |> take:0");
        assertEquals(list(list(t(v("u0")), t(v("u1")))), query.execute().data());
    }
}