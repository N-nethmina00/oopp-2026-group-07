
package com.fot.ams.model;

public class CAEvaluation {

    private int evaluationId;
    private int enrollmentId;

    private Double quiz1Marks;
    private Double quiz2Marks;
    private Double quiz3Marks;
    private Double quizMarks;
    private Double totalQuizMarks;

    private Double assignment1Marks;
    private Double assignment2Marks;
    private Double midExamMarks;

    private Double totalCAMarks;

    private String caStatus;
    private Integer eligibility;

    public CAEvaluation() {
    }

    public CAEvaluation(
            int evaluationId,
            int enrollmentId,
            Double quiz1Marks,
            Double quiz2Marks,
            Double quiz3Marks,
            Double quizMarks,
            Double totalQuizMarks,
            Double assignment1Marks,
            Double assignment2Marks,
            Double midExamMarks,
            Double totalCAMarks,
            String caStatus,
            Integer eligibility) {

        this.evaluationId = evaluationId;
        this.enrollmentId = enrollmentId;
        this.quiz1Marks = quiz1Marks;
        this.quiz2Marks = quiz2Marks;
        this.quiz3Marks = quiz3Marks;
        this.quizMarks = quizMarks;
        this.totalQuizMarks = totalQuizMarks;
        this.assignment1Marks = assignment1Marks;
        this.assignment2Marks = assignment2Marks;
        this.midExamMarks = midExamMarks;
        this.totalCAMarks = totalCAMarks;
        this.caStatus = caStatus;
        this.eligibility = eligibility;
    }

    public int getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(int evaluationId) {
        this.evaluationId = evaluationId;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Double getQuiz1Marks() {
        return quiz1Marks;
    }

    public void setQuiz1Marks(Double quiz1Marks) {
        this.quiz1Marks = quiz1Marks;
    }

    public Double getQuiz2Marks() {
        return quiz2Marks;
    }

    public void setQuiz2Marks(Double quiz2Marks) {
        this.quiz2Marks = quiz2Marks;
    }

    public Double getQuiz3Marks() {
        return quiz3Marks;
    }

    public void setQuiz3Marks(Double quiz3Marks) {
        this.quiz3Marks = quiz3Marks;
    }

    public Double getQuizMarks() {
        return quizMarks;
    }

    public void setQuizMarks(Double quizMarks) {
        this.quizMarks = quizMarks;
    }

    public Double getTotalQuizMarks() {
        return totalQuizMarks;
    }

    public void setTotalQuizMarks(Double totalQuizMarks) {
        this.totalQuizMarks = totalQuizMarks;
    }

    public Double getAssignment1Marks() {
        return assignment1Marks;
    }

    public void setAssignment1Marks(Double assignment1Marks) {
        this.assignment1Marks = assignment1Marks;
    }

    public Double getAssignment2Marks() {
        return assignment2Marks;
    }

    public void setAssignment2Marks(Double assignment2Marks) {
        this.assignment2Marks = assignment2Marks;
    }

    public Double getMidExamMarks() {
        return midExamMarks;
    }

    public void setMidExamMarks(Double midExamMarks) {
        this.midExamMarks = midExamMarks;
    }

    public Double getTotalCAMarks() {
        return totalCAMarks;
    }

    public void setTotalCAMarks(Double totalCAMarks) {
        this.totalCAMarks = totalCAMarks;
    }

    public String getCaStatus() {
        return caStatus;
    }

    public void setCaStatus(String caStatus) {
        this.caStatus = caStatus;
    }

    public Integer getEligibility() {
        return eligibility;
    }

    public void setEligibility(Integer eligibility) {
        this.eligibility = eligibility;
    }
}
