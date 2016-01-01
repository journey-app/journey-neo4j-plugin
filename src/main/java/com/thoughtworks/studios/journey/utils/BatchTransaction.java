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