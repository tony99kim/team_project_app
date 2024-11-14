package com.example.team_project.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

    private ArrayList<Chat> chats = new ArrayList<>(); // 채팅방 리스트
    private ArrayList<User> users = new ArrayList<>(); // 유저 리스트
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private String email = "";
    private String name = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar_chat);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setTitle("채팅창 목록");
        }

        Button addButton = view.findViewById(R.id.button_find_user);
        addButton.setOnClickListener(v -> showFragmentUser());

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatListAdapter(chats, users, chat -> {
            String receiverEmail = chat.getUserEmail1().equals(email) ? chat.getUserEmail2() : chat.getUserEmail1();
            String receiverName = getUserNameByEmail(receiverEmail);

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
                    users.add(user); // 사용자 추가
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
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "닉네임을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
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

    private String getUserNameByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user.getName(); // 사용자 이름 반환
            }
        }
        return email; // 사용자를 찾지 못한 경우 이메일 반환
    }
}
