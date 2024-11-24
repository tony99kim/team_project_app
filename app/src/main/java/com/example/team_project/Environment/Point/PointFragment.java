// PointFragment.java
package com.example.team_project.Environment.Point;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PointFragment extends Fragment {

    private List<PointItem> pointItems = new ArrayList<>();
    private RecyclerView recyclerView;
    private PointAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment_point, container, false);

        recyclerView = view.findViewById(R.id.pointListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new PointAdapter(pointItems);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(v -> {
            int position = recyclerView.getChildAdapterPosition(v);
            PointItem selectedItem = pointItems.get(position);
            // 선택된 아이템을 사용하여 필요한 작업 수행
            replaceFragment(PointAuthenticationFragment.newInstance(selectedItem));
        });

        loadImagesFromFirebaseStorage();

        return view;
    }

    private void loadImagesFromFirebaseStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("AuthenticationIconImages");

        // 기존 리스트를 비워줍니다.
        pointItems.clear();

        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<PointItem> tempItems = new ArrayList<>();
            for (int i = 0; i < listResult.getItems().size(); i++) {
                StorageReference item = listResult.getItems().get(i);
                int finalI = i;
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    // 인덱스를 사용하여 PointItem 생성
                    PointItem pointItem = new PointItem(finalI, uri.toString(), "대기"); // 상태를 대기로 설정
                    tempItems.add(pointItem);
                    // 모든 이미지를 다 불러왔을 때 리스트를 갱신합니다.
                    if (tempItems.size() == listResult.getItems().size()) {
                        pointItems.addAll(tempItems);
                        sortAndFilterItems(); // 정렬 및 필터링 메서드 호출
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(exception -> {
                    // 에러 처리
                });
            }
        }).addOnFailureListener(exception -> {
            // 에러 처리
        });
    }

    private void sortAndFilterItems() {
        // 상태가 "대기"인 항목만 필터링하고 정렬합니다.
        List<PointItem> filteredItems = new ArrayList<>();
        for (PointItem item : pointItems) {
            if ("대기".equals(item.getStatus())) {
                filteredItems.add(item);
            }
        }

        // 정렬 (여기서는 이름 기준으로 정렬)
        Collections.sort(filteredItems, Comparator.comparing(PointItem::getTitle));

        // 기존 리스트를 비워주고 필터링된 리스트로 업데이트
        pointItems.clear();
        pointItems.addAll(filteredItems);
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