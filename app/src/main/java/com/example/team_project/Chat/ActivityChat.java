package com.example.team_project.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Chat.adapter.MessageListAdapter;
import com.example.team_project.Data.Chat;
import com.example.team_project.Data.Message;
import com.example.team_project.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ActivityChat extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextView textTitle;
    private RecyclerView recyclerView;
    private EditText editInput;
    private ImageView btnSend;
    private ImageView btnBack;

    private String user1;
    private String user2;

    private ArrayList<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();

        String userEmail1 = getIntent().getStringExtra("userEmail1");
        String userEmail2 = getIntent().getStringExtra("userEmail2");
        user1 = getIntent().getStringExtra("user1");
        user2 = getIntent().getStringExtra("user2");

        String chatId = userEmail1.compareTo(userEmail2) < 0 ? userEmail1 + "_" + userEmail2 : userEmail2 + "_" + userEmail1;

        textTitle = findViewById(R.id.text_user);
        textTitle.setText(user2);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MessageListAdapter(userEmail1, messages));

        editInput = findViewById(R.id.edit_input);
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            if (!editInput.getText().toString().isEmpty()) {
                sendMessage(chatId, userEmail1, editInput.getText().toString());
                editInput.setText("");
            }
        });

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        fetchChat(chatId, userEmail1, userEmail2);
        setupRealtimeMessageUpdates(chatId);
    }

    private void fetchChat(String id, String email1, String email2) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("chats").document(id).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            Chat newChat = new Chat(id, email1, email2, "", new Date());
                            db.collection("chats").document(id).set(newChat);
                            addInitialMessage(id);
                        } else {
                            loadMessages(id);
                        }
                    } else {
                        Log.e("ChatActivity", "Error fetching chat: " + task.getException().getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e("ChatActivity", "Error fetching chat: " + e.getMessage());
            }
        });
    }

    private void setupRealtimeMessageUpdates(String chatId) {
        db.collection("messages")
                .whereEqualTo("chatId", chatId)
                .orderBy("createdAt")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("ChatActivity", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        ArrayList<Message> newMessages = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            newMessages.add(doc.toObject(Message.class));
                        }
                        messages.clear();
                        messages.addAll(newMessages);
                        Collections.sort(messages, Comparator.comparing(Message::getCreatedAt));
                        recyclerView.getAdapter().notifyDataSetChanged();
                        recyclerView.scrollToPosition(messages.size() - 1);
                    } else {
                        Log.d("ChatActivity", "Current data: null");
                    }
                });
    }

    private void addInitialMessage(String chatId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일(E)", Locale.KOREAN);
                String currentDate = dateFormat.format(new Date());
                Message initialMessage = new Message(chatId, "system", currentDate, new Date());

                db.collection("messages").add(initialMessage).addOnSuccessListener(documentReference -> {
                    messages.add(initialMessage);
                    runOnUiThread(() -> {
                        recyclerView.getAdapter().notifyDataSetChanged();
                        recyclerView.scrollToPosition(messages.size() - 1);
                    });

                    db.collection("chats").document(chatId)
                            .update("lastMessage", "채팅방이 열렸습니다.", "updatedAt", new Date());
                }).addOnFailureListener(e -> {
                    Log.e("ChatActivity", "addInitialMessage: Error adding initial message: " + e.getMessage());
                });
            } catch (Exception e) {
                Log.e("ChatActivity", "addInitialMessage: " + e.getMessage());
            }
        });
    }

    private void loadMessages(String chatId) {
        Executors.newSingleThreadExecutor().execute(() -> db.collection("messages")
                .whereEqualTo("chatId", chatId)
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Message> loadedMessages = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        loadedMessages.add(document.toObject(Message.class));
                    }
                    Collections.sort(loadedMessages, Comparator.comparing(Message::getCreatedAt));
                    messages.clear();
                    messages.addAll(loadedMessages);

                    runOnUiThread(() -> {
                        recyclerView.getAdapter().notifyDataSetChanged();
                        recyclerView.scrollToPosition(messages.size() - 1);
                    });
                }).addOnFailureListener(e -> {
                    Log.e("ChatActivity", "loadMessages: " + e.getMessage());
                }));
    }

    private void sendMessage(String chatId, String email, String content) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Message newMessage = new Message(chatId, email, content, new Date());
                db.collection("messages").add(newMessage).addOnSuccessListener(documentReference -> {
                    messages.add(newMessage);
                    runOnUiThread(() -> {
                        recyclerView.getAdapter().notifyDataSetChanged();
                        recyclerView.scrollToPosition(messages.size() - 1);
                    });

                    db.collection("chats").document(chatId)
                            .update("lastMessage", content, "updatedAt", new Date());
                }).addOnFailureListener(e -> {
                    Log.e("ChatActivity", "sendMessage: Error sending message: " + e.getMessage());
                });

                Log.d("ChatActivity", "Message sent successfully");
            } catch (Exception e) {
                Log.e("ChatActivity", "sendMessage: Error sending message: " + e.getMessage());
            }
        });
    }

}
