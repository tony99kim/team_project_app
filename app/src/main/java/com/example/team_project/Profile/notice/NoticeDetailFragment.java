package com.example.team_project.Profile.notice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.team_project.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class NoticeDetailFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_CONTENT = "content";
    private static final String ARG_IMAGE_URL = "imageUrl";
    private static final String ARG_CREATED_AT = "createdAt";

    public static NoticeDetailFragment newInstance(String title, String content, String imageUrl, String createdAt) {
        NoticeDetailFragment fragment = new NoticeDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putString(ARG_CREATED_AT, createdAt);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_notice_detail, container, false);

        // 툴바 설정
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            Toolbar toolbar = view.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 활성화
            activity.getSupportActionBar().setHomeButtonEnabled(true);
            activity.getSupportActionBar().setTitle("공지사항"); // 툴바 타이틀 설정

            // 툴바의 뒤로가기 버튼 클릭 리스너 설정
            toolbar.setNavigationOnClickListener(v -> {
                if (activity != null) {
                    activity.getSupportFragmentManager().popBackStack();
                }
            });
        }

        TextView titleTextView = view.findViewById(R.id.titleTextView);
        TextView dateTextView = view.findViewById(R.id.dateTextView);
        TextView contentTextView = view.findViewById(R.id.contentTextView);
        ImageView noticeImageView = view.findViewById(R.id.noticeImageView);

        if (getArguments() != null) {
            String title = getArguments().getString(ARG_TITLE);
            String content = getArguments().getString(ARG_CONTENT);
            String imageUrl = getArguments().getString(ARG_IMAGE_URL);
            String createdAt = getArguments().getString(ARG_CREATED_AT);

            titleTextView.setText(title);
            dateTextView.setText(createdAt);
            contentTextView.setText(content);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                noticeImageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageUrl).into(noticeImageView);
            } else {
                noticeImageView.setVisibility(View.GONE);
            }
        }

        return view;
    }
}