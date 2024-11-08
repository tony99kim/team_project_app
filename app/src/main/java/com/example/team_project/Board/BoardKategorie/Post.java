package com.example.team_project.Board.BoardKategorie;

import java.util.List;

public class Post {
    private String postId; // 게시물 ID (Firestore 문서 ID)
    private String title; // 게시물 제목
    private String content; // 게시물 내용
    private String authorId; // 작성자 ID
    private String authorName; // 작성자 이름
    private List<String> imageUrls; // 이미지 URL 목록
    private long viewCount; // 조회수
    private long likes; // 좋아요 수

    // 기본 생성자
    public Post() {
    }

    // 매개변수를 받는 생성자
    public Post(String postId, String title, String content, String authorId, String authorName, List<String> imageUrls, long viewCount, long likes) {
        this.postId = postId; // Firestore 문서 ID로 설정
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName; // 작성자 이름 설정
        this.imageUrls = imageUrls;
        this.viewCount = viewCount; // 조회수 초기화
        this.likes = likes; // 좋아요 수 초기화
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

    public String getAuthorId() {
        return authorId; // 작성자 ID 반환
    }

    public String getAuthorName() {
        return authorName; // 작성자 이름 반환
    }

    public List<String> getImageUrls() {
        return imageUrls; // 이미지 URL 목록 반환
    }

    public long getViewCount() {
        return viewCount; // 조회수 반환
    }

    public long getLikes() {
        return likes; // 좋아요 수 반환
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

    public void setAuthorId(String authorId) {
        this.authorId = authorId; // 작성자 ID 설정
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName; // 작성자 이름 설정
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls; // 이미지 URL 목록 설정
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount; // 조회수 설정
    }

    public void setLikes(long likes) {
        this.likes = likes; // 좋아요 수 설정
    }
}
