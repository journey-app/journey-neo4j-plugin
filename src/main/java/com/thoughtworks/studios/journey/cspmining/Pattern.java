package com.thoughtworks.studios.journey.cspmining;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Pattern implements Serializable {
    private List<String> actions;
    private Set<Long> journeys;

    private long totalJourneys;
    private long totalLeadActionInvolvedJourneys;

    public Pattern(List<String> actions, Set<Long> journeys) {
        this.actions = actions;
        this.journeys = journeys;
    }


    public void setTotalJourneys(long totalJourneys) {
        this.totalJourneys = totalJourneys;
    }

    public void setTotalLeadActionInvolvedJourneys(long totalLeadActionInvolvedJourneys) {
        this.totalLeadActionInvolvedJourneys = totalLeadActionInvolvedJourneys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pattern pattern = (Pattern) o;

        if (!actions.equals(pattern.actions)) return false;
        if (!journeys.equals(pattern.journeys)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = actions.hashCode();
        result = 31 * result + journeys.hashCode();
        return result;
    }

    @SuppressWarnings("UnusedDeclaration")
    @JsonProperty("absolute_support_base")
    public long getTotalJourneys() {
        return totalJourneys;
    }

    @SuppressWarnings("UnusedDeclaration")
    @JsonProperty("relative_support_base")
    public long getTotalLeadActionInvolvedJourneys() {
        return totalLeadActionInvolvedJourneys;
    }

    @SuppressWarnings("UnusedDeclaration")
    public long getSupport() {
        return journeys.size();
    }

    public List<String> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public Set<Long> getJourneys() {
        return this.journeys;
    }

    public int numberOfLeadActionRepeats() {
        int result = 0;
        for (String action : actions) {
            if (action.equals(actions.get(0))) {
                result++;
            }
        }

        return result;
    }
}
