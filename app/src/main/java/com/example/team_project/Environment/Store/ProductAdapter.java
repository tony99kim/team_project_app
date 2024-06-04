package com.example.team_project.Environment.Store;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.List;

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

        directoryReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                if (!listResult.getItems().isEmpty()) {
                    StorageReference firstFileRef = listResult.getItems().get(0);
                    firstFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context)
                                    .load(uri)
                                    .into(holder.productImageView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("ProductAdapter", "첫 번째 이미지 로드 실패", e);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ProductAdapter", "파일 목록 가져오기 실패", e);
            }
        });

        // 상품 클릭 이벤트 리스너 설정
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
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

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productTitleTextView = itemView.findViewById(R.id.productTitleTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
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

}
