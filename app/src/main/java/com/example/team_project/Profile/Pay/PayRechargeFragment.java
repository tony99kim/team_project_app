package com.example.team_project.Profile.Pay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PayRechargeFragment extends Fragment {

    private FirebaseFirestore db;
    private String userId;
    private EditText amountEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_pay_recharge, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("페이충전");
        }

        amountEditText = view.findViewById(R.id.amountEditText);
        Button rechargeButton = view.findViewById(R.id.rechargeButton);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rechargeButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            if (!amountStr.isEmpty()) {
                int amount = Integer.parseInt(amountStr);
                db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int currentBalance = documentSnapshot.getLong("accountBalance").intValue();
                        int newBalance = currentBalance + amount;
                        db.collection("users").document(userId).update("accountBalance", newBalance)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "충전 완료", Toast.LENGTH_SHORT).show();
                                    amountEditText.setText("");

                                    // 거래 기록 추가
                                    Map<String, Object> transaction = new HashMap<>();
                                    transaction.put("type", "충전");
                                    transaction.put("amount", amount);
                                    transaction.put("createdAt", new Date());

                                    db.collection("users").document(userId).collection("rechargeRecords").add(transaction);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "충전 실패", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
            } else {
                Toast.makeText(getContext(), "금액을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}