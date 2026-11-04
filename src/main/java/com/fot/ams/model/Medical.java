
package com.fot.ams.model;
// This class belongs to the model package.

import java.time.LocalDate;


// This class represents one student medical record.
public class Medical {

    private final int medicalId;
    private final String regNo;
    private final int courseId;
    private final String courseName;
    private final LocalDate medicalDate;
    private final LocalDate endDate;
    private final String reason;
    private final String documentPath;
    private final String status;

    // Constructor to initialize a Medical object.
    public Medical(
            int medicalId,
            String regNo,
            int courseId,
            String courseName,
            LocalDate medicalDate,
            LocalDate endDate,
            String reason,
            String documentPath,
            String status) {

        this.medicalId = medicalId;
        this.regNo = regNo;
        this.courseId = courseId;
        this.courseName = courseName;
        this.medicalDate = medicalDate;
        this.endDate = endDate;
        this.reason = reason;
        this.documentPath = documentPath;
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

    public String getCourseName() {
        return courseName;
    }

    public LocalDate getMedicalDate() {
        return medicalDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getReason() {
        return reason;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public String getStatus() {
        return status;
    }


    public String getDateDisplay() {

        // Check if the medical is for a single day.
        if (endDate == null || endDate.equals(medicalDate)) {

            // Return only the starting date.
            return medicalDate.toString();
        }

        // Return the date range for a multi-day medical.
        return medicalDate + " to " + endDate;
    }
}
