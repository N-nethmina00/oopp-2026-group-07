
package com.fot.ams.model;

public class EligibilityRecord {

    private final int enrollmentId;
    private final String registrationNo;
    private final String courseCode;

    private final Double attendancePercentage;
    private final Double theoryPercentage;
    private final Double practicalPercentage;

    private final int recordedSessions;
    private final int scheduledSessions;

    private final String caStatus;
    private final Double caMarks;
    private final Integer eligibilityFlag;

    private final int approvedMedicalCount;
    private final int pendingMedicalCount;

    public EligibilityRecord(
            int enrollmentId,
            String registrationNo,
            String courseCode,
            Double attendancePercentage,
            Double theoryPercentage,
            Double practicalPercentage,
            int recordedSessions,
            int scheduledSessions,
            String caStatus,
            Double caMarks,
            Integer eligibilityFlag,
            int approvedMedicalCount,
            int pendingMedicalCount) {

        this.enrollmentId = enrollmentId;
        this.registrationNo = registrationNo;
        this.courseCode = courseCode;
        this.attendancePercentage = attendancePercentage;
        this.theoryPercentage = theoryPercentage;
        this.practicalPercentage = practicalPercentage;
        this.recordedSessions = recordedSessions;
        this.scheduledSessions = scheduledSessions;
        this.caStatus = caStatus;
        this.caMarks = caMarks;
        this.eligibilityFlag = eligibilityFlag;
        this.approvedMedicalCount = approvedMedicalCount;
        this.pendingMedicalCount = pendingMedicalCount;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public Double getAttendancePercentage() {
        return attendancePercentage;
    }

    public Double getTheoryPercentage() {
        return theoryPercentage;
    }

    public Double getPracticalPercentage() {
        return practicalPercentage;
    }

    public int getRecordedSessions() {
        return recordedSessions;
    }

    public int getScheduledSessions() {
        return scheduledSessions;
    }

    public String getCaStatus() {
        return caStatus;
    }

    public Double getCaMarks() {
        return caMarks;
    }

    public Integer getEligibilityFlag() {
        return eligibilityFlag;
    }

    public int getApprovedMedicalCount() {
        return approvedMedicalCount;
    }

    public int getPendingMedicalCount() {
        return pendingMedicalCount;
    }
}
