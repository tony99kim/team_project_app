package com.example.team_project.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RecentVisitFragment extends Fragment {

    private ListView recentVisitListView;
    private ArrayAdapter<String> adapter;
    private List<String> recentVisitProducts;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_recent_visit_fragment, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("최근방문");
        }

        recentVisitListView = view.findViewById(R.id.recentVisitListView);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 사용자의 최근 방문 상품 가져오기
        getRecentVisits();

        // 리스트뷰 아이템 클릭 이벤트 처리
        recentVisitListView.setOnItemClickListener((parent, view1, position, id) -> {
            // 클릭한 상품의 이름을 가져옵니다.
            String productName = adapter.getItem(position);
            if (productName != null) {
                // 여기에 클릭한 상품에 대한 추가 동작을 수행할 수 있습니다.
                // 예를 들어, 상품 상세 페이지로 이동하거나 특정 작업을 수행할 수 있습니다.
                Toast.makeText(getActivity(), "클릭한 상품: " + productName, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void getRecentVisits() {
        // 현재 사용자의 ID 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Firestore에서 사용자의 최근 방문 상품 가져오기
            db.collection("recentVisits").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // 최근 방문 상품이 존재하면 가져옵니다.
                            List<String> visits = (List<String>) documentSnapshot.get("visits");
                            if (visits != null && !visits.isEmpty()) {
                                // 최근 방문 상품 리스트를 업데이트합니다.
                                updateRecentVisitList(visits);
                            } else {
                                // 최근 방문 상품이 없는 경우 처리할 내용을 추가하세요.
                                Toast.makeText(getActivity(), "최근 방문 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 최근 방문 상품 문서가 존재하지 않는 경우 처리할 내용을 추가하세요.
                            Toast.makeText(getActivity(), "최근 방문 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 최근 방문 상품 가져오기에 실패한 경우 처리할 내용을 추가하세요.
                        Toast.makeText(getActivity(), "최근 방문 상품을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateRecentVisitList(List<String> visits) {
        recentVisitProducts = visits;
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, recentVisitProducts);
        recentVisitListView.setAdapter(adapter);
    }
}
