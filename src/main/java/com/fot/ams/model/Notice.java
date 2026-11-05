package com.fot.ams.model;

import java.time.LocalDate;

public class Notice {

    private int noticeId;
    private String adminId;
    private String title;
    private String content;
    private LocalDate publishedDate;
    private LocalDate createdDate;
    private LocalDate expireDate;

    public Notice() {
    }

    public Notice(
            int noticeId,
            String title,
            String content,
            LocalDate publishedDate,
            LocalDate createdDate
    ) {
        this(
                noticeId,
                title,
                content,
                publishedDate,
                createdDate,
                null
        );
    }

    public Notice(
            int noticeId,
            String title,
            String content,
            LocalDate publishedDate,
            LocalDate createdDate,
            LocalDate expireDate
    ) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.publishedDate = publishedDate;
        this.createdDate = createdDate;
        this.expireDate = expireDate;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }
}