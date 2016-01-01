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
