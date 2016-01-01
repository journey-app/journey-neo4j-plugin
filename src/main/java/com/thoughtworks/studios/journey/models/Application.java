package com.thoughtworks.studios.journey.models;


import com.thoughtworks.studios.journey.utils.GraphDbUtils;
import org.neo4j.graphdb.*;

import java.util.Iterator;

import static com.thoughtworks.studios.journey.utils.IterableUtils.toIterable;

public class Application {

    private final Requests requests;
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
        requests = new Requests(this);
        journeys = new Journeys(this);
        actions = new Actions(this);
        users = new Users(this);
        customProperties = new CustomProperties(this);
        userTraits = new UserTraits(this);
        models = new Models[]{requests, journeys, actions, users, customProperties, userTraits};
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

    public Requests requests() {
        return requests;
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
