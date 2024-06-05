package com.example.team_project.Profile;

import android.content.Context;
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
    private SharedPreferences sharedPreferences; // sharedPreferences를 클래스 필드로 선언합니다.

    // onCreateView 메서드 수정
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
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber); // 전화번호 EditText
        passwordEditText = view.findViewById(R.id.etSignUpPassword); // 비밀번호 EditText
        changePhotoButton = view.findViewById(R.id.changePhotoButton);
        saveButton = view.findViewById(R.id.saveButton);

        // 저장된 사용자 정보 불러오기
        sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "사용자 이름");
        String savedPhone = sharedPreferences.getString("phone", ""); // SharedPreferences에서 전화번호를 가져옵니다.
        usernameEditText.setText(savedUsername);
        etPhoneNumber.setText(savedPhone);

        // 저장 버튼 클릭 이벤트 처리
        saveButton.setOnClickListener(v -> {
            // 사용자가 입력한 정보 가져오기
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim(); // 입력된 비밀번호 가져오기
            String phone = etPhoneNumber.getText().toString().trim(); // 입력된 전화번호 가져오기

            // Firebase에 정보 업로드
            saveProfileToFirebase(username, password, phone);

            etPhoneNumber.setText(phone); // 전화번호 업로드 후 EditText에 표시
            usernameEditText.setText(username); // 닉네임 업로드 후 EditText에 표시

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

        // 저장된 프로필 이미지가 있으면 설정
        String profileImageUrl = sharedPreferences.getString("profileImageUrl", null);
        if (profileImageUrl != null) {
            profileImageView.setImageURI(Uri.parse(profileImageUrl));
        }
        // 사용자 정보 가져오기
        setUserInfoFromFirebase();
        // 프로필 사진 설정
        setProfileImageFromFirebase();

        return view;
    }
    private void setUserInfoFromFirebase() {
// 현재 사용자의 UID 가져오기
        String userId = mAuth.getCurrentUser().getUid();

// Firestore에서 사용자 정보 가져오기
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 사용자 정보가 있을 경우
                        String username = documentSnapshot.getString("username");
                        String phone = documentSnapshot.getString("phone");

                        // 가져온 정보를 SharedPreferences에 저장
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username);
                        editor.putString("phone", phone);
                        editor.apply();

                        // 가져온 정보를 EditText에 설정
                        usernameEditText.setText(username);
                        etPhoneNumber.setText(phone);
                    } else {
                        // 사용자 정보가 없을 경우
                        Toast.makeText(getActivity(), "사용자 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // 정보를 가져오는 중 오류 발생 시
                    Toast.makeText(getActivity(), "사용자 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
    // 프로필 사진 가져와서 설정
    private void setProfileImageFromFirebase() {
        // 현재 사용자의 UID 가져오기
        String userId = mAuth.getCurrentUser().getUid();

        // Firestore에서 사용자의 프로필 사진 가져오기
        StorageReference profileImageRef = storageRef.child("profileImages/" + userId + ".jpg");
        profileImageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // 프로필 사진 URL을 가져옴
                    String profileImageUrl = uri.toString();

                    // 프로필 사진을 설정
                    Glide.with(requireContext())
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile) // 기본 이미지 설정
                            .error(R.drawable.ic_profile) // 에러 시 이미지 설정
                            .into(profileImageView);
                })
                .addOnFailureListener(e -> {
                    // 프로필 사진이 없을 경우
                    Toast.makeText(getActivity(), "프로필 사진을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }



    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void uploadProfileImage(Uri imageUri) {
        // 프로필 사진을 Firebase Storage에 업로드합니다.
        StorageReference profileImageRef = storageRef.child("profileImages/" + mAuth.getCurrentUser().getUid() + ".jpg");

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

    private void saveProfileToFirebase(String username, String password, String phone) {
        String userId = mAuth.getCurrentUser().getUid();
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