package com.example.team_project.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.team_project.Environment.Store.Product;
import com.example.team_project.Environment.Store.ProductDetailFragment;
import com.example.team_project.LoginActivity;
import com.example.team_project.Profile.CustomerService.CustomerServiceFragment;
import com.example.team_project.Profile.Wishlist.WishlistFragment;
import com.example.team_project.Profile.Wishlist.WishpostFragment;
import com.example.team_project.Profile.Wishlist.WithdrawFragment;
import com.example.team_project.Profile.notice.NoticeFragment;
import com.example.team_project.Profile.writtenproduct.WrittenProductFragment;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProfileFragment extends Fragment {

    private TextView usernameTextView, environmentPointsTextView, tvEnvironmentPoints, tvAccountBalance;
    private ImageView profileImageView;
    private Button payrecharge, wishpostButton, wishlistButton, editButton, recentVisitButton, noticeButton, customerServiceButton, logoutButton, withdrawButton;
    private Button writtenProductButton;  // 작성한 상품 버튼
    private androidx.appcompat.widget.Toolbar toolbar;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private String userId;

    private ListView recentVisitListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> recentVisitList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        storageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recentVisitListView = view.findViewById(R.id.recentVisitListView);
        recentVisitList = loadRecentVisits();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, recentVisitList);
        recentVisitListView.setAdapter(adapter);

        // 아이템 클릭 리스너 설정
        recentVisitListView.setOnItemClickListener((parent, view1, position, id) -> {
            String productName = recentVisitList.get(position);
            openProductDetailFragment(productName);
        });

        // UI 요소 초기화
        profileImageView = view.findViewById(R.id.profileImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        environmentPointsTextView = view.findViewById(R.id.environmentPointsTextView);
        tvEnvironmentPoints = view.findViewById(R.id.tvEnvironmentPoints);
        tvAccountBalance = view.findViewById(R.id.tvAccountBalance);
        payrecharge = view.findViewById(R.id.payrecharge);
        noticeButton = view.findViewById(R.id.noticeButton);
        customerServiceButton = view.findViewById(R.id.customerServiceButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        withdrawButton = view.findViewById(R.id.withdrawButton);
        editButton = view.findViewById(R.id.editButton);
        wishlistButton = view.findViewById(R.id.wishlistButton);
        wishpostButton = view.findViewById(R.id.wishpostButton);
        toolbar = view.findViewById(R.id.toolbar);
        writtenProductButton = view.findViewById(R.id.writtenProductButton);  // 작성한 상품 버튼 초기화

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
        payrecharge.setOnClickListener(v -> openFragment(new PayrechargeFragment()));
        noticeButton.setOnClickListener(v -> openFragment(new NoticeFragment()));
        customerServiceButton.setOnClickListener(v -> openFragment(new CustomerServiceFragment()));
        withdrawButton.setOnClickListener(v -> openFragment(new WithdrawFragment()));
        editButton.setOnClickListener(v -> openFragment(new EditButtonFragment()));
        wishlistButton.setOnClickListener(v -> openFragment(new WishlistFragment()));
        wishpostButton.setOnClickListener(v -> openFragment(new WishpostFragment()));

        // 작성한 상품 버튼 클릭 시 이동
        writtenProductButton.setOnClickListener(v -> openWrittenProductFragment());

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
        return new Product("productId", "userId", productName, "10000", "상품 설명");
    }

    private ArrayList<String> loadRecentVisits() {
        Set<String> set = sharedPreferences.getStringSet("recentVisitSet", new LinkedHashSet<>());
        return new ArrayList<>(set != null ? set : new LinkedHashSet<>());
    }

    private void saveRecentVisits() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = new LinkedHashSet<>(recentVisitList);
        editor.putStringSet("recentVisitSet", set);
        editor.apply();
    }

    public void addRecentVisit(String visit) {
        Log.d("ProfileFragment", "Adding visit: " + visit);
        recentVisitList.add(0, visit);
        adapter.notifyDataSetChanged();
        saveRecentVisits();
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
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // 백 스택 초기화
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // 백 스택에 추가하여 뒤로가기 가능
        transaction.commit();
    }

    private void openWrittenProductFragment() {
        // 작성한 상품 프래그먼트로 이동
        WrittenProductFragment writtenProductFragment = new WrittenProductFragment();
        openFragment(writtenProductFragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUsernameFromFirebase(); // Firebase에서 닉네임을 가져와서 설정
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
        StorageReference profileImageRef = storageRef.child("profileImage/" + userId + "/");

        profileImageRef.listAll().addOnSuccessListener(listResult -> {
            if (listResult.getItems().size() > 0) {
                StorageReference firstImageRef = listResult.getItems().get(0);
                firstImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String profileImageUrl = uri.toString();
                    Glide.with(requireContext())
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(profileImageView);
                }).addOnFailureListener(e -> {
                    profileImageView.setImageDrawable(null);
                    Toast.makeText(getActivity(), "프로필 사진을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
            } else {
                profileImageView.setImageDrawable(null);
                Toast.makeText(getActivity(), "프로필 사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            profileImageView.setImageDrawable(null);
            Toast.makeText(getActivity(), "프로필 사진 폴더를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
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
}
