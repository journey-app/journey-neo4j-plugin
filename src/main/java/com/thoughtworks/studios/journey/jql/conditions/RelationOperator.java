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
import org.apache.commons.lang.NotImplementedException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.util.HashMap;
import java.util.Map;

enum RelationOperator {
    MORE_THAN {
        @Override
        public Query generateIndexQuery(IndexField indexField, Value value) {
            switch (indexField.getType()) {
                case INTEGER: {
                    return NumericRangeQuery.newIntRange(indexField.getIndexFieldName(),
                            ((IntValue) value).asInteger(),
                            Integer.MAX_VALUE,
                            false,
                            true);
                }

                case LONG: {
                    return NumericRangeQuery.newLongRange(indexField.getIndexFieldName(),
                            ((IntValue) value).asLong(),
                            Long.MAX_VALUE,
                            false,
                            true);
                }
            }

            throw new DataQueryError("operator is not compatible with index type");
        }

        @Override
        public boolean apply(Value left, Value right) {
            if (left instanceof StringValue || right instanceof StringValue) {
                throw new DataQueryError("operator is not compatible data type");
            }

            return left.compareTo(right) > 0;
        }
    },

    LESS_THAN {
        @Override
        public Query generateIndexQuery(IndexField indexField, Value value) {
            switch (indexField.getType()) {
                case INTEGER: {
                    return NumericRangeQuery.newIntRange(indexField.getIndexFieldName(),
                            Integer.MIN_VALUE,
                            ((IntValue) value).asInteger(),
                            true,
                            false);
                }

                case LONG: {
                    return NumericRangeQuery.newLongRange(indexField.getIndexFieldName(),
                            Long.MIN_VALUE,
                            ((IntValue) value).asLong(),
                            true,
                            false);
                }
            }

            throw new DataQueryError("operator is not compatible with index type");
        }

        @Override
        public boolean apply(Value left, Value right) {
            if (left instanceof StringValue || right instanceof StringValue) {
                throw new DataQueryError("operator is not compatible data type");
            }

            return left.compareTo(right) < 0;
        }
    },

    LESS_OR_EQ_THAN {
        @Override
        public Query generateIndexQuery(IndexField indexField, Value value) {
            switch (indexField.getType()) {
                case INTEGER: {
                    return NumericRangeQuery.newIntRange(indexField.getIndexFieldName(),
                            Integer.MIN_VALUE,
                            ((IntValue) value).asInteger(),
                            true,
                            true);
                }

                case LONG: {
                    return NumericRangeQuery.newLongRange(indexField.getIndexFieldName(),
                            Long.MIN_VALUE,
                            ((IntValue) value).asLong(),
                            true,
                            true);
                }
            }

            throw new DataQueryError("operator is not compatible with index type");
        }

        @Override
        public boolean apply(Value left, Value right) {
            if (left instanceof StringValue || right instanceof StringValue) {
                throw new DataQueryError("operator is not compatible data type");
            }

            return left.compareTo(right) <= 0;
        }
    },

    MORE_OR_EQ_THAN {
        @Override
        public Query generateIndexQuery(IndexField indexField, Value value) {
            switch (indexField.getType()) {
                case INTEGER: {
                    return NumericRangeQuery.newIntRange(indexField.getIndexFieldName(),
                            ((IntValue) value).asInteger(),
                            Integer.MAX_VALUE,
                            true,
                            true);
                }

                case LONG: {
                    return NumericRangeQuery.newLongRange(indexField.getIndexFieldName(),
                            ((IntValue) value).asLong(),
                            Long.MAX_VALUE,
                            true,
                            true);
                }
            }

            throw new DataQueryError("operator is not compatible with index type");
        }

        @Override
        public boolean apply(Value left, Value right) {
            if (left instanceof StringValue || right instanceof StringValue) {
                throw new DataQueryError("operator is not compatible data type");
            }
            return left.compareTo(right) >= 0;
        }
    },

    EQ {
        @Override
        public Query generateIndexQuery(IndexField indexField, Value value) {
            switch (indexField.getType()) {
                case INTEGER: {
                    Integer val = ((IntValue) value).asInteger();
                    return NumericRangeQuery.newIntRange(indexField.getIndexFieldName(),
                            val,
                            val,
                            true,
                            true);
                }

                case LONG: {
                    Long val = ((IntValue) value).asLong();
                    return NumericRangeQuery.newLongRange(indexField.getIndexFieldName(),
                            val,
                            val,
                            true,
                            true);
                }

                case STRING: {
                    return new TermQuery(new Term(indexField.getIndexFieldName(), ((StringValue) value).asString()));
                }
            }

            throw new DataQueryError("operator is not compatible with index type");
        }

        @Override
        public boolean apply(Value left, Value right) {
            if (left instanceof StringValue && right instanceof StringValue) {
                return left.equals(right);
            } else {
                return left.compareTo(right) == 0;
            }
        }
    },

    NOT_EQ {
        @Override
        public Query generateIndexQuery(IndexField indexField, Value value) {
            BooleanQuery bq = new BooleanQuery();
            bq.add(EQ.generateIndexQuery(indexField, value), BooleanClause.Occur.MUST_NOT);
            return bq;
        }

        @Override
        public boolean apply(Value left, Value right) {
            return false;
        }
    },

    MATCH {
        @Override
        public Query generateIndexQuery(IndexField indexField, Value value) {
            switch (indexField.getType()) {
                case STRING: {
                    return new WildcardQuery(new Term(indexField.getIndexFieldName(), ((StringValue) value).asString()));
                }
            }

            throw new DataQueryError("operator is not compatible with index type");

        }

        @Override
        public boolean apply(Value left, Value right) {
            return false;
        }
    },

    NOT_MATCH {
        @Override
        public Query generateIndexQuery(IndexField indexField, Value value) {
            BooleanQuery bq = new BooleanQuery();
            bq.add(MATCH.generateIndexQuery(indexField, value), BooleanClause.Occur.MUST_NOT);
            return bq;
        }

        @Override
        public boolean apply(Value left, Value right) {
            return false;
        }
    },

    INCLUDES {
        @Override
        public Query generateIndexQuery(IndexField indexField, Value value) {
            return EQ.generateIndexQuery(indexField, value);
        }

        @Override
        public boolean apply(Value left, Value right) {
            throw new NotImplementedException("Currently has only supported when hitting an index (such as actions includes 'a0'.");
        }
    };

    private static Map<String, RelationOperator> registry = new HashMap<>(10);

    static {
        registry.put(">", MORE_THAN);
        registry.put("<", LESS_THAN);
        registry.put("<=", LESS_OR_EQ_THAN);
        registry.put(">=", MORE_OR_EQ_THAN);
        registry.put("=", EQ);
        registry.put("==", EQ);
        registry.put("!=", NOT_EQ);
        registry.put("<>", NOT_EQ);
        registry.put("=~", MATCH);
        registry.put("!~", NOT_MATCH);
        registry.put("includes", INCLUDES);
    }

    public static RelationOperator forSymbol(String symbol) {
        return registry.get(symbol);
    }

    public abstract Query generateIndexQuery(IndexField indexField, Value value);

    public abstract boolean apply(Value left, Value right);
}
