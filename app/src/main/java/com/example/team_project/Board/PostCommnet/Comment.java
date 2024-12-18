package com.example.team_project.Board.PostCommnet;

import com.google.firebase.Timestamp;
import java.util.UUID;

public class Comment {
    private String name; // 작성자 이름
    private String commentContent; // 댓글 내용
    private Timestamp timestamp; // 댓글 작성 시간
    private String postId; // 댓글이 달린 게시물 ID
    private String commentId; // 댓글 ID
    private String userId; // 댓글 작성자 ID

    // 기본 생성자
    public Comment() {
        // Firebase에서 데이터 변환을 위해 기본 생성자가 필요합니다.
    }

    // 매개변수를 받는 생성자
    public Comment(String name, String commentContent, Timestamp timestamp, String postId, String userId) {
        this.name = name; // 작성자 이름 초기화
        this.commentContent = commentContent; // 댓글 내용 초기화
        this.timestamp = timestamp; // 매개변수로 받은 timestamp로 초기화
        this.postId = postId; // 게시물 ID 초기화
        this.userId = userId; // 댓글 작성자 ID 초기화
        this.commentId = generateCommentId(); // 댓글 ID 생성
    }

    // 매개변수를 받는 생성자 (commentId 포함)
    public Comment(String name, String commentContent, Timestamp timestamp, String postId, String commentId, String userId) {
        this.name = name; // 작성자 이름 초기화
        this.commentContent = commentContent; // 댓글 내용 초기화
        this.timestamp = timestamp; // 매개변수로 받은 timestamp로 초기화
        this.postId = postId; // 게시물 ID 초기화
        this.commentId = commentId != null ? commentId : generateCommentId(); // 댓글 ID 초기화
        this.userId = userId; // 댓글 작성자 ID 초기화
    }

    // Getter 메서드
    public String getName() {
        return name; // 작성자 이름 반환
    }

    public String getCommentContent() {
        return commentContent; // 댓글 내용 반환
    }

    public Timestamp getTimestamp() {
        return timestamp; // 댓글 작성 시간 반환
    }

    public String getPostId() {
        return postId; // 게시물 ID 반환
    }

    public String getCommentId() {
        return commentId; // 댓글 ID 반환
    }

    public String getUserId() {
        return userId; // 댓글 작성자 ID 반환
    }

    // Setter 메서드
    public void setName(String name) {
        this.name = name; // 작성자 이름 설정
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent; // 댓글 내용 설정
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp; // 댓글 작성 시간 설정
    }

    public void setPostId(String postId) {
        this.postId = postId; // 게시물 ID 설정
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId; // 댓글 ID 설정
    }

    public void setUserId(String userId) {
        this.userId = userId; // 댓글 작성자 ID 설정
    }

    // 댓글 ID 생성 메서드
    private String generateCommentId() {
        return UUID.randomUUID().toString();
    }
}