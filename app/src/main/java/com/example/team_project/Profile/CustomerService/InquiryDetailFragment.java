package com.example.team_project.Profile.CustomerService;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.team_project.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class InquiryDetailFragment extends Fragment {
    private static final String ARG_INQUIRY = "inquiry";
    private Inquiry inquiry;
    private FirebaseFirestore db;

    public static InquiryDetailFragment newInstance(Inquiry inquiry) {
        InquiryDetailFragment fragment = new InquiryDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_INQUIRY, inquiry);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            inquiry = (Inquiry) getArguments().getSerializable(ARG_INQUIRY);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_customerservice_inquiry_detail, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setTitle("문의 상세");
            }
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white)); // 툴바 텍스트 색상 변경
            toolbar.setNavigationOnClickListener(v -> {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                } else {
                    activity.onBackPressed();
                }
            });
        }

        TextView tvTitle = view.findViewById(R.id.tvInquiryTitle);
        TextView tvContent = view.findViewById(R.id.tvInquiryContent);
        TextView tvResponse = view.findViewById(R.id.tvInquiryResponse);

        tvTitle.setText(inquiry.getTitle());
        tvContent.setText(inquiry.getContent());

        db.collection("inquiries").document(inquiry.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String response = documentSnapshot.getString("response");
                        tvResponse.setText(response != null ? response : "No response yet");
                    }
                });

        return view;
    }
}