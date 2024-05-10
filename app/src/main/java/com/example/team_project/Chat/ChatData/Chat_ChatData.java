package com.example.team_project.Chat.ChatData;


import java.util.Date;

public class Chat_ChatData {
    private String id;
    private String userEmail1;
    private String userEmail2;
    private String lastMessage;
    private Date updatedAt;

    public Chat_ChatData() {
        this.id = "";
        this.userEmail1 = "";
        this.userEmail2 = "";
        this.lastMessage = "";
        this.updatedAt = new Date();
    }

    public Chat_ChatData(String id, String userEmail1, String userEmail2, String lastMessage, Date updatedAt) {
        this.id = id;
        this.userEmail1 = userEmail1;
        this.userEmail2 = userEmail2;
        this.lastMessage = lastMessage;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserEmail1() {
        return userEmail1;
    }

    public void setUserEmail1(String userEmail1) {
        this.userEmail1 = userEmail1;
    }

    public String getUserEmail2() {
        return userEmail2;
    }

    public void setUserEmail2(String userEmail2) {
        this.userEmail2 = userEmail2;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
