package com.fot.ams.model;

public class Course {
    private String courseId;
    private String courseName;
    private int credit;
    private String type;
    private String lecturerId;
    private int departmentId;
    private String departmentName;

    public Course() {
    }

    public Course(String courseId, String courseName, int credit,
                  String type, String lecturerId, int departmentId,
                  String departmentName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credit = credit;
        this.type = type;
        this.lecturerId = lecturerId;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}