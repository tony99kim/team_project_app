package com.example.team_project.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.team_project.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoticeFragment extends Fragment {

    private FirebaseFirestore db; // Firestore 인스턴스
    private TextView noticeTextView; // 공지사항을 표시할 TextView

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

        // 공지사항을 표시할 TextView 초기화
        noticeTextView = view.findViewById(R.id.noticeTextView);

        // 공지사항 로드
        loadNotices();

        return view;
    }

    private void loadNotices() {
        db.collection("notices") // Firestore의 공지사항 컬렉션 이름
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder notices = new StringBuilder();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title"); // 제목 필드 이름
                            String content = document.getString("content"); // 내용 필드 이름
                            Date createdAt = document.getDate("createdAt"); // 생성 날짜 필드 이름

                            // 각 필드가 null이 아닐 때만 추가
                            if (title != null && content != null && createdAt != null) {
                                // 날짜 포맷 설정
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 a hh시 mm분 ss초", Locale.KOREA);
                                String formattedDate = sdf.format(createdAt);

                                // 공지사항 추가
                                notices.append("제목: ").append(title).append("\n")
                                        .append("내용: ").append(content).append("\n")
                                        .append("작성일: ").append(formattedDate).append("\n\n");
                            }
                        }
                        noticeTextView.setText(notices.toString().trim()); // TextView에 공지사항 설정
                    } else {
                        Toast.makeText(getContext(), "공지사항 로딩 실패: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

