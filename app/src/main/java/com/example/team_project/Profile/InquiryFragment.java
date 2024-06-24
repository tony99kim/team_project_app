package com.example.team_project.Profile;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class InquiryFragment extends Fragment {

    private FirebaseFirestore db;
    private View rootView;
    private EditText etInquiryTitle;
    private EditText etInquiryContent;
    private MaterialButton btnSubmitInquiry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile_customerservice_inquiry, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("문의하기");
        }

        // Firebase Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // 버튼과 입력 필드 초기화
        etInquiryTitle = rootView.findViewById(R.id.et_inquiry_title);
        etInquiryContent = rootView.findViewById(R.id.et_inquiry_content);
        btnSubmitInquiry = rootView.findViewById(R.id.btn_submit_inquiry);

        // 문의 접수 버튼 클릭 리스너
        btnSubmitInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });

        return rootView;
    }

    // 경고 메시지를 보여주는 메서드
    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("문의 접수");
        builder.setMessage("문의를 접수하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 예를 선택한 경우, 문의를 Firebase Firestore에 저장
                String title = etInquiryTitle.getText().toString().trim();
                String content = etInquiryContent.getText().toString().trim();

                if (!title.isEmpty() && !content.isEmpty()) {
                    // Firestore에 문의 데이터 추가
                    addInquiryToFirestore(title, content);
                } else {
                    Toast.makeText(requireContext(), "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 아무런 동작 없이 다이얼로그를 닫습니다.
                dialog.dismiss();
            }
        });
        builder.show();
    }

    // Firebase Firestore에 문의 데이터 추가
    private void addInquiryToFirestore(String title, String content) {
        // 콜렉션과 문서 생성
        Map<String, Object> inquiry = new HashMap<>();
        inquiry.put("title", title);
        inquiry.put("content", content);

        // Firestore에 데이터 추가
        db.collection("inquiries")
                .add(inquiry)
                .addOnSuccessListener(documentReference -> {
                    // 성공적으로 추가된 경우
                    Toast.makeText(requireContext(), "문의가 성공적으로 접수되었습니다.", Toast.LENGTH_SHORT).show();
                    // 추가 후 필요한 작업 수행 (예: 입력 필드 초기화 등)
                    etInquiryTitle.setText("");
                    etInquiryContent.setText("");
                })
                .addOnFailureListener(e -> {
                    // 실패한 경우
                    Toast.makeText(requireContext(), "문의 접수 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}