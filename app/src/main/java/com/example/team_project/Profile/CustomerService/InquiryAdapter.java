package com.example.team_project.Profile.CustomerService;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;

import java.util.ArrayList;

public class InquiryAdapter extends RecyclerView.Adapter<InquiryAdapter.InquiryViewHolder> {
    private ArrayList<Inquiry> inquiries;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Inquiry inquiry);
    }

    public InquiryAdapter(ArrayList<Inquiry> inquiries, OnItemClickListener listener) {
        this.inquiries = inquiries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InquiryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_customerservice_inquiryhistory_item, parent, false);
        return new InquiryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InquiryViewHolder holder, int position) {
        Inquiry inquiry = inquiries.get(position);
        holder.bind(inquiry, listener);
    }

    @Override
    public int getItemCount() {
        return inquiries.size();
    }

    public static class InquiryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDate;

        public InquiryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvInquiryTitle);
            tvDate = itemView.findViewById(R.id.tvInquiryDate);
        }

        public void bind(final Inquiry inquiry, final OnItemClickListener listener) {
            tvTitle.setText(inquiry.getTitle());
            tvDate.setText(inquiry.getDate()); // 날짜 필드 추가 필요
            itemView.setOnClickListener(v -> listener.onItemClick(inquiry));
        }
    }
}