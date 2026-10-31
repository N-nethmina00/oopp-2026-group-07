
package com.fot.ams.model;

public class Lecturer {

    private String lecturerId;
    private String userId;
    private int deptId;
    private String departmentName;
    private User user;

    public Lecturer() {
    }

    public Lecturer(
            String lecturerId,
            String userId,
            int deptId,
            String departmentName,
            User user) {

        this.lecturerId = lecturerId;
        this.userId = userId;
        this.deptId = deptId;
        this.departmentName = departmentName;
        this.user = user;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
