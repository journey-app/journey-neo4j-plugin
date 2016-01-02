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

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.StoppingCondition;
import com.thoughtworks.studios.journey.jql.transforms.ColumnTransformFn;
import org.neo4j.graphdb.Node;

import java.util.*;

public class DataQuery {
    private final Application app;
    private boolean crossJourney;
    private List<Map> conditions = new ArrayList<>();
    private List<StoppingCondition> stops;
    private Select select;

    public DataQuery(Application app, boolean crossJourney) {
        this.app = app;
        this.crossJourney = crossJourney;
        this.select = Select.parse(app, "event");
        this.stops = Collections.singletonList(StoppingCondition.eval(app, "*"));
    }

    public DataQuery(Application app) {
        this(app, true);
    }

    public DataQueryResult execute() {
        DataQueryResult result = new DataQueryResult(stops.size());

        try {
            JourneyQuery journeyQuery = JourneyQuery.Builder.query(app).
                    conditions(conditions).
                    build();

            Iterable<Node> journeys = crossJourney ? journeyQuery.uniqueJourneys() : journeyQuery.journeys();
            List<Select.CollectorBranch> branches = select.getBranches();
            for (Node journey : journeys) {
                Iterator<Node> iterator = crossJourney ? app.journeys().userRequestsCrossJourneys(journey) : app.journeys().userRequests(journey).iterator();
                Tuple[] row = new Tuple[stops.size()];

                for (int i = 0; i < row.length; i++) {
                    row[i] = new Tuple(branches.size());
                }

                boolean matchedAny = false;
                for (int i = 0; i < stops.size(); i++) {
                    StoppingCondition.StopMatchResult match = stops.get(i).match(iterator);
                    if (match.matched()) {
                        select.fillTuple(row[i], journey, match.last(), crossJourney);
                        iterator = match.iterator();
                        matchedAny = true;
                    } else {
                        for(int j = i; j < stops.size(); j++) {
                           select.fillTuple(row[j], journey, null, crossJourney);
                        }
                        break;
                    }
                }
                if (matchedAny) {
                    result.addRows(row);
                }
            }
            for (ColumnTransformFn transform : select.getColumnTransforms()) {
                result.apply(transform);
            }
            return result;
        } catch (DataQueryError e) {
            return DataQueryResult.error(e);
        }
    }


    public void select(String selectStatement) {
        this.select = Select.parse(app, selectStatement);
    }

    public void stops(List<String> stoppingExpressions) {
        if(stoppingExpressions.isEmpty()) {
            return;
        }

        stops = new ArrayList<>(stoppingExpressions.size());
        for (String stoppingExpression : stoppingExpressions) {
            stops.add(StoppingCondition.eval(app, stoppingExpression));
        }
    }

    public void conditions(List<Map> conditions) {
        this.conditions = conditions;
    }

    public void crossJourney(boolean val) {
        this.crossJourney = val;
    }
}
