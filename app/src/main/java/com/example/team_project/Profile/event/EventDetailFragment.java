package com.example.team_project.Profile.event;

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

public class EventDetailFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_CONTENT = "content";
    private static final String ARG_CREATED_AT = "createdAt";
    private static final String ARG_IMAGE_URL = "imageUrl";

    public static EventDetailFragment newInstance(String title, String content, String createdAt, String imageUrl) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);
        args.putString(ARG_CREATED_AT, createdAt);
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public static EventDetailFragment newInstance(Event event) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, event.getTitle());
        args.putString(ARG_CONTENT, event.getContent());
        args.putString(ARG_CREATED_AT, event.getCreatedAt());
        args.putString(ARG_IMAGE_URL, event.getImageUrl());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_event_detail, container, false);

        // 툴바 설정
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            Toolbar toolbar = view.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 활성화
            activity.getSupportActionBar().setHomeButtonEnabled(true);
            activity.getSupportActionBar().setTitle("이벤트"); // 툴바 타이틀 설정

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
        ImageView eventImageView = view.findViewById(R.id.eventImageView);

        if (getArguments() != null) {
            String title = getArguments().getString(ARG_TITLE);
            String content = getArguments().getString(ARG_CONTENT);
            String createdAt = getArguments().getString(ARG_CREATED_AT);
            String imageUrl = getArguments().getString(ARG_IMAGE_URL);

            titleTextView.setText(title);
            dateTextView.setText(createdAt);
            contentTextView.setText(content);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                eventImageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageUrl).into(eventImageView);
            } else {
                eventImageView.setVisibility(View.GONE);
            }
        }

        return view;
    }
}