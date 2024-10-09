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

        // 저장 버튼 클릭 이벤트 처리
        saveButton.setOnClickListener(v -> {
            // 사용자가 입력한 정보 가져오기
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String phone = etPhoneNumber.getText().toString().trim();

            // Firebase에 정보 업로드
            saveProfileToFirebase(username, password, phone);

            etPhoneNumber.setText(phone);
            usernameEditText.setText(username);

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
        StorageReference profileImageRef = storageRef.child("profileImage/" + userId + "/profile.jpg");
        profileImageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // 프로필 사진 URL을 가져옴
                    String profileImageUrl = uri.toString();

                    // 프로필 사진을 설정
                    Glide.with(requireContext())
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
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
        // 현재 사용자의 UID 가져오기
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference profileImageRef = storageRef.child("profileImage/" + userId + "/profile.jpg");

        // 기존 프로필 이미지 삭제
        profileImageRef.delete().addOnSuccessListener(aVoid -> {
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
        }).addOnFailureListener(e -> {
            // 기존 이미지 삭제 실패 시 동작
            Toast.makeText(getActivity(), "기존 프로필 사진 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfileToFirebase(String username, String password, String phone) {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("phone", phone);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // 비밀번호 업데이트
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        user.updatePassword(password)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(getActivity(), "비밀번호가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "비밀번호 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // 업로드 실패 시 동작
                    Toast.makeText(getActivity(), "프로필 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}