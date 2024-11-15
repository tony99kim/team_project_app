package com.example.team_project.Profile.Authentication;

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

import com.example.team_project.Environment.Point.PointAuthentication;
import com.example.team_project.R;

public class AuthenticationPostDetailFragment extends Fragment {

    private static final String ARG_POST = "post";

    private PointAuthentication post;

    public static AuthenticationPostDetailFragment newInstance(PointAuthentication post) {
        AuthenticationPostDetailFragment fragment = new AuthenticationPostDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST, post);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_authentication_post_detail, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(" ");
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        if (getArguments() != null) {
            post = (PointAuthentication) getArguments().getSerializable(ARG_POST);
        }

        TextView titleTextView = view.findViewById(R.id.textViewTitle);
        TextView timestampTextView = view.findViewById(R.id.textViewTimestamp);
        TextView statusTextView = view.findViewById(R.id.textViewStatus);
        TextView descriptionTextView = view.findViewById(R.id.textViewDescription);

        if (post != null) {
            titleTextView.setText(post.getTitle());
            timestampTextView.setText(post.getTimestamp());
            statusTextView.setText(post.getStatus());
            descriptionTextView.setText(post.getDescription());
        }

        return view;
    }
}