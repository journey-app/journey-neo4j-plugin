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
package com.thoughtworks.studios.journey.jql;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static org.junit.Assert.assertEquals;

public class SelectStatementParserTest {
    @Test
    public void testParseFunctions() {
        SelectStatementParser parser = createParser("user |> distinct");
        final List<String> collectors = new ArrayList<>();
        final List<String> fns = new ArrayList<>();
        parser.addParseListener(new SelectStatementBaseListener(){
            @Override
            public void exitCollector(SelectStatementParser.CollectorContext ctx) {
                collectors.add(ctx.getText());
            }

            @Override
            public void exitTransformFN(SelectStatementParser.TransformFNContext ctx) {
                fns.add(ctx.getText());
            }
        });

        parser.statement();

        assertEquals(list("user"), collectors);
        assertEquals(list("distinct"), fns);
    }
    @Test
    public void testParseSimpleStatement() {
        SelectStatementParser parser = createParser("user");
        final int[] counts = {0, 0, 0, 0}; // collectors, branches, tuple, transforms
        parser.addParseListener(new SelectStatementBaseListener() {
            @Override
            public void enterCollector(SelectStatementParser.CollectorContext ctx) {
                counts[0] ++;
            }

            @Override
            public void enterBranch(SelectStatementParser.BranchContext ctx) {
                counts[1] ++;
            }


            @Override
            public void enterTuple(SelectStatementParser.TupleContext ctx) {
                counts[2] ++;
            }

            @Override
            public void enterTransform(SelectStatementParser.TransformContext ctx) {
                counts[3] ++;
            }
        });
        parser.statement();

        assertEquals(1, counts[0]);
        assertEquals(1, counts[1]);
        assertEquals(1, counts[2]);
        assertEquals(0, counts[3]);

    }


    @Test
    public void testParseComplexStatement() {
        SelectStatementParser parser = createParser("(user, event.timestamp |> time_floor:day) |> distinct |> take:1 |> group_count");
        final int[] counts = {0, 0, 0}; // collectors, branches, transforms
        parser.addParseListener(new SelectStatementBaseListener() {
            @Override
            public void enterBranch(SelectStatementParser.BranchContext ctx) {
                counts[1]++;
            }

            @Override
            public void enterCollector(SelectStatementParser.CollectorContext ctx) {
                counts[0]++;
            }

            @Override
            public void enterTransformFN(SelectStatementParser.TransformFNContext ctx) {
                counts[2]++;
            }

        });
        parser.statement();
        assertEquals(2, counts[0]);
        assertEquals(2, counts[1]);
        assertEquals(4, counts[2]);
    }

    private SelectStatementParser createParser(String str) {
        ANTLRInputStream input = new ANTLRInputStream(str);
        SelectStatementLexer lexer = new SelectStatementLexer(input);
        return new SelectStatementParser(new CommonTokenStream(lexer));
    }
}
