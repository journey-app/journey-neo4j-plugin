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
package com.thoughtworks.studios.journey.jql;

import com.thoughtworks.studios.journey.jql.conditions.*;
import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.utils.TimeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Query;
import org.neo4j.graphdb.Node;

public class QueryCondition {

    public static QueryCondition parseCondition(String subject, String verb, Object object) {
        if (subject.equals("User")) {
            if (verb.equals("name is")) {
                return new UserNameIsCondition((String) object);
            }

            if (verb.equals("activated between")) {
                String[] splits = StringUtils.split((String) object, ',');
                long after = Long.valueOf(splits[0]);
                long before = Long.valueOf(splits[1]);
                return userActivatedBetween(after, before);
            }

            if (verb.equals("returned after")) {
                return userReturned(TimeUtils.hoursToMSec((int) object));
            }

            if (verb.equals("not returned after")) {
                return userNotReturned(TimeUtils.hoursToMSec((int) object));
            }

            if (verb.equals("journey start with")) {
                return new JourneyFirstActionIsCondition((String) object);
            }

        }
        if (subject.equals("Journey")) {
            if (verb.equals("starts before")) {
                return new JourneyStartsBeforeCondition(parseLong(object));
            }
            if (verb.equals("starts after")) {
                return new JourneyStartsAfterCondition(parseLong(object));
            }
            if (verb.equals("first action is")) {
                return new JourneyFirstActionIsCondition((String) object);
            }

            if (verb.equals("is first")) {
                return isFirstJourney();
            }

            if (verb.equals("includes action")) {
                return includesActionCondition((String) object);
            }

            if(verb.equals("at least N actions")) {
                return atLeastNActionsCondition((Integer) object);
            }
        }

        throw new RuntimeException("unknown condition: " + subject + "|" + verb);
    }

    private static Long parseLong(Object object) {
        if (object instanceof Long) {
            return (Long) object;
        }

        if (object instanceof Integer) {
            return ((Integer) object).longValue();
        }

        if (object instanceof String) {
            return Long.parseLong((String) object);
        }

        throw new RuntimeException("Unknown type for long parsing");
    }

    public static QueryCondition userNotReturned(long threshold) {
        return new UserNotReturnedCondition(threshold);
    }

    public static QueryCondition userReturned(long threshold) {
        return new UserReturnedCondition(threshold);
    }

    public static QueryCondition isFirstJourney() {
        return new IsFirstJourneyCondition();
    }

    public static QueryCondition userActivatedBetween(long after, long before) {
        return new UserActivatedBetweenCondition(after, before);
    }

    private static QueryCondition atLeastNActionsCondition(int n) {
        return new JourneyAtLeastNActionsCondition(n);
    }

    private static QueryCondition includesActionCondition(String actionLabel) {
        return new JourneyIncludesActionCondition(actionLabel);
    }

    public Query luceneQuery(Application app) {
        return null;
    }

    public Iterable<Node> filter(Application app, Iterable<Node> journeys) {
        return journeys;
    }
}
