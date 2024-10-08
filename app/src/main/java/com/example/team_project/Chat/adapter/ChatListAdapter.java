package com.example.team_project.Chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Chat.Data.Chat;
import com.example.team_project.Chat.Data.User;
import com.example.team_project.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private String userEmail;
    private List<Chat> dataList;
    private List<User> users;
    private OnClickUser listener;

    public ChatListAdapter(List<Chat> dataList, List<User> users, OnClickUser listener) {
        this.dataList = dataList;
        this.users = users;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chat_item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Chat chat = dataList.get(position);
        String otherUserEmail = chat.getUserEmail1().equals(userEmail) ? chat.getUserEmail2() : chat.getUserEmail1();
        User otherUser = findUserByEmail(otherUserEmail);

        if (otherUser != null) {
            holder.textName.setText(otherUser.getName());
        }
        holder.textMessage.setText(chat.getLastMessage() != null ? chat.getLastMessage() : "");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM월 dd일", Locale.KOREAN);
        String time = dateFormat.format(chat.getUpdatedAt());
        holder.textTime.setText(time);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onStartChat(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setUserEmail(String email) {
        userEmail = email;
    }

    private User findUserByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textName;
        public TextView textMessage;
        public TextView textTime;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textMessage = itemView.findViewById(R.id.text_message);
            textTime = itemView.findViewById(R.id.text_time);
        }
    }

    public interface OnClickUser {
        void onStartChat(Chat chat);
    }
}