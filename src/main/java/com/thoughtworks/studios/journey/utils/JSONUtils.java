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
