package com.fot.ams.model;

import java.time.LocalDate;

public class Attendance {

    // Private fields - Encapsulation
    // final means values cannot be reassigned after initialization.
    private final int sessionId;
    private final LocalDate sessionDate;
    private final String sessionType;
    private final String status;

    public Attendance(int sessionId, LocalDate sessionDate, String sessionType, String status) {

        this.sessionId = sessionId;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.status = status;
        // assign the values given fileds
    }

    // Getter method
    public int getSessionId() {
        return sessionId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getStatus() {
        return status;
    }
}