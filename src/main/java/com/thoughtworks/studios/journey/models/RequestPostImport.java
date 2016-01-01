package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.Node;

import java.util.Map;

public class RequestPostImport {
    private Application app;
    private Node request;
    private Map<String, Object> requestAttrs;

    public RequestPostImport(Application app, Node request, Map<String, Object> requestAttrs) {
        this.app = app;
        this.request = request;
        this.requestAttrs = requestAttrs;
    }

    public void process() {
        Node action = findOrCreateAction();
        Node journey = attachingToJourney(action);
        attachingToUsers(journey);
    }

    private void attachingToUsers(Node journey) {
        String anonymousId = getAnonymousId(journey);
        String userIdentifier = getUserIdentifier();

        Node existingUser = journeys().user(journey);
        if (existingUser != null && userIdentifier == null) {
            users().addJourney(existingUser, journey);
            return;
        }

        Node identified = users().identify(userIdentifier, anonymousId);
        users().addJourney(identified, journey);
    }


    private String getUserIdentifier() {
        Object identifier = requestAttrs.get("user");
        return (identifier == null) ? null : (String) identifier;
    }

    private String getAnonymousId(Node journey) {
        Object anonymousId = requestAttrs.get("anonymous_id");
        if (anonymousId == null) {
            return journeys().getSessionId(journey);
        }
        return (String) anonymousId;
    }

    private Node attachingToJourney(Node action) {
        return journeys().addRequest(getSessionId(), getUserIdentifier(), request, action);
    }

    private String getSessionId() {
        return (String) requestAttrs.get("session_id");
    }

    private Node findOrCreateAction() {
        String actionLabel = (String) requestAttrs.get("action_label");
        Node action = actions().findOrCreateByActionLabel(actionLabel);
        actions().addRequest(action, request);
        return action;
    }

    private Actions actions() {
        return app.actions();
    }

    private Journeys journeys() {
        return app.journeys();
    }

    private Users users() {
        return app.users();
    }
}
