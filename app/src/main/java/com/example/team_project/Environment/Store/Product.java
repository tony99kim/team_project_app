package com.example.team_project.Environment.Store;

public class Product {
    public String productId, userId, title, price, description;

    public Product() {}

    public Product(String productId, String userId, String title, String price, String description) {
        this.productId = productId;
        this.userId = userId;
        this.title = title;
        this.price = price;
        this.description = description;
    }

    public String getProductId() {
        return productId;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }
}
