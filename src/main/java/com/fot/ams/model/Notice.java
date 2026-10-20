
package com.fot.ams.model;

import java.time.LocalDate;

public class Notice {

    private int noticeId;
    private String title;
    private String content;
    private LocalDate publishedDate;
    private LocalDate createdDate;

    public Notice() {
    }

    public Notice(int noticeId, String title, String content,
                  LocalDate publishedDate, LocalDate createdDate) {
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
}
