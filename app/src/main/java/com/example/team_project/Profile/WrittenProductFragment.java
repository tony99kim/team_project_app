package com.example.team_project.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Environment.Store.Product;
import com.example.team_project.Environment.Store.ProductAdapter;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class WrittenProductFragment extends Fragment {

    private RecyclerView writtenProductRecyclerView;
    private ProductAdapter productAdapter;
    private ArrayList<Product> writtenProductList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_written_product, container, false);

        // 툴바 초기화 및 뒤로가기 버튼 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            activity.getSupportActionBar().setTitle(""); // 툴바 제목 설정
        }

        // 툴바 뒤로가기 버튼 클릭 리스너 설정
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // RecyclerView 설정
        writtenProductRecyclerView = view.findViewById(R.id.writtenProductRecyclerView);
        writtenProductRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        writtenProductList = new ArrayList<>();
        productAdapter = new ProductAdapter(getContext(), writtenProductList);
        writtenProductRecyclerView.setAdapter(productAdapter);

        loadWrittenProducts();

        return view;
    }

    private void loadWrittenProducts() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("products")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    writtenProductList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        writtenProductList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // 실패 처리 코드 추가 가능
                });
    }
}
