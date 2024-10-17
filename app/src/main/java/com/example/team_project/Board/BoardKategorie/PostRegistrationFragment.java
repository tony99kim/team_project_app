package com.example.team_project.Board.BoardKategorie;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.Board.BoardFragment;
import com.example.team_project.Board.BoardPostCompleteFragment;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class PostRegistrationFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private int imageCount = 0;

    private ImageView post_ivCameraIcon;
    private TextView post_tvImageCount;
    private EditText editTextTitle, editTextContent;
    private Button buttonPost;

    // Firebase 참조
    private StorageReference storageReference;
    private FirebaseFirestore db;

    private Intent imageData = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_board_post_registration.xml을 이용하여 뷰를 생성
        View view = inflater.inflate(R.layout.fragment_board_postregistration, container, false);

        // Firebase 초기화
        storageReference = FirebaseStorage.getInstance().getReference("PostImages");
        db = FirebaseFirestore.getInstance();

        // 뷰 초기화
        post_ivCameraIcon = view.findViewById(R.id.post_ivCameraIcon);
        post_tvImageCount = view.findViewById(R.id.post_tvImageCount);
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextContent = view.findViewById(R.id.editTextContent);
        buttonPost = view.findViewById(R.id.buttonPost);

        // 이미지 선택 이벤트
        post_ivCameraIcon.setOnClickListener(v -> openFileChooser());

        // 게시물 등록 이벤트
        buttonPost.setOnClickListener(v -> uploadPost());

        Toolbar toolbar = view.findViewById(R.id.board_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("게시판 글 등록");
        }

        return view;
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.getClipData() != null) {
            imageData = data; // 이미지 데이터 저장
            int itemCount = data.getClipData().getItemCount();
            imageCount = Math.min(itemCount, 10); // 최대 10개 이미지 선택 가능
            post_tvImageCount.setText(imageCount + "/10");
        }
    }

    private void uploadPost() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextContent.getText().toString().trim();

        // 제목과 내용이 비어있지 않은지 확인
        if (title.isEmpty()) {
            Toast.makeText(getActivity(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(getActivity(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 이미지가 선택되었는지 확인
        if (imageCount == 0) {
            Toast.makeText(getActivity(), "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // 현재 로그인된 사용자 가져오기
        if (user != null) {
            String userId = user.getUid(); // 사용자의 고유 ID 가져오기
            String postId = db.collection("posts").document().getId(); // 새로운 문서 ID 생성

            // postId를 추가
            Post post = new Post(postId, title, description, userId, new ArrayList<>()); // 빈 리스트로 초기화
            db.collection("posts").document(postId).set(post)
                    .addOnSuccessListener(aVoid -> {
                        // 이미지가 선택된 경우 업로드
                        for (int i = 0; i < imageCount; i++) {
                            Uri imageUri = imageData.getClipData().getItemAt(i).getUri();
                            StorageReference fileReference = storageReference.child(postId + "/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
                            UploadTask uploadTask = fileReference.putFile(imageUri);
                            uploadTask.addOnSuccessListener(taskSnapshot -> {
                                // 업로드 성공 후 URL 가져오기
                                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    // Firestore에 이미지 URL 추가
                                    db.collection("posts").document(postId)
                                            .update("imageUrls", FieldValue.arrayUnion(imageUrl))
                                            .addOnSuccessListener(aVoid1 -> {
                                                // URL 추가 성공
                                            })
                                            .addOnFailureListener(e -> {
                                                // URL 추가 실패 처리
                                                Toast.makeText(getActivity(), "이미지 URL 추가에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                            });
                                });
                            }).addOnFailureListener(e -> {
                                // 업로드 실패 처리
                                Toast.makeText(getActivity(), "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }

                        Toast.makeText(getActivity(), "게시물이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                        // 게시물 등록 완료 후 이동
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        if (getActivity() != null) {
                            transaction.replace(R.id.fragment_container, new BoardPostCompleteFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }

                        // 2초 후에 게시판 페이지로 이동
                        new Handler().postDelayed(() -> {
                            FragmentTransaction newTransaction = getParentFragmentManager().beginTransaction();
                            if (getActivity() != null) {
                                newTransaction.replace(R.id.fragment_container, new BoardFragment());
                                newTransaction.addToBackStack(null);
                                newTransaction.commit();
                            }
                        }, 2000);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "게시물 등록 중 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    // 파일 확장자 가져오기
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
