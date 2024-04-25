package com.example.team_project.Store;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.R;

public class ProductRegistrationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_store_productregistration.xml을 이용하여 뷰를 생성
        View view = inflater.inflate(R.layout.fragment_store_productregistration, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar_product_registration);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        // 툴바에 뒤로가기 버튼 추가
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        // 뒤로가기 버튼 클릭 이벤트 처리
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

}