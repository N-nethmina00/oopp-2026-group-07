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
    private String documentPath;

    // Existing constructor retained
    public MedicalRecord(int medicalId,
                         String regNo,
                         int courseId,
                         String courseCode,
                         LocalDate medicalDate,
                         String reason,
                         String status) {

        this(
                medicalId,
                regNo,
                courseId,
                courseCode,
                medicalDate,
                reason,
                status,
                null
        );
    }

    // Constructor including the uploaded document path
    public MedicalRecord(int medicalId,
                         String regNo,
                         int courseId,
                         String courseCode,
                         LocalDate medicalDate,
                         String reason,
                         String status,
                         String documentPath) {

        this.medicalId = medicalId;
        this.regNo = regNo;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.medicalDate = medicalDate;
        this.reason = reason;
        this.status = status;
        this.documentPath = documentPath;
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

    public String getDocumentPath() {
        return documentPath;
    }
}