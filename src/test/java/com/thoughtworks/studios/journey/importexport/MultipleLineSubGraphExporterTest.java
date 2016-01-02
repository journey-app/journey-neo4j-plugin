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
package com.thoughtworks.studios.journey.importexport;

import com.thoughtworks.studios.journey.ModelTestCase;
import org.junit.Test;
import org.neo4j.cypher.export.CypherResultSubGraph;
import org.neo4j.cypher.export.SubGraph;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class MultipleLineSubGraphExporterTest extends ModelTestCase {

    @Test
    public void testExportProduceCypherInMultipleStatementFormat() throws Exception {
        Node n1 = db.createNode(DynamicLabel.label("TestNode"));
        Node n2 = db.createNode(DynamicLabel.label("TestNode"));
        n1.setProperty("name", "n1");
        n2.setProperty("name", "n2");
        n1.createRelationshipTo(n2, DynamicRelationshipType.withName("connectTo"));

        SubGraph graph = CypherResultSubGraph.from(db.execute("MATCH n RETURN n;"), db, false);
        MultipleLineSubGraphExporter exporter = new MultipleLineSubGraphExporter(graph);
        StringWriter dump = new StringWriter();
        exporter.export(new PrintWriter(dump));

        assertEquals("create (_0:`TestNode` {`name`:\"n1\"}) return id(_0) as _0;//out\n" +
                "create (_1:`TestNode` {`name`:\"n2\"}) return id(_1) as _1;//out\n" +
                "match _0, _1 where id(_0)={_0} and id(_1)={_1} create _0-[:`connectTo`]->_1;//in:_0,_1\n", dump.toString());
    }
}