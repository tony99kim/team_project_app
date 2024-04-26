package com.example.team_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private FirebaseAuth mAuth;

    private final String PREFERENCES_NAME = "team_project_preferences";
    private final String PREF_KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        etUsername = findViewById(R.id.etUserid);
        etPassword = findViewById(R.id.etPassword);

        Button loginButton = findViewById(R.id.btnLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        Button registerButton = findViewById(R.id.btnSignUp);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // RegisterActivity로 이동
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(){
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(email.isEmpty()){
            Toast.makeText(LoginActivity.this, "아이디(이메일)를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.isEmpty()){
            Toast.makeText(LoginActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase를 사용하여 사용자 인증
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            // 로그인 실패
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(LoginActivity.this, "잘못된 비밀번호입니다.", Toast.LENGTH_SHORT).show();
                            } catch(FirebaseAuthInvalidUserException e) {
                                Toast.makeText(LoginActivity.this, "존재하지 않는 이메일입니다.", Toast.LENGTH_SHORT).show();
                            } catch(Exception e) {
                                Toast.makeText(LoginActivity.this, "로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 로그인 성공
                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                            // SharedPreferences를 사용하여 로그인 상태 업데이트
                            SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE).edit();
                            editor.putBoolean(PREF_KEY_IS_LOGGED_IN, true);
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
}
