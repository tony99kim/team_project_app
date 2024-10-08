package com.example.team_project.Environment.Store;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailFragment extends Fragment {

    private String productId, userId, title, price, description;
    private TextView titleTextView, descriptionTextView, priceTextView;
    private Button buttonFavorite;

    public static ProductDetailFragment newInstance(Product product) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString("productId", product.getProductId());
        args.putString("userId", product.getUserId());
        args.putString("title", product.getTitle());
        args.putString("price", product.getPrice());
        args.putString("description", product.getDescription());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            userId = getArguments().getString("userId");
            title = getArguments().getString("title");
            price = getArguments().getString("price");
            description = getArguments().getString("description");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment_store_productdetail, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar_product_detail);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        // 뷰 초기화
        titleTextView = view.findViewById(R.id.textView_product_title);
        descriptionTextView = view.findViewById(R.id.textView_product_description);
        priceTextView = view.findViewById(R.id.textView_product_price);
        buttonFavorite = view.findViewById(R.id.button_favorite);

        // 데이터 설정
        titleTextView.setText(title);
        descriptionTextView.setText(description);
        priceTextView.setText(price);

        // 이벤트 설정
        buttonFavorite.setOnClickListener(v -> addToWishlist());

        return view;
    }

    private void addToWishlist() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference wishlistRef = db.collection("wishlists").document(user.getUid()).collection("products").document(productId);

            wishlistRef.set(new Product(productId, userId, title, price, description))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "관심상품에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "관심상품 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}