
package com.fot.ams.model;

public class Student extends User {

    private String regNo;
    private String batchId;

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
}
