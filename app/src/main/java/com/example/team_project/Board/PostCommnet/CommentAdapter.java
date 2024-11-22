package com.example.team_project.Board.PostCommnet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments; // 댓글 리스트
    private OnCommentActionListener onCommentActionListener;

    public CommentAdapter(List<Comment> comments, OnCommentActionListener onCommentActionListener) {
        this.comments = comments; // 댓글 리스트 초기화
        this.onCommentActionListener = onCommentActionListener;
    }

    // ViewHolder 클래스 정의
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView; // 작성자 이름 TextView
        public TextView contentTextView; // 댓글 내용 TextView
        public TextView timestampTextView; // 댓글 작성 시간 TextView
        public ImageView commentMenu; // 댓글 메뉴 아이콘

        public CommentViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.comment_name);
            contentTextView = view.findViewById(R.id.comment_content);
            timestampTextView = view.findViewById(R.id.comment_timestamp); // 댓글 작성 시간 TextView
            commentMenu = view.findViewById(R.id.comment_menu); // 댓글 메뉴 아이콘
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_board_post_comment_item, parent, false); // 댓글 아이템 레이아웃 inflate
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position); // 댓글 리스트에서 특정 댓글 가져오기
        holder.nameTextView.setText(comment.getName()); // 작성자 이름 설정
        holder.contentTextView.setText(comment.getCommentContent()); // 댓글 내용 설정

        // 댓글 작성 시간을 포맷팅하여 설정
        long timeInMillis = comment.getTimestamp().getSeconds() * 1000; // Timestamp에서 밀리초로 변환
        String formattedDate = formatTimestamp(timeInMillis);
        holder.timestampTextView.setText(formattedDate); // 댓글 작성 시간 설정

        holder.commentMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.commentMenu);
            popupMenu.inflate(R.menu.menu_comment);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.edit_comment) {
                    onCommentActionListener.onEditComment(comment);
                    return true;
                } else if (itemId == R.id.delete_comment) {
                    onCommentActionListener.onDeleteComment(comment);
                    return true;
                } else {
                    return false;
                }
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return comments.size(); // 댓글 리스트의 크기 반환
    }

    // 타임스탬프 포맷팅 메서드
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp)); // 타임스탬프를 날짜 형식으로 변환
    }

    public interface OnCommentActionListener {
        void onEditComment(Comment comment);
        void onDeleteComment(Comment comment);
    }
}