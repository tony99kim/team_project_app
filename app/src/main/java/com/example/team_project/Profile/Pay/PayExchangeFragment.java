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

public class PayExchangeFragment extends Fragment {

    private FirebaseFirestore db;
    private String userId;
    private EditText amountEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_pay_exchange, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("페이환전");
        }

        amountEditText = view.findViewById(R.id.amountEditText);
        Button exchangeButton = view.findViewById(R.id.exchangeButton);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        exchangeButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            if (!amountStr.isEmpty()) {
                int amount = Integer.parseInt(amountStr);
                db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentBalance = documentSnapshot.getLong("accountBalance");
                        if (currentBalance != null && currentBalance >= amount) {
                            db.collection("users").document(userId).update("accountBalance", currentBalance - amount)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "환전이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                        amountEditText.setText("");
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "환전에 실패했습니다.", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(getContext(), "잔액이 부족합니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getContext(), "금액을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}