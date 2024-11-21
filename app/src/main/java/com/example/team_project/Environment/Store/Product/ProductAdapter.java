package com.example.team_project.Environment.Store.Product;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private boolean isInProfile; // 프로필 화면 여부를 확인하는 변수

    // 생성자
    public ProductAdapter(Context context, ArrayList<Product> productList, boolean isInProfile) {
        this.context = context;
        this.productList = productList;
        this.firestore = FirebaseFirestore.getInstance();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.isInProfile = isInProfile; // 초기화
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_environment_store_product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // 상품 제목 동적 설정
        if (product.isBusiness()) { // isBusiness()는 Product 클래스에 있는 메서드라고 가정
            holder.productTitleTextView.setText(product.getTitle() + " (판매상품)");
        } else {
            holder.productTitleTextView.setText(product.getTitle());
        }

        // 가격 설정
        holder.productPriceTextView.setText(product.getPrice());

        // Firebase Storage에서 이미지 로드
        String directoryPath = "ProductImages/" + product.getProductId();
        StorageReference directoryReference = FirebaseStorage.getInstance().getReference().child(directoryPath);
        directoryReference.listAll().addOnSuccessListener(listResult -> {
            if (!listResult.getItems().isEmpty()) {
                StorageReference firstFileRef = listResult.getItems().get(0);
                firstFileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (uri != null && context != null && holder.productImageView != null) {
                        Glide.with(context)
                                .load(uri)
                                .into(holder.productImageView);
                    }
                }).addOnFailureListener(e -> Log.e("ProductAdapter", "첫 번째 이미지 로드 실패", e));
            }
        }).addOnFailureListener(e -> Log.e("ProductAdapter", "파일 목록 가져오기 실패", e));

        // 상품 클릭 시 상세 페이지로 이동
        holder.itemView.setOnClickListener(v -> {
            ProductDetailFragment productDetailFragment = ProductDetailFragment.newInstance(product);
            replaceFragment(productDetailFragment);
        });

        // 버튼 가시성 설정
        if (isInProfile && product.getUserId().equals(currentUserId)) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }

        // 수정 및 삭제 버튼 클릭 리스너 설정
        holder.editButton.setOnClickListener(v -> navigateToEditProduct(product.getProductId()));
        holder.deleteButton.setOnClickListener(v -> deleteProduct(product, position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productTitleTextView;
        TextView productPriceTextView;
        Button editButton;
        Button deleteButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productTitleTextView = itemView.findViewById(R.id.productTitleTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private void deleteProduct(Product product, int position) {
        firestore.collection("products").document(product.getProductId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    productList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "상품이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductAdapter", "상품 삭제 실패", e);
                    Toast.makeText(context, "상품 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToEditProduct(String productId) {
        Fragment editProductFragment = new ProductRegistrationFragment(productId);
        replaceFragment(editProductFragment);
    }

    private void replaceFragment(Fragment fragment) {
        ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
