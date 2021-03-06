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
import org.neo4j.graphdb.*;

import java.util.Iterator;

import static com.thoughtworks.studios.journey.utils.IterableUtils.toIterable;

public class Application {

    private final Events events;
    private final Journeys journeys;
    private final Actions actions;
    private final Users users;
    private final Models[] models;
    private GraphDatabaseService graphDB;
    private String namespace;
    private CustomProperties customProperties;
    private UserTraits userTraits;

    public Application(GraphDatabaseService graphDB, String ns) {
        this.graphDB = graphDB;
        this.namespace = ns.toUpperCase();
        events = new Events(this);
        journeys = new Journeys(this);
        actions = new Actions(this);
        users = new Users(this);
        customProperties = new CustomProperties(this);
        userTraits = new UserTraits(this);
        models = new Models[]{events, journeys, actions, users, customProperties, userTraits};
    }

    /**
     * Setup related schema for the namespace
     */
    public void setupSchema() {
        for (Models model : models) {
            model.setupSchema();
        }
    }

    public CustomProperties customProperties() {
        return customProperties;
    }

    public UserTraits userTraits() {
        return userTraits;
    }

    public Events events() {
        return events;
    }

    public Journeys journeys() {
        return journeys;
    }

    public Users users() {
        return users;
    }

    public Actions actions() {
        return actions;
    }

    public GraphDatabaseService graphDB() {
        return graphDB;
    }

    public Label nameSpacedLabel(String label) {
        return DynamicLabel.label(nameSpaceName(label));
    }

    private String nameSpaceName(String label) {
        return label + "$" + namespace;
    }

    public void tearDownSchema() {
        for (Models model : models) {
            GraphDbUtils.dropAllIndex(graphDB, model.getLabel());
        }
    }

    public void destroyData() {
        for (Models model : models) {
            destroyData(model);
        }
    }

    public void destroyData(Models model) {
        GraphDbUtils.destroyAll(graphDB, model.getLabel());
    }

    public Iterable<Node> getAllNodesWithLabel(String label) {
        return toIterable(graphDB().findNodes(nameSpacedLabel(label)));
    }

    public String nameSpace() {
        return this.namespace;
    }

    public Iterator<Node> allNodes(Models models) {
        return graphDB.findNodes(models.getLabel());
    }

    public Models[] models() {
        return this.models;
    }
}
