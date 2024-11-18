// SendMoneyActivity.java
package com.example.team_project.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.team_project.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SendMoneyActivity extends AppCompatActivity {

    private static final String TAG = "SendMoneyActivity";
    private FirebaseFirestore db;
    private String senderEmail;
    private String receiverEmail;
    private String receiverName;
    private TextView textReceiverName;
    private EditText editAmount;
    private Button btnSendMoney;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_send_money);

        db = FirebaseFirestore.getInstance();

        senderEmail = getIntent().getStringExtra("senderEmail");
        receiverEmail = getIntent().getStringExtra("receiverEmail");
        receiverName = getIntent().getStringExtra("receiverName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("송금하기");

        toolbar.setNavigationOnClickListener(v -> finish());

        textReceiverName = findViewById(R.id.text_receiver_name);
        editAmount = findViewById(R.id.edit_amount);
        btnSendMoney = findViewById(R.id.btn_send_money);

        textReceiverName.setText(receiverName);

        btnSendMoney.setOnClickListener(v -> sendMoney());
    }

    private void sendMoney() {
        String amountStr = editAmount.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "송금할 금액을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        db.collection("users").whereEqualTo("email", senderEmail).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                Double senderBalanceObj = documentSnapshot.getDouble("accountBalance");
                if (senderBalanceObj == null) {
                    Log.e(TAG, "송금자의 잔액 정보가 없습니다.");
                    Toast.makeText(this, "송금자의 잔액 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                double senderBalance = senderBalanceObj;
                if (senderBalance < amount) {
                    Toast.makeText(this, "잔액이 부족합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("users").whereEqualTo("email", receiverEmail).get().addOnSuccessListener(receiverQuerySnapshots -> {
                    if (!receiverQuerySnapshots.isEmpty()) {
                        DocumentSnapshot receiverSnapshot = receiverQuerySnapshots.getDocuments().get(0);
                        Double receiverBalanceObj = receiverSnapshot.getDouble("accountBalance");
                        if (receiverBalanceObj == null) {
                            Log.e(TAG, "수신자의 잔액 정보가 없습니다.");
                            Toast.makeText(this, "수신자의 잔액 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        double receiverBalance = receiverBalanceObj;

                        // 잔액 업데이트
                        db.collection("users").document(documentSnapshot.getId()).update("accountBalance", senderBalance - amount);
                        db.collection("users").document(receiverSnapshot.getId()).update("accountBalance", receiverBalance + amount);

                        // 트랜잭션 기록
                        Map<String, Object> transaction = new HashMap<>();
                        transaction.put("sender", senderEmail);
                        transaction.put("receiver", receiverEmail);
                        transaction.put("amount", amount);
                        transaction.put("createdAt", new Date());

                        db.collection("transactions").add(transaction).addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "송금이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            sendTransactionMessage(amount); // 송금 메시지 전송
                            Intent intent = new Intent(SendMoneyActivity.this, ChatActivity.class);
                            intent.putExtra("userEmail1", senderEmail);
                            intent.putExtra("userEmail2", receiverEmail);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "송금에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.e(TAG, "수신자 문서가 존재하지 않습니다.");
                        Toast.makeText(this, "수신자의 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e(TAG, "송금자 문서가 존재하지 않습니다.");
                Toast.makeText(this, "송금자의 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendTransactionMessage(double amount) {
        String messageContent = String.format("%d원을 송금했습니다.", (int) amount);
        String chatId = senderEmail.compareTo(receiverEmail) < 0 ? senderEmail + "_" + receiverEmail : receiverEmail + "_" + senderEmail;

        Map<String, Object> message = new HashMap<>();
        message.put("chatId", chatId);
        message.put("sender", senderEmail);
        message.put("content", messageContent);
        message.put("createdAt", new Date());

        db.collection("messages").add(message).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "송금 메시지가 전송되었습니다.");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "송금 메시지 전송에 실패했습니다.", e);
        });
    }
}