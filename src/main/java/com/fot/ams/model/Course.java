package com.fot.ams.model;

public class Course {

    private int courseId;
    private String courseCode;
    private String courseName;
    private int credit;
    private String type;
    private String lecturerId;
    private int deptId;
    private String departmentName;

    public Course(int courseId, String courseCode, String courseName,
                  int credit, String type, String lecturerId,
                  int deptId, String departmentName) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.type = type;
        this.lecturerId = lecturerId;
        this.deptId = deptId;
        this.departmentName = departmentName;
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

    public int getCredit() {
        return credit;
    }

    public String getType() {
        return type;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public int getDeptId() {
        return deptId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}