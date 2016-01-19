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
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.thoughtworks.studios.journey.utils.GraphDbUtils.propertyValueOrNull;
import static com.thoughtworks.studios.journey.utils.IterableUtils.toIterable;

public class Users implements Models {
    private static final String PROP_IDENTIFIER = "identifier";
    private static final String PROP_ANONYMOUS_ID = "anonymous_id";
    private final Application app;
    private final ChronologicalChain journeysChainHelper;
    private GraphDatabaseService graphDb;


    public Users(Application application) {
        this.app = application;
        this.graphDb = application.graphDB();
        this.journeysChainHelper = new ChronologicalChain(RelTypes.FIRST_JOURNEY, RelTypes.NEXT, RelTypes.LAST_JOURNEY, Journeys.PROP_START_AT);
    }

    public void setupSchema() {
        GraphDbUtils.createIndexIfNotExists(graphDb, getLabel(), PROP_IDENTIFIER);
        GraphDbUtils.createIndexIfNotExists(graphDb, getLabel(), PROP_ANONYMOUS_ID);
    }

    public Label getLabel() {
        return app.nameSpacedLabel("User");
    }

    public Iterable<Node> findAll() {
        return toIterable(graphDb.findNodes(getLabel()));
    }

    public String getIdentifier(Node user) {
        if (isAnonymous(user)) {
            return null;
        }
        return (String) user.getProperty(PROP_IDENTIFIER);
    }

    public Node findByAnonymousId(String anonymousId) {
        return graphDb.findNode(getLabel(), PROP_ANONYMOUS_ID, anonymousId);
    }

    public Node findByIdentifier(String identifier) {
        return graphDb.findNode(getLabel(), PROP_IDENTIFIER, identifier);
    }

    public Iterable<Node> journeys(Node user, boolean reversed) {
        return reversed ? journeysChainHelper.reverseNodes(user) : journeysChainHelper.nodes(user);
    }

    public Iterable<Node> journeys(Node user) {
        return journeys(user, false);
    }


    public Iterable<Node> journeysFrom(Node journey) {
        return journeysChainHelper.nodesAfter(journey);
    }

    public void addJourney(Node user, Node journey) {
        if (user.equals(app.journeys().user(journey))) {
            journeysChainHelper.revise(journey);
        } else {
            journeysChainHelper.insert(user, journey);
            app.journeys().setUser(journey, user);
        }
    }

    public Long getStartActiveAt(Node user) {
        return app.journeys().getStartAt(journeysChainHelper.first(user));
    }

    public Long getLastActiveAt(Node user) {
        return app.journeys().getFinishAt(journeysChainHelper.last(user));
    }

    public boolean isAnonymous(Node user) {
        return !user.hasProperty(PROP_IDENTIFIER);
    }

    public Node createByAnonymousId(String anonymousId) {
        Node fresh = graphDb.createNode(getLabel());
        fresh.setProperty(PROP_ANONYMOUS_ID, anonymousId);
        return fresh;

    }

    public Node createByIdentifier(String uid) {
        Node fresh = graphDb.createNode(getLabel());
        fresh.setProperty(PROP_IDENTIFIER, uid);
        return fresh;
    }

    // rob everything left user have, give them to the right user
    // then destroy the left user -- an extremely cruel method :-)
    public void merge(Node leftUser, Node rightUser) {
        for (Node journey : journeys(leftUser)) {
            addJourney(rightUser, journey);
        }
        Map<String, Set> traits = traits(leftUser);
        for (String name : traits.keySet()) {
            addTrait(rightUser, name, traits.get(name));
        }
        Iterable<Relationship> relationships = leftUser.getRelationships();
        for (Relationship relationship : relationships) {
            relationship.delete();
        }
        leftUser.delete();
    }

    public Node identify(String uid, String anonymousId) {
        Node anonymous = findByAnonymousId(anonymousId);
        if (uid == null) {
            if (anonymous == null) {
                anonymous = createByAnonymousId(anonymousId);
            }
            return anonymous;
        }

        Node identified = findByIdentifier(uid);
        if (identified == null) {
            identified = createByIdentifier(uid);
        }

        if (anonymous != null) {
            merge(anonymous, identified);
        }

        return identified;
    }

    public void addTrait(Node user, String key, Object value) {
        app.userTraits().setProperty(user, key, value);
    }

    public Set<Object> getTraitValue(Node user, String traitName) {
        return app.userTraits().getProperty(user, traitName);
    }

    public Map<String, Set> traits(Node user) {
        return app.userTraits().properties(user);
    }

    @Override
    public Map<String, Object> toHash(Node user) {
        HashMap<String, Object> hash = new HashMap<>();
        hash.put("uid", getIdentifier(user));
        hash.put("anonymous_id", getAnonymousId(user));
        hash.put("traits", traits(user));
        return hash;
    }

    private String getAnonymousId(Node user) {
        return (String) propertyValueOrNull(user, PROP_ANONYMOUS_ID);
    }


    public Node firstEvent(Node user) {
        Node journey = this.journeysChainHelper.first(user);
        return app.journeys().firstEvent(journey);
    }
}
