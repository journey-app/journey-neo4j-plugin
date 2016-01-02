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

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.Models;
import org.apache.commons.lang.StringUtils;
import org.neo4j.cypher.export.CypherResultSubGraph;
import org.neo4j.cypher.export.SubGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DataImportExport {
    private final GraphDatabaseService graphDB;
    private Application app;
    private Reporter reporter;

    public DataImportExport(Application application) {
        this(application, new Reporter() {
            @Override
            public void report() {
                //do nothing
            }
        });
    }

    public DataImportExport(Application app, Reporter reporter) {
        this.app = app;
        this.graphDB = app.graphDB();
        this.reporter = reporter;
    }

    public void export(PrintWriter writer) {
        Result result = graphDB.execute(exportCypher());
        writer.println(app.nameSpace());
        SubGraph subGraph = CypherResultSubGraph.from(result, graphDB, false);
        new MultipleLineSubGraphExporter(subGraph).export(writer);
        writer.flush();
    }

    private String exportCypher() {
        List<String> cyphers = new ArrayList<>(app.models().length);
        for (Models model : app.models()) {
            cyphers.add(modelCypher(model));
        }
        return StringUtils.join(cyphers, " UNION ALL ");
    }

    private String modelCypher(Models model) {
        return "MATCH (n:`" + model.getLabel().name() + "`)-[r]->() RETURN n, r";
    }

    public void importFrom(BufferedReader reader) throws IOException {
        DirectivedCypherExecutor executor = new DirectivedCypherExecutor(graphDB);
        String line;
        String oldNameSpace = reader.readLine();
        String namespaceRegx = "`(\\w+)\\$" + oldNameSpace + "`";
        String namespaceReplacement = "`$1\\$" + app.nameSpace() + "`";

        while ((line = reader.readLine()) != null) {
            executor.execute(line.replaceAll(namespaceRegx, namespaceReplacement));
            reporter.report();
        }
    }

}
