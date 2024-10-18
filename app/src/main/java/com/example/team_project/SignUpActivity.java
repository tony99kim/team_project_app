package com.example.team_project;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextName, editTextUsername, editTextPhone, editTextBirthDate, editTextAddress, editTextDetailAddress;
    private RadioButton radioButtonMale, radioButtonFemale;
    private Button buttonRegister, btnSelectAddress;
    private FrameLayout frameLayoutProfilePhoto;
    private ImageView imageViewProfile;
    private TextView textViewImageCount;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri profileImageUri;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_ADDRESS_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar_sign_up);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        editTextEmail = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etSignUpPassword);
        editTextName = findViewById(R.id.etName);
        editTextUsername = findViewById(R.id.etUsername);
        editTextPhone = findViewById(R.id.etPhoneNumber);
        editTextBirthDate = findViewById(R.id.etBirthDate);
        editTextAddress = findViewById(R.id.etAddress);
        editTextDetailAddress = findViewById(R.id.etDetailAddress);
        radioButtonMale = findViewById(R.id.rbMale);
        radioButtonFemale = findViewById(R.id.rbFemale);
        buttonRegister = findViewById(R.id.btnSubmitSignUp);
        btnSelectAddress = findViewById(R.id.btnSelectAddress);
        frameLayoutProfilePhoto = findViewById(R.id.frameLayoutProfilePhoto);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewImageCount = findViewById(R.id.tvImageCount);

        editTextBirthDate.setOnClickListener(v -> showDatePickerDialog());

        frameLayoutProfilePhoto.setOnClickListener(v -> openFileChooser());

        btnSelectAddress.setOnClickListener(v -> openAddressPicker());

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    public void onUploadProfilePhotoClicked(View view) {
        openFileChooser(); // 사진 선택을 위한 메서드 호출
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                    editTextBirthDate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openAddressPicker() {
        Intent intent = new Intent(this, AddressPickerActivity.class);
        startActivityForResult(intent, PICK_ADDRESS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            imageViewProfile.setImageURI(profileImageUri);
            textViewImageCount.setText("1/1");
        } else if (requestCode == PICK_ADDRESS_REQUEST && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("selectedAddress");
            editTextAddress.setText(address);
        }
    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();
        final String birthDate = editTextBirthDate.getText().toString().trim();
        final String address = editTextAddress.getText().toString().trim();
        final String detailAddress = editTextDetailAddress.getText().toString().trim();
        final String gender = radioButtonMale.isChecked() ? "Male" : "Female";
        final String signUpDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || username.isEmpty() || phone.isEmpty() || birthDate.isEmpty() || gender.isEmpty() || address.isEmpty() || detailAddress.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User(name, username, email, phone, gender, birthDate, address, detailAddress, signUpDate, 0); // 환경 포인트 0으로 초기화
                        db.collection("users").document(mAuth.getCurrentUser().getUid()).set(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        if (profileImageUri != null) {
                                            uploadProfileImage(mAuth.getCurrentUser().getUid());
                                        }
                                        Toast.makeText(SignUpActivity.this, "회원가입 성공", Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "회원 정보 등록 실패", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(SignUpActivity.this, "이미 사용중인 아이디(이메일)입니다.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "회원가입에 실패하였습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void uploadProfileImage(String userId) {
        StorageReference profileImageRef = storage.getReference().child("profileImage/" + userId + "/profile.jpg");
        profileImageRef.putFile(profileImageUri)
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(SignUpActivity.this, "프로필 사진 업로드 성공", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, "프로필 사진 업로드 실패", Toast.LENGTH_SHORT).show());
    }

    private static class User {
        public String name, username, email, phone, gender, birthDate, address, detailAddress, signUpDate;
        public int environmentalPoint; // 환경 포인트 필드 추가

        public User(String name, String username, String email, String phone, String gender, String birthDate, String address, String detailAddress, String signUpDate, int environmentalPoint) {
            this.name = name;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.gender = gender;
            this.birthDate = birthDate;
            this.address = address;
            this.detailAddress = detailAddress;
            this.signUpDate = signUpDate;
            this.environmentalPoint = environmentalPoint; // 포인트 초기화
        }
    }
}
