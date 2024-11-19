package com.example.team_project.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team_project.Board.PostCommnet.Comment;
import com.example.team_project.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private CommentAdapter2 commentAdapter;
    private List<Comment> commentList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter2(commentList);
        recyclerView.setAdapter(commentAdapter);

        loadComments(); // Firestore에서 데이터 로드

        return view;
    }

    private void loadComments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("comment") // Firestore의 'comment' 컬렉션 사용
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        commentList.clear(); // 기존 데이터 초기화
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Firestore 문서에서 데이터 가져오기
                            String name = document.getString("name");
                            String commentContent = document.getString("commentContent");
                            com.google.firebase.Timestamp timestamp = document.getTimestamp("timestamp");

                            if (name != null && commentContent != null && timestamp != null) {
                                // Comment 객체 생성 및 리스트에 추가
                                Comment comment = new Comment(name, commentContent, timestamp, document.getId());
                                commentList.add(comment);
                            } else {
                                Log.w("NotificationsFragment", "Missing fields in document: " + document.getId());
                            }
                        }

                        // 데이터 로드 후 RecyclerView 업데이트
                        commentAdapter.notifyDataSetChanged();
                        Log.d("NotificationsFragment", "Comments loaded: " + commentList.size());
                    } else {
                        Log.w("NotificationsFragment", "Error getting documents.", task.getException());
                    }
                });
    }
}
