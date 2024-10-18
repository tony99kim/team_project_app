package com.example.team_project.Profile;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditButtonFragment extends Fragment {
    private EditText usernameEditText, etPhoneNumber, passwordEditText;
    private ImageView profileImageView;
    private Button changePhotoButton, saveButton;

    private Uri imageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private ActivityResultLauncher<String> galleryLauncher;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit_button, container, false);

        // FirebaseAuth, FirebaseFirestore, FirebaseStorage 인스턴스 생성
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
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        passwordEditText = view.findViewById(R.id.etSignUpPassword);
        changePhotoButton = view.findViewById(R.id.changePhotoButton);
        saveButton = view.findViewById(R.id.saveButton);

        // 저장된 사용자 정보 불러오기
        sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "사용자 프로필 이름");
        String savedPhone = sharedPreferences.getString("phone", "");
        usernameEditText.setText(savedUsername);
        etPhoneNumber.setText(savedPhone);

        // 프로필 사진 설정
        setProfileImageFromFirebase();

        // 저장 버튼 클릭 이벤트 처리
        saveButton.setOnClickListener(v -> {
            // 사용자가 입력한 정보 가져오기
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String phone = etPhoneNumber.getText().toString().trim();

            // Firebase에 정보 업로드
            saveProfileToFirebase(username, password, phone);

            // 프로필 사진이 변경되었으면 업로드
            if (imageUri != null) {
                uploadProfileImage(imageUri);
            }

            // 업로드 성공 메시지 표시
            Toast.makeText(getActivity(), "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
        });

        // 프로필 사진 변경 버튼 클릭 이벤트 처리
        changePhotoButton.setOnClickListener(v -> openGallery());

        // 갤러리 런처 설정
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                imageUri = result;
                profileImageView.setImageURI(imageUri);
            }
        });

        return view;
    }

    private void setProfileImageFromFirebase() {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference profileImageRef = storageRef.child("profileImage/" + userId + "/"); // 폴더 경로 설정

        profileImageRef.listAll().addOnSuccessListener(listResult -> {
            if (listResult.getItems().size() > 0) {
                // 첫 번째 이미지 가져오기
                StorageReference firstImageRef = listResult.getItems().get(0);
                firstImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(requireContext())
                            .load(uri)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(profileImageView);
                }).addOnFailureListener(e -> {
                    // 이미지 URL 가져오기 실패 시
                    profileImageView.setImageDrawable(null); // 이미지 제거
                    Toast.makeText(getActivity(), "프로필 사진을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
            } else {
                // 이미지가 없는 경우 처리
                profileImageView.setImageDrawable(null); // 이미지 제거
                Toast.makeText(getActivity(), "프로필 사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // 폴더 목록 가져오기 실패 시
            profileImageView.setImageDrawable(null); // 이미지 제거
            Toast.makeText(getActivity(), "프로필 사진 폴더를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfileToFirebase(String username, String password, String phone) {
        // 현재 사용자의 UID 가져오기
        String userId = mAuth.getCurrentUser().getUid();

        // 사용자 정보 업데이트
        DocumentReference userRef = db.collection("users").document(userId);
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("phone", phone);

        // 사용자 정보 업데이트
        userRef.update(user)
                .addOnSuccessListener(aVoid -> {
                    // 사용자 정보 업데이트 성공 시 SharedPreferences에 저장
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.putString("phone", phone);
                    editor.apply();

                    // 사용자 정보 업데이트 성공 메시지 표시
                    Toast.makeText(getActivity(), "사용자 정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // 사용자 정보 업데이트 실패 시 동작
                    Toast.makeText(getActivity(), "사용자 정보 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });

        // 비밀번호 변경
        if (!password.isEmpty()) {
            FirebaseUser userAuth = mAuth.getCurrentUser();
            userAuth.updatePassword(password)
                    .addOnSuccessListener(aVoid -> {
                        // 비밀번호 변경 성공 시
                        Toast.makeText(getActivity(), "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // 비밀번호 변경 실패 시
                        Toast.makeText(getActivity(), "비밀번호 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void uploadProfileImage(Uri imageUri) {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference profileImageRef = storageRef.child("profileImage/" + userId + "/profile.jpg");

        // 새 프로필 이미지 업로드
        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // 업로드 성공 시 다운로드 URL 가져와서 저장
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // 이미지 다운로드 URL을 SharedPreferences에 저장
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("profileImageUrl", uri.toString());
                        editor.apply();
                    });

                    Toast.makeText(getActivity(), "프로필 사진이 업로드되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // 업로드 실패 시 동작
                    Toast.makeText(getActivity(), "프로필 사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}
