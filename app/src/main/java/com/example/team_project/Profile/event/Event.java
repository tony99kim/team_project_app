package com.example.team_project.Profile.event;

public class Event {
    private String eventId;
    private String title;
    private String content;
    private String createdAt;
    private String imageUrl;

    public Event(String eventId, String title, String content, String createdAt, String imageUrl) {
        this.eventId = eventId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}