package com.example.team_project.Profile.Wishlist;

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
import com.example.team_project.Environment.Store.Product;
import com.example.team_project.Environment.Store.ProductDetailFragment;
import com.example.team_project.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private Context context;
    private List<Product> productList;
    private String userId;

    public WishlistAdapter(Context context, List<Product> productList, String userId) {
        this.context = context;
        this.productList = productList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_profile_wishlist_item_product, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
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
                }).addOnFailureListener(e -> Log.e("WishlistAdapter", "첫 번째 이미지 로드 실패", e));
            }
        }).addOnFailureListener(e -> Log.e("WishlistAdapter", "파일 목록 가져오기 실패", e));

        holder.deleteButton.setOnClickListener(v -> {
            deleteProduct(product, position);
        });

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

    public static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productTitleTextView;
        TextView productPriceTextView;
        Button deleteButton;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productTitleTextView = itemView.findViewById(R.id.productTitleTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
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