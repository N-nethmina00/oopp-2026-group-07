
package com.fot.ams.model;


import java.util.ArrayList;
import java.util.List;

public class Student extends User {

    private String regNo;
    private String batchId;
    private int enrolledCourses;
    private Double gpa;
    private String profileImage;

    public Student() {
        super();
    }

    public Student(String userId, String firstName,
                   String lastName) {

        super();

        setUserId(userId);
        setFirstName(firstName);
        setLastName(lastName);
        setRole("Undergraduate");
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }


    public int getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(int enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    private List<CourseResult> semesterResults =
            new ArrayList<>();

    public List<CourseResult> getSemesterResults() {
        return semesterResults;
    }

    public void setSemesterResults(List<CourseResult> results) {
        this.semesterResults = results;
    }

    public static class CourseResult {

        private final String courseCode;
        private final String courseName;
        private final int credits;
        private final String grade;
        private final Double gradePoint;

        public CourseResult(String courseCode,
                            String courseName,
                            int credits,
                            String grade,
                            Double gradePoint) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
            this.grade = grade;
            this.gradePoint = gradePoint;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getCourseName() {
            return courseName;
        }

        public int getCredits() {
            return credits;
        }

        public String getGrade() {
            return grade;
        }

        public Double getGradePoint() {
            return gradePoint;
        }
    }

}
