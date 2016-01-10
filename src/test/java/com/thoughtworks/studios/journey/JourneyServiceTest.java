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
package com.thoughtworks.studios.journey;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.function.Function;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.test.TestGraphDatabaseFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.studios.journey.TestHelper.*;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.JSONUtils.*;
import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;
import static com.thoughtworks.studios.journey.utils.MapUtils.merge;
import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.Iterables.*;
import static org.neo4j.server.rest.domain.JsonHelper.jsonToList;
import static org.neo4j.server.rest.domain.JsonHelper.readJson;

public class JourneyServiceTest {

    private JourneyService service;

    @Before
    public void setup() throws IOException {
        GraphDatabaseService db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        service = new JourneyService(db);
    }

    @Test
    public void statusShouldReturn200() {
        assertEquals(200, service.status().getStatus());
    }

    @Test
    public void setupSchemaShouldBeIdempotent() {
        assertEquals(200, service.setupSchema("foo").getStatus());
        assertEquals(200, service.setupSchema("bar").getStatus());
        assertEquals(200, service.setupSchema("bar").getStatus());
    }


    @Test
    public void testDestroyAllDataUnderOneNameSpace() throws IOException {
        service.setupSchema("parsley");
        service.setupSchema("acme");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a0", 10L, "u1"),
                createEventAttributes("s0", "a1", 20L, "u1"),
                createEventAttributes("s1", "a1", 30L),
                createEventAttributes("s1", "a3", 40L, "u2")
        ));
        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        assertEquals(201, service.addEvents("acme", eventJson).getStatus());

        assertEquals(200, service.destroy("parsley").getStatus());

        assertEquals(0, jsonToListMap((String) service.journeys("parsley", "", 1, 0, true, 101).getEntity()).size());
        assertEquals(1, jsonToListMap((String) service.journeys("acme", "", 1, 0, true, 101).getEntity()).size());

    }

    @Test
    public void canFetchJourneysAfterImportEvents() throws IOException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a0", 10L, "u1"),
                createEventAttributes("s0", "a1", 20L, "u1"),
                createEventAttributes("s1", "a1", 30L, (String) null),
                createEventAttributes("s1", "a3", 40L, "u2")
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        Response response = service.journeys("parsley", "", -1, 0, true, 101);
        assertEquals(200, response.getStatus());
        List<Map> journeys = jsonToListMap((String) response.getEntity());
        assertEquals(2, count(journeys));
        Iterable<Map> events0 = (Iterable<Map>) journeys.get(0).get("events");
        assertIterableEquals(iterable("/url/a1", "/url/a3"), map(pluck("url"), events0));
        assertIterableEquals(iterable("a1", "a3"), map(pluck("action_label"), events0));
        assertEquals("u2", ((Map) journeys.get(0).get("user")).get("uid"));

        Iterable<Map> events1 = (Iterable<Map>) journeys.get(1).get("events");
        assertIterableEquals(iterable("/url/a0", "/url/a1"), map(pluck("url"), events1));
        assertIterableEquals(iterable("a0", "a1"), map(pluck("action_label"), events1));
        assertEquals("u1", ((Map) journeys.get(1).get("user")).get("uid"));
    }


    @Test
    public void importEventsWithOptionalFields() throws IOException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                merge(createEventAttributes("s0", "a0", 10L, "u1"),
                        mapOf("client_ip", (Object) "10.3.3.3",
                                "status_code", "302"))
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        Response response = service.journeys("parsley", "", -1, 0, true, 101);
        assertEquals(200, response.getStatus());
        List<Map> journeys = jsonToListMap((String) response.getEntity());
        assertEquals(1, count(journeys));
        Iterable<Map> events0 = (Iterable<Map>) journeys.get(0).get("events");
        assertIterableEquals(iterable("10.3.3.3"), map(pluck("client_ip"), events0));
        assertIterableEquals(iterable(302), map(pluck("status_code"), events0));
    }

    @Test
    public void fetchJourneyWithJsonQuery() throws IOException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a0", 10L, "u1"),
                createEventAttributes("s0", "a1", 20L, "u1"),
                createEventAttributes("s1", "a1", 30L),
                createEventAttributes("s1", "a1", 30L),
                createEventAttributes("s1", "a3", 40L, "u2")
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        String queryJson = "[\"user.identifier = 'u1'\"]";
        Response response = service.journeys("parsley", queryJson, -1, 0, true, 101);
        assertEquals(200, response.getStatus());
        List<Map> journeys = jsonToListMap((String) response.getEntity());
        assertEquals(1, count(journeys));
        Iterable<Map> events0 = (Iterable<Map>) journeys.get(0).get("events");
        assertIterableEquals(iterable("/url/a0", "/url/a1"), map(pluck("url"), events0));
        assertIterableEquals(iterable("a0", "a1"), map(pluck("action_label"), events0));
        assertEquals("u1", ((Map) journeys.get(0).get("user")).get("uid"));
    }

    @Test
    public void reindexJourneys() throws IOException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a0", 10L, "u1"),
                createEventAttributes("s0", "a1", 20L, "u1"),
                createEventAttributes("s1", "a1", 30L),
                createEventAttributes("s1", "a3", 40L, "u2")
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        assertEquals(200, service.reindex("parsley").getStatus());

        String queryJson = "[\"user.identifier =~ 'u*'\"]";
        Response response = service.journeys("parsley", queryJson, -1, 0, true, 101);
        assertEquals(200, response.getStatus());
        List<Map> journeys = jsonToListMap((String) response.getEntity());
        assertEquals(2, count(journeys));
    }

    @Test
    public void fetchJourneysSummary() throws IOException, JsonParseException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a0", 10L, "u1"),
                createEventAttributes("s0", "a1", 20L, "u1"),
                createEventAttributes("s1", "a1", 30L),
                createEventAttributes("s1", "a3", 40L, "u2")
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        String queryJson = "[\"user.identifier = 'u1'\"]";
        Response response = service.journeysSummary("parsley", queryJson);
        assertEquals(200, response.getStatus());
        Map<String, Object> summary = (Map<String, Object>) readJson((String) response.getEntity());
        assertEquals(1, summary.get("journey_count"));
        assertEquals(1, summary.get("user_count"));
    }

    @Test
    public void fetchJourneysActionGraph() throws IOException, JsonParseException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a0", 10L, "u1"),
                createEventAttributes("s0", "a1", 20L, "u1"),
                createEventAttributes("s1", "a1", 30L),
                createEventAttributes("s1", "a3", 40L, "u2")
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        String queryJson = "[\"user.identifier = 'u1'\"]";
        Response response = service.journeysActionGraph("parsley", queryJson, 10);
        assertEquals(200, response.getStatus());
        Map<String, Object> graph = (Map<String, Object>) readJson((String) response.getEntity());
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) graph.get("nodes");
        List<Map<String, Object>> links = (List<Map<String, Object>>) graph.get("links");

        assertIterableEquals(list("$start", "a0", "a1"), map(pluck("name"), nodes));
        assertIterableEquals(list(1, 0), map(pluck("source"), links));
        assertIterableEquals(list(2, 1), map(pluck("target"), links));
        assertIterableEquals(list(1, 1), map(pluck("weight"), links));
    }


    @Test
    public void testFrequentPathMining() throws IOException, JsonParseException {
        service.setupSchema("parsley");
        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a", dateToMillis(2011, 12, 11, 11), "u1"),
                createEventAttributes("s0", "e", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "f", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "g", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "c", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s1", "a", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "d", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "b", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "f", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "g", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s2", "d", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "e", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "f", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "g", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "c", dateToMillis(2011, 12, 13, 5), "u2")

        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        assertEquals(201, service.buildSuffixTrees("parsley", 3, 100).getStatus());


        Response response = service.frequentPaths("parsley", true, 0.5f, "GLOBAL");

        assertEquals(200, response.getStatus());
        List<Map<String, Object>> patterns = jsonToList((String) response.getEntity());

        assertEquals(3, patterns.size());
        Map<String, Object> pattern = sortByPatternLength(patterns).get(0);

        assertEquals(list("e", "f", "g", "c"), pattern.get("actions"));
        assertEquals(2, pattern.get("support"));
        assertEquals(4, pattern.get("absolute_support_base"));
        assertEquals(2, pattern.get("relative_support_base"));
        assertEquals(2, ((List) pattern.get("journeys")).size());

    }

    private List<Map<String, Object>> sortByPatternLength(List<Map<String, Object>> patterns) {
        return toList(sort(patterns, new Function<Map<String, Object>, Comparable>() {
            @Override
            public Comparable apply(Map<String, Object> pattern) throws RuntimeException {
                return -1 * ((List) pattern.get("actions")).size();
            }
        }));
    }

    @Test
    public void testChurnPOSTAction() throws IOException, JsonParseException {
        service.setupSchema("parsley");
        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a", dateToMillis(2011, 12, 11, 11), "u1"),
                createEventAttributes("s0", "e", dateToMillis(2011, 12, 12, 11), "u1", "POST"),
                createEventAttributes("s0", "e", dateToMillis(2011, 12, 12, 11), "u1", "POST"),
                createEventAttributes("s0", "e", dateToMillis(2011, 12, 12, 11), "u1", "POST"),
                createEventAttributes("s0", "g", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "c", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s1", "a", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "d", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "b", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "e", dateToMillis(2011, 12, 12, 5), "u1", "POST"),
                createEventAttributes("s1", "f", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "f", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "f", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "g", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s2", "d", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "e", dateToMillis(2011, 12, 13, 5), "u2", "POST"),
                createEventAttributes("s2", "e", dateToMillis(2011, 12, 13, 5), "u2", "POST"),
                createEventAttributes("s2", "e", dateToMillis(2011, 12, 13, 5), "u2", "POST"),
                createEventAttributes("s2", "e", dateToMillis(2011, 12, 13, 5), "u2", "POST"),
                createEventAttributes("s2", "f", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "g", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "c", dateToMillis(2011, 12, 13, 5), "u2")
        ));
        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        assertEquals(201, service.buildSuffixTrees("parsley", 3, 100).getStatus());


        Response response = service.churnPOSTActions("parsley", 2, 0.1f);

        assertEquals(200, response.getStatus());
        List<Map<String, Object>> actions = jsonToList((String) response.getEntity());
        assertEquals(1, actions.size());

        Map<String, Object> action = actions.get(0);
        assertEquals("e", action.get("label"));
        assertEquals(2, action.get("total_repeated_journeys"));
        assertEquals(3, action.get("involved_journey_count"));
        assertEquals(2.3333333, action.get("average_repeats"));
    }

    @Test
    public void testActionLabels() throws IOException, JsonParseException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a1", dateToMillis(2014, 12, 11, 11), "u1"),
                createEventAttributes("s0", "a0", dateToMillis(2014, 12, 12, 11), "u1"),
                createEventAttributes("s1", "a1", dateToMillis(2014, 12, 12, 5)),
                createEventAttributes("s1", "a3", dateToMillis(2014, 12, 13, 5), "u2"),
                createEventAttributes("s1", "a3", dateToMillis(2014, 12, 14, 5), "u2")
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());

        Response response = service.actionLabels("parsley");
        assertEquals(200, response.getStatus());
        List<String> labels = (List<String>) readJson((String) response.getEntity());
        assertIterableEquals(iterable("a0", "a1", "a3"), labels);
    }


    @Test
    public void testActionLabelsExcludeIgnoredActions() throws IOException, JsonParseException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a0", dateToMillis(2014, 12, 11, 11), "u1"),
                createEventAttributes("s0", "a1", dateToMillis(2014, 12, 12, 11), "u1"),
                createEventAttributes("s1", "a1", dateToMillis(2014, 12, 12, 5)),
                createEventAttributes("s1", "a3", dateToMillis(2014, 12, 13, 5), "u2"),
                createEventAttributes("s1", "a3", dateToMillis(2014, 12, 14, 5), "u2")
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        assertEquals(200, service.ignoreAction("parsley", "a1", true).getStatus());

        Response response = service.actionLabels("parsley");
        assertEquals(200, response.getStatus());
        List<String> labels = (List<String>) readJson((String) response.getEntity());
        assertIterableEquals(iterable("a0", "a3"), labels);
    }

    @Test
    public void testFetchAllIgnoredActions() throws IOException, JsonParseException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a0", dateToMillis(2014, 12, 11, 11), "u1"),
                createEventAttributes("s0", "a1", dateToMillis(2014, 12, 12, 11), "u1"),
                createEventAttributes("s1", "a1", dateToMillis(2014, 12, 12, 5)),
                createEventAttributes("s1", "a3", dateToMillis(2014, 12, 13, 5), "u2"),
                createEventAttributes("s1", "a3", dateToMillis(2014, 12, 14, 5), "u2")
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        assertEquals(200, service.ignoreAction("parsley", "a1", true).getStatus());
        assertEquals(200, service.ignoreAction("parsley", "a3", true).getStatus());

        Response response = service.ignoredActionLabels("parsley");
        assertEquals(200, response.getStatus());
        List<String> labels = (List<String>) readJson((String) response.getEntity());
        assertIterableEquals(iterable("a1", "a3"), labels);

        assertEquals(200, service.ignoreAction("parsley", "a3", false).getStatus());


        labels = (List<String>) readJson((String) service.ignoredActionLabels("parsley").getEntity());
        assertIterableEquals(iterable("a1"), labels);

    }

    @Test
    public void testFetchEventCountForAnActionForPeriodOfTime() throws IOException, JsonParseException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a0", dateToMillis(2014, 12, 11, 11), "u1"),
                createEventAttributes("s0", "a1", dateToMillis(2014, 12, 12, 11), "u1"),
                createEventAttributes("s1", "a1", dateToMillis(2014, 12, 12, 5)),
                createEventAttributes("s1", "a3", dateToMillis(2014, 12, 13, 5), "u2"),
                createEventAttributes("s1", "a3", dateToMillis(2014, 12, 14, 5), "u2")
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());

        Response response = service.query("parsley", "event.timestamp |> time_floor:day |> to_date |> group_count", "[]", "[\"a1\"]", false);
        assertEquals(200, response.getStatus());
        assertEquals(list(list(list("2014-12-12T00:00:00.000Z", 2))), extractDataFromQueryResult((String) response.getEntity()));
    }


    @Test
    public void testLoadJourneysById() throws IOException {
        service.setupSchema("parsley");

        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a0", 10L, "u1"),
                createEventAttributes("s0", "a1", 20L, "u1"),
                createEventAttributes("s1", "a1", 30L),
                createEventAttributes("s1", "a3", 40L, "u2")
        ));

        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());

        List<Map> journeyList = jsonToListMap((String) service.journeys("parsley", "", -1, 0, true, 101).getEntity());
        int j1Id = (int) journeyList.get(0).get("id");


        Response response = service.journeyByIds("parsley", String.valueOf(j1Id), 1, 1);
        List<Map> js = jsonToListMap((String) response.getEntity());
        Iterable<Map> events0 = (Iterable<Map>) js.get(0).get("events");
        assertIterableEquals(iterable("/url/a3"), map(pluck("url"), events0));
        assertIterableEquals(iterable("a3"), map(pluck("action_label"), events0));
        assertEquals("u2", ((Map) journeyList.get(0).get("user")).get("uid"));
    }

    @Test
    public void testQueryFunnelData() throws IOException {
        service.setupSchema("parsley");
        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a", dateToMillis(2011, 12, 11, 11), "u1"),
                createEventAttributes("s0", "e", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "f", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "g", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "c", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s1", "a", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "d", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "b", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "f", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "g", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s2", "d", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "e", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "f", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "g", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "c", dateToMillis(2011, 12, 13, 5), "u2")
        ));
        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        Response response = service.query("parsley", "user |> distinct |> count", "[]", "[\"a\", \"d\"]", true);
        assertEquals(200, response.getStatus());
        assertEquals(list(list(list(1)), list(list(1))), extractDataFromQueryResult((String) response.getEntity()));
    }

    @Test
    public void testSegmentationData() throws IOException {
        service.setupSchema("parsley");
        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a", dateToMillis(2011, 12, 11, 11), "u1"),
                createEventAttributes("s0", "e", dateToMillis(2011, 12, 12, 11), "u1", mapOf("color", "red")),
                createEventAttributes("s0", "f", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "g", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "c", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s1", "a", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "d", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "b", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "f", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "g", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s2", "d", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "e", dateToMillis(2011, 12, 13, 5), "u2", mapOf("color", "green")),
                createEventAttributes("s2", "f", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "g", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "c", dateToMillis(2011, 12, 13, 5), "u2")
        ));
        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        Response response = service.query("parsley", "event.color |> group_count", "[]", "[\"e\"]", true);
        assertEquals(200, response.getStatus());
        assertEquals(list(list(list("red", 1),
                list("green", 1))), extractDataFromQueryResult((String) response.getEntity()));
    }

    @Test
    public void testSelectUser() throws IOException {
        service.setupSchema("parsley");
        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a", dateToMillis(2011, 12, 11, 11), "u1"),
                createEventAttributes("s0", "e", dateToMillis(2011, 12, 12, 11), "u1", mapOf("color", "red")),
                createEventAttributes("s0", "f", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "g", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s0", "c", dateToMillis(2011, 12, 12, 11), "u1"),
                createEventAttributes("s1", "a", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "d", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "b", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "f", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s1", "g", dateToMillis(2011, 12, 12, 5), "u1"),
                createEventAttributes("s2", "d", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "e", dateToMillis(2011, 12, 13, 5), "u2", mapOf("color", "green")),
                createEventAttributes("s2", "f", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "g", dateToMillis(2011, 12, 13, 5), "u2"),
                createEventAttributes("s2", "c", dateToMillis(2011, 12, 13, 5), "u2")
        ));
        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        Response response = service.query("parsley", "user", "[]", "[\"e\"]", true);
        assertEquals(200, response.getStatus());
        assertEquals(list(list(list(mapOf("uid", "u1", "traits", mapOf(), "anonymous_id", null)),
                list(mapOf("uid", "u2", "traits", mapOf(), "anonymous_id", null)))), extractDataFromQueryResult((String) response.getEntity()));
    }

    @Test
    public void testSelectDate() throws IOException {
        service.setupSchema("parsley");
        String eventJson = toJson(iterable(
                createEventAttributes("s0", "a", dateToMillis(2011, 12, 11, 11), "u1")
        ));
        assertEquals(201, service.addEvents("parsley", eventJson).getStatus());
        Response response = service.query("parsley", "event.timestamp |> to_date", "[]", "[\"*\"]", true);
        assertEquals(200, response.getStatus());
        assertEquals(list(list(list("2011-12-11T11:00:00.000Z"))), extractDataFromQueryResult((String) response.getEntity()));
    }

    private Object extractDataFromQueryResult(String responseBody) throws IOException {
        Map<String, Object> result = jsonToMap(responseBody);
        return result.get("data");
    }


}
