package com.thoughtworks.studios.journey.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.studios.journey.cspmining.Pattern;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepeatedAction {

    private String label;
    private long involvedJourneyCount;
    private List<Pattern> patterns = new ArrayList<>();

    public RepeatedAction(String label, long involvedJourneyCount) {
        this.label = label;
        this.involvedJourneyCount = involvedJourneyCount;
    }

    public void addPattern(Pattern pattern) {
        patterns.add(pattern);
    }

    public List<Pattern> getPatterns() {
        return Collections.unmodifiableList(this.patterns);
    }

    @JsonProperty("average_repeats")
    public float averageRepeats() {
        int totalRepeats = 0;
        for (Pattern pattern : patterns) {
            totalRepeats += pattern.numberOfLeadActionRepeats() * pattern.getSupport();
        }
        return (float) totalRepeats / involvedJourneyCount;
    }

    public String getLabel() {
        return label;
    }

    @JsonProperty("total_repeated_journeys")
    public int totalRepeatedJourneys() {
        LongSet result = new LongOpenHashSet();
        for (Pattern pattern : patterns) {
            result.addAll(pattern.getJourneys());
        }

        return result.size();
    }

    @JsonProperty("involved_journey_count")
    public long involvedJourneyCount() {
        return involvedJourneyCount;
    }
}
