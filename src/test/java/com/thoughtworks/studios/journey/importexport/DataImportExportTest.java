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
package com.thoughtworks.studios.journey.importexport;

import com.thoughtworks.studios.journey.ModelTestCase;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.Iterables;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static org.junit.Assert.assertEquals;

public class DataImportExportTest extends ModelTestCase {

    @Test
    public void testExportEmptyApp() {
        StringWriter writer = new StringWriter();
        new DataImportExport(app).export(new PrintWriter(writer));
        assertEquals(app.nameSpace() + "\n", writer.toString());
    }

    @Test
    public void testExportAndImportJourneys() throws IOException {
        Node j = setupJourney(list("a1", "a2"), 200L, "u1");
        dumpToApp2();
        List<Node> j2s = Iterables.toList(app2.allNodes(app2.journeys()));
        assertEquals(1, j2s.size());
        assertEquals(removeId(journeys.toHash(j)), removeId(app2.journeys().toHash(j2s.get(0))));
    }

    @Test
    public void testNewLineEscaping() throws IOException {
        Node j = setupJourney(list("a1\na11"), 200L, "u1");
        dumpToApp2();
        List<Node> actions = Iterables.toList(app2.actions().all());
        assertEquals(1, actions.size());
        assertEquals("a1\na11", app2.actions().getActionLabel(actions.get(0)));
    }

    private void dumpToApp2() throws IOException {
        StringWriter dump = new StringWriter();
        new DataImportExport(app).export(new PrintWriter(dump));

        String content = dump.toString();
        BufferedReader bufferedReader = new BufferedReader(new StringReader(content));
        new DataImportExport(app2).importFrom(bufferedReader);
    }

    private Map<String, Object> removeId(Map<String, Object> hash) {
        HashMap<String, Object> result = new HashMap<>();
        for (String key : result.keySet()) {
            if (!key.equals("id")) {
                Object value = result.get(key);
                if (value instanceof Map) {
                    value = removeId((Map<String, Object>) value);
                }
                result.put(key, value);
            }
        }
        return result;
    }


}