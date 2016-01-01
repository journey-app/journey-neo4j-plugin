package com.thoughtworks.studios.journey.jql.values;

public interface JQLValue extends Iterable<JQLValue> {
    Object serializable();
}
