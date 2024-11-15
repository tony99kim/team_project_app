package com.example.team_project.Profile.Authentication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.team_project.Environment.Point.PointAuthentication;
import com.example.team_project.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationPostDetailFragment extends Fragment {

    private static final String ARG_AUTHENTICATION = "authentication";

    private PointAuthentication authentication;
    private ViewPager2 viewPager2;
    private TextView titleTextView, timestampTextView, statusTextView, descriptionTextView;

    public static AuthenticationPostDetailFragment newInstance(PointAuthentication authentication) {
        AuthenticationPostDetailFragment fragment = new AuthenticationPostDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_AUTHENTICATION, authentication);
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
            authentication = (PointAuthentication) getArguments().getSerializable(ARG_AUTHENTICATION);
        }

        titleTextView = view.findViewById(R.id.textViewTitle);
        timestampTextView = view.findViewById(R.id.textViewTimestamp);
        statusTextView = view.findViewById(R.id.textViewStatus);
        descriptionTextView = view.findViewById(R.id.textViewDescription);
        viewPager2 = view.findViewById(R.id.viewPager_images);

        if (authentication != null) {
            titleTextView.setText(authentication.getTitle());
            timestampTextView.setText(authentication.getTimestamp());
            statusTextView.setText(authentication.getStatus());
            descriptionTextView.setText(authentication.getDescription());

            loadImageFromFirebase(authentication.getId());
        }

        return view;
    }

    private void loadImageFromFirebase(String authenticationId) {
        String directoryPath = "PointAuthenticationImages/" + authenticationId;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(directoryPath);

        storageReference.listAll().addOnSuccessListener(listResult -> {
            List<String> imageUrls = new ArrayList<>();
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    if (imageUrls.size() == listResult.getItems().size()) {
                        setupViewPager(imageUrls);
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    private void setupViewPager(List<String> imageUrls) {
        viewPager2.setAdapter(new ViewPagerAdapter(imageUrls));
    }

    private static class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {
        private final List<String> imageUrls;

        public ViewPagerAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_authentication_post_detail_item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);
            Glide.with(holder.imageView.getContext())
                    .load(imageUrl)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}