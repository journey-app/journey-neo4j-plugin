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
