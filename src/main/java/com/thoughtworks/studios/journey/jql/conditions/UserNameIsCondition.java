package com.thoughtworks.studios.journey.jql.conditions;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.Journeys;
import com.thoughtworks.studios.journey.jql.QueryCondition;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;

public class UserNameIsCondition extends QueryCondition {

    private String object;

    public UserNameIsCondition(String object) {
        this.object = object;
    }

    @Override
    public Query luceneQuery(Application app) {
        return new WildcardQuery(new Term(Journeys.IDX_PROP_UID, object));
    }
}
