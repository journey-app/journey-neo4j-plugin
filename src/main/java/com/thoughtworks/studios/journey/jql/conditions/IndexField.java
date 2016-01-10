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
import com.thoughtworks.studios.journey.models.Journeys;
import org.neo4j.graphdb.Node;

import java.util.HashMap;
import java.util.Map;

class IndexField {

    private static Map<String, IndexField> indexes = new HashMap<>();

    static {
        indexes.put("length", new IndexField(Journeys.PROP_LENGTH, IndexFieldType.INTEGER));
        indexes.put("user.identifier", new IndexField(Journeys.IDX_PROP_UID, IndexFieldType.STRING));
        indexes.put("start_at", new IndexField(Journeys.PROP_START_AT, IndexFieldType.LONG));
        indexes.put("finish_at", new IndexField(Journeys.PROP_FINISH_AT, IndexFieldType.LONG));
        indexes.put("actions", new IndexField(Journeys.IDX_PROP_ACTION_IDS, IndexFieldType.LONG, new ValueConverter() {
            @Override
            public Value convert(Application app, Value original) {
                if (!(original instanceof StringValue)) {
                    throw new DataQueryError("Error: expected action label in string");
                }
                Node action = app.actions().findByActionLabel(((StringValue) original).asString());
                if (action == null) {
                    throw new DataQueryError("Error: cannot find action named '" + ((StringValue) original).asString() + "'");
                }
                return new IntValue(action.getId());
            }
        }));
    }

    private final String indexFieldName;
    private final IndexFieldType type;
    private final ValueConverter valueConverter;

    public IndexField(String indexFieldName, IndexFieldType type) {
        this(indexFieldName, type, new ValueConverter() {
            @Override
            public Value convert(Application app, Value original) {
                return original;
            }
        });
    }

    public IndexField(String indexFieldName, IndexFieldType type, ValueConverter valueConverter) {
        this.indexFieldName = indexFieldName;
        this.type = type;
        this.valueConverter = valueConverter;
    }


    public String getIndexFieldName() {
        return indexFieldName;
    }

    public IndexFieldType getType() {
        return type;
    }

    public static IndexField get(String fieldName) {
        return indexes.get(fieldName);
    }

    public Value convertValue(Application app, Value value) {
        return valueConverter.convert(app, value);
    }
}
