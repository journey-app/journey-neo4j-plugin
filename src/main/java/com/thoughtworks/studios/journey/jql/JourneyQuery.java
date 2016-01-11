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

import com.thoughtworks.studios.journey.jql.conditions.JourneyCondition;
import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.Journeys;
import org.apache.lucene.search.*;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.LimitingIterable;
import org.neo4j.index.lucene.QueryContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.neo4j.helpers.collection.Iterables.skip;

public class JourneyQuery {
    private Application app;
    private boolean descOrder;
    private List<JourneyCondition> conditions;
    private int limit;
    private int offset;

    public static class Builder {
        private Application app;
        private boolean descOrder;
        private List<JourneyCondition> conditions = new ArrayList<>();
        private int limit = -1;
        private int offset = 0;

        public static Builder query(Application app) {
            return new Builder(app);
        }

        private Builder(Application app) {
            this.app = app;
        }

        public Builder desc() {
            descOrder = true;
            return this;
        }

        public JourneyQuery build() {
            return new JourneyQuery(app, conditions, descOrder, limit, offset);
        }

        public Builder desc(boolean descOrder) {
            this.descOrder = descOrder;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder conditions(List<String> conditions) {
            for (String condition : conditions) {
                this.conditions.add(JourneyCondition.parse(condition));
            }
            return this;
        }
    }

    private JourneyQuery(Application app, List<JourneyCondition> conditions, boolean descOrder, int limit, int offset) {
        this.app = app;
        this.conditions = conditions;
        this.descOrder = descOrder;
        this.limit = limit;
        this.offset = offset;
    }


    public Iterable<Node> journeys() {
        return applyLimitAndOffset(filter(query()));
    }

    public List<Node> uniqueJourneys() {
        Set<Object> users = new HashSet<>();
        List<Node> result = new ArrayList<>();
        Iterable<Node> journeys = journeys();
        for (Node journey : journeys) {
            Object user = app.journeys().user(journey);
            if (!users.contains(user)) {
                users.add(user);
                result.add(journey);
            }
        }
        return result;
    }

    private Iterable<Node> filter(Iterable<Node> journeys) {
        for (final JourneyCondition condition : conditions) {
            if (condition.matchingIndexes()) {
                continue;
            }

            journeys = Iterables.filter(new Predicate<Node>() {
                @Override
                public boolean accept(Node journey) {
                    return condition.evaluate(app, journey);
                }
            }, journeys);
        }
        return journeys;
    }

    private Iterable<Node> query() {
        BooleanQuery luceneQuery = new BooleanQuery();
        luceneQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        for (JourneyCondition condition : conditions) {
            if (condition.matchingIndexes()) {
                Query q = condition.indexQuery(app);
                luceneQuery.add(q, BooleanClause.Occur.MUST);
            }
        }
        Sort sorting = new Sort(new SortField(Journeys.PROP_START_AT, SortField.LONG, descOrder));
        QueryContext queryContext = new QueryContext(luceneQuery).sort(sorting);
        return app.journeys().query(queryContext);
    }

    private Iterable<Node> applyLimitAndOffset(Iterable<Node> journeys) {
        if (this.limit < 0) {
            return skip(this.offset, journeys);
        }
        return new LimitingIterable<>(skip(this.offset, journeys), this.limit);
    }

}
