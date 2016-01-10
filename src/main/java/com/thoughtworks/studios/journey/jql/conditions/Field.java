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

class Field implements Expression {
    private final String fullName;


    public Field(String fieldName, boolean user) {
        fieldName = StringUtils.unquote(fieldName, "`");
        this.fullName = user ? "user." + fieldName : fieldName;
    }

    public IndexField indexField() {
        return IndexField.get(fullName);
    }

    @Override
    public boolean includeField() {
        return true;
    }

    @Override
    public Value eval(Application app, Node journey) {
        ModelAccessor accessor = ModelAccessor.forField(fullName);
        if (accessor != null) {
            return accessor.get(app, journey);
        }

        throw new DataQueryError("Field " + fullName + " is not valid");
    }

    public boolean matchIndex() {
        return indexField() != null;
    }
}
