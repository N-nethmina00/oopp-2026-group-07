
package com.fot.ams.model;

import java.time.LocalDate;

public class CourseMaterial {

    private int materialId;
    private int courseId;
    private String courseCode;
    private String lecturerId;

    private String materialTitle;
    private String materialType;
    private String fileName;
    private String filePath;

    private LocalDate uploadedDate;

    public CourseMaterial() {
    }

    public CourseMaterial(
            int materialId,
            int courseId,
            String courseCode,
            String lecturerId,
            String materialTitle,
            String materialType,
            String fileName,
            String filePath,
            LocalDate uploadedDate) {

        this.materialId = materialId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.lecturerId = lecturerId;
        this.materialTitle = materialTitle;
        this.materialType = materialType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadedDate = uploadedDate;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getMaterialTitle() {
        return materialTitle;
    }

    public void setMaterialTitle(String materialTitle) {
        this.materialTitle = materialTitle;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDate getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(LocalDate uploadedDate) {
        this.uploadedDate = uploadedDate;
    }
}
