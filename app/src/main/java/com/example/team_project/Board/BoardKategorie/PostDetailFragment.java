package com.example.team_project.Board.BoardKategorie;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;

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

public class PostDetailFragment extends Fragment implements CommentAdapter.OnCommentActionListener {

    private String postId; // Firestore 문서 ID
    private String postAuthorId; // 게시물 작성자 ID
    private String postTitle; // 게시물 제목
    private String postContent; // 게시물 내용
    private String postName; // 게시물 작성자 이름
    private String userId; // 현재 사용자 ID

    private String commentId;

    private ViewPager2 viewPager2;
    private TextView titleTextView, contentTextView, posterNameTextView, viewsTextView;
    private ImageView bookmarkButton; // 북마크 버튼
    private boolean isBookmarked = false; // 북마크 상태

    private ImageView favoriteButton; // 좋아요 버튼
    private boolean isFavorite = false; // 좋아요 상태

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
        setHasOptionsMenu(true); // 메뉴 사용을 활성화
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            postAuthorId = getArguments().getString("postAuthorId");
            postTitle = getArguments().getString("postTitle");
            postContent = getArguments().getString("postContent");
            postName = getArguments().getString("authorName");

            commentList = new ArrayList<>();
            commentAdapter = new CommentAdapter(commentList, this);
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        if (postId != null) {
            incrementViewCount();
            loadBookmarkStatus(); // Load bookmark status when fragment is created
        } else {
            Log.e("PostDetailFragment", "postId가 null입니다.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board_postdetail, container, false);

        setHasOptionsMenu(true);

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
        favoriteButton = view.findViewById(R.id.button_favorite);

        commentEditText = view.findViewById(R.id.commentEditText);
        commentButton = view.findViewById(R.id.commentButton);
        commentRecyclerView = view.findViewById(R.id.commentListRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentRecyclerView.setAdapter(commentAdapter);

        commentButton.setOnClickListener(v -> addComment());

        titleTextView.setText(postTitle);
        contentTextView.setText(postContent);

        loadPostImages();
        loadPosterName(); // 올바른 Firestore 문서 참조를 사용하여 호출
        loadComments();
        loadFavoriteStatus(); // 초기 좋아요 상태 로드

        bookmarkButton.setOnClickListener(v -> toggleBookmark());
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_post_detail, menu); // 메뉴 인플레이트

        // 메뉴 아이템을 수정/삭제 버튼을 보이도록 설정
        MenuItem editItem = menu.findItem(R.id.menu_edit); // 수정 버튼
        MenuItem deleteItem = menu.findItem(R.id.menu_delete); // 삭제 버튼

        // 작성자 ID와 현재 사용자 ID를 비교하여 수정/삭제 버튼 표시 여부 결정
        if (userId != null && postAuthorId != null && userId.equals(postAuthorId)) {
            // 작성자만 수정 및 삭제 가능
            editItem.setVisible(true);
            deleteItem.setVisible(true);

            // 글자 색을 변경하기 위해 SpannableString 사용
            SpannableString editTitle = new SpannableString("수정");
            editTitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, editTitle.length(), 0); // 검정색

            SpannableString deleteTitle = new SpannableString("삭제");
            deleteTitle.setSpan(new ForegroundColorSpan(Color.RED), 0, deleteTitle.length(), 0); // 빨간색

            // 제목을 설정
            editItem.setTitle(editTitle);
            deleteItem.setTitle(deleteTitle);
        } else {
            editItem.setVisible(false);
            deleteItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_edit) {
            // 수정 버튼 클릭 시 처리
            openEditDialog();
            return true;
        } else if (item.getItemId() == R.id.menu_delete) {
            // 삭제 버튼 클릭 시 처리
            deletePost();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void openEditDialog() {
        // 수정할 내용을 입력할 수 있는 다이얼로그 표시
        final EditText titleEditText = new EditText(getContext());
        titleEditText.setText(postTitle);
        final EditText contentEditText = new EditText(getContext());
        contentEditText.setText(postContent);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(titleEditText);
        layout.addView(contentEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("게시물 수정")
                .setView(layout)
                .setPositiveButton("저장", (dialog, which) -> {
                    String newTitle = titleEditText.getText().toString();
                    String newContent = contentEditText.getText().toString();
                    updatePost(newTitle, newContent);
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updatePost(String newTitle, String newContent) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts")
                .document(postId)
                .update(
                        "title", newTitle,
                        "content", newContent
                )
                .addOnSuccessListener(aVoid -> {
                    postTitle = newTitle;
                    postContent = newContent;
                    titleTextView.setText(postTitle);
                    contentTextView.setText(postContent);
                    Toast.makeText(getContext(), "게시물이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "게시물 수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deletePost() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts")
                .document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "게시물 삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFavoriteStatus() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference favoriteRef = db.collection("users").document(userId)
                .collection("likes").document(postId);

        favoriteRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                isFavorite = true;
                favoriteButton.setImageResource(R.drawable.post_favorite); // 좋아요 상태 이미지
            } else {
                isFavorite = false;
                favoriteButton.setImageResource(R.drawable.post_favorite_border); // 기본 상태 이미지
            }
        });
    }

    private void toggleFavorite() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(postId);
        DocumentReference favoriteRef = db.collection("users").document(userId)
                .collection("likes").document(postId);

        db.runTransaction(transaction -> {
            DocumentSnapshot postSnapshot = transaction.get(postRef);
            long favoriteCount = postSnapshot.getLong("likes") != null ? postSnapshot.getLong("likes") : 0;

            if (isFavorite) {
                // 좋아요 취소
                transaction.update(postRef, "likes", favoriteCount - 1);
                transaction.delete(favoriteRef);
            } else {
                // 좋아요 추가
                transaction.update(postRef, "likes", favoriteCount + 1);
                Map<String, Object> favoriteData = new HashMap<>();
                favoriteData.put("timestamp", Timestamp.now());
                transaction.set(favoriteRef, favoriteData);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            // 좋아요 상태 토글
            isFavorite = !isFavorite;
            favoriteButton.setImageResource(isFavorite ? R.drawable.post_favorite : R.drawable.post_favorite_border);

            // 좋아요 수 업데이트
            updateLikeCount();

            // 사용자에게 결과 알림
            Toast.makeText(getContext(), isFavorite ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("PostDetailFragment", "좋아요 업데이트 실패", e);
            Toast.makeText(getContext(), "좋아요 상태 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLikeCount();  // 화면을 재개할 때마다 좋아요 수 갱신
    }

    private void updateLikeCount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(postId);

        postRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long likeCount = documentSnapshot.getLong("likes");
                likeCount = (likeCount != null) ? likeCount : 0;

                // 안전한 방식으로 UI를 갱신
                TextView likeCountTextView = getView() != null ? getView().findViewById(R.id.textView_like_count) : null;
                if (likeCountTextView != null) {
                    likeCountTextView.setText("" + likeCount);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("PostDetailFragment", "좋아요 수 로드 실패", e);
        });
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
                            commentData.put("userId", userId); // 댓글 작성자 ID 추가

                            db.collection("comment").document(userId)
                                    .set(commentData)
                                    .addOnSuccessListener(aVoid -> {
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
                        String commentId = document.getId(); // 댓글 ID 가져오기

                        commentList.add(new Comment(name, commentContent, timestamp, postId, commentId));
                    }
                    commentAdapter.notifyDataSetChanged(); // RecyclerView 업데이트
                })
                .addOnFailureListener(e -> Log.e("PostDetailFragment", "댓글 로드 실패", e));
    }

    private void loadBookmarkStatus() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference bookmarkRef = db.collection("users").document(userId)
                .collection("bookmarks").document(postId);

        bookmarkRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                isBookmarked = true;
                bookmarkButton.setImageResource(R.drawable.post_bookmark); // 북마크 상태 이미지
            } else {
                isBookmarked = false;
                bookmarkButton.setImageResource(R.drawable.post_bookmark_border); // 기본 상태 이미지
            }
        }).addOnFailureListener(e -> {
            Log.e("PostDetailFragment", "북마크 상태 로드 실패", e);
        });
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
        if (postAuthorId == null || postAuthorId.isEmpty()) {
            posterNameTextView.setText("알 수 없는 사용자");
            return;
        }
        DocumentReference userRef = db.collection("users").document(postAuthorId); // postAuthorId로 사용자 정보 조회

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String posterName = document.getString("name");
                    posterNameTextView.setText(posterName != null ? posterName : "알 수 없는 사용자");
                } else {
                    posterNameTextView.setText("알 수 없는 사용자");
                }
            } else {
                posterNameTextView.setText("사용자 로드 오류");
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


    @Override
    public void onEditComment(Comment comment) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(comment.getUserId())) {
            openEditCommentDialog(comment);
        } else {
            Toast.makeText(getContext(), "자신의 댓글만 수정할 수 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteComment(Comment comment) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(comment.getUserId())) {
            deleteComment(comment);
        } else {
            Toast.makeText(getContext(), "자신의 댓글만 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openEditCommentDialog(Comment comment) {
        final EditText commentEditText = new EditText(getContext());
        commentEditText.setText(comment.getCommentContent());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("댓글 수정")
                .setView(commentEditText)
                .setPositiveButton("저장", (dialog, which) -> {
                    String newContent = commentEditText.getText().toString();
                    updateComment(comment, newContent);
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateComment(Comment comment, String newContent) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("comment").document(comment.getCommentId())
                .update("commentContent", newContent)
                .addOnSuccessListener(aVoid -> {
                    comment.setCommentContent(newContent);
                    commentAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "댓글 수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteComment(Comment comment) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("comment").document(comment.getCommentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    commentList.remove(comment);
                    commentAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "댓글 삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
