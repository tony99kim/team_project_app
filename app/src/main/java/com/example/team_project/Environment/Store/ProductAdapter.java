package com.example.team_project.Environment.Store;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private String userId;
    private boolean isWishlist; // 관심상품인지 여부

    // 생성자
    public ProductAdapter(Context context, ArrayList<Product> productList, String userId, boolean isWishlist) {
        this.context = context;
        this.productList = productList;
        this.userId = userId;
        this.isWishlist = isWishlist; // 관심상품 여부 초기화
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productTitleTextView.setText(product.getTitle());
        holder.productPriceTextView.setText(product.getPrice());

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

        // 삭제 버튼 설정
        holder.deleteButton.setVisibility(isWishlist ? View.VISIBLE : View.GONE); // 관심상품일 때만 보이도록 설정
        holder.deleteButton.setOnClickListener(v -> {
            deleteProduct(product, position);
        });

        // 상품 클릭 이벤트 리스너 설정
        holder.itemView.setOnClickListener(v -> {
            ProductDetailFragment productDetailFragment = ProductDetailFragment.newInstance(product);
            replaceFragment(productDetailFragment);
        });
    }

    private void deleteProduct(Product product, int position) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("wishlists").document(userId).collection("products")
                .document(product.getProductId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    productList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "상품이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "상품 삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productTitleTextView;
        TextView productPriceTextView;
        Button deleteButton; // 삭제 버튼 추가

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productTitleTextView = itemView.findViewById(R.id.productTitleTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            deleteButton = itemView.findViewById(R.id.delete_button); // 삭제 버튼 초기화
        }
    }

    // Fragment 교체를 위한 메서드
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
