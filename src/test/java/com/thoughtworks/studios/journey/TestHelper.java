/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provids out-of-box action path analysis features on top of the graph database.
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
package com.thoughtworks.studios.journey;

import com.thoughtworks.studios.journey.jql.Tuple;
import com.thoughtworks.studios.journey.jql.Values;
import com.thoughtworks.studios.journey.jql.values.JQLValue;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.neo4j.function.Function;
import org.neo4j.helpers.collection.IteratorUtil;

import java.util.*;

import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;
import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.Iterables.toList;

public class TestHelper {

    public static Function<Map, Object> pluck(final String property) {
        return new Function<Map, Object>() {
            @Override
            public Object apply(Map request) {
                return request.get(property);
            }
        };
    }


    public static void assertIterableEquals(Iterable left, Iterable right) {
        assertEquals(toList(left), toList(right));
    }

    public static <T> void assertIteratorEquals(Iterator<T> expected, Iterator<T> actual) {
        assertEquals(IteratorUtil.asCollection(expected), IteratorUtil.asCollection(actual));
    }

    public static Map<String, Object> createRequestAttributes(String sessionId) {
        return createRequestAttributes(sessionId,
                "Profile login",
                0L
        );
    }

    public static Map<String, Object> createRequestAttributes(String sessionId, String action, Long startAt) {
        return createRequestAttributes(sessionId, action, startAt, (String) null);
    }

    public static Map<String, Object> createRequestAttributes(String sessionId, String action, Long startAt, Map properties) {
        return createRequestAttributes(sessionId, action, startAt, null, properties);
    }


    public static Map<String, Object> createRequestAttributes(String sessionId, String action, Long startAt, String user) {
        return createRequestAttributes(sessionId, action, startAt, user, "GET");
    }

    public static Map<String, Object> createRequestAttributes(String sessionId, String action, Long startAt, String user, Map properties) {
        return createRequestAttributes(sessionId, action, startAt, user, "GET", properties);
    }

    public static Map<String, Object> createRequestAttributes(String sessionId, String action, Long startAt, String user, String httpMethod) {
        return createRequestAttributes(sessionId,
                action, startAt,
                "/url/" + action, httpMethod, UUID.randomUUID().toString(),
                user, mapOf()
        );
    }

    public static Map<String, Object> createRequestAttributes(String sessionId, String action, Long startAt, String user, String httpMethod, Map properties) {
        return createRequestAttributes(sessionId,
                action, startAt,
                "/url/" + action, httpMethod, UUID.randomUUID().toString(),
                user, properties
        );
    }


    public static Map<String, Object> createRequestAttributesWithDigest(String sessionId, String digest) {
        Map<String, Object> attributes = createRequestAttributes(sessionId);
        attributes.put("digest", digest);
        return attributes;
    }

    public static Map<String, Object> createRequestAttributes(String sessionId,
                                                              String action, Long startAt,
                                                              String url, String method, String digest, String user, Map properties) {
        Map<String, Object> request = new HashMap<>();
        request.put("action_label", action);
        request.put("session_id", sessionId);
        request.put("url", url);
        request.put("http_method", method);
        request.put("digest", digest);
        request.put("start_at", startAt);
        request.put("user", user);
        request.put("properties", properties);
        return request;
    }

    public static long dateToMillis(int year, int month, int day) {
        return dateToMillis(year, month, day, DateTimeZone.UTC);
    }

    public static long dateToMillis(int year, int month, int day, DateTimeZone timeZone) {
        return new DateTime(year, month, day, 0, 0, timeZone).getMillis();
    }


    public static long dateToMillis(int year, int month, int day, int hour) {
        return new DateTime(year, month, day, hour, 0).getMillis();
    }

    public static long dateToMillis(int year, int month, int day, int hour, int minutes) {
        return new DateTime(year, month, day, hour, minutes).getMillis();
    }

    public static JQLValue v(Object... wrapped) {
        return Values.wrapMulti(new LinkedHashSet<>(Arrays.asList(wrapped)));
    }
    public static Tuple t(JQLValue... vs) {
        return new Tuple(vs);
    }
}
