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
import org.apache.commons.lang.NotImplementedException;
import org.neo4j.graphdb.Node;

public class IntValue implements Expression, Value {
    Long val;

    public IntValue(String val) {
        this.val = Long.valueOf(val);
    }

    public IntValue(Long val) {
        this.val = val;
    }

    public IntValue(Integer val) {
        this.val = val.longValue();
    }

    @Override
    public boolean includeField() {
        return false;
    }

    @Override
    public Value eval(Application app, Node journey) {
        return this;
    }

    @Override
    public Value plus(Value right) {
        if (right instanceof IntValue) {
            return new IntValue(val + ((IntValue) right).val);
        }

        if (right instanceof FloatValue) {
            return new FloatValue(val + ((FloatValue) right).val);
        }

        if (right instanceof StringValue) {
            throw new DataQueryError("cannot add string value to an integer value");
        }

        throw new NotImplementedException();
    }

    @Override
    public Value minus(Value right) {
        if (right instanceof IntValue) {
            return new IntValue(val - ((IntValue) right).val);
        }

        if (right instanceof FloatValue) {
            return new FloatValue(val - ((FloatValue) right).val);
        }

        if (right instanceof StringValue) {
            throw new DataQueryError("cannot minus string value from an integer value");
        }

        throw new NotImplementedException();
    }

    @Override
    public Value multiply(Value right) {
        if (right instanceof IntValue) {
            return new IntValue(val * ((IntValue) right).val);
        }

        if (right instanceof FloatValue) {
            return new FloatValue(val * ((FloatValue) right).val);
        }

        if (right instanceof StringValue) {
            throw new DataQueryError("cannot times string value to an integer value");
        }

        throw new NotImplementedException();
    }

    @Override
    public Value divide(Value right) {
        if(right instanceof IntValue) {
            return new IntValue(val / ((IntValue) right).val);
        }

        if(right instanceof FloatValue) {
            return new FloatValue(val / ((FloatValue) right).val);
        }

        if(right instanceof StringValue) {
            throw new DataQueryError("cannot add string value to an integer value");
        }

        throw new NotImplementedException();
    }

    @Override
    public Value reminder(Value right) {
        if(right instanceof IntValue) {
            return new IntValue(val % ((IntValue) right).val);
        }

        if(right instanceof FloatValue) {
            return new FloatValue(val % ((FloatValue) right).val);
        }

        if(right instanceof StringValue) {
            throw new DataQueryError("cannot use string value for reminder");
        }

        throw new NotImplementedException();
    }


    public Integer asInteger() {
        return val.intValue();
    }

    public Long asLong() {
        return val;
    }

    @Override
    public int compareTo(Value o) {
        if (o instanceof StringValue) {
            throw new DataQueryError("Cannot compare int value with string value");
        }

        if (o instanceof FloatValue) {
            return Double.valueOf(val.doubleValue()).compareTo(((FloatValue) o).val);
        }

        if (o instanceof IntValue) {
            return val.compareTo(((IntValue) o).val);
        }

        throw new NotImplementedException();
    }
}
