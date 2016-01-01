package com.thoughtworks.studios.journey.jql;

import com.thoughtworks.studios.journey.models.Models;
import com.thoughtworks.studios.journey.jql.values.*;
import org.neo4j.graphdb.Node;

import java.util.Set;

public class Values {
    public static JQLValue wrapSingle(Object value) {
        if (value == null) {
            return NullValue.instance;
        } else {
            return new SingleValue(value);
        }
    }

    public static <T> JQLValue wrapMulti(Set<T> values) {
        if (values.isEmpty()) {
            return NullValue.instance;
        } else if (values.size() == 1) {
            return wrapSingle(values.iterator().next());
        } else {
            return new MultiValue<T>(values);
        }
    }

    public static JQLValue wrapModel(Node node, Models models) {
        if(node == null) {
            return NullValue.instance;
        }
        return new ModelValue(node, models);
    }

}
