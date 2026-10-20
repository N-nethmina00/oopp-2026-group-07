package com.fot.ams.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Notice {

    private int noticeId;
    private String title;
    private String content;
    private LocalDate publishedDate;
    private LocalTime createdDate;

    public Notice() {
    }

    public Notice(int noticeId, String title, String content, LocalDate publishedDate, LocalTime createdDate) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.publishedDate = publishedDate;
        this.createdDate = createdDate;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public LocalTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
