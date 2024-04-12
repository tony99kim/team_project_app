package com.example.team_project.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.team_project.LoginActivity;
import com.example.team_project.R;

public class ProfileFragment extends Fragment {

    private TextView usernameTextView;
    private ImageView profileImageView;
    private Button recentVisitButton, noticeButton, customerServiceButton, logoutButton, withdrawButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.profileImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        recentVisitButton = view.findViewById(R.id.recentVisitButton);
        noticeButton = view.findViewById(R.id.noticeButton);
        customerServiceButton = view.findViewById(R.id.customerServiceButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        withdrawButton = view.findViewById(R.id.withdrawButton);

        // 사용자 정보 설정
        String username = "사용자 이름"; // 여기에 사용자 이름을 가져오는 로직을 추가해야 합니다.
        int profileImageResId = R.drawable.ic_profile; // 사용자 프로필 이미지 리소스 ID
        usernameTextView.setText(username);
        profileImageView.setImageResource(profileImageResId);


        // 로그아웃 버튼 클릭 리스너 설정
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        return view;
    }

    private  void logout() {
        Intent intent = new Intent(getActivity() , LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}

