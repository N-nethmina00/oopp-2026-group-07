package com.fot.ams.model;

public class User {

    private String userId;
    private String roleSpecificId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String role;

    public User() {
    }

    public User(
            String userId,
            String roleSpecificId,
            String username,
            String password,
            String firstName,
            String lastName,
            String email,
            String role
    ) {
        this.userId = userId;
        this.roleSpecificId = roleSpecificId;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleSpecificId() {
        return roleSpecificId;
    }

    public void setRoleSpecificId(String roleSpecificId) {
        this.roleSpecificId = roleSpecificId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;

        return (first + " " + last).trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}