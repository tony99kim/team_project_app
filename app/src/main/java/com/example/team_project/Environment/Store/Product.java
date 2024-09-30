package com.example.team_project.Environment.Store;

public class Product {
    public String productId, userId, title, price, description;

    // Firestore 데이터를 위한 빈 생성자
    public Product() {}

    // 모든 필드를 초기화하는 생성자
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

    public String getDescription() {
        return description; // 수정된 부분
    }
}
