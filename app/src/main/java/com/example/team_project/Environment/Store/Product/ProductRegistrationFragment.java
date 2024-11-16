package com.example.team_project.Environment.Store.Product;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProductRegistrationFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private int imageCount = 0;

    private ImageView ivCameraIcon;
    private TextView tvImageCount;
    private EditText etTitle, etPrice, etDescription;
    private Button btnRegisterProduct;
    private CheckBox cbIsBusiness; // 기업으로 등록 체크박스 추가

    // Firebase references
    private StorageReference storageReference;
    private FirebaseFirestore db;

    private Intent imageData = null;
    private String productId; // 상품 ID (수정용)

    public ProductRegistrationFragment() {
        // 기본 생성자
    }

    // 수정할 상품 ID를 전달받을 수 있는 생성자
    public ProductRegistrationFragment(String productId) {
        this.productId = productId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 레이아웃 인플레이션
        View view = inflater.inflate(R.layout.fragment_environment_store_productregistration, container, false);

        // Firebase 초기화
        storageReference = FirebaseStorage.getInstance().getReference("ProductImages");
        db = FirebaseFirestore.getInstance();

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar_product_registration);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        // 뷰 초기화
        ivCameraIcon = view.findViewById(R.id.ivCameraIcon);
        tvImageCount = view.findViewById(R.id.tvImageCount);
        etTitle = view.findViewById(R.id.etTitle);
        etPrice = view.findViewById(R.id.etPrice);
        etDescription = view.findViewById(R.id.etDescription);
        cbIsBusiness = view.findViewById(R.id.cbBusinessOrIndividual); // 기업 여부 체크박스
        btnRegisterProduct = view.findViewById(R.id.btnRegisterProduct);

        // 이미지 선택기능
        ivCameraIcon.setOnClickListener(v -> openFileChooser());

        // 상품 등록 또는 수정
        btnRegisterProduct.setOnClickListener(v -> {
            if (imageData != null) {
                if (productId != null) {
                    updateProduct(imageData); // 상품 수정
                } else {
                    uploadProduct(imageData); // 새로운 상품 등록
                }
            } else {
                Toast.makeText(getActivity(), "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 수정할 상품이 있을 경우 해당 데이터 로드
        if (productId != null) {
            loadProductData(productId);
        }

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "이미지 선택"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.getClipData() != null) {
            imageData = data;
            int itemCount = data.getClipData().getItemCount();
            imageCount = Math.min(itemCount, 10); // 최대 10개의 이미지만 선택 가능
            tvImageCount.setText(imageCount + "/10");
        }
    }

    private void loadProductData(String productId) {
        db.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            etTitle.setText(product.getTitle());
                            etPrice.setText(product.getPrice());
                            etDescription.setText(product.getDescription());
                            cbIsBusiness.setChecked(product.isBusiness()); // 기업 등록 여부 반영
                        }
                    } else {
                        Toast.makeText(getActivity(), "상품 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductRegistrationFragment", "상품 데이터 불러오기 오류", e);
                    Toast.makeText(getActivity(), "상품 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadProduct(Intent data) {
        String title = etTitle.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isBusiness = cbIsBusiness.isChecked(); // 체크박스 값

        if (!title.isEmpty() && !price.isEmpty() && !description.isEmpty() && imageCount > 0) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                String productId = db.collection("products").document().getId(); // 새로운 상품 ID 생성

                Product product = new Product(productId, userId, title, price, description, isBusiness); // 상품 객체 생성
                db.collection("products").document(productId).set(product)
                        .addOnSuccessListener(aVoid -> {
                            // 이미지 업로드
                            for (int i = 0; i < imageCount; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                StorageReference fileReference = storageReference.child(productId + "/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
                                fileReference.putFile(imageUri)
                                        .addOnSuccessListener(taskSnapshot -> {})
                                        .addOnFailureListener(e -> Log.e("ProductRegistrationFragment", "이미지 업로드 실패", e));
                            }

                            Toast.makeText(getActivity(), "상품이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "상품 등록에 실패했습니다.", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(getActivity(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProduct(Intent data) {
        String title = etTitle.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isBusiness = cbIsBusiness.isChecked(); // 체크박스 값

        if (!title.isEmpty() && !price.isEmpty() && !description.isEmpty()) {
            Product updatedProduct = new Product(productId, FirebaseAuth.getInstance().getCurrentUser().getUid(), title, price, description, isBusiness);
            db.collection("products").document(productId).set(updatedProduct)
                    .addOnSuccessListener(aVoid -> {
                        // 이미지 업로드
                        for (int i = 0; i < imageCount; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            StorageReference fileReference = storageReference.child(productId + "/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
                            fileReference.putFile(imageUri)
                                    .addOnSuccessListener(taskSnapshot -> {})
                                    .addOnFailureListener(e -> Log.e("ProductRegistrationFragment", "이미지 업로드 실패", e));
                        }

                        Toast.makeText(getActivity(), "상품이 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "상품 수정에 실패했습니다.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getActivity(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        return "jpg"; // 파일 확장자 (jpg로 고정)
    }
}
