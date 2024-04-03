package com.example.team_project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final EditText etEmail = findViewById(R.id.etEmail);
        final EditText etPassword = findViewById(R.id.etSignUpPassword);
        final EditText etName = findViewById(R.id.etName);
        Button btnSubmitSignUp = findViewById(R.id.btnSubmitSignUp);
        Button btnBack = findViewById(R.id.btnBackToLogin); // 뒤로 가기 버튼 찾기

        btnSubmitSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 여기에서 회원가입 로직을 처리합니다.
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String name = etName.getText().toString();

                // 회원가입 처리 로직을 구현하세요.
            }
        });

        // 뒤로 가기 버튼 클릭 이벤트 처리
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 액티비티 종료하여 이전 화면(로그인 화면)으로 돌아가기
                finish();
            }
        });
    }
}
