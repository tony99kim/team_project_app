package com.example.team_project.Profile;

import android.content.Context;
import android.content.SharedPreferences;
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

public class EditButtonFragment extends Fragment {
    private ImageView profileImageView;
    private EditText usernameEditText;
    private Button changePhotoButton, saveButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit_button_fragment, container, false);

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
        changePhotoButton = view.findViewById(R.id.changePhotoButton);
        saveButton = view.findViewById(R.id.saveButton);

        // 저장된 사용자 이름 불러오기
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "사용자 이름");
        usernameEditText.setText(savedUsername);

        // 저장 버튼 클릭 이벤트 처리
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 입력한 이름 가져오기
                String username = usernameEditText.getText().toString().trim();
                if (!username.isEmpty()) {
                    saveProfile(username);
                } else {
                    Toast.makeText(getActivity(), "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 프로필 사진 변경 버튼 클릭 이벤트 처리
        changePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 프로필 사진 변경 기능을 구현할 수 있도록 코드를 작성하세요.
                // 예시로 Toast 메시지를 사용합니다.
                Toast.makeText(getActivity(), "프로필 사진을 변경합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void saveProfile(String username) {
        // 저장 버튼을 눌렀을 때의 동작을 구현합니다.
        // SharedPreferences를 사용하여 사용자 이름을 저장합니다.
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.apply(); // 변경 사항 저장

        // 변경된 이름을 표시
        usernameEditText.setText(username);
        Toast.makeText(getActivity(), "이름이 변경되었습니다: " + username, Toast.LENGTH_SHORT).show();
    }
}
