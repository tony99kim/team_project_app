package com.example.team_project.Chat.Data;

import java.util.Date;

public class Message {
    private String chatId;
    private String sender;
    private String content;
    private Date createdAt;

    public Message() {
        this.chatId = "";
        this.sender = "";
        this.content = "";
        this.createdAt = new Date();
    }
    public Message(String chatId, String sender, String content, Date createdAt) {
        this.chatId = chatId;
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getChatId() {
        return chatId;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
