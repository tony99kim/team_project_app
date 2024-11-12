package com.example.team_project.Environment.Store;

public class Product {
    public String productId, userId, title, price, description;
    public boolean isBusiness; // 기업 여부를 나타내는 필드 추가

    // 기본 생성자
    public Product() {}

    // 생성자에서 필드 초기화
    public Product(String productId, String userId, String title, String price, String description, boolean isBusiness) {
        this.productId = productId;
        this.userId = userId;
        this.title = title;
        this.price = price;
        this.description = description;
        this.isBusiness = isBusiness; // 기업 여부 필드 초기화
    }

    // Getter 메서드들
    public String getProductId() {
        return productId;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBusiness() {
        return isBusiness; // 기업 여부 반환
    }

    // Setter 메서드들
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBusiness(boolean isBusiness) {
        this.isBusiness = isBusiness; // 기업 여부 설정
    }
}
