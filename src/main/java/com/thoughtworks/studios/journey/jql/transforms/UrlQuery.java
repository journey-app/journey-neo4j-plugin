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
package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Values;
import com.thoughtworks.studios.journey.jql.values.JQLValue;
import com.thoughtworks.studios.journey.jql.values.NullValue;
import com.thoughtworks.studios.journey.jql.values.SingleValue;
import com.thoughtworks.studios.journey.utils.URIUtils;

public class UrlQuery implements ValueTransformFn {

    @Override
    public JQLValue apply(JQLValue value, String... params) {
        String field = params[0];

        if (value == NullValue.instance) {
            return value;
        }

        if (value instanceof SingleValue) {
            String queryValue = URIUtils.queryValue((String) ((SingleValue) value).getWrapped(), field);
            return Values.wrapSingle(queryValue);
        }

        throw new RuntimeException("Can not apply function url_query to " + value.toString());
    }
}
