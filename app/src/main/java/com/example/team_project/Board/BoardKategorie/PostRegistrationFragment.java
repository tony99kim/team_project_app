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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class PostRegistrationFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private int imageCount = 0;

    private ImageView post_ivCameraIcon;
    private TextView post_tvImageCount;
    private EditText editTextTitle, editTextContent;
    private Button buttonPost;

    private StorageReference storageReference;
    private FirebaseFirestore db;
    private Intent imageData = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board_postregistration, container, false);

        // Firebase 초기화
        storageReference = FirebaseStorage.getInstance().getReference("PostImages");
        db = FirebaseFirestore.getInstance();

        post_ivCameraIcon = view.findViewById(R.id.post_ivCameraIcon);
        post_tvImageCount = view.findViewById(R.id.post_tvImageCount);
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextContent = view.findViewById(R.id.editTextContent);
        buttonPost = view.findViewById(R.id.buttonPost);

        post_ivCameraIcon.setOnClickListener(v -> openFileChooser());
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
            imageData = data;
            int itemCount = data.getClipData().getItemCount();
            imageCount = Math.min(itemCount, 10);
            post_tvImageCount.setText(imageCount + "/10");
        }
    }

    private void uploadPost() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(getActivity(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(getActivity(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageCount == 0) {
            Toast.makeText(getActivity(), "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String userName = user.getDisplayName(); // 작성자 이름 가져오기
            String postId = db.collection("posts").document().getId();

            // Post 객체 생성 시 authorName 추가
            Post post = new Post(postId, title, description, userId, userName, new ArrayList<>(), 0, 0); // 초기 조회수 0
            db.collection("posts").document(postId).set(post)
                    .addOnSuccessListener(aVoid -> uploadImages(postId))
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "게시물 등록 중 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void uploadImages(String postId) {
        List<String> imageUrls = new ArrayList<>();

        for (int i = 0; i < imageCount; i++) {
            Uri imageUri = imageData.getClipData().getItemAt(i).getUri();
            StorageReference fileReference = storageReference.child(postId + "/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            UploadTask uploadTask = fileReference.putFile(imageUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                imageUrls.add(uri.toString());
                if (imageUrls.size() == imageCount) { // 모든 이미지가 업로드된 후
                    db.collection("posts").document(postId)
                            .update("imageUrls", imageUrls)
                            .addOnSuccessListener(aVoid -> navigateToCompletePage())
                            .addOnFailureListener(e -> Toast.makeText(getActivity(), "이미지 URL 추가에 실패했습니다.", Toast.LENGTH_SHORT).show());
                }
            })).addOnFailureListener(e -> Toast.makeText(getActivity(), "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void navigateToCompletePage() {
        Toast.makeText(getActivity(), "게시물이 등록되었습니다.", Toast.LENGTH_SHORT).show();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new BoardPostCompleteFragment());
        transaction.addToBackStack(null);
        transaction.commit();

        new Handler().postDelayed(() -> {
            FragmentTransaction newTransaction = getParentFragmentManager().beginTransaction();
            newTransaction.replace(R.id.fragment_container, new BoardFragment());
            newTransaction.addToBackStack(null);
            newTransaction.commit();
        }, 2000);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
