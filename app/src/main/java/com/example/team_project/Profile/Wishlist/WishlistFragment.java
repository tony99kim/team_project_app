package com.example.team_project.Profile.Wishlist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Environment.Store.Product.Product;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class WishlistFragment extends Fragment {

    private RecyclerView wishlistRecyclerView;
    private WishlistAdapter wishlistAdapter;
    private ArrayList<Product> wishlist;
    private FirebaseFirestore firestore;
    private String userId;
    private TextView textNoWishlist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_wishlist, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        // RecyclerView와 Adapter 초기화
        wishlistRecyclerView = view.findViewById(R.id.recycler_view_wishlist);
        textNoWishlist = view.findViewById(R.id.text_no_wishlist);
        wishlist = new ArrayList<>();

        // 사용자 ID 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : null;

        wishlistAdapter = new WishlistAdapter(getContext(), wishlist, userId);
        wishlistRecyclerView.setAdapter(wishlistAdapter);
        wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Firestore 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance();

        // 관심상품 데이터 로드
        loadWishlistFromFirestore();

        return view;
    }

    private void loadWishlistFromFirestore() {
        firestore.collection("wishlists").document(userId).collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        wishlist.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            wishlist.add(product);
                        }
                        wishlistAdapter.notifyDataSetChanged();
                        updateUI();
                    } else {
                        Log.e("WishlistFragment", "관심상품 로드 실패: " + task.getException().getMessage());
                    }
                });
    }

    private void updateUI() {
        if (wishlist.isEmpty()) {
            textNoWishlist.setVisibility(View.VISIBLE);
            wishlistRecyclerView.setVisibility(View.GONE);
        } else {
            textNoWishlist.setVisibility(View.GONE);
            wishlistRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}