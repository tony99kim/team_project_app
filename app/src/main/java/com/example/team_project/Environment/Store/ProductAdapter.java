package com.example.team_project.Environment.Store;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.example.team_project.Profile.ProfileFragment;
import com.example.team_project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    // 생성자
    public ProductAdapter(Context context, ArrayList<Product> productList) {
        this.context = context;
        this.productList = productList;
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

        // 상품 클릭 이벤트 리스너 설정
        holder.itemView.setOnClickListener(v -> {
            // 상품 상세 페이지로 이동
            ProductDetailFragment productDetailFragment = ProductDetailFragment.newInstance(product);
            replaceFragment(productDetailFragment);

            // 최근 방문 목록에 추가
            Fragment currentFragment = ((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof ProfileFragment) {
                ((ProfileFragment) currentFragment).addRecentVisit(product.getTitle());
            } else {
                Log.d("ProductAdapter", "ProfileFragment is not found");
            }
        });

        // 찜하기 버튼 클릭 리스너 설정
        holder.favoriteButton.setOnClickListener(v -> {
            addToWishlist(product);
            Toast.makeText(context, "찜한 상품에 추가되었습니다.", Toast.LENGTH_SHORT).show();
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
        Button favoriteButton; // 찜하기 버튼 추가

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productTitleTextView = itemView.findViewById(R.id.productTitleTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton); // 찜하기 버튼 초기화
        }
    }

    // Fragment 교체를 위한 메서드
    private void replaceFragment(Fragment fragment) {
        // context를 FragmentActivity로 캐스팅하여 getSupportFragmentManager()에 접근
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

    private void addToWishlist(Product product) {
        SharedPreferences prefs = context.getSharedPreferences("wishlist", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 찜한 상품 목록을 추가하는 로직
        Set<String> wishlistSet = prefs.getStringSet("wishlistItems", new HashSet<>());
        wishlistSet.add(product.getTitle()); // 상품 제목을 저장

        // 찜한 상품의 상세 정보 저장
        Set<String> wishlistDetailSet = prefs.getStringSet("wishlistDetails", new HashSet<>());
        String productDetails = product.getTitle() + " - " + product.getPrice() + " - " + product.getDescription();
        wishlistDetailSet.add(productDetails); // 상품 제목, 가격, 설명을 저장

        editor.putStringSet("wishlistItems", wishlistSet);
        editor.putStringSet("wishlistDetails", wishlistDetailSet); // 상세 정보 저장
        editor.apply();
    }
}
