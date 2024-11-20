package com.example.team_project.Profile.Pay;

import java.util.Date;

public class Transaction {
    private String type;
    private double amount;
    private Date createdAt;
    private String description;
    private String receiver;
    private String sender;
    private String productId;
    private String price;
    private double finalPrice;
    private String productTitle;
    private String senderUsername;
    private String receiverUsername;

    public Transaction() {
        // Firestore requires a public no-argument constructor
    }

    public Transaction(String type, double amount, Date createdAt, String description, String receiver, String sender, String productId, String price, double finalPrice, String productTitle, String senderUsername, String receiverUsername) {
        this.type = type;
        this.amount = amount;
        this.createdAt = createdAt;
        this.description = description;
        this.receiver = receiver;
        this.sender = sender;
        this.productId = productId;
        this.price = price;
        this.finalPrice = finalPrice;
        this.productTitle = productTitle;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
    }

    // Getters and setters for all fields
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }
}