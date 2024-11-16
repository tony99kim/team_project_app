package com.example.team_project.Environment.Store.Product;

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
import com.example.team_project.Chat.Data.Chat;
import com.example.team_project.Environment.Store.Payment.PaymentFragment;
import com.example.team_project.Environment.Store.Product.Product;
import com.example.team_project.R;
import com.example.team_project.Chat.ChatActivity;
import com.example.team_project.Chat.Data.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ProductDetailFragment extends Fragment {

    private String productId, userId, title, price, description;
    private boolean isBusiness; // 기업 여부 추가
    private ViewPager2 viewPager2;
    private TextView titleTextView, descriptionTextView, priceTextView, sellerNameTextView;
    private Button buttonFavorite, buttonChat;
    private FirebaseFirestore db; // Firestore 인스턴스 선언

    public static ProductDetailFragment newInstance(Product product) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString("productId", product.getProductId());
        args.putString("userId", product.getUserId());
        args.putString("title", product.getTitle());
        args.putString("price", product.getPrice());
        args.putString("description", product.getDescription());
        args.putBoolean("isBusiness", product.isBusiness()); // 기업 여부를 전달
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Firestore 인스턴스 초기화
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            userId = getArguments().getString("userId");
            title = getArguments().getString("title");
            price = getArguments().getString("price");
            description = getArguments().getString("description");
            isBusiness = getArguments().getBoolean("isBusiness"); // 기업 여부 받기
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

        if (isBusiness) {
            buttonChat.setText("결제하기");
        } else {
            buttonChat.setText("채팅하기");
        }

        // 이벤트 설정
        buttonFavorite.setOnClickListener(v -> addToWishlist());
        buttonChat.setOnClickListener(v -> {
            if (isBusiness) {
                // "결제하기" 버튼 클릭 시 결제 화면으로 이동
                navigateToPaymentPage();
            } else {
                // "채팅하기" 버튼 클릭 시 채팅방을 만든다
                onStartChat();
            }
        });

        loadProductPrice();
        loadProductImages();
        loadSellerName(); // 판매자 이름 불러오기 함수 호출

        return view;
    }

    private void loadSellerName() {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String sellerName = document.getString("name"); // 이름 필드로 변경
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
            DocumentReference wishlistRef = db.collection("wishlists").document(user.getUid());

            // isBusiness 필드를 추가하여 상품 객체 생성
            Product product = new Product(productId, userId, title, price, description, isBusiness); // 기업 여부 반영
            wishlistRef.collection("products").document(productId)
                    .set(product)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "위시리스트에 추가되었습니다", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Log.e("ProductDetailFragment", "위시리스트 추가 실패", e));
        } else {
            Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onStartChat() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();

            // Firestore에서 판매자 이름 가져오기
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String sellerEmail = document.getString("email"); // 판매자 이메일 가져오기

                        // chatRoomId를 이메일 형식으로 설정
                        String chatRoomId = currentUserEmail + "_" + sellerEmail;

                        // 채팅 문서 가져오기
                        db.collection("chats").document(chatRoomId).get().addOnCompleteListener(chatTask -> {
                            if (chatTask.isSuccessful()) {
                                DocumentSnapshot chatDocument = chatTask.getResult();
                                if (!chatDocument.exists()) {
                                    // 새로운 채팅 생성
                                    Chat newChat = new Chat(chatRoomId, currentUserEmail, sellerEmail, "", new Date());
                                    db.collection("chats").document(chatRoomId).set(newChat)
                                            .addOnSuccessListener(aVoid -> addInitialMessage(chatRoomId, currentUserEmail, sellerEmail))
                                            .addOnFailureListener(e -> Log.e("ProductDetailFragment", "채팅 생성 실패: ", e));
                                }
                                // 채팅방으로 이동
                                navigateToChatRoom(chatRoomId, sellerEmail);
                            } else {
                                Log.e("ProductDetailFragment", "채팅 문서 가져오기 실패", chatTask.getException());
                            }
                        });
                    } else {
                        Log.e("ProductDetailFragment", "판매자 정보가 존재하지 않습니다.");
                    }
                } else {
                    Log.e("ProductDetailFragment", "판매자 정보 가져오기 실패", task.getException());
                }
            });
        }
    }

    private void addInitialMessage(String chatId, String currentUserEmail, String sellerEmail) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 현재 날짜를 "yyyy년 MM월 dd일(E)" 형식으로 포맷합니다.
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일(E)", Locale.KOREAN);
            String currentDate = dateFormat.format(new Date());

            // 초기 메시지 객체를 생성합니다.
            Message initialMessage = new Message(chatId, "system", currentDate, new Date());

            // Firestore에 초기 메시지를 추가합니다.
            db.collection("messages").add(initialMessage).addOnSuccessListener(documentReference -> {
                Log.d("ProductDetailFragment", "초기 메시지 추가 성공");
                // 채팅방의 마지막 메시지 및 업데이트 시간을 설정합니다.
                db.collection("chats").document(chatId)
                        .update("lastMessage", "채팅방이 열렸습니다.", "updatedAt", new Date());
            }).addOnFailureListener(e -> {
                Log.e("ProductDetailFragment", "초기 메시지 추가 실패: ", e);
            });
        });
    }

    private void navigateToChat(String chatRoomId) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("chatRoomId", chatRoomId);
        startActivity(intent);
    }

    private void navigateToPaymentPage() {
        Intent intent = new Intent(getActivity(), PaymentFragment.class); // 결제 페이지로 이동
        intent.putExtra("productId", productId); // 상품 ID 전달
        intent.putExtra("price", price); // 가격 전달
        startActivity(intent);
    }

    private void navigateToChatRoom(String chatRoomId, String userName) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("userEmail1", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        intent.putExtra("userEmail2", userName);

        // 판매자 이름을 추가로 전달
        intent.putExtra("user2", userName); // 판매자 이름 전달

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
