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

public class InquiryHistoryFragment extends Fragment {

    private FirebaseFirestore db;
    private TextView inquiryTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inquiry_history, container, false);

        // Firebase Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("문의 내역");
        }

        inquiryTextView = view.findViewById(R.id.inquiryTextView);

        // 문의 내역 로드
        loadInquiries();

        return view;
    }

    private void loadInquiries() {
        db.collection("inquiries")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder inquiries = new StringBuilder();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String content = document.getString("content");
                            inquiries.append("제목: ").append(title).append("\n내용: ").append(content).append("\n\n");
                        }
                        inquiryTextView.setText(inquiries.toString().trim());
                    } else {
                        Toast.makeText(requireContext(), "문의 내역 로딩 실패: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
