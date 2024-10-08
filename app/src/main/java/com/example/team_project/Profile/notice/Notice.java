package com.example.team_project.Profile.notice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Notice {
    private String title;
    private String content;
    private Date createdAt;
    private String imageUrl;

    public Notice(String title, String content, Date createdAt, String imageUrl) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 a hh시 mm분 ss초", Locale.KOREA);
        return sdf.format(createdAt);
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
