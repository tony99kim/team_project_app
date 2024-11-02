package com.example.team_project.Board.BoardKategorie;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.team_project.Board.BoardKategorie.ViewPagerAdapter;
import com.example.team_project.Board.PostCommnet.Comment;
import com.example.team_project.Board.PostCommnet.CommentAdapter;
import com.example.team_project.Board.PostCommnet.CommentListFragment;
import com.example.team_project.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailFragment extends Fragment {

    private String postId; // Firestore 문서 ID
    private String postAuthorId; // 게시물 작성자 ID
    private String postTitle; // 게시물 제목
    private String postContent; // 게시물 내용


    private String postName; // 게시물 작성자 이름
    private String userId; // 현재 사용자 ID

    private ViewPager2 viewPager2;
    private TextView titleTextView, contentTextView, posterNameTextView, viewsTextView;
    private ImageView bookmarkButton; // 북마크 버튼
    private boolean isBookmarked = false; // 북마크 상태

    private EditText commentEditText; // 댓글 입력 EditText
    private Button commentButton; // 댓글 작성 버튼
    private RecyclerView commentRecyclerView; // 댓글 목록 RecyclerView
    private CommentAdapter commentAdapter; // 댓글 Adapter
    private List<Comment> commentList; // 댓글 목록

    public static PostDetailFragment newInstance(String postId, String postAuthorId, String postTitle, String postContent, String postName) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        args.putString("postAuthorId", postAuthorId);
        args.putString("postTitle", postTitle);
        args.putString("postContent", postContent);
        args.putString("authorName", postName);
        fragment.setArguments(args);
        return fragment;
    }

    public static PostDetailFragment newInstance(Post post) {
        return newInstance(post.getPostId(), post.getAuthorId(), post.getTitle(), post.getContent(), post.getAuthorName());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            postAuthorId = getArguments().getString("postAuthorId");
            postTitle = getArguments().getString("postTitle");
            postContent = getArguments().getString("postContent");
            postName = getArguments().getString("authorName");

            commentList = new ArrayList<>();
            commentAdapter = new CommentAdapter(commentList);
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        incrementViewCount();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board_postdetail, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar_post_detail);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        viewPager2 = view.findViewById(R.id.viewPager_images);
        titleTextView = view.findViewById(R.id.textView_post_title);
        contentTextView = view.findViewById(R.id.textView_post_content);
        posterNameTextView = view.findViewById(R.id.textView_poster_name);
        viewsTextView = view.findViewById(R.id.textView_post_views);
        bookmarkButton = view.findViewById(R.id.button_bookmark);

        commentEditText = view.findViewById(R.id.commentEditText);
        commentButton = view.findViewById(R.id.commentButton);
        commentRecyclerView = view.findViewById(R.id.commentListRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentRecyclerView.setAdapter(commentAdapter);

        commentButton.setOnClickListener(v -> addComment());

        titleTextView.setText(postTitle);
        contentTextView.setText(postContent);

        loadPostImages();
        loadPosterName();
        loadComments();

        bookmarkButton.setOnClickListener(v -> toggleBookmark());

        return view;
    }

    private void addComment() {
        String commentContent = commentEditText.getText().toString().trim();
        if (!commentContent.isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // 현재 사용자의 이름을 Firestore에서 조회
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name"); // 사용자 이름 가져오기
                            Timestamp timestamp = Timestamp.now();
                            Map<String, Object> commentData = new HashMap<>();
                            commentData.put("commentContent", commentContent);
                            commentData.put("name", userName); // 댓글 작성자 이름을 사용자 이름으로 설정
                            commentData.put("timestamp", timestamp); // 타임스탬프 추가
                            commentData.put("postId", postId); // 게시물 ID

                            db.collection("comment")
                                    .add(commentData)
                                    .addOnSuccessListener(documentReference -> {
                                        commentEditText.setText(""); // 입력란 초기화
                                        loadComments(); // 댓글 로드
                                        Toast.makeText(getContext(), "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Log.e("PostDetailFragment", "댓글 추가 실패", e));
                        } else {
                            Toast.makeText(getContext(), "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("PostDetailFragment", "사용자 이름 가져오기 실패", e));
        } else {
            Toast.makeText(getContext(), "댓글을 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }



    private void loadComments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("comment")
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentList.clear(); // 기존 댓글 목록 초기화
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // 각 댓글 데이터를 가져와서 추가
                        String commentContent = document.getString("commentContent");
                        String name = document.getString("name");
                        Timestamp timestamp = document.getTimestamp("timestamp");
                        String postId = document.getString("postId");

                        commentList.add(new Comment(name, commentContent, timestamp, postId));
                    }
                    commentAdapter.notifyDataSetChanged(); // RecyclerView 업데이트
                })
                .addOnFailureListener(e -> Log.e("PostDetailFragment", "댓글 로드 실패", e));
    }




    private void toggleBookmark() {
        if (isBookmarked) {
            bookmarkButton.setImageResource(R.drawable.post_bookmark_border); // 원래 이미지
            removeBookmark();
            Toast.makeText(getContext(), "북마크가 취소되었습니다.", Toast.LENGTH_SHORT).show(); // 토스트 메시지 추가
        } else {
            bookmarkButton.setImageResource(R.drawable.post_bookmark); // 북마크 이미지
            bookmarkPost();
            Toast.makeText(getContext(), "관심 게시물에 추가되었습니다.", Toast.LENGTH_SHORT).show(); // 토스트 메시지 추가
        }
        isBookmarked = !isBookmarked; // 상태 토글
    }

    private void bookmarkPost() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> bookmarkData = new HashMap<>();
        bookmarkData.put("postId", postId);
        bookmarkData.put("title", postTitle);
        bookmarkData.put("content", postContent);
        bookmarkData.put("authorName", postName);

        // 사용자 ID로 북마크 컬렉션에 추가
        db.collection("users").document(userId).collection("bookmarks").document(postId)
                .set(bookmarkData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PostDetailFragment", "북마크 추가 성공");
                    // 북마크 추가 성공 시 Toast 메시지 표시
                    Toast.makeText(getContext(), "관심 게시물에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("PostDetailFragment", "북마크 추가 실패", e));
    }

    private void removeBookmark() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).collection("bookmarks").document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("PostDetailFragment", "북마크 해제 성공");
                    Toast.makeText(getContext(), "관심 게시물에서 제거되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("PostDetailFragment", "북마크 해제 실패", e));
    }


    private void loadPosterName() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(postAuthorId); // postAuthorId로 사용자 정보 조회

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String posterName = document.getString("name");
                    posterNameTextView.setText(posterName != null ? posterName : "Unknown User");
                } else {
                    Log.d("PostDetailFragment", "문서가 존재하지 않습니다");
                }
            } else {
                Log.d("PostDetailFragment", "문서 가져오기 실패: ", task.getException());
            }
        });
    }

    private void loadPostImages() {
        String directoryPath = "PostImages/" + postId; // Firestore 문서 ID를 사용하여 이미지 경로 설정
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(directoryPath);

        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<String> imageUrls = new ArrayList<>();
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    Log.d("PostDetailFragment", "가져온 이미지 URL: " + uri.toString());
                    if (imageUrls.size() == listResult.getItems().size()) {
                        setupViewPager(imageUrls);
                    }
                });
            }
        }).addOnFailureListener(e -> Log.e("PostDetailFragment", "이미지 목록 가져오기 실패", e));
    }

    private void setupViewPager(List<String> imageUrls) {
        if (!imageUrls.isEmpty()) {
            viewPager2.setAdapter(new ViewPagerAdapter(imageUrls));
        } else {
            Log.d("PostDetailFragment", "이미지 URL 목록이 비어 있습니다");
        }
    }

    // 조회수 증가 메서드
    private void incrementViewCount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(postId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    DocumentSnapshot snapshot = transaction.get(postRef);
                    long newViewCount = snapshot.getLong("viewCount") != null ? snapshot.getLong("viewCount") + 1 : 1;
                    transaction.update(postRef, "viewCount", newViewCount);
                    return null;
                }).addOnSuccessListener(aVoid -> {
                    Log.d("PostDetailFragment", "조회수 증가 성공");
                    updateViewCount();
                })
                .addOnFailureListener(e -> Log.e("PostDetailFragment", "조회수 증가 실패", e));
    }

    // 조회수 가져오기 및 TextView에 설정
    private void updateViewCount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(postId);

        postRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                long viewCount = documentSnapshot.getLong("viewCount");
                viewsTextView.setText("조회수: " + viewCount);
            } else {
                Log.d("PostDetailFragment", "문서가 존재하지 않습니다");
            }
        }).addOnFailureListener(e -> {
            Log.e("PostDetailFragment", "조회수 가져오기 실패", e);
        });
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
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
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