/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provids out-of-box action path analysis features on top of the graph database.
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
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IteratorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.thoughtworks.studios.journey.utils.GraphDbUtils.getSingleEndNode;
import static com.thoughtworks.studios.journey.utils.GraphDbUtils.propertyValueOrNull;

public class Requests implements Models {

    static final String PROP_START_AT = "start_at";
    private static final String PROP_DIGEST = "digest";
    private static final String PROP_URL = "url";
    private static final String PROP_HTTP_METHOD = "http_method";
    private static final String PROP_CLIENT_IP = "client_ip";
    private static final String PROP_STATUS_CODE = "status_code";
    private static final String PROP_REFERRER = "referrer";

    private Application app = null;

    private GraphDatabaseService graphDb;

    public Requests(Application application) {
        this.app = application;
        this.graphDb = app.graphDB();
    }

    /**
     * Setup request related schema for the namespace
     */
    public void setupSchema() {
        GraphDbUtils.createIndexIfNotExists(graphDb, getLabel(), PROP_START_AT);
        GraphDbUtils.createIndexIfNotExists(graphDb, getLabel(), PROP_DIGEST);
    }

    public String getHttpMethod(Node node) {
        return (String) propertyValueOrNull(node, PROP_HTTP_METHOD);
    }

    public String getUrl(Node node) {
        return (String) propertyValueOrNull(node, PROP_URL);
    }

    public Long getStartAt(Node node) {
        return (Long) node.getProperty(PROP_START_AT);
    }

    /**
     * This method add single request to the database
     *
     * @param requestAttrs : map of attributes
     */
    public Node add(Map<String, Object> requestAttrs) {
        String digest = (String) requestAttrs.get("digest");
        if (requestExists(digest)) {
            return null;
        }
        Node request = createRequestNode(requestAttrs);
        RequestPostImport processor = new RequestPostImport(app, request, requestAttrs);
        processor.process();

        return request;
    }


    private boolean requestExists(String digest) {
        return findByDigest(digest) != null;
    }

    private Actions actions() {
        return app.actions();
    }

    private Node createRequestNode(Map<String, Object> attributes) {
        Node node = graphDb.createNode(getLabel());
        node.setProperty(PROP_DIGEST, attributes.get("digest"));
        node.setProperty(PROP_START_AT, ((Number) attributes.get("start_at")).longValue());
        if (attributes.get("url") != null) {
            node.setProperty(PROP_URL, attributes.get("url"));
        }

        if (attributes.get("referrer") != null) {
            node.setProperty(PROP_REFERRER, attributes.get("referrer"));
        }

        if (attributes.get("http_method") != null) {
            node.setProperty(PROP_HTTP_METHOD, attributes.get("http_method"));
        }
        if (attributes.get("client_ip") != null) {
            node.setProperty(PROP_CLIENT_IP, attributes.get("client_ip"));
        }
        if (attributes.get("status_code") != null) {
            node.setProperty(PROP_STATUS_CODE, Integer.parseInt((String) attributes.get("status_code")));
        }
        processProperties(node, attributes);

        return node;
    }


    private void processProperties(Node request, Map<String, Object> requestAttrs) {
        Object properties = requestAttrs.get("properties");
        if (properties != null) {
            //noinspection unchecked
            Map<String, Object> propertiesMap = (Map<String, Object>) properties;
            Set<String> propertyNames = propertiesMap.keySet();
            for (String propertyName : propertyNames) {
                Object propertyValue = propertiesMap.get(propertyName);
                addProperty(request, propertyName, propertyValue);
            }
        }
    }

    public void addProperty(Node request, String propertyName, Object propertyValue) {
        app.customProperties().setProperty(request, propertyName, propertyValue);
    }


    public Node findByDigest(String digest) {
        return graphDb.findNode(getLabel(), PROP_DIGEST, digest);
    }

    public Label getLabel() {
        return app.nameSpacedLabel("Request");
    }

    public long count() {
        return IteratorUtil.count(graphDb.findNodes(getLabel()));
    }

    public Node journeyOf(Node request) {
        return getSingleEndNode(request, RelTypes.BELONGS_TO);
    }

    public Map<String, Object> toHash(Node request) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", request.getId());
        result.put("url", getUrl(request));
        result.put("start_at", getStartAt(request));
        result.put("http_method", getHttpMethod(request));
        result.put("client_ip", getClientIp(request));
        result.put("status_code", getStatusCode(request));
        result.put("action_label", getActionLabel(request));
        result.put("referrer", getReferrer(request));
        result.put("properties", properties(request));
        return result;
    }

    public String getReferrer(Node request) {
        return (String) propertyValueOrNull(request, PROP_REFERRER);
    }

    private Integer getStatusCode(Node request) {
        return (Integer) propertyValueOrNull(request, PROP_STATUS_CODE);
    }

    private String getClientIp(Node request) {
        return (String) propertyValueOrNull(request, PROP_CLIENT_IP);
    }

    public String getActionLabel(Node request) {
        return actions().getActionLabel(action(request));
    }

    public Node action(Node request) {
        return getSingleEndNode(request, RelTypes.ACTION);
    }

    public Function<Node, String> getActionLabelFn() {
        return new Function<Node, String>() {
            @Override
            public String apply(Node request) {
                return getActionLabel(request);
            }
        };
    }

    public Map<String, Set> properties(Node request) {
        return app.customProperties().properties(request);
    }

    public Set<Object> values(Node request, String propertyName) {
        return app.customProperties().getProperty(request, propertyName);
    }
}