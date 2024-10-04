package com.example.team_project.Environment.Store;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class StoreFragment extends Fragment {

    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private ArrayList<Product> productList;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment_store, container, false);

        // 상품 등록 버튼 초기화 및 클릭 이벤트 설정
        Button addProductButton = view.findViewById(R.id.addProductButton);
        addProductButton.setText("상품 등록");
        addProductButton.setOnClickListener(v -> {
            // 상품 등록 페이지로 이동
            replaceFragment(new ProductRegistrationFragment());
        });

        // RecyclerView와 Adapter 초기화
        productsRecyclerView = view.findViewById(R.id.productListRecyclerView); // RecyclerView ID 확인 필요
        productList = new ArrayList<>();

        // 사용자 ID 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null; // 사용자 ID 가져오기

        productAdapter = new ProductAdapter(getContext(), productList, userId, false); // 사용자 ID 및 관심상품 여부 전달
        productsRecyclerView.setAdapter(productAdapter);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Firestore 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance();

        // 제품 데이터 로드
        loadProductsFromFirestore();

        return view;
    }

    private void loadProductsFromFirestore() {
        firestore.collection("products") // Firestore에서 "products" 컬렉션 참조
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear(); // 기존 목록을 클리어
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class); // Document를 Product 객체로 변환
                            productList.add(product); // 목록에 추가
                        }
                        productAdapter.notifyDataSetChanged(); // 데이터 변경 알림
                    } else {
                        // 에러 처리
                        Log.e("StoreFragment", "제품 로드 실패: " + task.getException().getMessage());
                    }
                });
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
