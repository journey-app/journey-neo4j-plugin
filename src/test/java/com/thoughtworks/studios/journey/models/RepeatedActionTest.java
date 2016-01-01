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
