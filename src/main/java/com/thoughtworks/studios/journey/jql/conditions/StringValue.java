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
package com.thoughtworks.studios.journey.jql.conditions;

import com.thoughtworks.studios.journey.jql.DataQueryError;
import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.utils.StringUtils;
import org.neo4j.graphdb.Node;

public class StringValue implements Expression, Value {
    private String val;

    public StringValue(String val) {
        this.val = StringUtils.unquote(val, "'");
    }

    @Override
    public boolean includeField() {
        return false;
    }

    @Override
    public Value eval(Application app, Node journey) {
        return this;
    }

    @Override
    public Value plus(Value right) {
        throw new DataQueryError("'+' operator cannot be applied to string value");
    }

    @Override
    public Value minus(Value right) {
        throw new DataQueryError("'-' operator cannot be applied to string value");
    }

    @Override
    public Value multiply(Value right) {
        throw new DataQueryError("'*' operator cannot be applied to string value");
    }

    @Override
    public Value divide(Value right) {
        throw new DataQueryError("'/' operator cannot be applied to string value");
    }

    @Override
    public Value reminder(Value right) {
        throw new DataQueryError("'%' operator cannot be applied to string value");
    }

    public String asString() {
        return val;
    }

    @Override
    public int compareTo(Value o) {
        throw new DataQueryError("Cannot compare string value");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringValue that = (StringValue) o;

        return val.equals(that.val);

    }

    @Override
    public int hashCode() {
        return val.hashCode();
    }
}
