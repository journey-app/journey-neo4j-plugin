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
package com.thoughtworks.studios.journey.jql;

import com.thoughtworks.studios.journey.jql.conditions.JourneyStartsBeforeCondition;
import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.Journeys;
import com.thoughtworks.studios.journey.utils.MatchNothingLuceneQuery;
import org.apache.lucene.search.*;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.LimitingIterable;
import org.neo4j.index.lucene.QueryContext;

import java.util.*;

import static org.neo4j.helpers.collection.Iterables.skip;

public class JourneyQuery {
    private Application app;
    private boolean descOrder;
    private List<QueryCondition> conditions;
    private int limit;
    private int offset;

    public static class Builder {
        private Application app;
        private boolean descOrder;
        private List<QueryCondition> conditions = new ArrayList<>();
        private int limit = -1;
        private int offset = 0;

        public static Builder query(Application app) {
            return new Builder(app);
        }

        private Builder(Application app) {
            this.app = app;
        }

        public Builder conditions(List<Map> conditions) {
            for (Map condition : conditions) {
                QueryCondition parsedCondition = QueryCondition.parseCondition(
                        (String) condition.get("subject"),
                        (String) condition.get("verb"),
                        condition.get("object"));
                condition(parsedCondition);
            }

            return this;
        }

        public Builder condition(QueryCondition condition) {
            this.conditions.add(condition);
            return this;
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
    }

    private JourneyQuery(Application app, List<QueryCondition> conditions, boolean descOrder, int limit, int offset) {
        this.app = app;
        this.descOrder = descOrder;
        this.conditions = conditions;
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

    public int uniqueCount() {
        return uniqueJourneys().size();
    }


    private Iterable<Node> filter(Iterable<Node> journeys) {
        for (QueryCondition condition : conditions) {
            journeys = condition.filter(app, journeys);
        }
        return journeys;
    }

    private Iterable<Node> query() {
        BooleanQuery luceneQuery = new BooleanQuery();
        luceneQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        for (QueryCondition condition : conditions) {
            Query query = condition.luceneQuery(app);
            if (query instanceof MatchNothingLuceneQuery) {
                return Iterables.empty();
            }
            if (query != null) {
                luceneQuery.add(query, BooleanClause.Occur.MUST);
            }
        }
        luceneQuery.add(new JourneyStartsBeforeCondition(System.currentTimeMillis()).luceneQuery(app), BooleanClause.Occur.MUST);

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
