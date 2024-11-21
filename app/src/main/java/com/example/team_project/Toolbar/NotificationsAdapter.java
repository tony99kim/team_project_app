package com.example.team_project.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<NotificationItem> notifications;
    private SharedPreferences preferences;

    public NotificationsAdapter(Context context, List<NotificationItem> notifications) {
        this.preferences = context.getSharedPreferences("deleted_notifications", Context.MODE_PRIVATE);

        // 삭제된 항목 필터링
        this.notifications = filterDeletedNotifications(notifications);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_toobar_notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notifications.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateNotifications(List<NotificationItem> newNotifications) {
        // 삭제된 항목 필터링 후 업데이트
        if (newNotifications != null) {
            this.notifications = filterDeletedNotifications(newNotifications);
            notifyDataSetChanged();
        }
    }

    public void removeNotification(int position) {
        if (position >= 0 && position < notifications.size()) {
            NotificationItem item = notifications.get(position);

            // 삭제 상태 기록
            markAsDeleted(item.getDocumentId());

            // 리스트에서 제거 후 갱신
            notifications.remove(position);
            notifyItemRemoved(position);
        }
    }

    private void markAsDeleted(String documentId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(documentId, true); // 삭제 상태 저장
        editor.apply();
    }

    private boolean isDeleted(String documentId) {
        return preferences.getBoolean(documentId, false); // 삭제 상태 확인
    }

    private List<NotificationItem> filterDeletedNotifications(List<NotificationItem> notifications) {
        List<NotificationItem> filteredList = new ArrayList<>();
        for (NotificationItem item : notifications) {
            if (!isDeleted(item.getDocumentId())) {
                filteredList.add(item); // 삭제되지 않은 항목만 추가
            }
        }
        return filteredList;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final TextView sender;
        private final TextView message;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            sender = itemView.findViewById(R.id.text_sender);
            message = itemView.findViewById(R.id.text_message);
        }

        public void bind(NotificationItem item) {
            if (item != null) {
                sender.setText(item.getTitle());
                message.setText(item.getContent());
            }
        }
    }
}
