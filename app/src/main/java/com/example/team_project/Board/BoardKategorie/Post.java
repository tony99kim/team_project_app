package com.example.team_project.Board.BoardKategorie;

import java.util.List;

public class Post {
    private String postId; // 게시물 ID (Firestore 문서 ID)
    private String title; // 게시물 제목
    private String content; // 게시물 내용
    private String name; // 작성자 이름
    private List<String> imageUrls; // 이미지 URL 목록
    private long viewCount; // 조회수

    // 기본 생성자
    public Post() {
    }

    // 매개변수를 받는 생성자
    public Post(String postId, String title, String content, String name, List<String> imageUrls, long viewCount) {
        this.postId = postId; // Firestore 문서 ID로 설정
        this.title = title;
        this.content = content;
        this.name = name;
        this.imageUrls = imageUrls;
        this.viewCount = viewCount; // 조회수 초기화
    }

    // Getter 메서드
    public String getPostId() {
        return postId; // Firestore 문서 ID 반환
    }

    public String getTitle() {
        return title; // 게시물 제목 반환
    }

    public String getContent() {
        return content; // 게시물 내용 반환
    }

    public String getName() {
        return name; // 작성자 이름 반환
    }

    public List<String> getImageUrls() {
        return imageUrls; // 이미지 URL 목록 반환
    }

    public long getViewCount() {
        return viewCount; // 조회수 반환
    }

    // Setter 메서드
    public void setPostId(String postId) {
        this.postId = postId; // Firestore 문서 ID 설정
    }

    public void setTitle(String title) {
        this.title = title; // 게시물 제목 설정
    }

    public void setContent(String content) {
        this.content = content; // 게시물 내용 설정
    }

    public void setName(String name) {
        this.name = name; // 작성자 이름 설정
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls; // 이미지 URL 목록 설정
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount; // 조회수 설정
    }
}
