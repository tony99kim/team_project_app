package com.example.team_project.Environment.Store.Payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import com.example.team_project.MainActivity;
import com.example.team_project.R;

public class PaymentCompleteFragment extends Fragment {

    private Button homeButton, backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment_store_product_payment_complete, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar_payment_complete);
        toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        homeButton = view.findViewById(R.id.button_home);
        backButton = view.findViewById(R.id.button_back);

        homeButton.setOnClickListener(v -> {
            // 스토어 페이지로 이동
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("navigateTo", "EnvironmentFragment");
            intent.putExtra("targetPage", 1); // 스토어 페이지를 지정
            startActivity(intent);
            getActivity().finish();
        });

        backButton.setOnClickListener(v -> {
            // 이전 화면으로 이동
            getActivity().onBackPressed();
        });

        return view;
    }
}