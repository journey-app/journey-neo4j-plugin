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
package com.thoughtworks.studios.journey.utils;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public class BatchTransaction implements AutoCloseable {
    private final GraphDatabaseService db;
    private final int batchSize;
    Transaction tx;
    int batchCount = 0;

    public BatchTransaction(GraphDatabaseService db, int batchSize) {
        this.db = db;
        this.batchSize = batchSize;
        tx = this.db.beginTx();
    }

    public void increment() {
        batchCount++;
        if (batchCount >= batchSize) {
            commit();
        }
    }

    public void commit() {
        tx.success();
        tx.close();
        tx = db.beginTx();
        batchCount = 0;
    }

    @Override
    public void close() {
        if (tx!=null) {
            tx.success();
            tx.close();
        }
    }
}