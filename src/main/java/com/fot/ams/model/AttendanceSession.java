package com.fot.ams.model;

import java.sql.Time;

// TG2070 - Session details for attendance dropdown
public class AttendanceSession {

    private int sessionId;
    private Time startTime;
    private Time endTime;

    public AttendanceSession(int sessionId, Time startTime, Time endTime) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getSessionId() {
        return sessionId;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    // TG2070 - Display session ID and times in dropdown
    @Override
    public String toString() {
        return "Session " + sessionId + " | " + startTime + " - " + endTime;
    }
}