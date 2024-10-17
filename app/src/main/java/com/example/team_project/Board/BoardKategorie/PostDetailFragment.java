package com.example.team_project.Board.BoardKategorie;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class PostDetailFragment extends Fragment {

    private String postId, postName, postTitle, postContent, userId;

    private ViewPager2 viewPager2;
    private TextView titleTextView, contentTextView, posterNameTextView;
    private Toolbar toolbar;

    public static PostDetailFragment newInstance(String postId, String postName, String postTitle, String postContent) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        args.putString("postName", postName);
        args.putString("postTitle", postTitle);
        args.putString("postContent", postContent);
        fragment.setArguments(args);
        return fragment;
    }

    public static PostDetailFragment newInstance(Post post) {
        String postId = post.getImageUrls() != null && !post.getImageUrls().isEmpty() ? post.getImageUrls().get(0) : null;
        return newInstance(postId, post.getName(), post.getTitle(), post.getContent());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            postName = getArguments().getString("postName");
            postTitle = getArguments().getString("postTitle");
            postContent = getArguments().getString("postContent");
        }

        // userId 초기화
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board_postdetail, container, false);

        // 툴바 설정
        toolbar = view.findViewById(R.id.toolbar_post_detail);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        // 뷰 초기화
        viewPager2 = view.findViewById(R.id.viewPager_images);
        titleTextView = view.findViewById(R.id.textView_post_title);
        contentTextView = view.findViewById(R.id.textView_post_content);
        posterNameTextView = view.findViewById(R.id.textView_poster_name);

        // 데이터 설정
        titleTextView.setText(postTitle);
        contentTextView.setText(postContent);

        loadPostImages();
        loadPosterName();

        return view;
    }

    private void loadPosterName() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String posterName = document.getString("name");
                    posterNameTextView.setText(posterName);
                } else {
                    Log.d("PostDetailFragment", "문서가 존재하지 않습니다");
                }
            } else {
                Log.d("PostDetailFragment", "문서 가져오기 실패: ", task.getException());
            }
        });
    }

    private void loadPostImages() {
        String directoryPath = "PostImages/" + postId;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(directoryPath);

        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<String> imageUrls = new ArrayList<>();
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    Log.d("PostDetailFragment", "가져온 이미지 URL: " + uri.toString()); // URL 로그 추가
                    if (imageUrls.size() == listResult.getItems().size()) {
                        setupViewPager(imageUrls);
                    }
                });
            }
        }).addOnFailureListener(e -> Log.e("PostDetailFragment", "이미지 목록 가져오기 실패", e));
    }

    private void setupViewPager(List<String> imageUrls) {
        if (!imageUrls.isEmpty()) { // 이미지 URL 목록이 비어있지 않은지 확인
            viewPager2.setAdapter(new ViewPagerAdapter(imageUrls));
        } else {
            Log.d("PostDetailFragment", "이미지 URL 목록이 비어 있습니다");
        }
    }

    private static class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {
        private final List<String> imageUrls;

        public ViewPagerAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_board_postdetail_item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);
            Glide.with(holder.imageView.getContext())
                    .load(imageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("ViewPagerAdapter", "이미지 로드 실패: " + e);
                            return false; // 오류를 허용
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false; // 리소스를 Glide가 처리하도록 허용
                        }
                    })
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
                imageView = itemView.findViewById(R.id.postImageView);
            }
        }
    }
}
