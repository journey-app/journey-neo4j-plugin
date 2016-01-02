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
package com.thoughtworks.studios.journey.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JSONUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.registerModule(new JodaModule());
    }

    public static String toJson(Object obj) throws IOException {
        return MAPPER.writeValueAsString(obj);
    }

    public static List<Map> jsonToListMap(String json) throws IOException {
        return MAPPER.readValue(json, TypeFactory.defaultInstance().constructCollectionType(List.class, Map.class));
    }

    public static Map<String, Object> jsonToMap(String json) throws IOException {
        return MAPPER.readValue(json, TypeFactory.defaultInstance().constructMapType(java.util.HashMap.class, String.class, Object.class));
    }

    public static List<String> jsonToListString(String json) throws IOException {
        return MAPPER.readValue(json, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
    }

}
