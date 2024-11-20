package com.example.team_project.Toolbar;

public class NotificationItem {
    private String sender; // 상대방의 이름
    private String message;

    public NotificationItem(String sender, String message) {
        this.sender = sender; // 사용자 이름
        this.message = message;
    }

    public String getSender() {
        return sender; // 상대방의 이름을 반환
    }

    public String getMessage() {
        return message;
    }
}
