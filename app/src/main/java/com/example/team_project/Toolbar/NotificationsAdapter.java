package com.example.team_project.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private List<NotificationItem> notificationList;

    public NotificationsAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_toobar__notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);
        holder.textSender.setText(item.getSender()); // 여기에서 상대방의 이름을 설정
        holder.textMessage.setText(item.getMessage());
    }


    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void updateNotifications(List<NotificationItem> newNotifications) {
        this.notificationList = newNotifications;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textSender;
        TextView textMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textSender = itemView.findViewById(R.id.text_sender);
            textMessage = itemView.findViewById(R.id.text_message);
        }
    }
}
