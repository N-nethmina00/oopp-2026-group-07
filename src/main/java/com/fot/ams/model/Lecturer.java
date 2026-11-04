package com.fot.ams.model;

public class Lecturer extends User {

    private int deptId;
    private String departmentName;

    // No-argument constructor
    public Lecturer() {
        super();
        setRole("Lecturer");
    }

    // Parameterized constructor
    public Lecturer(
            String lecturerId,
            String userId,
            int deptId,
            String departmentName,
            String username,
            String password,
            String firstName,
            String lastName,
            String email) {

        // Call parent constructor
        super(
                userId,
                lecturerId,
                username,
                password,
                firstName,
                lastName,
                email,
                "Lecturer"
        );

        this.deptId = deptId;
        this.departmentName = departmentName;
    }

    // Lecturer-specific ID
    public String getLecturerId() {
        return getRoleSpecificId();
    }

    public void setLecturerId(String lecturerId) {
        setRoleSpecificId(lecturerId);
    }

    // Department ID
    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    // Department Name
    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}