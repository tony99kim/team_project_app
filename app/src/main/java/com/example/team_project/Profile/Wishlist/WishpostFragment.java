package com.example.team_project.Profile.Wishlist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.concurrent.atomic.AtomicBoolean;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Board.BoardKategorie.Post;
import com.example.team_project.Board.BoardKategorie.PostDetailFragment;
import com.example.team_project.Board.BoardKategorie.PostImagesAdapter;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WishpostFragment extends Fragment {

    private LinearLayout bookmarksLayout;
    private TextView noBookmarksMessage;
    private RecyclerView imagesRecyclerView;
    private PostImagesAdapter postImagesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_wishpost, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("관심게시물");
        }

        bookmarksLayout = view.findViewById(R.id.bookmarks_layout);
        noBookmarksMessage = view.findViewById(R.id.no_bookmarks_message);

        // RecyclerView 초기화
        imagesRecyclerView = view.findViewById(R.id.recycler_view_images);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        postImagesAdapter = new PostImagesAdapter(new ArrayList<>());
        imagesRecyclerView.setAdapter(postImagesAdapter);

        loadBookmarkedPosts();

        return view;
    }

    private void loadBookmarkedPosts() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("bookmarks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        AtomicBoolean hasBookmarks = new AtomicBoolean(false); // AtomicBoolean 사용
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String postId = document.getId(); // 현재 문서의 ID를 가져옴

                            // posts 컬렉션에서 해당 postId에 대한 이미지 URL 가져오기
                            db.collection("posts").document(postId).get()
                                    .addOnCompleteListener(postTask -> {
                                        if (postTask.isSuccessful() && postTask.getResult() != null) {
                                            String title = postTask.getResult().getString("title");
                                            String content = postTask.getResult().getString("content");
                                            List<String> imageUrls = (List<String>) postTask.getResult().get("imageUrls");

                                            // Post 객체 생성
                                            Post post = new Post(postId, title, content, "", document.getString("posterName"), imageUrls != null ? imageUrls : new ArrayList<>(), 0, 0);

                                            addBookmarkView(post);
                                            hasBookmarks.set(true); // AtomicBoolean의 값을 true로 설정
                                        }
                                        // 모든 문서 처리가 끝난 후 hasBookmarks 상태에 따라 메시지 표시
                                        if (document.getId().equals(task.getResult().getDocuments().get(task.getResult().size() - 1).getId())) {
                                            noBookmarksMessage.setVisibility(hasBookmarks.get() ? View.GONE : View.VISIBLE);
                                        }
                                    });
                        }
                    } else {
                        Log.e("WishpostFragment", "북마크 가져오기 실패", task.getException());
                    }
                });
    }

    private void addBookmarkView(Post post) {
        View bookmarkView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_profile_wishpost_item_bookmark, bookmarksLayout, false);
        TextView titleTextView = bookmarkView.findViewById(R.id.bookmark_title);
        TextView contentTextView = bookmarkView.findViewById(R.id.bookmark_content);
        Button deleteButton = bookmarkView.findViewById(R.id.button_delete_bookmark);
        RecyclerView imagesRecyclerView = bookmarkView.findViewById(R.id.recycler_view_images); // RecyclerView 가져오기

        titleTextView.setText(post.getTitle());
        contentTextView.setText(post.getContent());

        // 삭제 버튼 클릭 리스너 추가
        deleteButton.setOnClickListener(v -> deleteBookmark(post.getPostId()));

        // 북마크 항목 클릭 시 게시물 상세 페이지로 이동
        bookmarkView.setOnClickListener(v -> {
            PostDetailFragment postDetailFragment = PostDetailFragment.newInstance(post);
            replaceFragment(postDetailFragment);
        });

        bookmarksLayout.addView(bookmarkView);

        // 이미지 표시를 위한 RecyclerView 설정
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        PostImagesAdapter postImagesAdapter = new PostImagesAdapter(post.getImageUrls()); // PostImagesAdapter 생성
        imagesRecyclerView.setAdapter(postImagesAdapter); // Adapter 설정
    }

    private void deleteBookmark(String postId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firestore에서 해당 북마크 삭제
        db.collection("users").document(userId).collection("bookmarks").document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("WishpostFragment", "북마크 삭제 성공");
                    // UI에서 북마크 항목 제거
                    refreshBookmarkedPosts();
                })
                .addOnFailureListener(e -> Log.e("WishpostFragment", "북마크 삭제 실패", e));
    }

    private void refreshBookmarkedPosts() {
        bookmarksLayout.removeAllViews(); // 기존 뷰 제거
        loadBookmarkedPosts(); // 북마크 목록 다시 로드
    }

    // Fragment 교체를 위한 메서드
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
