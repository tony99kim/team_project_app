package com.example.team_project.Profile.Notice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoticeFragment extends Fragment {

    private FirebaseFirestore db; // Firestore 인스턴스
    private RecyclerView noticeRecyclerView; // RecyclerView
    private NoticeAdapter noticeAdapter; // Adapter
    private List<Notice> noticeList; // 공지사항 리스트

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_notice, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("공지사항");
        }

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 공지사항 리스트 초기화
        noticeList = new ArrayList<>();
        noticeRecyclerView = view.findViewById(R.id.noticeRecyclerView);
        noticeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noticeAdapter = new NoticeAdapter(noticeList);
        noticeRecyclerView.setAdapter(noticeAdapter);

        // 공지사항 로드
        loadNotices();

        return view;
    }

    private void loadNotices() {
        db.collection("notices") // Firestore의 공지사항 컬렉션 이름
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title"); // 제목 필드 이름
                            String content = document.getString("content"); // 내용 필드 이름
                            Date createdAt = document.getDate("createdAt"); // 생성 날짜 필드 이름
                            String imageUrl = document.getString("imageUrl"); // 이미지 URL 필드 이름

                            // 각 필드가 null이 아닐 때만 추가
                            if (title != null && content != null && createdAt != null) {
                                // 공지사항 추가
                                noticeList.add(new Notice(title, content, createdAt, imageUrl));
                            }
                        }
                        noticeAdapter.notifyDataSetChanged(); // Adapter에 데이터 변경 알림
                    } else {
                        Toast.makeText(getContext(), "공지사항 로딩 실패: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
