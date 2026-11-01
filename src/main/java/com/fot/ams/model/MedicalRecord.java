package com.fot.ams.model;

import java.time.LocalDate;

// TG2070 - Medical record details
public class MedicalRecord {

    private int medicalId;
    private String regNo;
    private int courseId;
    private String courseCode;
    private LocalDate medicalDate;
    private String reason;
    private String status;

    public MedicalRecord(int medicalId,
                         String regNo,
                         int courseId,
                         String courseCode,
                         LocalDate medicalDate,
                         String reason,
                         String status) {

        this.medicalId = medicalId;
        this.regNo = regNo;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.medicalDate = medicalDate;
        this.reason = reason;
        this.status = status;
    }

    public int getMedicalId() {
        return medicalId;
    }

    public String getRegNo() {
        return regNo;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public LocalDate getMedicalDate() {
        return medicalDate;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }
}