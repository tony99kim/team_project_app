package com.example.team_project.Chat.ChatAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Chat.Data.Message;
import com.example.team_project.Chat.Data.User;
import com.example.team_project.R;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private String email;
    private List<Message> dataList;
    private List<User> users;

    public MessageListAdapter(String email, List<Message> dataList, List<User> users) {
        this.email = email;
        this.dataList = dataList;
        this.users = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chat_item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = dataList.get(position);

        if ("system".equals(message.getSender())) {
            holder.textSystemMessage.setText(message.getContent());
            holder.textSystemMessage.setVisibility(View.VISIBLE);
            holder.textSenderMessage.setVisibility(View.GONE);
            holder.layoutReceiver.setVisibility(View.GONE);
        } else if (email.equals(message.getSender())) {
            holder.textSenderMessage.setText(message.getContent());
            holder.textSenderMessage.setVisibility(View.VISIBLE);
            holder.layoutReceiver.setVisibility(View.GONE);
            holder.textSystemMessage.setVisibility(View.GONE);
        } else {
            String senderName = getUserNameByEmail(message.getSender());
            holder.textReceiver.setText(senderName);
            holder.textReceiverMessage.setText(message.getContent());
            holder.textSenderMessage.setVisibility(View.GONE);
            holder.layoutReceiver.setVisibility(View.VISIBLE);
            holder.textSystemMessage.setVisibility(View.GONE);
        }
    }

    private String getUserNameByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user.getName();
            }
        }
        return email;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textSystemMessage;
        public TextView textReceiver;
        public TextView textReceiverMessage;
        public TextView textSenderMessage;
        public LinearLayout layoutReceiver;

        public ViewHolder(View itemView) {
            super(itemView);
            textSystemMessage = itemView.findViewById(R.id.text_message_system);
            textReceiver = itemView.findViewById(R.id.text_receiver);
            textReceiverMessage = itemView.findViewById(R.id.text_message_receiver);
            textSenderMessage = itemView.findViewById(R.id.text_message_sender);
            layoutReceiver = itemView.findViewById(R.id.layout_receiver);
        }
    }
}

