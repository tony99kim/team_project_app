package com.example.team_project.Profile.notice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.team_project.R;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private List<Notice> noticeList;

    public NoticeAdapter(List<Notice> noticeList) {
        this.noticeList = noticeList;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        holder.titleTextView.setText(notice.getTitle());

        // 제목 클릭 이벤트 리스너 추가
        holder.titleTextView.setOnClickListener(v -> {
            Fragment detailFragment = NoticeDetailFragment.newInstance(notice.getContent(), notice.getImageUrl());
            ((AppCompatActivity) holder.itemView.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // 나머지 코드...
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;
        TextView dateTextView;
        ImageView noticeImageView;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            noticeImageView = itemView.findViewById(R.id.noticeImageView);
        }
    }
}
