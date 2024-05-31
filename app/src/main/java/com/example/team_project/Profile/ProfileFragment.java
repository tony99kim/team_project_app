package com.example.team_project.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.team_project.LoginActivity;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private TextView usernameTextView, environmentPointsTextView;
    private ImageView profileImageView;
    private Button editButton, recentVisitButton, noticeButton, customerServiceButton, logoutButton, withdrawButton;
    private androidx.appcompat.widget.Toolbar toolbar;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        storageRef = FirebaseStorage.getInstance().getReference();

        profileImageView = view.findViewById(R.id.profileImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        recentVisitButton = view.findViewById(R.id.recentVisitButton);
        noticeButton = view.findViewById(R.id.noticeButton);
        customerServiceButton = view.findViewById(R.id.customerServiceButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        withdrawButton = view.findViewById(R.id.withdrawButton);
        editButton = view.findViewById(R.id.editButton);
        toolbar = view.findViewById(R.id.toolbar);
        environmentPointsTextView = view.findViewById(R.id.environmentPointsTextView);

        setUsername();
        setProfileImageFromFirebase();

        // 사용자의 환경 포인트 설정 (예시로 1000이라고 가정)
        int environmentPoints = 1000;
        environmentPointsTextView.setText("환경 포인트: " + environmentPoints);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setTitle("마이 페이지");
        }
        // 로그아웃 버튼 클릭 리스너 설정
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        // 최근 방문 버튼 클릭 리스너 설정
        recentVisitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRecentVisitFragment();
            }
        });
        // 공지사항 버튼 클릭 리스너 설정
        noticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoticeFragment();
            }
        });
        // 고객센터 버튼 클릭 리스너 설정
        customerServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCustomerServiceFragment();
            }
        });
        // 회원탈퇴 버튼 클릭 리스너 설정
        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWithdrawFragment();
            }
        });
        // 수정하기 버튼 클릭 리스너 설정
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditButtonFragment();
            }
        });

        return view;
    }
    private void logout() {
        // SharedPreferences에서 로그인 상태와 자동 로그인 설정을 초기화
        SharedPreferences prefs = getActivity().getSharedPreferences("team_project_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("autoLogin", false);
        editor.apply();
        // 로그인 액티비티로 이동
        mAuth.signOut();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
    // 최근 방문 프래그먼트 객체 생성
    private void openRecentVisitFragment() {
        RecentVisitFragment fragment = new RecentVisitFragment();
        // 현재 액티비티가 AppCompatActivity 인지 확인
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        // 현재 액티비티가 AppCompatActivity 인 경우에만 프래그먼트 트랜잭션 수행
        if (activity != null) {
            // 프래그먼트 트랜잭션을 사용하여 최근 방문 프래그먼트를 화면에 표시
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)// R.id.fragment_container는 프래그먼트를 표시할 레이아웃의 ID입니다.
                    .addToBackStack(null) // 백 스택에 프래그먼트를 추가하여 뒤로 가기 기능을 지원합니다.
                    .commit();
        }
    }

    private void openNoticeFragment() {
        NoticeFragment fragment = new NoticeFragment();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openCustomerServiceFragment() {
        CustomerServiceFragment fragment = new CustomerServiceFragment();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openWithdrawFragment() {
        WithdrawFragment fragment = new WithdrawFragment();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openEditButtonFragment() {
        EditButtonFragment fragment = new EditButtonFragment();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUsernameFromFirebase(); // Firebase에서 닉네임을 가져와서 설정
    }
    // 사용자 환경페이 레이아웃 클릭 이벤트
    public void onUserPayLayoutClicked(View view) {
        // PayFragment로 전환
        PayFragment payFragment = new PayFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new PayFragment());
        transaction.addToBackStack(null); // 뒤로가기 버튼을 눌렀을 때 이전 상태로 돌아갈 수 있도록 스택에 추가
        transaction.commit();
    }

    private void setUsernameFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        if (username != null && !username.isEmpty()) {
                            // 가져온 닉네임을 SharedPreferences에 저장
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", username);
                            editor.apply();

                            // 화면에 표시
                            usernameTextView.setText(username);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "닉네임을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
    }
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

    private void setUsername() {
        String savedUsername = sharedPreferences.getString("username", "사용자 이름");
        usernameTextView.setText(savedUsername);
    }
}


