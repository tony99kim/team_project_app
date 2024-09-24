package com.example.team_project.Environment.Point;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PointAuthenticationFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1; // 이미지 선택 요청 코드
    private int imageCount = 0; // 선택된 이미지 개수

    private TextView tvAuthenticationTitle; // 인증 제목을 표시할 TextView
    private Button btnUploadPointAuthentication; // 인증 업로드 버튼
    private TextView tvImageCount; // 선택된 이미지 개수 표시 TextView
    private ImageView ivCameraIcon; // 카메라 아이콘

    // Firebase 참조
    private StorageReference storageReference; // Firebase Storage 참조
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // 현재 사용자
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore 데이터베이스
    private Intent imageData = null; // 선택된 이미지 데이터

    private PointItem pointItem; // 외부에서 전달받은 PointItem

    public PointAuthenticationFragment() {
        // Required empty public constructor
    }

    public static PointAuthenticationFragment newInstance(PointItem pointItem) {
        PointAuthenticationFragment fragment = new PointAuthenticationFragment();
        fragment.pointItem = pointItem; // PointItem을 전달받음
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment_point_authentication, container, false);

        // Firebase 초기화
        storageReference = FirebaseStorage.getInstance().getReference("PointAuthenticationImages");

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar_point_authentication);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        // 뷰 초기화
        tvAuthenticationTitle = view.findViewById(R.id.tvAuthenticationTitle); // 인증 제목 TextView
        btnUploadPointAuthentication = view.findViewById(R.id.btnAuthentication); // 인증 업로드 버튼
        tvImageCount = view.findViewById(R.id.tvImageCount); // 이미지 개수 표시 TextView
        ivCameraIcon = view.findViewById(R.id.ivCameraIcon); // 카메라 아이콘

        // 제목 설정
        if (pointItem != null) {
            int index = getPointItemIndex(pointItem); // 포인트 아이템의 인덱스 가져오기
            if (index >= 0 && index < PointItem.getFixedTitles().length) {
                tvAuthenticationTitle.setText(PointItem.getFixedTitles()[index]); // 고정된 제목을 설정
            } else {
                tvAuthenticationTitle.setText("제목을 찾을 수 없습니다."); // 디버깅을 위한 기본 제목
            }
        } else {
            tvAuthenticationTitle.setText("포인트 아이템이 없습니다."); // 디버깅을 위한 기본 제목
        }

        // 이미지 선택 이벤트
        ivCameraIcon.setOnClickListener(v -> openFileChooser());

        // 인증 업로드 이벤트
        btnUploadPointAuthentication.setOnClickListener(v -> {
            if (imageData != null) {
                uploadPointAuthentication(imageData); // imageData가 null이 아닐 때만 업로드 호출
            } else {
                Toast.makeText(getActivity(), "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // 포인트 아이템의 인덱스를 가져오는 메소드
    private int getPointItemIndex(PointItem pointItem) {
        // 고정된 제목 배열과 비교하여 인덱스 반환
        for (int i = 0; i < PointItem.getFixedTitles().length; i++) {
            if (PointItem.getFixedTitles()[i].equals(pointItem.getTitle())) {
                return i;
            }
        }
        return -1; // 일치하는 제목이 없을 경우
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            imageData = data; // 이미지 데이터 저장
            if (data.getClipData() != null) {
                int itemCount = data.getClipData().getItemCount();
                imageCount = Math.min(itemCount, 2); // 최대 2개 이미지 선택 가능
                tvImageCount.setText(imageCount + "/2");
            } else {
                // 단일 이미지 선택
                imageCount = 1;
                tvImageCount.setText(imageCount + "/2");
            }
        }
    }

    private void uploadPointAuthentication(Intent data) {
        String authenticationId = db.collection("pointAuthentications").document().getId(); // 새로운 문서 ID 생성

        if (imageCount > 0 && pointItem != null) {
            String userId = user.getUid(); // 사용자의 고유 ID 가져오기
            String title = pointItem.getTitle(); // 인증 제목 가져오기
            String status = "대기"; // 초기 상태는 "대기"

            // 인증 항목 객체 생성
            PointAuthentication pointAuthentication = new PointAuthentication(authenticationId, userId, title, status);
            db.collection("pointAuthentications").document(authenticationId).set(pointAuthentication)
                    .addOnSuccessListener(aVoid -> {
                        // 이미지 스토리지에 업로드
                        for (int i = 0; i < imageCount; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            StorageReference fileReference = storageReference.child(authenticationId + "/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
                            fileReference.putFile(imageUri).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "인증이 업로드되었습니다.", Toast.LENGTH_SHORT).show();
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "인증 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getActivity(), "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // 파일 확장자 가져오기
    private String getFileExtension(Uri uri) {
        // ContentResolver를 사용하여 파일 확장자를 가져옵니다.
        return "jpg"; // 예시로 "jpg"를 반환합니다.
    }
}
