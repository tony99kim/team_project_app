package com.example.team_project.Toolbar;

public class NotificationItem {
    private String title;  // 보낸 사람
    private String content;  // 메시지 내용
    private String documentId; // Firebase 문서 ID (고유 ID)

    public NotificationItem(String title, String content, String documentId) {
        this.title = title;
        this.content = content;
        this.documentId = documentId;
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

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}

