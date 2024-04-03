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

        btnSubmitSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 여기에서 회원가입 로직을 처리합니다.
                // 예를 들어, etEmail, etPassword, etName에서 텍스트를 가져와서 사용할 수 있습니다.
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String name = etName.getText().toString();

                // 회원가입 처리 로직을 구현하세요.
                // 예: 서버에 사용자 정보를 전송하고 응답을 처리합니다.

                // 회원가입 성공 후 액션, 예를 들어 로그인 화면으로 돌아가거나 메인 화면으로 이동 등
            }
        });
    }
}
