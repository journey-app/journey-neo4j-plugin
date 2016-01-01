package com.thoughtworks.studios.journey.jql;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.jql.transforms.ColumnTransformFn;
import com.thoughtworks.studios.journey.jql.transforms.ValueTransformFn;
import com.thoughtworks.studios.journey.jql.values.JQLValue;
import org.antlr.v4.runtime.*;
import org.neo4j.graphdb.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Select {
    private List<CollectorBranch> branches;
    private boolean branchesInitFinished;
    private List<ColumnTransformFn> columnTransforms = new ArrayList<>();

    public static Select parse(final Application app, String statement) {
        final Select select = new Select();
        SelectStatementLexer lexer = new SelectStatementLexer(new ANTLRInputStream(statement));
        SelectStatementParser parser = new SelectStatementParser(new CommonTokenStream(lexer));
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new DataQueryError("\"select\" parsing error (" + line + "," + charPositionInLine + "): " + msg);
            }
        });
        parser.addParseListener(new SelectStatementBaseListener() {
            @Override
            public void enterTuple(SelectStatementParser.TupleContext ctx) {
                select.branches = new ArrayList<>();
            }

            @Override
            public void exitTuple(SelectStatementParser.TupleContext ctx) {
                select.branchesInitFinished = true;
            }

            @Override
            public void enterBranch(SelectStatementParser.BranchContext ctx) {
                select.branches.add(new CollectorBranch());
            }

            @Override
            public void exitCollector(SelectStatementParser.CollectorContext ctx) {
                CollectorBranch branch = lastBranch();
                branch.collector = ValueCollector.eval(app, ctx.getText());
            }

            private CollectorBranch lastBranch() {
                return select.branches.get(select.branches.size() - 1);
            }

            @Override
            public void exitTransformFN(SelectStatementParser.TransformFNContext ctx) {
                if (select.branchesInitFinished) {
                    ColumnTransformFn fn = Transforms.evalColumnTransform(ctx.getText());
                    select.columnTransforms.add(fn);
                } else {
                    ValueTransformFn fn = Transforms.evalValueTransform(ctx.getText());
                    lastBranch().transforms.add(fn);
                }
            }
        });
        parser.statement();
        return select;
    }

    private Select() {
    }

    public List<CollectorBranch> getBranches() {
        return Collections.unmodifiableList(branches);
    }

    public List<ColumnTransformFn> getColumnTransforms() {
        return Collections.unmodifiableList(columnTransforms);
    }

    public void fillTuple(Tuple tuple, Node journey, Node event, boolean cross) {
        for (int i = 0; i < branches.size(); i++) {
            CollectorBranch branch = branches.get(i);
            tuple.set(i, branch.collect(journey, event, cross));
        }
    }

    static class CollectorBranch {
        private ValueCollector collector;
        private List<ValueTransformFn> transforms = new ArrayList<>();

        private CollectorBranch() {
        }

        public JQLValue collect(Node journey, Node event, boolean cross) {
            JQLValue value = collector.values(journey, event, cross);
            for (ValueTransformFn transform : transforms) {
                value = transform.apply(value);
            }

            return value;
        }
    }
}
