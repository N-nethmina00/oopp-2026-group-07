package com.fot.ams.model;

// TG2070 - One student row in the attendance table
public class StudentAttendance {

    private int enrollmentId;
    private String regNo;
    private String studentName;
    private String status;

    public StudentAttendance(int enrollmentId, String regNo,
                             String studentName, String status) {
        this.enrollmentId = enrollmentId;
        this.regNo = regNo;
        this.studentName = studentName;
        this.status = status;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}