package com.example.team_project.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team_project.Board.PostCommnet.Comment;
import com.example.team_project.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter2 extends RecyclerView.Adapter<CommentAdapter2.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter2(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notifications_comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.nameTextView.setText(comment.getName());
        holder.commentTextView.setText(comment.getCommentContent());

        // 타임스탬프를 형식화하여 표시
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.timestampTextView.setText(sdf.format(comment.getTimestamp().toDate()));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView commentTextView;
        TextView timestampTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
