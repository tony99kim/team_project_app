package com.example.team_project.Profile.Pay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_pay_transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.typeTextView.setText(transaction.getType());
        holder.amountTextView.setText(String.valueOf((int) transaction.getAmount()));
        holder.dateTextView.setText(transaction.getCreatedAt().toString());

        if (transaction.getType().equals("송금")) {
            if (transaction.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                holder.descriptionTextView.setText("To: " + transaction.getReceiverUsername());
            } else {
                holder.descriptionTextView.setText("From: " + transaction.getSenderUsername());
            }
        } else if (transaction.getType().equals("결제")) {
            holder.amountTextView.setText(String.valueOf((int) transaction.getFinalPrice()));
            holder.descriptionTextView.setText(transaction.getProductTitle());
        } else {
            holder.descriptionTextView.setText(transaction.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView;
        TextView descriptionTextView;
        TextView amountTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.transaction_type);
            descriptionTextView = itemView.findViewById(R.id.transaction_description);
            amountTextView = itemView.findViewById(R.id.transaction_amount);
            dateTextView = itemView.findViewById(R.id.transaction_date);
        }
    }
}