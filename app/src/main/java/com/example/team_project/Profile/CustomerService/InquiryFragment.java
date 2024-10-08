package com.example.team_project.Profile.CustomerService;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InquiryFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1; // 이미지 선택 요청 코드
    private ArrayList<Uri> imageUris = new ArrayList<>(); // 선택한 이미지 URI 리스트
    private int imageCount = 0; // 선택한 이미지 수

    private FirebaseFirestore db;
    private EditText etInquiryTitle, etInquiryContent;
    private MaterialButton btnSubmitInquiry;
    private ImageView ivCameraIcon; // 이미지 선택 아이콘
    private TextView tvImageCount; // 이미지 수 표시 텍스트
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // 현재 사용자

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_customerservice_inquiry, container, false);

        db = FirebaseFirestore.getInstance();
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("문의하기");
            toolbar.setNavigationOnClickListener(v -> {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                } else {
                    activity.onBackPressed();
                }
            });
        }

        etInquiryTitle = view.findViewById(R.id.et_inquiry_title);
        etInquiryContent = view.findViewById(R.id.et_inquiry_content);
        btnSubmitInquiry = view.findViewById(R.id.btn_submit_inquiry);
        ivCameraIcon = view.findViewById(R.id.ivCameraIcon);
        tvImageCount = view.findViewById(R.id.tvImageCount); // 이미지 수 표시 텍스트

        // 이미지 선택 이벤트
        ivCameraIcon.setOnClickListener(v -> openFileChooser());

        btnSubmitInquiry.setOnClickListener(v -> submitInquiry());

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 다중 선택 허용
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int itemCount = data.getClipData().getItemCount();
                imageCount = Math.min(itemCount, 3); // 최대 3개 이미지 선택 가능
                for (int i = 0; i < imageCount; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(uri); // 선택한 이미지 URI 저장
                }
                tvImageCount.setText(imageCount + "/3"); // 선택한 이미지 수 표시
            } else if (data.getData() != null) {
                Uri imageUri = data.getData(); // 단일 이미지 선택
                imageUris.add(imageUri);
                imageCount = 1;
                tvImageCount.setText(imageCount + "/3");
            }
        }
    }

    private void submitInquiry() {
        String title = etInquiryTitle.getText().toString().trim();
        String content = etInquiryContent.getText().toString().trim();

        // 제목과 내용은 필수로 입력해야 함
        if (!title.isEmpty() && !content.isEmpty()) {
            Map<String, Object> inquiry = new HashMap<>();
            inquiry.put("title", title);
            inquiry.put("content", content);
            inquiry.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())); // 현재 날짜 및 시간 추가
            inquiry.put("status", "답변 대기"); // 상태 추가
            inquiry.put("userId", user.getUid()); // 사용자 ID 추가

            // Firestore에 데이터 추가
            db.collection("inquiries")
                    .add(inquiry)
                    .addOnSuccessListener(documentReference -> {
                        // 이미지가 선택되었을 경우에만 업로드
                        if (!imageUris.isEmpty()) {
                            uploadImages(documentReference.getId());
                        }
                        Toast.makeText(requireContext(), "문의가 성공적으로 접수되었습니다.", Toast.LENGTH_SHORT).show();

                        // CustomerServiceFragment로 이동 (스택을 관리하여 겹치지 않도록)
                        navigateToCustomerServiceFragment();

                        // 입력 필드 초기화
                        etInquiryTitle.setText("");
                        etInquiryContent.setText("");
                        imageUris.clear(); // 이미지 URI 리스트 초기화
                        tvImageCount.setText("0/3"); // 이미지 수 초기화
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "문의 접수 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImages(String inquiryId) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("inquiry_images/" + inquiryId); // Firestore 문서 ID로 이미지 저장
        for (Uri uri : imageUris) {
            UploadTask uploadTask = storageReference.child(uri.getLastPathSegment()).putFile(uri); // URI를 사용해 이미지 업로드
            uploadTask.addOnFailureListener(e -> {
                // 오류 발생 시의 처리
                Toast.makeText(requireContext(), "이미지 업로드 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void navigateToCustomerServiceFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // 백 스택 초기화
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new CustomerServiceFragment()); // CustomerServiceFragment로 교체
        fragmentTransaction.addToBackStack(null); // 백 스택에 추가하여 뒤로가기 가능
        fragmentTransaction.commit();
    }
}