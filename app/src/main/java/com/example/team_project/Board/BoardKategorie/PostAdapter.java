package com.example.team_project.Board.BoardKategorie;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;

    // 생성자
    public PostAdapter(Context context, ArrayList<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_board_post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        if (post == null) {
            return; // Null post 체크
        }

        holder.postTitleTextView.setText(post.getTitle());
        holder.postContentTextView.setText(post.getContent());

        // Firebase에서 사용자 이름 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid(); // 현재 로그인한 사용자의 UID를 가져옴
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("name");
                    holder.nameTextView.setText(userName); // nameTextView에 사용자 이름 설정
                } else {
                    holder.nameTextView.setText("Unknown User"); // 이름을 찾을 수 없을 때 기본값 설정
                }
            }).addOnFailureListener(e -> {
                Log.e("PostAdapter", "Failed to fetch user name", e);
                holder.nameTextView.setText("Unknown User"); // 오류 발생 시 기본값 설정
            });
        }

        // 게시물 이미지 경로 설정
        List<String> imageUrls = post.getImageUrls(); // 이미지 URL 리스트 가져오기
        if (imageUrls != null && !imageUrls.isEmpty()) {
            // 첫 번째 이미지 URL 사용
            String imageUrl = imageUrls.get(0);
            Glide.with(context)
                    .load(imageUrl) // URL을 사용하여 이미지 로드
                    .into(holder.postImageView);
        } else {
            // 기본 이미지 또는 빈 상태 설정
            holder.postImageView.setImageResource(R.drawable.tree_image); // 기본 이미지
        }

        // 게시물 클릭 이벤트 리스너 설정
        holder.itemView.setOnClickListener(v -> {
            // 게시물 상세 페이지로 이동
            PostDetailFragment postDetailFragment = PostDetailFragment.newInstance(post); // 게시물 객체를 전달
            replaceFragment(postDetailFragment);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;
        TextView postTitleTextView;
        TextView postContentTextView;
        TextView nameTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
            postTitleTextView = itemView.findViewById(R.id.postTitleTextView);
            postContentTextView = itemView.findViewById(R.id.postContentTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
        }
    }

    // Fragment 교체를 위한 메서드
    private void replaceFragment(Fragment fragment) {
        ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
