package com.fot.ams.model;

public class Timetable {

    private int timetableId;
    private String adminId;
    private int courseId;
    private String courseCode;
    private String courseName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String lectureHall;
    private String sessionType;
    private String academicYear;
    private String semester;

    public Timetable(int timetableId, String adminId, int courseId,
                     String courseCode, String courseName, String dayOfWeek,
                     String startTime, String endTime, String lectureHall,
                     String sessionType, String academicYear, String semester) {
        this.timetableId = timetableId;
        this.adminId = adminId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lectureHall = lectureHall;
        this.sessionType = sessionType;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    public int getTimetableId() {
        return timetableId;
    }

    public String getAdminId() {
        return adminId;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getLectureHall() {
        return lectureHall;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public String getSemester()
    { return semester;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}