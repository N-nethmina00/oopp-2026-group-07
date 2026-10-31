
package com.fot.ams.model;

public class Mark {

    private int markId;
    private int enrollmentId;

    private Double endExamMarks;
    private Double totalCAMarks;
    private Double totalMarks;

    private String grade;
    private Double gradePoint;

    public Mark() {
    }

    public Mark(int markId, int enrollmentId,
                Double endExamMarks,
                Double totalCAMarks,
                Double totalMarks,
                String grade,
                Double gradePoint) {

        this.markId = markId;
        this.enrollmentId = enrollmentId;
        this.endExamMarks = endExamMarks;
        this.totalCAMarks = totalCAMarks;
        this.totalMarks = totalMarks;
        this.grade = grade;
        this.gradePoint = gradePoint;
    }

    public int getMarkId() {
        return markId;
    }

    public void setMarkId(int markId) {
        this.markId = markId;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Double getEndExamMarks() {
        return endExamMarks;
    }

    public void setEndExamMarks(Double endExamMarks) {
        this.endExamMarks = endExamMarks;
    }

    public Double getTotalCAMarks() {
        return totalCAMarks;
    }

    public void setTotalCAMarks(Double totalCAMarks) {
        this.totalCAMarks = totalCAMarks;
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
