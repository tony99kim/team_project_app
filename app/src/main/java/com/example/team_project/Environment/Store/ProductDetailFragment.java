package com.example.team_project.Environment.Store;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.team_project.Chat.ChatData.Chat_ChatData;
import com.example.team_project.R;
import com.example.team_project.Chat.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductDetailFragment extends Fragment {

    private String productId, userId, title, price, description;
    private ViewPager2 viewPager2;
    private TextView titleTextView, descriptionTextView, priceTextView, sellerNameTextView;
    private Button buttonFavorite, buttonChat;
    private Toolbar priceToolBar;

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
        viewPager2 = view.findViewById(R.id.viewPager_images);
        titleTextView = view.findViewById(R.id.textView_product_title);
        descriptionTextView = view.findViewById(R.id.textView_product_description);
        priceTextView = view.findViewById(R.id.textView_product_price);
        sellerNameTextView = view.findViewById(R.id.textView_seller_name);
        buttonFavorite = view.findViewById(R.id.button_favorite);
        buttonChat = view.findViewById(R.id.product_chat_button);

        // 데이터 설정
        titleTextView.setText(title);
        descriptionTextView.setText(description);
        priceTextView.setText(price);

        // 이벤트 설정
        buttonFavorite.setOnClickListener(v -> addToWishlist());
        buttonChat.setOnClickListener(v -> onStartChat());

        loadProductPrice();
        loadProductImages();
        loadSellerName(); // 판매자 이름 불러오기 함수 호출

        return view;
    }

    private void loadSellerName() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String sellerName = document.getString("name");
                    sellerNameTextView.setText(sellerName);
                } else {
                    Log.d("ProductDetailFragment", "문서가 존재하지 않습니다");
                }
            } else {
                Log.d("ProductDetailFragment", "문서 가져오기 실패: ", task.getException());
            }
        });
    }

    private void loadProductImages() {
        String directoryPath = "ProductImages/" + productId;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(directoryPath);

        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<String> imageUrls = new ArrayList<>();
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    if (imageUrls.size() == listResult.getItems().size()) {
                        setupViewPager(imageUrls);
                    }
                });
            }
        }).addOnFailureListener(e -> Log.e("ProductDetailFragment", "이미지 목록 가져오기 실패", e));
    }

    private void setupViewPager(List<String> imageUrls) {
        viewPager2.setAdapter(new ViewPagerAdapter(imageUrls));
    }

    private void loadProductPrice() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference productRef = db.collection("products").document(productId);

        productRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String price = document.getString("price");
                    priceTextView.setText(price);
                } else {
                    Log.d("ProductDetailFragment", "문서가 존재하지 않습니다");
                }
            } else {
                Log.d("ProductDetailFragment", "문서 가져오기 실패: ", task.getException());
            }
        });
    }

    private void addToWishlist() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference wishlistRef = db.collection("wishlists").document(user.getUid());

            wishlistRef.update("products", FieldValue.arrayUnion(productId))
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "위시리스트에 추가되었습니다", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Log.e("ProductDetailFragment", "위시리스트 추가 실패", e));
        }
    }

    private void onStartChat() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String currentUserId = currentUser.getUid();
            String chatRoomId = currentUserId.compareTo(userId) < 0 ? currentUserId + "_" + userId : userId + "_" + currentUserId;

            db.collection("chats").document(chatRoomId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        Chat_ChatData newChat = new Chat_ChatData(chatRoomId, currentUserId, userId, sellerNameTextView.getText().toString(), new Date());
                        db.collection("chats").document(chatRoomId).set(newChat);
                    }
                    navigateToChatRoom(chatRoomId, sellerNameTextView.getText().toString());
                } else {
                    Log.e("ProductDetailFragment", "채팅 문서 가져오기 실패", task.getException());
                }
            });
        }
    }

    private void navigateToChatRoom(String chatRoomId, String userName) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("userEmail1", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        intent.putExtra("userEmail2", userId);
        intent.putExtra("chatRoomTitle", userName); // 채팅방 제목으로 판매자 이름 전달
        startActivity(intent);
    }

    private static class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {
        private final List<String> imageUrls;

        public ViewPagerAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_environment_store_productdetail_item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);
            Glide.with(holder.imageView.getContext())
                    .load(imageUrl)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.productImageView);
            }
        }
    }
}