package com.example.team_project.Environment.Point;

public class PointAuthentication {

    public String authenticationId, userId, authenticationDescription;

    // Firestore 데이터를 위한 빈 생성자
    public PointAuthentication() {}

    // 모든 필드를 초기화하는 생성자
    public PointAuthentication(String authenticationId, String userId, String authenticationDescription) {
        this.authenticationId = authenticationId;
        this.userId = userId;
        this.authenticationDescription = authenticationDescription;
    }

}
