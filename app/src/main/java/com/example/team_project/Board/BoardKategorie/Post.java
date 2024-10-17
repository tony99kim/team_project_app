package com.example.team_project.Board.BoardKategorie;

import java.util.List;

public class Post {
    private String postId; // 게시물 ID 추가
    private String title; // 게시물 제목
    private String content; // 게시물 내용
    private String name; // 작성자
    private List<String> imageUrls; // 여러 사진 URL을 저장하기 위한 리스트

    // 기본 생성자
    public Post() {
        // Firestore에서 데이터 변환을 위해 기본 생성자가 필요합니다.
    }

    // 매개변수를 받는 생성자
    public Post(String postId, String title, String content, String name, List<String> imageUrls) {
        this.postId = postId; // 게시물 ID 초기화
        this.title = title; // 게시물 제목 초기화
        this.content = content; // 게시물 내용 초기화
        this.name = name; // 작성자 초기화
        this.imageUrls = imageUrls; // 사진 URL 리스트 초기화
    }

    // Getter 메서드
    public String getPostId() {
        return postId; // 게시물 ID 반환
    }

    public String getTitle() {
        return title; // 게시물 제목 반환
    }

    public String getContent() {
        return content; // 게시물 내용 반환
    }

    public String getName() {
        return name; // 작성자 반환
    }

    public List<String> getImageUrls() {
        return imageUrls; // 사진 URL 리스트 반환
    }

    // Setter 메서드
    public void setPostId(String postId) {
        this.postId = postId; // 게시물 ID 설정
    }

    public void setPostTitle(String postTitle) {
        this.title = postTitle; // 게시물 제목 설정
    }

    public void setPostContent(String postContent) {
        this.content = postContent; // 게시물 내용 설정
    }

    public void setName(String name) {
        this.name = name; // 작성자 설정
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls; // 사진 URL 리스트 설정
    }
}
