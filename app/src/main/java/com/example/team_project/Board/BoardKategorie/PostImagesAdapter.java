package com.example.team_project.Board.BoardKategorie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.team_project.R;

import java.util.List;

public class PostImagesAdapter extends RecyclerView.Adapter<PostImagesAdapter.ViewHolder> {
    private List<String> imageUrls;

    public PostImagesAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 이미지 전용 레이아웃으로 변경
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_board_postdetail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // URL이 null인지 확인
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.imageView);
        } else {
            // URL이 null이거나 비어있을 경우 기본 이미지 설정
            holder.imageView.setImageResource(R.drawable.base); // 기본 이미지 리소스
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.postImageView); // 게시물 이미지 뷰로 변경
        }
    }

    public void setImageUrls(List<String> newImageUrls) {
        imageUrls.clear(); // 기존의 이미지 URL 리스트를 지웁니다.
        if (newImageUrls != null) { // null 체크를 추가합니다.
            imageUrls.addAll(newImageUrls); // 새로운 이미지 URL 리스트를 추가합니다.
        }
        notifyDataSetChanged(); // 데이터가 변경되었음을 알려 UI를 갱신합니다.
    }
}
