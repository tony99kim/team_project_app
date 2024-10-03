package com.example.team_project.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.team_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CustomerServiceFragment extends Fragment {

    private FirebaseFirestore db;
    private EditText etInquiryTitle, etInquiryContent;
    private MaterialButton btnSubmitInquiry, btnCheckInquiry;

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
            activity.getSupportActionBar().setTitle("고객 서비스");
        }

        // 뷰 초기화
        etInquiryTitle = view.findViewById(R.id.et_inquiry_title);
        etInquiryContent = view.findViewById(R.id.et_inquiry_content);
        btnSubmitInquiry = view.findViewById(R.id.btn_submit_inquiry);
        btnCheckInquiry = view.findViewById(R.id.btn_check_inquiry);

        // 문의 접수 버튼 클릭 리스너
        btnSubmitInquiry.setOnClickListener(v -> submitInquiry());

        // 문의 내역 확인 버튼 클릭 리스너 추가
        btnCheckInquiry.setOnClickListener(v -> openInquiryHistoryFragment());

        return view;
    }

    // 문의 접수 메서드
    private void submitInquiry() {
        String title = etInquiryTitle.getText().toString().trim();
        String content = etInquiryContent.getText().toString().trim();

        if (!title.isEmpty() && !content.isEmpty()) {
            Map<String, Object> inquiry = new HashMap<>();
            inquiry.put("title", title);
            inquiry.put("content", content);

            db.collection("inquiries")
                    .add(inquiry)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(requireContext(), "문의가 성공적으로 접수되었습니다.", Toast.LENGTH_SHORT).show();
                        etInquiryTitle.setText("");
                        etInquiryContent.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "문의 접수 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // InquiryHistoryFragment로 전환하는 메서드
    private void openInquiryHistoryFragment() {
        InquiryHistoryFragment fragment = new InquiryHistoryFragment();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment) // fragment_container는 실제 프래그먼트를 추가할 컨테이너 ID로 변경하세요.
                    .addToBackStack(null) // 이전 프래그먼트로 돌아갈 수 있게 하기 위함
                    .commit();
        }
    }
}
