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

import com.thoughtworks.studios.journey.jql.transforms.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Transforms {
    private static Map<String, ColumnTransformFn> columnTransforms = new HashMap<>();
    private static Map<String, ValueTransformFn> valueTransforms = new HashMap<>();

    static {
        columnTransforms.put("count", new Count());
        columnTransforms.put("distinct", new Distinct());
        columnTransforms.put("distinct_by", new DistinctBy());
        columnTransforms.put("flatten", new Flatten());
        columnTransforms.put("compact", new Compact());
        columnTransforms.put("group_count", new GroupCount());
        columnTransforms.put("take", new Take());
        columnTransforms.put("drop", new Drop());
        columnTransforms.put("url_query", new Multiplexer(new UrlQuery()));
        columnTransforms.put("url_domain", new Multiplexer(new UrlDomain()));
        columnTransforms.put("time_floor", new Multiplexer(new TimeFloor()));
        columnTransforms.put("to_date", new Multiplexer(new ToDate()));
    }

    static {
        valueTransforms.put("time_floor", new TimeFloor());
        valueTransforms.put("url_query", new UrlQuery());
        valueTransforms.put("url_domain", new UrlDomain());
        valueTransforms.put("to_date", new ToDate());

    }

    public static ColumnTransformFn evalColumnTransform(String fnExpression) {
        String[] splits = fnExpression.split(":");
        ColumnTransformFn fn = columnTransforms.get(splits[0]);

        if (fn == null) {
            throw new DataQueryError("'" + fnExpression + "' does not match any column transform function");
        }

        if (splits.length < 2) {
            return fn;
        } else {
            return new CurryColumnFn(fn, Arrays.copyOfRange(splits, 1, splits.length));
        }
    }

    public static ValueTransformFn evalValueTransform(String fnExpression) {
        String[] splits = fnExpression.split(":");
        ValueTransformFn fn = valueTransforms.get(splits[0]);

        if (fn == null) {
            throw new DataQueryError("'" + fnExpression + "' does not match any value transform function");
        }

        if (splits.length < 2) {
            return fn;
        } else {
            return new CurryValueFn(fn, Arrays.copyOfRange(splits, 1, splits.length));
        }
    }
}
