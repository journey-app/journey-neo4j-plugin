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
package com.thoughtworks.studios.journey.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class CorrelationResult {
    private String action;
    private double correlation;
    private double pValue;
    private SummaryStatistics successSt;
    private SummaryStatistics failSt;

    public CorrelationResult(String action, double correlation, double pValue, SummaryStatistics successSt, SummaryStatistics failSt) {
        this.action = action;
        this.correlation = correlation;
        this.pValue = pValue;
        this.successSt = successSt;
        this.failSt = failSt;
    }

    public String getAction() {
        return action;
    }

    public double getCorrelation() {
        return correlation;
    }

    @JsonProperty("p_value")
    public double getpValue() {
        return pValue;
    }

    @JsonProperty("left_mean")
    public double getSuccessMean() {
        return this.successSt.getMean();
    }

    @JsonProperty("right_mean")
    public double getFailedMean() {
        return this.failSt.getMean();
    }

}
