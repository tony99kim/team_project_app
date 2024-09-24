package com.example.team_project.Environment.Point;

public class PointAuthentication {
    private String authenticationId;
    private String userId;
    private String title; // 인증 제목
    private String status; // 상태
    private String description; // 인증 설명

    public PointAuthentication(String authenticationId, String userId, String title, String status, String description) {
        this.authenticationId = authenticationId;
        this.userId = userId;
        this.title = title;
        this.status = status;
        this.description = description;
    }

    // Getter 메소드 추가
    public String getAuthenticationId() {
        return authenticationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
}
