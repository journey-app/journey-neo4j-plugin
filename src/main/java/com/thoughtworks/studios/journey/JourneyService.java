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

import com.thoughtworks.studios.journey.cspmining.*;
import com.thoughtworks.studios.journey.importexport.DataImportExport;
import com.thoughtworks.studios.journey.importexport.Reporter;
import com.thoughtworks.studios.journey.jql.DataQuery;
import com.thoughtworks.studios.journey.jql.DataQueryResult;
import com.thoughtworks.studios.journey.jql.JourneyQuery;
import com.thoughtworks.studios.journey.jql.Stop;
import com.thoughtworks.studios.journey.models.*;
import com.thoughtworks.studios.journey.utils.BatchTransaction;
import com.thoughtworks.studios.journey.utils.GraphDbUtils;
import com.thoughtworks.studios.journey.utils.JSONUtils;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.index.lucene.ValueContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.thoughtworks.studios.journey.utils.JSONUtils.*;
import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;

@Path("/journey")
public class JourneyService {

    private static final Logger logger = LoggerFactory.getLogger(JourneyService.class);

    private GraphDatabaseService graphDB;

    public JourneyService(@Context GraphDatabaseService graphDB) {
        this.graphDB = graphDB;
    }

    /**
     * API for testing plugin installed correctly
     *
     * @return 200 response
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/status")
    public Response status() {
        return Response.status(Response.Status.OK).entity("OK").build();
    }

    /**
     * API for setup a schema namespace (indexes)
     *
     * @param ns: namespace name
     * @return 200 response
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{ns}/setup_schema")
    public Response setupSchema(@PathParam("ns") String ns) {
        Lock writingLock = getWritingLock(ns);
        writingLock.lock();
        try {
            Application app = new Application(graphDB, ns);
            try (Transaction tx = graphDB.beginTx()) {
                app.setupSchema();
                tx.success();
            }
        } finally {
            writingLock.unlock();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * API for exporting all data under a namespace
     *
     * @param ns: namespace name
     * @return 200 response if migrate finished
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{ns}/export")
    public Response export(@PathParam("ns") String ns) {
        final Application app = new Application(graphDB, ns);
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)));
                try (Transaction ignored = graphDB.beginTx()) {
                    new DataImportExport(app).export(writer);
                }
            }
        };
        return Response.ok(stream).build();
    }

    /**
     * API for importing data exported via export api. The importing does not setup all the indexes,
     * so user need run /reindex api after importing.
     *
     * @param ns:     namespace
     * @param stream: request body stream
     * @return 200 response if import success
     * @throws IOException
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{ns}/import")
    public Response imports(@PathParam("ns") String ns, InputStream stream) throws IOException {
        Lock writingLock = getWritingLock(ns);
        writingLock.lock();
        try {
            final Application app = new Application(graphDB, ns);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            try (BatchTransaction tx = new BatchTransaction(graphDB, 1000)) {
                DataImportExport importer = new DataImportExport(app, new Reporter() {
                    @Override
                    public void report() {
                        tx.increment();
                    }
                });
                importer.importFrom(bufferedReader);
            }
        } finally {
            writingLock.unlock();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * API for run migrations under the namespace
     *
     * @param ns namespace name
     * @return 200 response
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{ns}/migrate")
    public Response migrate(@PathParam("ns") String ns) {
        Lock writingLock = getWritingLock(ns);
        writingLock.lock();
        try {
            Application app = new Application(graphDB, ns);
            ArrayList<Long> ids = new ArrayList<>();
            try (Transaction ignored = graphDB.beginTx()) {
                ResourceIterator<Node> nodes = graphDB.findNodes(app.journeys().getLabel());
                while (nodes.hasNext()) {
                    Node node = nodes.next();
                    ids.add(node.getId());
                }
            }

            try (BatchTransaction tx = new BatchTransaction(graphDB, 5000)) {
                for (Long id : ids) {
                    Node journey = graphDB.getNodeById(id);
                    if(!journey.hasProperty(Journeys.PROP_LENGTH)) {
                        Integer length = journey.getDegree(RelTypes.BELONGS_TO, Direction.INCOMING);
                        journey.setProperty(Journeys.PROP_LENGTH, length);
                        Index<Node> index = GraphDbUtils.legacyIndex(graphDB, app.journeys().getLabel());
                        index.add(journey, Journeys.PROP_LENGTH, new ValueContext(length).indexNumeric());
                    }
                    tx.increment();
                }
            }
        } finally {
            writingLock.unlock();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * API for destroying all data under a namespace
     *
     * @param ns namespace name
     * @return 200 response
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{ns}/destroy")
    public Response destroy(@PathParam("ns") String ns) {
        Lock writingLock = getWritingLock(ns);
        writingLock.lock();
        try {
            Application app = new Application(graphDB, ns);
            try (Transaction tx = graphDB.beginTx()) {
                app.tearDownSchema();
                tx.success();
            }

            try (Transaction tx = graphDB.beginTx()) {
                app.journeys().tearDownLegacyIndex();
                tx.success();
            }

            app.destroyData();
        } finally {
            writingLock.unlock();
        }

        return Response.status(Response.Status.OK).build();
    }


    /**
     * API for reindex journeys.
     *
     * @param ns namespace under operation
     * @return 200 response
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{ns}/reindex")
    public Response reindex(@PathParam("ns") String ns) {
        Lock writingLock = getWritingLock(ns);
        writingLock.lock();
        try {
            Application app = new Application(graphDB, ns);
            try (Transaction tx = graphDB.beginTx()) {
                app.journeys().tearDownLegacyIndex();
                tx.success();
            }

            ArrayList<Long> ids = new ArrayList<>();
            try (Transaction tx = graphDB.beginTx()) {
                ResourceIterator<Node> journeys = graphDB.findNodes(app.journeys().getLabel());
                while (journeys.hasNext()) {
                    ids.add(journeys.next().getId());
                }
                tx.success();
            }

            try (BatchTransaction tx = new BatchTransaction(graphDB, 100)) {
                for (Long id : ids) {
                    Node journey = graphDB.getNodeById(id);
                    app.journeys().reindex(journey);
                    tx.increment();
                }
            }
        } finally {
            writingLock.unlock();
        }

        return Response.status(Response.Status.OK).build();
    }


    /**
     * API for adding multiple events.
     * Post events via request body in json format. e.g.
     *   [{
     *      "action_label":"do x",
     *      "start_at":1451956588844,
     *      "digest":"907e7d49",
     *      "anonymous_id":"9f0d0311",
     *      "session_id":"9f0d0311",
     *      "user":"johndoe@example.com",
     *      "properties":{
     *          "prop-a":"foo",
     *          "prop-b":"bar"
     *      }
     *    }]
     * @param ns: namespace under operation
     * @param eventsJSON: request body, json format, array event
     * @return 201 response
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/add_events")
    public Response addEvents(@PathParam("ns") String ns,
                              String eventsJSON) throws IOException {

        Lock writingLock = getWritingLock(ns);
        writingLock.lock();
        try {
            Application app = new Application(graphDB, ns);

            List<Map> eventsAttrs = jsonToListMap(eventsJSON);
            try (Transaction tx = graphDB.beginTx()) {
                for (Map eventAttrs : eventsAttrs) {
                    //noinspection unchecked
                    app.events().add(eventAttrs);
                }
                tx.success();
            }
        } finally {
            writingLock.unlock();
        }
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * API for identify a user and setup traits.
     * @param ns: namespace under operation
     * @param uid: unique identify for the user
     * @param anonymousId: anonymous_id to associate existing anonymous events
     * @param traitsJSON: request body for traits for the user
     * @return 201 response
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/identify")
    public Response identify(@PathParam("ns") String ns,
                             @QueryParam("uid") String uid,
                             @QueryParam("anonymous_id") String anonymousId,
                             String traitsJSON) throws IOException {
        Lock writingLock = getWritingLock(ns);
        writingLock.lock();

        try {
            Application app = new Application(graphDB, ns);
            Users users = app.users();

            try (Transaction tx = graphDB.beginTx()) {
                Node user = users.identify(uid, anonymousId);
                if (traitsJSON != null) {
                    Map<String, Object> traits = jsonToMap(traitsJSON);
                    for (String key : traits.keySet()) {
                        users.addTrait(user, key, traits.get(key));
                    }
                }
                tx.success();
            }
        } finally {
            writingLock.unlock();
        }
        return Response.status(Response.Status.CREATED).build();
    }

    private static ConcurrentHashMap<String, Lock> writingLocks = new ConcurrentHashMap<>();

    private static Lock getWritingLock(String ns) {
        Lock lock = new ReentrantLock();
        Lock existingLock = writingLocks.putIfAbsent(ns, lock);
        if (existingLock != null) {
            lock = existingLock;
        }
        return lock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/user/{identifier}")
    public Response user(@PathParam("ns") String ns, @PathParam("identifier") String identifier) throws IOException {
        Application app = new Application(graphDB, ns);
        try (Transaction ignored = graphDB.beginTx()) {
            Node user = app.users().findByIdentifier(identifier);
            Map<String, Object> map = app.users().toHash(user);
            ArrayList<Map<String, Object>> journeys = new ArrayList<>();
            for (Node journey : app.users().journeys(user)) {
                journeys.add(app.journeys().toHash(journey));
            }
            map.put("journeys", journeys);
            return jsonOkResponse(map);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/anonymous_user/{anonymous_id}")
    public Response anonymousUser(@PathParam("ns") String ns, @PathParam("anonymous_id") String anonymousId) throws IOException {
        Application app = new Application(graphDB, ns);
        try (Transaction ignored = graphDB.beginTx()) {
            Node user = app.users().findByAnonymousId(anonymousId);
            Map<String, Object> map = app.users().toHash(user);
            ArrayList<Map<String, Object>> journeys = new ArrayList<>();
            for (Node journey : app.users().journeys(user)) {
                journeys.add(app.journeys().toHash(journey));
            }
            map.put("journeys", journeys);
            return jsonOkResponse(map);
        }
    }

    /**
     * API for query journeys
     * @param ns: namespace under operation
     * @param queryJson: journey conditions. json format.
     * @param limit: limit of return result
     * @param offset: offset the result from start from
     * @param descOrder: whether in desc order
     * @param eventsLimit: max events loaded per journey
     * @return array of journeys in JSON format
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/journeys")
    public Response journeys(@PathParam("ns") String ns,
                             @QueryParam("query") @DefaultValue("") String queryJson,
                             @QueryParam("limit") @DefaultValue("100") int limit,
                             @QueryParam("offset") @DefaultValue("0") int offset,
                             @QueryParam("desc") @DefaultValue("true") boolean descOrder,
                             @QueryParam("events_limit") @DefaultValue("50") int eventsLimit) throws IOException {
        Application app = new Application(graphDB, ns);
        List<String> conditions = parseQueryCondition(queryJson);
        List<Map> result = new ArrayList<>();

        try (Transaction ignored = graphDB.beginTx()) {
            JourneyQuery query = JourneyQuery.Builder.query(app).
                    conditions(conditions).
                    desc(descOrder).
                    limit(limit).
                    offset(offset).
                    build();

            for (Node journey : query.journeys()) {
                result.add(app.journeys().toHash(journey, eventsLimit, 0));
            }
        }
        return jsonOkResponse(result);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/journeys_summary")
    public Response journeysSummary(@PathParam("ns") String ns,
                                    @QueryParam("query") @DefaultValue("") String queryJson) throws IOException {
        Application app = new Application(graphDB, ns);
        List<String> conditions = parseQueryCondition(queryJson);
        int journeyCount = 0;
        Set<Node> users = new HashSet<>();


        try (Transaction ignored = graphDB.beginTx()) {
            JourneyQuery query = JourneyQuery.Builder.query(app).
                    conditions(conditions).
                    build();
            for (Node journey : query.journeys()) {
                journeyCount++;
                users.add(app.journeys().user(journey));
            }

        }
        return jsonOkResponse(mapOf("journey_count", journeyCount, "user_count", users.size()));
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/journeys_action_graph")
    public Response journeysActionGraph(@PathParam("ns") String ns,
                                        @QueryParam("query") @DefaultValue("") String queryJson,
                                        @QueryParam("steps") @DefaultValue("10") int steps) throws IOException {
        Application app = new Application(graphDB, ns);
        List<String> conditions = parseQueryCondition(queryJson);
        ActionsGraph graph = new ActionsGraph(app, steps);
        try (Transaction ignored = graphDB.beginTx()) {
            JourneyQuery query = JourneyQuery.Builder.query(app).
                    conditions(conditions).
                    build();
            for (Node journey : query.journeys()) {
                graph.add(app.journeys().events(journey));
            }
        }
        return jsonOkResponse(graph);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/forward_journey_graph")
    public Response forwardJourneyGraph(@PathParam("ns") String ns,
                                          @QueryParam("query") String queryJson,
                                          @QueryParam("label") String startActionLabel,
                                          @QueryParam("steps") @DefaultValue("4") int steps) throws IOException {
        Application app = new Application(graphDB, ns);
        ActionsGraph graph = new ActionsGraph(app, steps);
        try (Transaction ignored = graphDB.beginTx()) {
            JourneyQuery query = JourneyQuery.Builder.query(app).
                    conditions(parseQueryCondition(queryJson)).
                    desc().
                    build();
            for (Node journey : query.uniqueJourneys()) {
                graph.add(app.journeys().suffixFor(journey, startActionLabel));
            }
        }
        return jsonOkResponse(graph);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/backtrace_journey_graph")
    public Response backtraceJourneyGraph(@PathParam("ns") String ns,
                                          @QueryParam("query") String queryJson,
                                          @QueryParam("label") String startActionLabel,
                                          @QueryParam("steps") @DefaultValue("4") int steps) throws IOException {
        Application app = new Application(graphDB, ns);
        ActionsGraph graph = new ActionsGraph(app, steps);
        try (Transaction ignored = graphDB.beginTx()) {
            JourneyQuery query = JourneyQuery.Builder.query(app).
                    conditions(parseQueryCondition(queryJson)).
                    desc().
                    build();
            for (Node journey : query.uniqueJourneys()) {
                graph.add(app.journeys().reversedPrefixFor(journey, startActionLabel));
            }
        }
        return jsonOkResponse(graph);
    }


    /**
     * API for general data queries.
     * @param ns: namespace under operation.
     * @param select: select expression. Reference the data query document.
     * @param stopsJson: stop expression. Reference the data query document.
     * @param cross: whether to connect journeys for same user.
     * @return json format array of array of array.
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/query")
    public Response query(@PathParam("ns") String ns,
                          @QueryParam("select") String select,
                          @QueryParam("stops") String stopsJson,
                          @QueryParam("cross") @DefaultValue("true") boolean cross) throws IOException {
        Application app = new Application(graphDB, ns);
        try (Transaction ignored = graphDB.beginTx()) {
            try {
                DataQuery dataQuery = new DataQuery(app, cross);
                dataQuery.select(select);
                dataQuery.addStops(JSONUtils.<Map<String, Object>>jsonToListOfT(stopsJson, Map.class));
                return jsonOkResponse(dataQuery.execute());
            } catch (Exception e) {
                logger.warn("Unexpected query error happened.", e);
                return jsonOkResponse(DataQueryResult.error(e));
            }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/experiment_data")
    public Response experimentData(@PathParam("ns") String ns,
                                   @QueryParam("trait_name") String groupTraitName,
                                   @QueryParam("base_stop") String baseStopExpression,
                                   @QueryParam("convert_stop") String convertStopExpression) throws IOException {

        Application app = new Application(graphDB, ns);

        try (Transaction ignored = graphDB.beginTx()) {
            Stop baseStop = Stop.build(app, JSONUtils.jsonToMap(baseStopExpression));
            Stop convertStop = Stop.build(app, JSONUtils.jsonToMap(convertStopExpression));

            JourneyQuery query = baseStop.journeyQuery();

            List<Object[]> data = new ArrayList<>();

            for (Node journey : query.uniqueJourneys()) {
                Node user = app.journeys().user(journey);
                Set<Object> userGroups = app.users().getTraitValue(user, groupTraitName);
                if (userGroups.size() != 1) {
                    continue;
                }

                Object userGroup = userGroups.iterator().next();
                EventIterator iterator = app.journeys().eventIterator(journey, true);

                Stop.MatchResult baseMatch = baseStop.match(iterator);
                if (!baseMatch.matched()) {
                    continue;
                }

                Stop.MatchResult convertMatch = convertStop.match(iterator);
                data.add(new Object[]{userGroup, convertMatch.matched() ? 1 : 0});
            }

            return jsonOkResponse(data);
        }
    }


    /**
     * API to load journeys by ids.
     * @param ns: namespace under operation.
     * @param ids: journey ids, numbers separated by ','.
     * @param eventsLimit: max number of events to be loaded per journey.
     * @param eventsOffset: offset for events pagination.
     * @return array of journeys in JSON format.
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/journeys/{ids}")
    public Response journeyByIds(@PathParam("ns") String ns,
                                 @PathParam("ids") String ids,
                                 @QueryParam("events_limit") @DefaultValue("50") int eventsLimit,
                                 @QueryParam("events_offset") @DefaultValue("0") int eventsOffset) throws IOException {
        Application app = new Application(graphDB, ns);

        List<Map> result = new ArrayList<>();
        try (Transaction ignored = graphDB.beginTx()) {
            for (Node journey : app.journeys().findByIds(StringUtils.split(ids, ","))) {
                result.add(app.journeys().toHash(journey, eventsLimit, eventsOffset));
            }
        }
        return jsonOkResponse(result);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/frequent_paths")
    public Response frequentPaths(@PathParam("ns") String ns,
                                  @QueryParam("ab") @DefaultValue("true") boolean absoluteSupport,
                                  @QueryParam("threshold") @DefaultValue("0.1") float threshold,
                                  @QueryParam("tree_category") @DefaultValue("GLOBAL") String treeCategory) throws IOException {
        Application app = new Application(graphDB, ns);
        List<Pattern> patterns = new ArrayList<>();
        try (Transaction ignored = graphDB.beginTx()) {
            for (SuffixTree tree : SuffixTree.findByCategory(app, TreeCategory.valueOf(treeCategory))) {
                CSPMiner miner = new CSPMiner(tree, threshold, absoluteSupport);
                for (Pattern pattern : miner.suffixPatterns()) {
                    patterns.add(pattern);
                }
            }
        }
        return jsonOkResponse(patterns);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/churn_actions")
    public Response churnPOSTActions(@PathParam("ns") String ns,
                                     @QueryParam("min_churn") @DefaultValue("2") int minNumberOfRepeats,
                                     @QueryParam("threshold") @DefaultValue("0.1") float threshold) throws IOException {
        Application app = new Application(graphDB, ns);
        List<RepeatedAction> actions = new ArrayList<>();

        try (Transaction ignored = graphDB.beginTx()) {
            for (SuffixTree tree : SuffixTree.findByCategory(app, TreeCategory.GLOBAL)) {
                Node action = app.actions().findByActionLabel(tree.getTreeName());
                if (app.actions().getHttpMethod(action).equals("GET")) {
                    continue;
                }

                RepeatedAction repeatedAction = new RepeatedAction(tree.getTreeName(), tree.getJourneyCount());

                CSPMiner miner = new CSPMiner(tree, threshold, false);

                for (Pattern pattern : miner.suffixPatterns()) {
                    if (pattern.numberOfLeadActionRepeats() >= minNumberOfRepeats) {
                        repeatedAction.addPattern(pattern);
                    }
                }

                if (repeatedAction.averageRepeats() >= minNumberOfRepeats) {
                    actions.add(repeatedAction);
                }
            }
        }
        return jsonOkResponse(actions);
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/build_suffix_trees")
    public Response buildSuffixTrees(@PathParam("ns") String ns,
                                     @QueryParam("tree_height_limit") @DefaultValue("8") int treeHeightLimit,
                                     @QueryParam("journeys_limit") @DefaultValue("10000") int journeyLimit) throws IOException {
        Application app = new Application(graphDB, ns);

        destroySuffixTreesWithCategory(app, TreeCategory.GLOBAL);
        JourneyQuery query = JourneyQuery.Builder.
                query(app).
                limit(journeyLimit).
                build();

        new SuffixTreeBuilder(app,
                query,
                TreeCategory.GLOBAL,
                treeHeightLimit
        ).build();

        return Response.status(Response.Status.CREATED).build();
    }

    private void destroySuffixTreesWithCategory(Application app, TreeCategory category) {
        try (Transaction tx = graphDB.beginTx()) {
            for (SuffixTree tree : SuffixTree.findByCategory(app, category)) {
                tree.destroy();
            }
            tx.success();
        }
    }


    private List<String> parseQueryCondition(String conditionJSON) throws IOException {
        return conditionJSON.length() == 0 ? new ArrayList<String>() : jsonToListString(conditionJSON);
    }


    private Response jsonOkResponse(Object results) throws IOException {
        return okResponse(toJson(results));
    }

    private Response okResponse(Object results) throws IOException {
        return Response.status(Response.Status.OK).entity(results).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/destroy_suffix_trees")
    public Response destroySuffixTrees(@PathParam("ns") String ns,
                                       @QueryParam("tree_category") @DefaultValue("GLOBAL") String treeCategory) throws IOException {
        Application app = new Application(graphDB, ns);
        destroySuffixTreesWithCategory(app, TreeCategory.valueOf(treeCategory));
        return Response.status(Response.Status.OK).build();
    }


    /**
     * API for ignore or recover a label.
     * @param ns: namespace under operation.
     * @param label: label of action to be ignored or recovered.
     * @param toggle: 'true' for ignore, 'false' for recover.
     * @return 200 response.
     * @throws IOException
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/ignore_action")
    public Response ignoreAction(@PathParam("ns") String ns,
                                 @QueryParam("label") String label,
                                 @QueryParam("toggle") @DefaultValue("true") boolean toggle) throws IOException {
        Application app = new Application(graphDB, ns);

        try (Transaction tx = graphDB.beginTx()) {
            Node action = app.actions().findByActionLabel(label);
            if (toggle) {
                app.actions().ignore(action);
            } else {
                app.actions().unIgnore(action);
            }

            tx.success();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * API for getting action labels under namespace (ignored label are excluded)
     * @param ns: namespace under operation
     * @return json format of list of string label
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/action_labels")
    public Response actionLabels(@PathParam("ns") String ns) throws IOException {
        Application app = new Application(graphDB, ns);
        List<String> labels = new ArrayList<>();
        try (Transaction ignored = graphDB.beginTx()) {
            Iterable<Node> actions = app.actions().allExcludeIgnored();
            for (Node action : actions) {
                labels.add(app.actions().getActionLabel(action));
            }
        }

        Collections.sort(labels);
        return jsonOkResponse(labels);
    }

    /**
     * API for getting all ignored actions
     * @param ns: namespace under operation
     * @return list of action label in json format
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/ignored_action_labels")
    public Response ignoredActionLabels(@PathParam("ns") String ns) throws IOException {
        Application app = new Application(graphDB, ns);
        LinkedList<String> labels = new LinkedList<>();
        try (Transaction ignored = graphDB.beginTx()) {
            Iterable<Node> actions = app.actions().allIgnored();
            for (Node action : actions) {
                labels.addLast(app.actions().getActionLabel(action));
            }
        }
        return jsonOkResponse(labels);
    }

    /**
     * API for getting all custom property names under namespace
     * @param ns namespace under operation
     * @return json format of list of string label
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/custom_properties")
    public Response customProperties(@PathParam("ns") String ns) throws IOException {
        Application app = new Application(graphDB, ns);
        List<String> names = new ArrayList<>();
        try (Transaction ignored = graphDB.beginTx()) {
            Iterable<Node> properties = app.customProperties().all();

            for (Node property : properties) {
                names.add(app.customProperties().getName(property));
            }
        }

        Collections.sort(names);
        return jsonOkResponse(names);

    }

    /**
     * API for getting all user traits names under namespace
     * @param ns namespace under operation
     * @return json format of list of string
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/user_traits")
    public Response userTraits(@PathParam("ns") String ns) throws IOException {
        Application app = new Application(graphDB, ns);
        List<String> names = new ArrayList<>();
        UserTraits module = app.userTraits();
        try (Transaction ignored = graphDB.beginTx()) {
            Iterable<Node> traits = module.all();
            for (Node trait : traits) {
                names.add(module.getName(trait));
            }
        }

        Collections.sort(names);
        return jsonOkResponse(names);
    }

    /**
     * Get sample values for all traits. OK it is not really a sampling for the performance reason.
     * What we get are likely to be added values because of how neo4j store the relationships
     *
     * @param ns namespace under operation
     * @return json format of map of trait name and values
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ns}/traits_sample")
    public Response traitsSample(@PathParam("ns") String ns, @QueryParam("sample") @DefaultValue("500") int sampleLimit) throws IOException {
        Application app = new Application(graphDB, ns);
        Map<String, Set> sampleResult = new HashMap<>();

        UserTraits module = app.userTraits();
        try (Transaction ignored = graphDB.beginTx()) {
            Iterable<Node> traits = module.all();
            for (Node trait : traits) {
                sampleResult.put(module.getName(trait), module.sampleValues(trait, sampleLimit));
            }
        }

        return jsonOkResponse(sampleResult);
    }

}
