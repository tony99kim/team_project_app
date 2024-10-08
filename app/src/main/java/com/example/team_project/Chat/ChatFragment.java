package com.example.team_project.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Chat.ChatAdapter.ChatListAdapter;
import com.example.team_project.Chat.Data.Chat;
import com.example.team_project.Chat.Data.User;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private LinearLayout layout; // 뷰 참조를 유지
    private ArrayList<Chat> chats = new ArrayList<>(); // 채팅방 리스트
    private ArrayList<User> users = new ArrayList<>(); // 유저 리스트
    private ArrayList<String> chatRooms = new ArrayList<>(); // 채팅방 리스트

    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private String email = "";
    private String name = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        //layout = view.findViewById(R.id.linear_layout_chat_rooms);

        Button addButton = view.findViewById(R.id.button_find_user);
        addButton.setOnClickListener(v -> showFragmentUser());

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatListAdapter(chats, users, chat -> {

            String receiverEmail = "";
            String receiverName = "";

            if (chat.getUserEmail1().equals(email)) {
                receiverEmail = chat.getUserEmail2();
            } else {
                receiverEmail = chat.getUserEmail1();
            }

            for (User user : users) {
                if (user.getEmail().equals(receiverEmail)) {
                    receiverName = user.getName();
                    break;
                }
            }

            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("userEmail1", email);
            intent.putExtra("userEmail2", receiverEmail);
            intent.putExtra("user1", name);
            intent.putExtra("user2", receiverName);

            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    private void showFragmentUser() {
        UserFragment fragmentUser = new UserFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragmentUser);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    private void loadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);

                    users.add(user);
                }
                getMyInfo();

            } else {
                // 데이터 로드 실패 처리
            }
        });
    }

    private void getMyInfo() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        email = documentSnapshot.getString("email");
                        name = documentSnapshot.getString("name");

                        adapter.setUserEmail(email);
                        loadChats();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "닉네임을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    private void loadChats() {
        chats.clear();

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("chats").whereEqualTo("userEmail1", email).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        chats.addAll(queryDocumentSnapshots.toObjects(Chat.class));
                        db.collection("chats").whereEqualTo("userEmail2", email).get()
                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                    chats.addAll(queryDocumentSnapshots2.toObjects(Chat.class));
                                    chats.sort((c1, c2) -> c2.getUpdatedAt().compareTo(c1.getUpdatedAt()));

                                    adapter.notifyDataSetChanged();
                                });
                    });
        } catch (Exception e) {
            Log.e("ChatListFragment", "loadChats: " + e.getMessage());
        }
    }

    private void showAddRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("새 채팅방 이름 입력");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("추가", (dialog, which) -> {
            String roomName = input.getText().toString();
            if (!roomName.isEmpty()) {
                chatRooms.add(roomName);
                updateUI();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUI() {
        if (getContext() == null) return;
        layout.removeAllViews();

        for (String roomName : chatRooms) {
            LinearLayout roomLayout = new LinearLayout(getContext());
            roomLayout.setOrientation(LinearLayout.HORIZONTAL);
            roomLayout.setPadding(10, 10, 10, 10);

            TextView textView = new TextView(getContext());
            textView.setText(roomName);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            roomLayout.addView(textView);

            Button enterButton = new Button(getContext());
            enterButton.setText("입장");
            enterButton.setOnClickListener(v -> enterChatRoom(roomName));
            roomLayout.addView(enterButton);

            Button deleteButton = new Button(getContext());
            deleteButton.setText("나가기");
            deleteButton.setOnClickListener(v -> deleteChatRoom(roomName));
            roomLayout.addView(deleteButton);

            layout.addView(roomLayout);
        }

    }

    private void deleteChatRoom(String roomName) {
        chatRooms.remove(roomName);
        updateUI();
    }

    private void enterChatRoom(String roomName) {
        ChatRoomFragment fragmentChatRoom = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putString("room_name", roomName);
        fragmentChatRoom.setArguments(args);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragmentChatRoom);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}