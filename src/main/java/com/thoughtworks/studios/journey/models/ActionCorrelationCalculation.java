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
package com.thoughtworks.studios.journey.models;


import com.thoughtworks.studios.journey.jql.JourneyQuery;
import com.thoughtworks.studios.journey.utils.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.FastMath;
import org.neo4j.graphdb.Node;

import java.util.*;

import static com.thoughtworks.studios.journey.utils.MapUtils.incrementValue;
import static org.neo4j.helpers.collection.Iterables.toList;

public class ActionCorrelationCalculation {
    private Application app;
    private JourneyQuery trueCriteria;
    private JourneyQuery falseCriteria;

    public ActionCorrelationCalculation(Application app, JourneyQuery trueCriteria, JourneyQuery falseCriteria) {
        this.app = app;
        this.trueCriteria = trueCriteria;
        this.falseCriteria = falseCriteria;
    }

    public String rawDataCSV() {

        Map<String, List<List<Integer>>> data = rawData();

        List<String> actions = toList(data.keySet());
        int count = data.get(actions.get(0)).get(0).size();

        StringBuilder csv = new StringBuilder(count * (actions.size() + 1));

        csv.append(StringUtils.join(actions, ","));
        csv.append(",Success\n");

        for (int i = 0; i < count; i++) {
            int success = -1;
            for (String action : actions) {
                List<List<Integer>> variables = data.get(action);
                List<Integer> counts = variables.get(0);
                List<Integer> successes = variables.get(1);
                success = successes.get(i);
                csv.append(counts.get(i));
                csv.append(",");
            }

            csv.append(success == 0 ? "Unsuccessful" : "Successful");
            csv.append("\n");
        }
        return csv.toString();
    }


    private Map<String, List<List<Integer>>> rawData() {
        Map<String, List<List<Integer>>> results = new HashMap<>();
        Map<String, List<Integer>> actionsToCount = new HashMap<>();
        Map<String, List<Integer>> actionsToSuccess = new HashMap<>();

        Set<String> actions = CollectionUtils.union(collectActions(trueCriteria.journeys()), collectActions(falseCriteria.journeys()));
        for (String action : actions) {
            actionsToCount.put(action, new ArrayList<Integer>());
            actionsToSuccess.put(action, new ArrayList<Integer>());
        }

        aggregatesCountAndSuccess(actions, actionsToCount, actionsToSuccess, trueCriteria, 1);
        aggregatesCountAndSuccess(actions, actionsToCount, actionsToSuccess, falseCriteria, 0);
        for (String action : actions) {
            List<List<Integer>> variables = new ArrayList<>(2);
            variables.add(actionsToCount.get(action));
            variables.add(actionsToSuccess.get(action));
            results.put(action, variables);
        }

        return results;
    }

    public List<CorrelationResult> calculate() {
        Map<String, List<List<Integer>>> data = rawData();
        List<CorrelationResult> results = new ArrayList<>(data.size());

        for (String action : data.keySet()) {
            SpearmansCorrelation correlation = new SpearmansCorrelation();

            List<List<Integer>> variables = data.get(action);
            double[] x = toDoubleArray(variables.get(0));
            double[] y = toDoubleArray(variables.get(1));

            double r = correlation.correlation(x, y);
            TDistribution tDistribution = new TDistribution(x.length - 2);
            double t = FastMath.abs(r * FastMath.sqrt((x.length - 2) / (1 - r * r)));
            double pValue = 2 * tDistribution.cumulativeProbability(-t);


            SummaryStatistics successSt = new SummaryStatistics();
            SummaryStatistics failSt = new SummaryStatistics();
            for (int i = 0; i < x.length; i++) {
                if(y[i] == 1) {
                    successSt.addValue(x[i]);
                } else {
                    failSt.addValue(x[i]);
                }
            }

            results.add(new CorrelationResult(action, r, pValue, successSt, failSt));
        }

        Collections.sort(results, new Comparator<CorrelationResult>() {
            @Override
            public int compare(CorrelationResult r1, CorrelationResult r2) {
                Double abs1 = Math.abs(r2.getCorrelation());
                Double abs2 = Math.abs(r1.getCorrelation());
                return abs1.compareTo(abs2);
            }
        });
        return results;
    }

    private double[] toDoubleArray(List<Integer> list) {
        double[] result = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Integer val = list.get(i);
            result[i] = val;
        }

        return result;
    }

    private Set<String> collectActions(Iterable<Node> journeys) {
        HashSet<String> result = new HashSet<>();
        for (Node journey : journeys) {
            for (Node event : app.journeys().events(journey)) {
                result.add(app.events().getActionLabel(event));
            }
        }
        return result;
    }

    private void aggregatesCountAndSuccess(Set<String> actions,
                                           Map<String, List<Integer>> actionsToCount,
                                           Map<String, List<Integer>> actionsToSuccess,
                                           JourneyQuery criteria, int success) {
        for (Node journey : criteria.journeys()) {
            Map<String, Integer> counts = countActions(journey);

            for (String action : actions) {
                actionsToSuccess.get(action).add(success);

                if (counts.containsKey(action)) {
                    actionsToCount.get(action).add(counts.get(action));
                } else {
                    actionsToCount.get(action).add(0);
                }
            }
        }
    }

    private Map<String, Integer> countActions(Node journey) {
        HashMap<String, Integer> result = new HashMap<>();

        for (Node event : app.journeys().events(journey)) {
            String actionLabel = app.events().getActionLabel(event);
            incrementValue(result, actionLabel, 1);
        }

        return result;
    }

}
