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