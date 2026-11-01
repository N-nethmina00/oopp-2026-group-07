package com.fot.ams.model;

import java.util.regex.Pattern;

public class Course {

    private static final Pattern COURSE_CODE_PATTERN =
            Pattern.compile("[A-Z]{3}[0-9]{4}");

    public static final String COURSE_CODE_ERROR =
            "Course code must contain exactly 3 uppercase English letters "
                    + "followed by 4 digits. Example: ICT1124. "
                    + "Spaces, lowercase letters and symbols are not allowed.";

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

    public static boolean isValidCourseCode(String courseCode) {
        return courseCode != null
                && COURSE_CODE_PATTERN.matcher(courseCode).matches();
    }

    public static void validateCourseCode(String courseCode) {
        if (!isValidCourseCode(courseCode)) {
            throw new IllegalArgumentException(COURSE_CODE_ERROR);
        }
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
