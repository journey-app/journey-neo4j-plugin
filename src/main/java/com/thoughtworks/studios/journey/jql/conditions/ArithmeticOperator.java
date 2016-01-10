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

import java.util.HashMap;
import java.util.Map;

enum ArithmeticOperator {
    PLUS {
        @Override
        public Value apply(Value left, Value right) {
            return left.plus(right);
        }
    },
    MINUS {
        @Override
        public Value apply(Value left, Value right) {
            return left.minus(right);
        }
    },

    MULTIPLY {
        @Override
        public Value apply(Value left, Value right) {
            return left.multiply(right);
        }
    },

    DIVIDE {
        @Override
        public Value apply(Value left, Value right) {
            return left.divide(right);
        }
    },

    REMINDER {
        @Override
        public Value apply(Value left, Value right) {
            return left.reminder(right);
        }
    };

    private static Map<String, ArithmeticOperator> registry = new HashMap<>(2);

    static {
        registry.put("+", PLUS);
        registry.put("-", MINUS);
        registry.put("*", MULTIPLY);
        registry.put("/", DIVIDE);
        registry.put("%", REMINDER);
    }

    public static ArithmeticOperator forSymbol(String symbol) {
        return registry.get(symbol);
    }

    public abstract Value apply(Value left, Value right);
}
