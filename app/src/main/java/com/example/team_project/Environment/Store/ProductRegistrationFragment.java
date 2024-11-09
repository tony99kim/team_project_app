package com.example.team_project.Environment.Store;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.example.team_project.Environment.Store.Product;

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
    private StorageReference storageReference;
    private FirebaseFirestore db;

    private Intent imageData = null;
    private String productId; // 수정할 상품의 ID

    public ProductRegistrationFragment() {
        // Required empty public constructor
    }

    // 수정할 상품의 ID를 전달받는 생성자
    public ProductRegistrationFragment(String productId) {
        this.productId = productId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_store_productregistration.xml을 이용하여 뷰를 생성
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
        btnRegisterProduct = view.findViewById(R.id.btnRegisterProduct);

        // 이미지 선택 이벤트
        ivCameraIcon.setOnClickListener(v -> openFileChooser());

        // 상품 등록 또는 수정 이벤트
        btnRegisterProduct.setOnClickListener(v -> {
            if (imageData != null) {
                if (productId != null) {
                    updateProduct(imageData); // 수정할 경우
                } else {
                    uploadProduct(imageData); // 새로 등록할 경우
                }
            } else {
                Toast.makeText(getActivity(), "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 수정할 상품의 정보 불러오기
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

    private void loadProductData(String productId) {
        db.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            etTitle.setText(product.getTitle());
                            etPrice.setText(product.getPrice());
                            etDescription.setText(product.getDescription());
                            // 이미지 수는 따로 처리할 수 있습니다.
                        }
                    } else {
                        Toast.makeText(getActivity(), "상품 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductRegistrationFragment", "상품 정보 로드 실패", e);
                    Toast.makeText(getActivity(), "상품 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadProduct(Intent data) {
        String title = etTitle.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        if (!title.isEmpty() && !price.isEmpty() && !description.isEmpty() && imageCount > 0) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // 현재 로그인된 사용자 가져오기
            if (user != null) {
                String userId = user.getUid(); // 사용자의 고유 ID 가져오기
                String productId = db.collection("products").document().getId(); // 새로운 문서 ID 생성

                Product product = new Product(productId, userId, title, price, description); // 사용자 ID를 Product 객체에 저장
                db.collection("products").document(productId).set(product)
                        .addOnSuccessListener(aVoid -> {
                            // 이미지 스토리지에 업로드
                            for (int i = 0; i < imageCount; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                StorageReference fileReference = storageReference.child(productId + "/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
                                fileReference.putFile(imageUri)
                                        .addOnSuccessListener(taskSnapshot -> {
                                            // 업로드 성공 처리
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("ProductRegistrationFragment", "이미지 업로드 실패", e);
                                        });
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

    private void updateProduct(Intent data) {
        String title = etTitle.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        if (!title.isEmpty() && !price.isEmpty() && !description.isEmpty()) {
            Product updatedProduct = new Product(productId, FirebaseAuth.getInstance().getCurrentUser().getUid(), title, price, description);
            db.collection("products").document(productId).set(updatedProduct)
                    .addOnSuccessListener(aVoid -> {
                        // 이미지 스토리지에 업로드
                        for (int i = 0; i < imageCount; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            StorageReference fileReference = storageReference.child(productId + "/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
                            fileReference.putFile(imageUri)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        // 업로드 성공 처리
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ProductRegistrationFragment", "이미지 업로드 실패", e);
                                    });
                        }

                        Toast.makeText(getActivity(), "상품이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "상품 수정에 실패했습니다.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getActivity(), "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // 파일 확장자 가져오기
    private String getFileExtension(Uri uri) {
        // ContentResolver를 사용하여 파일 확장자를 가져옵니다.
        return "jpg"; // 예시로 "jpg"를 반환합니다. 실제 구현에서는 uri에 따른 실제 확장자를 반환해야 합니다.
    }
}