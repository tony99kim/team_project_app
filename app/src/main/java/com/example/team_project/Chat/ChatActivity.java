package com.example.team_project.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.team_project.MainActivity;
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
    private ImageView btnSendMoney; // 송금 버튼 추가
    private ConstraintLayout layoutInput;
    private ConstraintLayout layoutMenu; // 메뉴 레이아웃 추가
    private boolean isMenuVisible = false; // 메뉴의 상태를 관리할 변수

    private String user1;
    private String user2;
    private String receiverName;

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

        fetchUserName(userEmail2);

        String chatId = userEmail1.compareTo(userEmail2) < 0 ? userEmail1 + "_" + userEmail2 : userEmail2 + "_" + userEmail1;

        textTitle = findViewById(R.id.text_user);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MessageListAdapter(userEmail1, messages, users));

        editInput = findViewById(R.id.edit_input);
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> sendMessage(chatId, userEmail1, editInput.getText().toString()));

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });

        btnExit = findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(v -> showExitConfirmationDialog(chatId));

        btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(v -> toggleMenu());

        layoutInput = findViewById(R.id.layout_input);
        layoutMenu = findViewById(R.id.layout_menu);

        btnSendMoney = findViewById(R.id.send_money);
        btnSendMoney.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, SendMoneyActivity.class);
            intent.putExtra("senderEmail", userEmail1);
            intent.putExtra("receiverEmail", userEmail2);
            intent.putExtra("receiverName", receiverName);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });

        fetchChat(chatId, userEmail1, userEmail2);
        setupRealtimeMessageUpdates(chatId);
        loadUsers();
    }

    private void showExitConfirmationDialog(String chatId) {
        new AlertDialog.Builder(this)
                .setMessage("채팅방을 나가겠습니까?")
                .setPositiveButton("나가기", (dialog, which) -> {
                    deleteChatAndMessages(chatId);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void toggleMenu() {
        if (isMenuVisible) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    private void showMenu() {
        // 메뉴의 높이를 미리 계산
        layoutMenu.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        int menuHeight = layoutMenu.getMeasuredHeight(); // 메뉴의 높이 가져오기

        layoutInput.animate().translationY(-menuHeight).setDuration(300); // 입력창 올리기
        layoutMenu.setVisibility(View.VISIBLE);
        layoutMenu.setTranslationY(menuHeight); // 메뉴를 아래에서 시작하도록 위치 설정
        layoutMenu.animate().translationY(0).setDuration(300); // 메뉴 나타내기

        // + 아이콘을 X 아이콘으로 변경
        btnAdd.setImageResource(R.drawable.ic_close); // X 아이콘으로 변경
        isMenuVisible = true;
    }

    private void hideMenu() {
        layoutInput.animate().translationY(0).setDuration(300); // 입력창 내리기
        layoutMenu.animate().translationY(layoutMenu.getHeight()).setDuration(300)
                .withEndAction(() -> layoutMenu.setVisibility(View.GONE)); // 메뉴 숨기기

        // X 아이콘을 + 아이콘으로 변경
        btnAdd.setImageResource(R.drawable.ic_add); // + 아이콘으로 변경
        isMenuVisible = false;
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
                    Log.e("ChatActivity", "사용자 로드 중 오류: " + task.getException().getMessage());
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
                        Log.e("ChatActivity", "실시간 메시지 업데이트 실패.", e);
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
                        Log.d("ChatActivity", "현재 데이터: null");
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
                    Log.e("ChatActivity", "채팅 가져오기 오류: " + task.getException().getMessage());
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
                Log.e("ChatActivity", "초기 메시지 추가 오류: " + e.getMessage());
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
                        Log.e("ChatActivity", "메시지 로드 오류: " + e.getMessage());
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
                    Log.e("ChatActivity", "메시지 전송 오류: " + e.getMessage());
                });

                Log.d("ChatActivity", "메시지 전송 성공");
            } catch (Exception e) {
                Log.e("ChatActivity", "메시지 전송 오류: " + e.getMessage());
            }
        });
    }

    private void deleteChatAndMessages(String chatId) {
        db.collection("chats").document(chatId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("ChatActivity", "채팅 삭제 성공!"))
                .addOnFailureListener(e -> Log.e("ChatActivity", "채팅 삭제 오류", e));

        db.collection("messages")
                .whereEqualTo("chatId", chatId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        db.collection("messages").document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> Log.d("ChatActivity", "메시지 삭제 성공!"))
                                .addOnFailureListener(e -> Log.e("ChatActivity", "메시지 삭제 오류", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatActivity", "삭제할 메시지 찾기 오류", e));
    }

    private void fetchUserName(String userEmail) {
        db.collection("users").whereEqualTo("email", userEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                String userName = document.getString("name"); // users 컬렉션의 name 필드 가져오기
                textTitle.setText(userName); // 상단에 판매자 이름 설정
                receiverName = userName; // receiverName 설정
            } else {
                Log.e("ChatActivity", "사용자 이름 가져오기 오류: " + task.getException());
            }
        });
    }
}