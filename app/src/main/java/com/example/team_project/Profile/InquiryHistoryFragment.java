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

import com.example.team_project.R;
import com.google.android.material.button.MaterialButton;

public class InquiryHistoryFragment extends Fragment {

    private View rootView;
    private MaterialButton btnBackToInquiry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile_customerservice_inquiry_history, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("문의 내역 확인");
        }

        // 뒤로 가기 버튼 클릭 리스너
        btnBackToInquiry = rootView.findViewById(R.id.btn_back_to_inquiry);
        btnBackToInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 문의하기 화면으로 돌아가기
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return rootView;
    }
}
