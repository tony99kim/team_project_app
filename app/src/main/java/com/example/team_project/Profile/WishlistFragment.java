package com.example.team_project.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.example.team_project.Environment.Store.Product;
import com.example.team_project.Environment.Store.ProductAdapter;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class WishlistFragment extends Fragment {

    private RecyclerView wishlistRecyclerView;
    private ProductAdapter productAdapter;
    private ArrayList<Product> wishlist;
    private FirebaseFirestore firestore;
    private TextView textNoWishlist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_wishlist, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        // 뒤로가기 버튼 활성화
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // 툴바 제목 설정
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("관심상품목록");
        }

        // 뒤로가기 버튼 클릭 리스너
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // RecyclerView 및 초기화
        wishlistRecyclerView = view.findViewById(R.id.recycler_view_wishlist);
        textNoWishlist = view.findViewById(R.id.text_no_wishlist);
        wishlist = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null; // 사용자 ID 가져오기
        productAdapter = new ProductAdapter(getContext(), wishlist, userId, true); // 사용자 ID 및 관심상품 여부 전달
        wishlistRecyclerView.setAdapter(productAdapter);
        wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance();
        loadWishlist();

        return view;
    }

    private void loadWishlist() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            firestore.collection("wishlists").document(userId).collection("products")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            wishlist.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Product product = document.toObject(Product.class);
                                wishlist.add(product);
                            }
                            productAdapter.notifyDataSetChanged();

                            // 찜 목록이 비어있으면 메시지 표시
                            if (wishlist.isEmpty()) {
                                textNoWishlist.setVisibility(View.VISIBLE);
                            } else {
                                textNoWishlist.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(getContext(), "관심상품 로드 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "로그인 후 찜한 상품을 볼 수 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
