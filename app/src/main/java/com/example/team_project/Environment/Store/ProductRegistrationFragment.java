package com.example.team_project.Environment.Store;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.Environment.EnvironmentFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.team_project.R;

public class ProductRegistrationFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private int imageCount = 0;

    private ImageView ivCameraIcon;
    private TextView tvImageCount;
    private EditText etTitle, etPrice, etDescription;
    private Button btnRegisterProduct;

    // Firebase 참조
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private Intent imageData = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_store_productregistration.xml을 이용하여 뷰를 생성
        View view = inflater.inflate(R.layout.fragment_environment_store_productregistration, container, false);

        // Firebase 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("Products");
        storageReference = FirebaseStorage.getInstance().getReference("ProductImages");

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar_product_registration);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        // 툴바에 뒤로가기 버튼 추가
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        // 뒤로가기 버튼 클릭 이벤트 처리
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        // 뷰 초기화
        ivCameraIcon = view.findViewById(R.id.ivCameraIcon);
        tvImageCount = view.findViewById(R.id.tvImageCount);
        etTitle = view.findViewById(R.id.etTitle);
        etPrice = view.findViewById(R.id.etPrice);
        etDescription = view.findViewById(R.id.etDescription);
        btnRegisterProduct = view.findViewById(R.id.btnRegisterProduct);

        // 이미지 선택 이벤트
        ivCameraIcon.setOnClickListener(v -> openFileChooser());

        // 상품 등록 이벤트
        btnRegisterProduct.setOnClickListener(v -> {
            if (imageData != null) {
                uploadProduct(imageData); // imageData가 null이 아닐 때만 uploadProduct 호출
            } else {
                Toast.makeText(getActivity(), "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.getClipData() != null) {
            imageData = data; // 이미지 데이터 저장
            int itemCount = data.getClipData().getItemCount();
            imageCount = Math.min(itemCount, 10); // 최대 10개 이미지 선택 가능
            tvImageCount.setText(imageCount + "/10");
        }
    }



    private void uploadProduct(Intent data) {
        String title = etTitle.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        if (!title.isEmpty() && !price.isEmpty() && !description.isEmpty() && imageCount > 0) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // 현재 로그인된 사용자 가져오기
            if (user != null) {
                String userId = user.getUid(); // 사용자의 고유 ID 가져오기

                FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore 인스턴스 가져오기
                String productId = db.collection("products").document().getId(); // 새로운 문서 ID 생성

                Product product = new Product(productId, userId, title, price, description); // 사용자 ID를 Product 객체에 저장
                db.collection("products").document(productId).set(product)
                        .addOnSuccessListener(aVoid -> {
                            // 이미지 스토리지에 업로드
                            for (int i = 0; i < imageCount; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                StorageReference fileReference = storageReference.child(productId + "/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
                                UploadTask uploadTask = fileReference.putFile(imageUri);
                                // 업로드 성공 또는 실패에 대한 처리를 추가할 수 있습니다.
                            }

                            Toast.makeText(getActivity(), "상품이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "상품 등록에 실패했습니다.", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(getActivity(), "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }



    // 파일 확장자 가져오기 (이 부분은 구현에 따라 다를 수 있습니다.)
    private String getFileExtension(Uri uri) {
        // ContentResolver를 사용하여 파일 확장자를 가져옵니다.
        return "jpg"; // 예시로 "jpg"를 반환합니다. 실제 구현에서는 uri에 따른 실제 확장자를 반환해야 합니다.
    }

    // Product 클래스 (상품 정보 저장을 위한 클래스)
    // 이 클래스는 상품의 아이디, 제목, 가격, 설명을 저장합니다.
    public static class Product {
        public String productId, userId, title, price, description;

        public Product(String productId, String userId, String title, String price, String description) {
            this.productId = productId;
            this.userId = userId; // 사용자 ID 필드 추가
            this.title = title;
            this.price = price;
            this.description = description;
        }
    }

}
