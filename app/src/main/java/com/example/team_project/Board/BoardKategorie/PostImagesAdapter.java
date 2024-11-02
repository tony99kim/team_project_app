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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bord_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.imageView); // placeholder와 error 없이 단순히 이미지를 로드합니다.
        } else {
            holder.imageView.setImageResource(R.drawable.base); // 기본 이미지가 필요하다면 설정
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
            imageView = itemView.findViewById(R.id.postImageView);
        }
    }

    public void setImageUrls(List<String> newImageUrls) {
        imageUrls.clear();
        if (newImageUrls != null) {
            imageUrls.addAll(newImageUrls);
        }
        notifyDataSetChanged();
    }
}
