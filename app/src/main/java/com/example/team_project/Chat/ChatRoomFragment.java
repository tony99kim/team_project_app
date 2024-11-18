package com.example.team_project.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
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

public class ChatRoomFragment extends Fragment {

    private FirebaseFirestore db;

    private TextView textTitle;
    private RecyclerView recyclerView;
    private EditText editInput;
    private ImageView btnSend;
    private ImageView btnBack;
    private ImageView btnExit;
    private ImageView btnAdd;
    private ConstraintLayout layoutInput;
    private LinearLayout layoutMenu;

    private String user1;
    private String user2;

    private ArrayList<Message> messages = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);

        db = FirebaseFirestore.getInstance();

        String userEmail1 = getActivity().getIntent().getStringExtra("userEmail1");
        String userEmail2 = getActivity().getIntent().getStringExtra("userEmail2");
        user1 = getActivity().getIntent().getStringExtra("user1");
        user2 = getActivity().getIntent().getStringExtra("user2");

        // 상대방 이메일로 사용자 이름 가져오기
        fetchUserName(userEmail2);

        String chatId = userEmail1.compareTo(userEmail2) < 0 ? userEmail1 + "_" + userEmail2 : userEmail2 + "_" + userEmail1;

        textTitle = view.findViewById(R.id.text_user);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new MessageListAdapter(userEmail1, messages, users));

        editInput = view.findViewById(R.id.edit_input);
        btnSend = view.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            if (!editInput.getText().toString().isEmpty()) {
                sendMessage(chatId, userEmail1, editInput.getText().toString());
                editInput.setText("");
            }
        });

        btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> getActivity().finish());

        btnExit = view.findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("채팅 종료");
            builder.setMessage("채팅방을 나가시겠습니까?");
            builder.setPositiveButton("확인", (dialog, which) -> {
                deleteChatAndMessages(chatId);
                getActivity().finish();
            });
            builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        btnAdd = view.findViewById(R.id.btn_add);
        layoutInput = view.findViewById(R.id.layout_input);
        layoutMenu = view.findViewById(R.id.layout_menu);

        btnAdd.setOnClickListener(v -> {
            if (layoutMenu.getVisibility() == View.GONE) {
                layoutMenu.setVisibility(View.VISIBLE);
                layoutInput.animate().translationY(-layoutMenu.getHeight()).setDuration(300).start();
            } else {
                layoutMenu.setVisibility(View.GONE);
                layoutInput.animate().translationY(0).setDuration(300).start();
            }
        });

        view.findViewById(R.id.main_layout).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && layoutMenu.getVisibility() == View.VISIBLE) {
                layoutMenu.setVisibility(View.GONE);
                layoutInput.animate().translationY(0).setDuration(300).start();
            }
            return false;
        });

        fetchChat(chatId, userEmail1, userEmail2);
        setupRealtimeMessageUpdates(chatId);
        loadUsers();

        return view;
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
                    getActivity().runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged());
                } else {
                    Log.e("ChatRoomFragment", "Error loading users: " + task.getException().getMessage());
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
                        Log.e("ChatRoomFragment", "Listen failed.", e);
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

                        getActivity().runOnUiThread(() -> {
                            recyclerView.getAdapter().notifyDataSetChanged();
                            recyclerView.scrollToPosition(messages.size() - 1);
                        });
                    } else {
                        Log.d("ChatRoomFragment", "Current data: null");
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
                    Log.e("ChatRoomFragment", "Error fetching chat: " + task.getException().getMessage());
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
                getActivity().runOnUiThread(() -> {
                    recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.scrollToPosition(messages.size() - 1);
                });
                db.collection("chats").document(chatId)
                        .update("lastMessage", "채팅방이 열렸습니다.", "updatedAt", new Date());
            }).addOnFailureListener(e -> {
                Log.e("ChatRoomFragment", "addInitialMessage: Error adding initial message: " + e.getMessage());
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

                        getActivity().runOnUiThread(() -> {
                            recyclerView.getAdapter().notifyDataSetChanged();
                            recyclerView.scrollToPosition(messages.size() - 1);
                        });
                    }).addOnFailureListener(e -> {
                        Log.e("ChatRoomFragment", "loadMessages: " + e.getMessage());
                    });
        });
    }

    private void sendMessage(String chatId, String email, String content) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Message newMessage = new Message(chatId, email, content, new Date());
                messages.add(newMessage);
                getActivity().runOnUiThread(() -> {
                    recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.scrollToPosition(messages.size() - 1);
                });

                db.collection("messages").add(newMessage).addOnSuccessListener(documentReference -> {
                    db.collection("chats").document(chatId)
                            .update("lastMessage", content, "updatedAt", new Date());
                }).addOnFailureListener(e -> {
                    Log.e("ChatRoomFragment", "sendMessage: Error sending message: " + e.getMessage());
                });

                Log.d("ChatRoomFragment", "Message sent successfully");
            } catch (Exception e) {
                Log.e("ChatRoomFragment", "sendMessage: Error sending message: " + e.getMessage());
            }
        });
    }

    private void deleteChatAndMessages(String chatId) {
        db.collection("chats").document(chatId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("ChatRoomFragment", "Chat successfully deleted!"))
                .addOnFailureListener(e -> Log.e("ChatRoomFragment", "Error deleting chat", e));

        db.collection("messages")
                .whereEqualTo("chatId", chatId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        db.collection("messages").document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> Log.d("ChatRoomFragment", "Message successfully deleted!"))
                                .addOnFailureListener(e -> Log.e("ChatRoomFragment", "Error deleting message", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatRoomFragment", "Error finding messages to delete", e));
    }

    private void fetchUserName(String userEmail) {
        db.collection("users").whereEqualTo("email", userEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                String userName = document.getString("name"); // users 컬렉션의 name 필드 가져오기
                textTitle.setText(userName); // 상단에 판매자 이름 설정
            } else {
                Log.e("ChatRoomFragment", "Error fetching user name: " + task.getException());
            }
        });
    }

}