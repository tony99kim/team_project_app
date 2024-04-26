package com.example.team_project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextName, editTextPhone, editTextBirthDate;
    private RadioButton radioButtonMale, radioButtonFemale;
    private Button buttonRegister, btnBack;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firestore 인스턴스 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Firestore 인스턴스 초기화

        editTextEmail = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etSignUpPassword);
        editTextName = findViewById(R.id.etName);
        editTextPhone = findViewById(R.id.etPhoneNumber);
        editTextBirthDate = findViewById(R.id.etBirthDate);
        radioButtonMale = findViewById(R.id.rbMale);
        radioButtonFemale = findViewById(R.id.rbFemale);
        buttonRegister = findViewById(R.id.btnSubmitSignUp);
        btnBack = findViewById(R.id.btnBackToLogin);

        editTextBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 액티비티 종료하여 이전 화면으로 돌아가기
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 날짜 선택 후 editTextBirthDate에 날짜 설정
                        String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        editTextBirthDate.setText(date);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();
        final String birthDate = editTextBirthDate.getText().toString().trim(); // 생년월일 정보 가져오기
        final String gender = radioButtonMale.isChecked() ? "Male" : "Female";

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty() || birthDate.isEmpty() || gender.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Firestore에 사용자 정보 저장
                            User user = new User(name, email, phone, gender, birthDate);
                            db.collection("users").document(mAuth.getCurrentUser().getUid()).set(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "회원가입 성공", Toast.LENGTH_LONG).show();
                                                finish();
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

    // User 클래스 정의, 생년월일(birthDate) 필드 추가
    private static class User {
        public String name, email, phone, gender, birthDate;

        public User(String name, String email, String phone, String gender, String birthDate) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.gender = gender;
            this.birthDate = birthDate;
        }
    }
}
