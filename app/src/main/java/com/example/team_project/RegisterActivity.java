package com.example.team_project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextName, editTextPhone;
    private RadioButton radioButtonMale, radioButtonFemale;
    private Button buttonRegister, btnBack;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editTextEmail = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etSignUpPassword);
        editTextName = findViewById(R.id.etName);
        editTextPhone = findViewById(R.id.etPhoneNumber);
        radioButtonMale = findViewById(R.id.rbMale);
        radioButtonFemale = findViewById(R.id.rbFemale);
        buttonRegister = findViewById(R.id.btnSubmitSignUp);
        btnBack = findViewById(R.id.btnBackToLogin);


        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 액티비티 종료하여 이전 화면(로그인 화면)으로 돌아가기
            }
        });
    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();
        final String gender = radioButtonMale.isChecked() ? "Male" : "Female";

        // 입력 값 검증
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty() || gender.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_LONG).show();
            return; // 필요한 모든 항목이 입력되지 않았으므로 여기서 메소드 종료
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(name, email, phone, gender);
                            mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "회원가입 성공", Toast.LENGTH_LONG).show();
                                                finish(); // 등록 성공 후 이전 화면으로 돌아가기
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "회원 정보 등록 실패", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(RegisterActivity.this, "이미 사용중인 아이디(이메일)입니다.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "회원가입에 실패하였습니다.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                });
    }

    private static class User {
        public String name, email, phone, gender;

        public User(String name, String email, String phone, String gender) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.gender = gender;
        }
    }
}