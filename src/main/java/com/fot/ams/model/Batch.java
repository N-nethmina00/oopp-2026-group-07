package com.fot.ams.model;

// TG2070 - Batch details for attendance
public class Batch {

    private int batchId;
    private String batchName;

    public Batch(int batchId, String batchName) {
        this.batchId = batchId;
        this.batchName = batchName;
    }

    public int getBatchId() {
        return batchId;
    }

    public String getBatchName() {
        return batchName;
    }

    @Override
    public String toString() {
        return batchName;
    }
}