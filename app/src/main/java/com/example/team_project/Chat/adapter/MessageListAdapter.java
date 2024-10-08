package com.example.team_project.Chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Chat.Data.Message;
import com.example.team_project.R;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private String email;
    private List<Message> dataList;

    public MessageListAdapter(String email, List<Message> dataList) {
        this.email = email;
        this.dataList = dataList;
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
            holder.textSenderMessage.setVisibility(View.GONE);
            holder.layoutReceiver.setVisibility(View.GONE);
            holder.textSystemMessage.setVisibility(View.VISIBLE);
        } else if (email.equals(message.getSender())) {
            holder.textSenderMessage.setText(message.getContent());
            holder.textSenderMessage.setVisibility(View.VISIBLE);
            holder.layoutReceiver.setVisibility(View.GONE);
            holder.textSystemMessage.setVisibility(View.GONE);
        } else {
            holder.textReceiver.setText(message.getSender());
            holder.textReceiverMessage.setText(message.getContent());
            holder.textSenderMessage.setVisibility(View.GONE);
            holder.layoutReceiver.setVisibility(View.VISIBLE);
            holder.textSystemMessage.setVisibility(View.GONE);
        }
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