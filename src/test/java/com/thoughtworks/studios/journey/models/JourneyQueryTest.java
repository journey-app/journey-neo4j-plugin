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

import com.thoughtworks.studios.journey.ModelTestCase;
import com.thoughtworks.studios.journey.jql.DataQueryError;
import com.thoughtworks.studios.journey.jql.JourneyQuery;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.util.Arrays;

import static com.thoughtworks.studios.journey.TestHelper.assertIterableEquals;
import static com.thoughtworks.studios.journey.TestHelper.dateToMillis;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.neo4j.helpers.collection.Iterables.iterable;

public class JourneyQueryTest extends ModelTestCase {

    private Node j1;
    private Node j2;
    private Node j3;
    private Node j4;

    @Before
    public void setup() {
        j1 = setupJourney(iterable("a0", "a1", "a1", "a3"), dateToMillis(2015, 1, 1), 100L, "u0", "s0");
        j2 = setupJourney(iterable("a1", "a2"), dateToMillis(2015, 1, 2), 100L, "u1", "s1");
        j3 = setupJourney(iterable("a2", "a3", "a0"), dateToMillis(2015, 1, 3), 100L, null, "s2");
        j4 = setupJourney(iterable("a0", "a1"), dateToMillis(2015, 1, 4), 100L, "u1", "s1");
    }

    @Test
    public void shouldReturnAllWithoutCondition() {
        assertIterableEquals(list(j1, j2, j3, j4), query().journeys());
    }


    @Test
    public void queryDescOrder() {
        assertIterableEquals(list(j4, j3, j2, j1), builder().desc().build().journeys());
    }

    @Test
    public void testQueryJourneyWithUID() {
        assertIterableEquals(list(j2, j4), query("user.identifier = 'u1'").journeys());
    }

    @Test
    public void testNotEqual() {
        assertIterableEquals(list(j1, j3), query("user.identifier <> 'u1'").journeys());
        assertIterableEquals(list(j1, j3), query("user.identifier != 'u1'").journeys());
    }

    @Test
    public void testFirstActionEqual() {
        assertIterableEquals(list(j1, j4), query("first_action = 'a0'").journeys());
        assertIterableEquals(list(j3), query("first_action == 'a2'").journeys());
        assertIterableEquals(list(j2, j3), query("first_action <> 'a0'").journeys());
    }

    @Test
    public void testUserIdMatch() {
        assertIterableEquals(list(j1, j2, j4), query("user.identifier =~ 'u*'").journeys());
        assertIterableEquals(list(j3), query("user.identifier !~ 'u*'").journeys());
    }

    @Test
    public void testFirstActionMatch() {
        assertIterableEquals(list(j1, j2, j3, j4), query("first_action =~ 'a*'").journeys());
        assertIterableEquals(list(j2), query("first_action =~ '*1'").journeys());
        assertIterableEquals(list(), query("first_action !~ 'a*'").journeys());
        assertIterableEquals(list(j1, j3, j4), query("first_action !~ '*1'").journeys());
    }


    @Test
    public void testQueryJourneyWithStartBeforeDate() {
        assertIterableEquals(list(j1, j2), query("start_at < " + dateToMillis(2015, 1, 3)).journeys());
    }

    @Test
    public void testQueryJourneyWithStartAfterDate() {
        assertIterableEquals(list(j2, j3, j4), query("start_at >=" + dateToMillis(2015, 1, 2)).journeys());
    }

    @Test
    public void queryWithLimit() {
        assertIterableEquals(iterable(j1, j2, j3), builder().limit(3).build().journeys());
        assertIterableEquals(list(), builder().limit(0).build().journeys());
    }

    @Test
    public void queryWithUserActivatedBetween() {
        JourneyQuery query = query(
                "user.start_active_at >=" + dateToMillis(2015, 1, 1),
                "user.start_active_at <" + dateToMillis(2015, 1, 2));
        assertIterableEquals(list(j1), query.journeys());
    }

    @Test
    public void queryJourneyWithStartWithAction() {
        actions.ignore(actions.findByActionLabel("a1"));
        actions.ignore(actions.findByActionLabel("a2"));
        assertIterableEquals(list(j1, j4), query("first_action = 'a0'").journeys());
    }

    @Test
    public void queryIgnoredEmptyJourneyWithStartWith() {
        assertIterableEquals(list(j1, j4), query("first_action = 'a0'").journeys());
        assertIterableEquals(list(j2), query("first_action = 'a1'").journeys());
        assertIterableEquals(list(), query("first_action = 'a3'").journeys());
    }

    @Test
    public void queryCanBeLimitedToFirstJourneys() {
        assertIterableEquals(list(j1, j2, j3), query("start_at = user.start_active_at").journeys());
    }

    @Test
    public void queryJourneyForUReturnedJourney() {
        assertIterableEquals(list(j4), query("start_at >= user.start_active_at + 24 * 3600 * 1000").journeys());
        assertIterableEquals(list(), query("start_at >= user.start_active_at + 72 * 3600 * 1000" ).journeys());
    }

    @Test
    public void queryJourneyForUReturnedUser() {
        assertIterableEquals(list(j2, j4), query("user.last_active_at >= user.start_active_at + " + 24 * 3600 * 1000L).journeys());
        assertIterableEquals(list(), query("user.last_active_at >= user.start_active_at + " + 72 * 3600 * 1000L).journeys());
    }


    @Test
    public void queryJourneyForNotReturnedJourney() {
        assertIterableEquals(list(j1, j2, j3), query("start_at <= user.start_active_at + 24 * 60 * 60 * 1000").journeys());
        assertIterableEquals(list(j1, j2, j3, j4), query("start_at <= user.start_active_at + 72 * 60 * 60 * 1000").journeys());
    }

    @Test
    public void queryJourneyIncludeCertainAction() {
        assertIterableEquals(list(j3), query("actions excludes 'a1'").journeys());
        assertIterableEquals(list(j1, j4), query("actions excludes 'a2'").journeys());
        assertIterableEquals(list(j1), query("actions includes 'a3'", "actions excludes 'a2'").journeys());
    }

    @Test
    public void queryJourneyExcludeCertainAction() {
        assertIterableEquals(list(j1, j2, j4), query("actions includes 'a1'").journeys());
        assertIterableEquals(list(j2, j3), query("actions includes 'a2'").journeys());
        assertIterableEquals(list(j2), query("actions includes 'a1'", "actions includes 'a2'").journeys());
    }


    @Test(expected= DataQueryError.class)
    public void shouldRaiseExceptionWhenActionNotFound() {
        assertIterableEquals(list(), query("actions includes 'a4'").journeys());
    }

    @Test
    public void queryJourneyAtLeastActionCount() {
        assertIterableEquals(list(j1, j2, j3, j4), query("length >= 0").journeys());
        assertIterableEquals(list(j1, j2, j3, j4), query("length >= 1").journeys());
        assertIterableEquals(list(j1, j2, j3, j4), query("length >= 2").journeys());
        assertIterableEquals(list(j1, j3), query("length >= 3").journeys());
        assertIterableEquals(list(j1), query("length >= 4").journeys());
    }

    @Test
    public void queryWithReminderOperator() {
        assertIterableEquals(list(j1, j2, j4), query("length % 2 = 0").journeys());
        assertIterableEquals(list(j3), query("length % 2 == 1").journeys());
    }

    @Test
    public void testMatchesWithWildcard() {
        assertIterableEquals(list(j1, j2, j4), query("user.identifier =~ 'u*'").journeys());
    }

    @Test
    public void queryJourneysWithUniqueUser() {
        assertIterableEquals(list(j1, j2), query("actions includes 'a1'").uniqueJourneys());
    }

    @Test
    public void testQuotedFieldName() {
        assertIterableEquals(list(j1, j2), query("`actions` includes 'a1'").uniqueJourneys());
    }

    @Test
    public void evalConditions() {
        assertFalse(query("user.identifier = 'u1'").evalConditions(j1));
        assertTrue(query("user.identifier = 'u1'").evalConditions(j2));
    }

    private JourneyQuery query(String... conditions) {
        return builder().conditions(Arrays.asList(conditions)).build();
    }

    private JourneyQuery.Builder builder() {
        return JourneyQuery.Builder.query(app);
    }
}
