
package com.fot.ams.model;

public class Grade {

    private int enrollmentId;
    private String registrationNo;
    private String courseCode;

    private Double totalMarks;
    private String grade;
    private Double gradePoint;

    public Grade() {
    }

    public Grade(
            int enrollmentId,
            String registrationNo,
            String courseCode,
            Double totalMarks,
            String grade,
            Double gradePoint) {

        this.enrollmentId = enrollmentId;
        this.registrationNo = registrationNo;
        this.courseCode = courseCode;
        this.totalMarks = totalMarks;
        this.grade = grade;
        this.gradePoint = gradePoint;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Double getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(Double gradePoint) {
        this.gradePoint = gradePoint;
    }
}
