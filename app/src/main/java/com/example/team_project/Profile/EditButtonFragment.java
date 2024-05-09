package com.example.team_project.Profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.team_project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class EditButtonFragment extends Fragment {
    private EditText usernameEditText, phoneEditText, passwordEditText;
    private ImageView profileImageView;
    private Button changePhotoButton, saveButton;

    private Uri imageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit_button_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("프로필 편집");
        }

        // 뷰 초기화
        profileImageView = view.findViewById(R.id.profileImageView);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        phoneEditText = view.findViewById(R.id.etPhoneNumber); // 전화번호 EditText
        passwordEditText = view.findViewById(R.id.etSignUpPassword); // 비밀번호 EditText
        changePhotoButton = view.findViewById(R.id.changePhotoButton);
        saveButton = view.findViewById(R.id.saveButton);

        // 저장된 사용자 정보 불러오기
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "사용자 이름");
        usernameEditText.setText(savedUsername);

        // 저장 버튼 클릭 이벤트 처리
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 입력한 정보 가져오기
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim(); // 입력된 비밀번호 가져오기
                String phone = phoneEditText.getText().toString().trim(); // 입력된 전화번호 가져오기

                // 입력한 정보가 비어있지 않은지 확인
                if (!username.isEmpty() && !password.isEmpty() && !phone.isEmpty()) {
                    // 정보 저장 및 업로드 함수 호출
                    saveProfile(username, password, phone);
                } else {
                    Toast.makeText(getActivity(), "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 프로필 사진 변경 버튼 클릭 이벤트 처리
        changePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void saveProfile(String username, String password, String phone) {
        // SharedPreferences를 사용하여 사용자 정보 저장
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.apply(); // 변경 사항 저장

        // Firebase에 정보 업로드
        saveProfileToFirebase(username, password, phone);

        // 업로드 후 화면에 변경된 정보 표시
        usernameEditText.setText(username);

        // 프로필 사진이 변경되었으면 업로드
        if (imageUri != null) {
            uploadProfileImage(imageUri);
        }

        // 업로드 성공 메시지 표시
        Toast.makeText(getActivity(), "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void uploadProfileImage(Uri imageUri) {
        // 프로필 사진을 Firebase Storage에 업로드합니다.
        StorageReference profileImageRef = storageRef.child("profileImages/" + mAuth.getCurrentUser().getUid() + ".jpg");

        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // 업로드 성공 시 동작
                        Toast.makeText(getActivity(), "프로필 사진이 업로드되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 업로드 실패 시 동작
                        Toast.makeText(getActivity(), "프로필 사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveProfileToFirebase(String username, String password, String phone) {
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("password", password); // 비밀번호 업로드
        updates.put("phone", phone); // 전화번호 업로드

        // phoneNumber 정보는 더 이상 업데이트하지 않음
        updates.remove("phoneNumber");

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // 업로드 성공 시 동작
                    Toast.makeText(getActivity(), "Firebase에 프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // 업로드 실패 시 동작
                    Toast.makeText(getActivity(), "Firebase에 프로필 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}