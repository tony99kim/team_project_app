package com.example.team_project.Chat.ChatData;

import java.util.Date;

public class Message_ChatData {
    private String chatId;
    private String sender;
    private String content;
    private Date createdAt;

    public Message_ChatData() {
        this.chatId = "";
        this.sender = "";
        this.content = "";
        this.createdAt = new Date();
    }
    public Message_ChatData(String chatId, String sender, String content, Date createdAt) {
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
