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
