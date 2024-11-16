package com.example.team_project.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Chat.ChatAdapter.MessageListAdapter;
import com.example.team_project.Chat.Data.Chat;
import com.example.team_project.Chat.Data.Message;
import com.example.team_project.Chat.Data.User;
import com.example.team_project.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextView textTitle;
    private RecyclerView recyclerView;
    private EditText editInput;
    private ImageView btnSend;
    private ImageView btnBack;
    private ImageView btnExit;
    private ImageView btnAdd;
    private LinearLayout layoutMenu;
    private ConstraintLayout layoutInput;

    private String user1;
    private String user2;

    private ArrayList<Message> messages = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();

        String userEmail1 = getIntent().getStringExtra("userEmail1");
        String userEmail2 = getIntent().getStringExtra("userEmail2");
        user1 = getIntent().getStringExtra("user1");
        user2 = getIntent().getStringExtra("user2");

        // 상대방 이메일로 사용자 이름 가져오기
        fetchUserName(userEmail2);

        String chatId = userEmail1.compareTo(userEmail2) < 0 ? userEmail1 + "_" + userEmail2 : userEmail2 + "_" + userEmail1;

        textTitle = findViewById(R.id.text_user);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MessageListAdapter(userEmail1, messages, users));

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

        btnExit = findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("채팅 종료");
            builder.setMessage("채팅방을 나가시겠습니까?");
            builder.setPositiveButton("확인", (dialog, which) -> {
                deleteChatAndMessages(chatId);
                finish();
            });
            builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        btnAdd = findViewById(R.id.btn_add);
        layoutMenu = findViewById(R.id.layout_menu);
        layoutInput = findViewById(R.id.layout_input);
        btnAdd.setOnClickListener(v -> toggleMenu());

        fetchChat(chatId, userEmail1, userEmail2);
        setupRealtimeMessageUpdates(chatId);
        loadUsers();
    }

    private void toggleMenu() {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        if (layoutMenu.getVisibility() == View.GONE) {
            layoutInput.startAnimation(slideUp);
            layoutMenu.startAnimation(slideUp);
            layoutMenu.setVisibility(View.VISIBLE);
            btnAdd.setImageResource(R.drawable.ic_close);
        } else {
            layoutInput.startAnimation(slideDown);
            layoutMenu.startAnimation(slideDown);
            layoutMenu.setVisibility(View.GONE);
            btnAdd.setImageResource(R.drawable.ic_add);
        }
    }

    private void loadUsers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.collection("users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    users.clear();
                    for (DocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        users.add(user);
                    }
                    runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged());
                } else {
                    Log.e("ChatActivity", "Error loading users: " + task.getException().getMessage());
                }
            });
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

                        runOnUiThread(() -> {
                            recyclerView.getAdapter().notifyDataSetChanged();
                            recyclerView.scrollToPosition(messages.size() - 1);
                        });
                    } else {
                        Log.d("ChatActivity", "Current data: null");
                    }
                });
    }

    private void fetchChat(String id, String email1, String email2) {
        Executors.newSingleThreadExecutor().execute(() -> {
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
        });
    }

    private void addInitialMessage(String chatId) {
        Executors.newSingleThreadExecutor().execute(() -> {
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
        });
    }

    private void loadMessages(String chatId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.collection("messages")
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
                    });
        });
    }

    private void sendMessage(String chatId, String email, String content) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Message newMessage = new Message(chatId, email, content, new Date());
                messages.add(newMessage);
                runOnUiThread(() -> {
                    recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.scrollToPosition(messages.size() - 1);
                });

                db.collection("messages").add(newMessage).addOnSuccessListener(documentReference -> {
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

    private void deleteChatAndMessages(String chatId) {
        db.collection("chats").document(chatId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("ChatActivity", "Chat successfully deleted!"))
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error deleting chat", e));

        db.collection("messages")
                .whereEqualTo("chatId", chatId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        db.collection("messages").document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> Log.d("ChatActivity", "Message successfully deleted!"))
                                .addOnFailureListener(e -> Log.e("ChatActivity", "Error deleting message", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error finding messages to delete", e));
    }

    private void fetchUserName(String userEmail) {
        db.collection("users").whereEqualTo("email", userEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                String userName = document.getString("name"); // users 컬렉션의 name 필드 가져오기
                textTitle.setText(userName); // 상단에 판매자 이름 설정
            } else {
                Log.e("ChatActivity", "Error fetching user name: " + task.getException());
            }
        });
    }

}