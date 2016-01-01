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