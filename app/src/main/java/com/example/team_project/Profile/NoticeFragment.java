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
                            String notice = document.getString("notice"); // 공지사항 필드 이름
                            if (notice != null) {
                                notices.append(notice).append("\n\n"); // 공지사항 추가
                            }
                        }
                        noticeTextView.setText(notices.toString().trim()); // TextView에 공지사항 설정
                    } else {
                        Toast.makeText(getContext(), "공지사항 로딩 실패: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
