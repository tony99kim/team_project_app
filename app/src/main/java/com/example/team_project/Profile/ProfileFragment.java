package com.example.team_project.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.team_project.Environment.Store.Product.Product;
import com.example.team_project.Environment.Store.Product.ProductDetailFragment;
import com.example.team_project.LoginActivity;
import com.example.team_project.Profile.Authentication.AuthenticationPostFragment;
import com.example.team_project.Profile.CustomerService.CustomerServiceFragment;
import com.example.team_project.Profile.Pay.PayExchangeFragment;
import com.example.team_project.Profile.Pay.PayRechargeFragment;
import com.example.team_project.Profile.Pay.TransactionHistoryFragment;
import com.example.team_project.Profile.Wishlist.WishlistFragment;
import com.example.team_project.Profile.Wishlist.WishpostFragment;
import com.example.team_project.Profile.Wishlist.WithdrawFragment;
import com.example.team_project.Profile.event.EventFragment;
import com.example.team_project.Profile.notice.NoticeFragment;
import com.example.team_project.Profile.writtenproduct.WrittenProductFragment;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private TextView usernameTextView, environmentPointsTextView, tvEnvironmentPoints, tvAccountBalance;
    private ImageView profileImageView;
    private Button payrecharge, payExchangeButton, wishpostButton, wishlistButton, editButton, noticeButton, customerServiceButton, logoutButton, withdrawButton;
    private Button writtenProductButton;  // 작성한 상품 버튼
    private Button eventButton;  // 이벤트 버튼 추가
    private Button myAuthenticationPostsButton; // 내 인증글 버튼
    private Button appIntroductionButton; // 앱 소개 버튼 추가
    private androidx.appcompat.widget.Toolbar toolbar;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        storageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return view;
        }

        // UI 요소 초기화
        profileImageView = view.findViewById(R.id.profileImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        environmentPointsTextView = view.findViewById(R.id.environmentPointsTextView);
        tvEnvironmentPoints = view.findViewById(R.id.tvEnvironmentPoints);
        tvAccountBalance = view.findViewById(R.id.tvAccountBalance);
        payrecharge = view.findViewById(R.id.payRecharge);
        payExchangeButton = view.findViewById(R.id.payExchange);
        noticeButton = view.findViewById(R.id.noticeButton);
        customerServiceButton = view.findViewById(R.id.customerServiceButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        withdrawButton = view.findViewById(R.id.withdrawButton);
        editButton = view.findViewById(R.id.editButton);
        wishlistButton = view.findViewById(R.id.wishlistButton);
        wishpostButton = view.findViewById(R.id.wishpostButton);
        toolbar = view.findViewById(R.id.toolbar);
        writtenProductButton = view.findViewById(R.id.writtenProductButton);  // 작성한 상품 버튼 초기화
        eventButton = view.findViewById(R.id.eventButton);  // 이벤트 버튼 초기화
        myAuthenticationPostsButton = view.findViewById(R.id.myAuthenticationPostsButton); // 내 인증글 버튼 초기화
        appIntroductionButton = view.findViewById(R.id.appIntroductionButton); // 앱 소개 버튼 초기화

        setUsername();
        setProfileImageFromFirebase();
        loadenvironmentPoints();
        loadAccountBalance();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setTitle("프로필");
        }

        // 각 버튼 클릭 리스너 설정
        logoutButton.setOnClickListener(v -> logout());
        payrecharge.setOnClickListener(v -> openFragment(new PayRechargeFragment()));
        payExchangeButton.setOnClickListener(v -> openFragment(new PayExchangeFragment()));
        noticeButton.setOnClickListener(v -> openFragment(new NoticeFragment()));
        customerServiceButton.setOnClickListener(v -> openFragment(new CustomerServiceFragment()));
        withdrawButton.setOnClickListener(v -> openFragment(new WithdrawFragment()));
        editButton.setOnClickListener(v -> openFragment(new EditButtonFragment()));
        wishlistButton.setOnClickListener(v -> openFragment(new WishlistFragment()));
        wishpostButton.setOnClickListener(v -> openFragment(new WishpostFragment()));
        eventButton.setOnClickListener(v -> openFragment(new EventFragment()));  // 이벤트 버튼 클릭 리스너 설정
        myAuthenticationPostsButton.setOnClickListener(v -> openFragment(new AuthenticationPostFragment())); // 내 인증글 버튼 클릭 리스너 설정
        appIntroductionButton.setOnClickListener(v -> openFragment(new AppIntroductionFragment())); // 앱 소개 버튼 클릭 리스너 설정

        // 작성한 상품 버튼 클릭 시 이동
        writtenProductButton.setOnClickListener(v -> openWrittenProductFragment());

        // userPayLayout 클릭 리스너 설정
        RelativeLayout userPayLayout = view.findViewById(R.id.userPayLayout);
        userPayLayout.setOnClickListener(v -> openTransactionHistoryFragment());

        return view;
    }

    private void setUsername() {
        // 사용자 이름을 설정하는 메서드
    }

    private void openProductDetailFragment(String productName) {
        Product product = findProductByName(productName);
        ProductDetailFragment productDetailFragment = ProductDetailFragment.newInstance(product);
        openFragment(productDetailFragment);
    }

    private Product findProductByName(String productName) {
        // 예시로 "isBusiness"를 false로 설정, 실제로는 조건에 맞게 값을 설정해야 함
        boolean isBusiness = false; // 예시 값, 실제 로직에 맞게 변경

        // "productName"에 해당하는 상품을 찾아서 반환
        return new Product("productId", "userId", productName, "10000", "상품 설명", isBusiness);
    }

    private void logout() {
        SharedPreferences prefs = getActivity().getSharedPreferences("team_project_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("autoLogin", false);
        editor.apply();
        mAuth.signOut();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void openFragment(Fragment fragment) {
        replaceFragment(fragment);
    }

    private void openWrittenProductFragment() {
        // 작성한 상품 프래그먼트로 이동
        WrittenProductFragment writtenProductFragment = new WrittenProductFragment();
        openFragment(writtenProductFragment);
    }

    private void openTransactionHistoryFragment() {
        // TransactionHistoryFragment로 이동
        TransactionHistoryFragment transactionHistoryFragment = new TransactionHistoryFragment();
        openFragment(transactionHistoryFragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            setUsernameFromFirebase(); // Firebase에서 닉네임을 가져와서 설정
        } else {
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void setUsernameFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        if (username != null && !username.isEmpty()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", username);
                            editor.apply();
                            usernameTextView.setText(username);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "닉네임을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    private void setProfileImageFromFirebase() {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference profileImageRef = storageRef.child("profileImage/" + userId + "/"); // 폴더 경로만 설정

        profileImageRef.listAll().addOnSuccessListener(listResult -> {
            if (listResult.getItems().size() > 0) {
                // 첫 번째 이미지 가져오기
                StorageReference firstImageRef = listResult.getItems().get(0);
                firstImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String profileImageUrl = uri.toString();
                    if (getContext() != null) {
                        Glide.with(getContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(profileImageView);
                    }
                }).addOnFailureListener(e -> {
                    // 이미지 URL 가져오기 실패 시
                    profileImageView.setImageDrawable(null); // 이미지 제거
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "프로필 사진을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // 이미지가 없는 경우 처리
                profileImageView.setImageDrawable(null); // 이미지 제거
                if (getContext() != null) {
                    Toast.makeText(getContext(), "프로필 사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> {
            // 폴더 목록 가져오기 실패 시
            profileImageView.setImageDrawable(null); // 이미지 제거
            if (getContext() != null) {
                Toast.makeText(getContext(), "프로필 사진 폴더를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadenvironmentPoints() {
        DocumentReference docRef = db.collection("users").document(userId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long environmentPoint = documentSnapshot.getLong("environmentPoint");

                if (environmentPoint != null) {
                    tvEnvironmentPoints.setText(String.valueOf(environmentPoint));
                } else {
                    tvEnvironmentPoints.setText("0");
                }
            } else {
                tvEnvironmentPoints.setText("0");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "환경 포인트를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadAccountBalance() {
        DocumentReference docRef = db.collection("users").document(userId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long accountBalance = documentSnapshot.getLong("accountBalance");

                if (accountBalance != null) {
                    tvAccountBalance.setText(String.valueOf(accountBalance));
                } else {
                    tvAccountBalance.setText("0");
                }
            } else {
                tvAccountBalance.setText("0");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "계좌 잔액을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
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