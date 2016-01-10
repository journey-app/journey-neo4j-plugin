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
package com.thoughtworks.studios.journey.jql.conditions;

import com.thoughtworks.studios.journey.jql.DataQueryError;
import com.thoughtworks.studios.journey.models.Application;
import org.antlr.v4.runtime.*;
import org.apache.lucene.search.Query;
import org.neo4j.graphdb.Node;

public class JourneyCondition {
    private Expression leftExpr;
    private Expression rightExpr;
    private RelationOperator relationOperator;

    public static JourneyCondition parse(String expression) {
        JourneyConditionLexer lexer = new JourneyConditionLexer(new ANTLRInputStream(expression));
        JourneyConditionParser parser = new JourneyConditionParser(new CommonTokenStream(lexer));
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new DataQueryError("condition parsing error (" + line + "," + charPositionInLine + "): " + msg);
            }
        });

        final JourneyCondition condition = new JourneyCondition();
        JourneyConditionParser.ConditionContext context = parser.condition();
        condition.leftExpr = visit(context.expr(0));
        condition.rightExpr = visit(context.expr(1));
        condition.relationOperator = RelationOperator.forSymbol(context.rel_op().getText());
        return condition;
    }

    private static Expression visit(JourneyConditionParser.ExprContext expr) {
        if (expr.field_name() != null) {
            return new Field(expr.field_name().getText(), expr.K_USER() != null);
        }
        if (expr.literal_value() != null) {
            if (expr.literal_value().INT_LITERAL() != null) {
                return new IntValue(expr.literal_value().INT_LITERAL().getText());
            } else if (expr.literal_value().FLOAT_LITERAL() != null) {
                return new FloatValue(expr.literal_value().FLOAT_LITERAL().getText());
            } else {
                return new StringValue(expr.literal_value().STRING_LITERAL().getText());
            }
        }

        if (expr.expr(0) != null) {
            if (expr.arithmetic_op1() != null) {
                return new CombinedExpression(ArithmeticOperator.forSymbol(expr.arithmetic_op1().getText()),
                        visit(expr.expr(0)),
                        visit(expr.expr(1)));
            } else {
                return new CombinedExpression(ArithmeticOperator.forSymbol(expr.arithmetic_op2().getText()),
                        visit(expr.expr(0)),
                        visit(expr.expr(1)));
            }
        }
        return null;
    }

    public boolean matchingIndexes() {
        return (leftExpr instanceof Field)
                && ((Field) leftExpr).matchIndex()
                && !rightExpr.includeField();
    }

    public Query indexQuery(Application app) {
        Field field = (Field) this.leftExpr;
        Value value = this.rightExpr.eval(app, null);
        IndexField indexField = field.indexField();
        return relationOperator.generateIndexQuery(indexField, indexField.convertValue(app, value));
    }

    public boolean evaluate(Application app, Node journey) {
        Value left = leftExpr.eval(app, journey);
        Value right = rightExpr.eval(app, journey);
        return relationOperator.apply(left, right);
    }
}
