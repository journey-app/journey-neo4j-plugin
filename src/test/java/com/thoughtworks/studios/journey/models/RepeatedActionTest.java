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

import com.thoughtworks.studios.journey.ModelTestCase;
import com.thoughtworks.studios.journey.cspmining.Pattern;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.set;
import static org.junit.Assert.assertEquals;

public class RepeatedActionTest extends ModelTestCase {

    @Test
    public void testShouldReturnAverageRepeatsForAction() {
        RepeatedAction action = new RepeatedAction("e", 10);
        action.addPattern(createPattern(list("e", "e", "g", "e"), set(1l, 2l, 3l)));
        action.addPattern(createPattern(list("e", "e", "e", "e"), set(1l, 2l)));
        assertEquals(1.7f, action.averageRepeats(), 0.0001f);
    }

    @Test
    public void supportShouldBeTotalSupportFromEachPatternExcludeDuplicateJourneys() {
        RepeatedAction action = new RepeatedAction("e", 10);
        action.addPattern(createPattern(list("e", "e", "g", "e"), set(1l, 2l, 3l)));
        action.addPattern(createPattern(list("e", "e", "e", "e"), set(3l, 4l)));
        assertEquals(4, action.totalRepeatedJourneys());
    }

    private Pattern createPattern(List<String> actions, Set<Long> journeys) {
        return new Pattern(actions, journeys);
    }
}
