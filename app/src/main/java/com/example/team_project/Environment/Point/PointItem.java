package com.example.team_project.Environment.Point;

public class PointItem {
    private String title;
    private String imageUrl;
    private String status; // "대기", "승인" 등의 상태

    // 고정된 제목 배열
    private static String[] fixedTitles = {
            "분리수거하기",
            "불 끄고다니기",
            "텀블러 사용하기",
            "사용하지 않는 전기제품 코드 뽑기",
            "탄소를 줄이는 활동 자유롭게 실천하기"
    };

    public PointItem(int index, String imageUrl, String status) {
        if (index >= 0 && index < fixedTitles.length) {
            this.title = fixedTitles[index]; // 고정된 제목을 설정
        } else {
            this.title = "제목 없음"; // 기본 제목 설정
        }
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public static String[] getFixedTitles() {
        return fixedTitles;
    }
}
