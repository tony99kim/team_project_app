package com.example.team_project.Environment.Point;

import java.io.Serializable;

public class PointAuthentication implements Serializable {
    private String id;
    private String userId;
    private String title;
    private String status;
    private String description;
    private String timestamp;
    private String authenticationId; // 추가된 필드

    // 기본 생성자 추가
    public PointAuthentication() {
    }

    // 모든 필드를 포함하는 생성자 추가
    public PointAuthentication(String id, String userId, String title, String status, String description, String timestamp, String authenticationId) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.status = status;
        this.description = description;
        this.timestamp = timestamp;
        this.authenticationId = authenticationId; // 추가된 필드 초기화
    }

    // Getter 및 Setter 메서드 추가
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthenticationId() {
        return authenticationId;
    }

    public void setAuthenticationId(String authenticationId) {
        this.authenticationId = authenticationId;
    }
}