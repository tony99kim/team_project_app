package com.example.team_project;

public class NotificationItem {
    private String title;
    private String time;

    public NotificationItem(String title, String time) {
        this.title = title;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }
}
