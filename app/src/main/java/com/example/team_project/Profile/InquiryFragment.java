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
    private MaterialButton btnInquire;
    private MaterialButton btnCheckInquiry;
    private EditText etInquiryTitle;
    private EditText etInquiryContent;
    private MaterialButton btnSubmitInquiry;
    private boolean isInquiryMode = true; // 초기 상태는 문의하기 모드

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile_customerservice_inquiry, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("문의");

        db = FirebaseFirestore.getInstance();
        btnInquire = rootView.findViewById(R.id.btn_inquire);
        btnCheckInquiry = rootView.findViewById(R.id.btn_check_inquiry);
        etInquiryTitle = rootView.findViewById(R.id.et_inquiry_title);
        etInquiryContent = rootView.findViewById(R.id.et_inquiry_content);
        btnSubmitInquiry = rootView.findViewById(R.id.btn_submit_inquiry);

        // 초기 상태에서는 문의하기 UI가 보이도록 설정
        switchToInquiryMode();

        btnInquire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isInquiryMode) {
                    isInquiryMode = true;
                    switchToInquiryMode();
                }
            }
        });

        btnCheckInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInquiryMode) {
                    isInquiryMode = false;
                    switchToInquiryHistoryMode();
                }
            }
        });

        btnSubmitInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInquiryMode) {
                    showConfirmationDialog();
                }
            }
        });

        return rootView;
    }

    private void switchToInquiryMode() {
        etInquiryTitle.setVisibility(View.VISIBLE);
        etInquiryContent.setVisibility(View.VISIBLE);
        btnSubmitInquiry.setVisibility(View.VISIBLE);

        // 문의 내역 확인 UI 요소 숨기기
        btnCheckInquiry.setVisibility(View.VISIBLE);

    }

    private void switchToInquiryHistoryMode() {
        etInquiryTitle.setVisibility(View.GONE);
        etInquiryContent.setVisibility(View.GONE);
        btnSubmitInquiry.setVisibility(View.GONE);

        // 문의하기 UI 요소 숨기기
        btnCheckInquiry.setVisibility(View.VISIBLE);

    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("문의 접수");
        builder.setMessage("문의를 접수하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = etInquiryTitle.getText().toString().trim();
                String content = etInquiryContent.getText().toString().trim();

                if (!title.isEmpty() && !content.isEmpty()) {
                    addInquiryToFirestore(title, content);
                } else {
                    Toast.makeText(requireContext(), "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void addInquiryToFirestore(String title, String content) {
        Map<String, Object> inquiry = new HashMap<>();
        inquiry.put("title", title);
        inquiry.put("content", content);

        db.collection("inquiries")
                .add(inquiry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "문의가 성공적으로 접수되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "문의 접수 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}