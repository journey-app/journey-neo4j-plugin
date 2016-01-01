package com.thoughtworks.studios.journey.utils;

import org.apache.lucene.search.Query;

public class MatchNothingLuceneQuery extends Query {
    @Override
    public String toString(String field) {
        return ":matching-nothing";
    }
}
