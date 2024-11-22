package com.example.team_project.Board.PostCommnet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommentListFragment extends Fragment implements CommentAdapter.OnCommentActionListener {
    private RecyclerView commentRecyclerView; // 댓글 RecyclerView
    private CommentAdapter commentAdapter; // 댓글 어댑터
    private List<Comment> comments; // 댓글 리스트
    private FirebaseFirestore db; // Firestore 인스턴스
    private String postId; // 게시물 ID

    // 생성자에서 게시물 ID 초기화
    public CommentListFragment(String postId) {
        this.postId = postId; // 댓글을 불러올 게시물 ID 초기화
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board_post_comment_list, container, false);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();
        comments = new ArrayList<>();

        // RecyclerView 초기화
        commentRecyclerView = view.findViewById(R.id.commentListRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 댓글 목록 로드
        loadComments();

        return view;
    }

    private void loadComments() {
        db.collection("comment")
                .whereEqualTo("postId", postId) // 특정 게시물 ID에 해당하는 댓글 불러오기
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        comments.clear(); // 기존 댓글 목록 초기화
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String commentContent = document.getString("commentContent");
                            // Timestamp 객체로 변경
                            Timestamp timestamp = document.getTimestamp("timestamp");
                            String commentId = document.getId(); // 댓글 ID 가져오기
                            // 댓글 객체 생성 후 리스트에 추가
                            Comment comment = new Comment(name, commentContent, timestamp, postId, commentId);
                            comments.add(comment);
                        }
                        // 어댑터 설정
                        commentAdapter = new CommentAdapter(comments, this);
                        commentRecyclerView.setAdapter(commentAdapter);
                    }
                })
                .addOnFailureListener(e -> {
                    // 오류 처리
                });
    }

    @Override
    public void onEditComment(Comment comment) {
        // 댓글 수정 로직 구현
    }

    @Override
    public void onDeleteComment(Comment comment) {
        // 댓글 삭제 로직 구현
    }
}