package com.example.team_project.Profile.Pay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RechargeExchangeHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_pay_transaction_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(transactionAdapter);

        loadTransactions();

        return view;
    }

    private void loadTransactions() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 충전 내역 불러오기
        db.collection("users").document(userId).collection("rechargeRecords")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Transaction transaction = document.toObject(Transaction.class);
                            transactionList.add(transaction);
                        }
                        transactionAdapter.notifyDataSetChanged();
                    }
                });

        // 환전 내역 불러오기
        db.collection("users").document(userId).collection("exchangeRecords")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Transaction transaction = document.toObject(Transaction.class);
                            transactionList.add(transaction);
                        }
                        transactionAdapter.notifyDataSetChanged();
                    }
                });
    }
}