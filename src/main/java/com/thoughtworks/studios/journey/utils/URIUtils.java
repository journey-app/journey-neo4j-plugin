/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provids out-of-box action path analysis features on top of the graph database.
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
package com.thoughtworks.studios.journey.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

public class URIUtils {
    public static String topDomain(String uriStr) {
        if (uriStr == null) {
            return null;
        }
        URI uri;
        try {
            uri = URI.create(uriStr);
        } catch (Exception e) {
            return null;
        }
        return uri.getHost();
    }

    public static String queryValue(String uriStr, String paramName) {
        if (uriStr == null || paramName == null) {
            return null;
        }

        URI uri;
        try {
            uri = URI.create(uriStr);
        } catch (Exception e) {
            return null;
        }
        String query = uri.getQuery();

        if (query == null) {
            return null;
        }

        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=");
            try {
                for (int i = 0; i < parts.length - 1; i++) {
                    String key = URLDecoder.decode(parts[i], "UTF-8");
                    if (key.equals(paramName)) {
                        return URLDecoder.decode(parts[parts.length - 1], "UTF-8");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                // skip to next
            }
        }
        return null;
    }
}