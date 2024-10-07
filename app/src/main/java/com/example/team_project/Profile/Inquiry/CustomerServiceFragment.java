package com.example.team_project.Profile.Inquiry;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Data.Inquiry; // Inquiry 모델 추가
import com.example.team_project.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CustomerServiceFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private InquiryAdapter adapter; // InquiryAdapter는 문의 내역을 보여줄 RecyclerView 어댑터입니다.
    private ArrayList<Inquiry> inquiries = new ArrayList<>(); // 문의 리스트

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_customer_service, container, false);

        // Firebase Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("고객 문의 내역");
        }

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InquiryAdapter(inquiries);
        recyclerView.setAdapter(adapter);

        // 문의 내역 불러오기
        loadInquiries();

        return view;
    }

    private void loadInquiries() {
        db.collection("inquiries")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        inquiries.clear(); // 기존 문의 내역 초기화
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Inquiry inquiry = document.toObject(Inquiry.class);
                            inquiries.add(inquiry);
                        }
                        adapter.notifyDataSetChanged(); // 어댑터에 변경 사항 알리기
                    } else {
                        Toast.makeText(getContext(), "문의 내역 로딩 실패: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
