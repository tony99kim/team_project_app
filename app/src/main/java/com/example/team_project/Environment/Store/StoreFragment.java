package com.example.team_project.Environment.Store;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.R;

public class StoreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_environment_store 레이아웃으로 뷰 인플레이션
        View view = inflater.inflate(R.layout.fragment_environment_store, container, false);

        // 상품 등록 버튼 초기화 및 클릭 이벤트 설정
        Button addProductButton = view.findViewById(R.id.addProductButton);
        addProductButton.setOnClickListener(v ->            // 상품 등록 페이지로 이동
            replaceFragment(new ProductRegistrationFragment())
        );

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_right
        );
        // fragment_container ID를 가진 뷰를 찾아 프래그먼트 교체
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // 프래그먼트 스택에 추가
        transaction.commit();
    }
}
