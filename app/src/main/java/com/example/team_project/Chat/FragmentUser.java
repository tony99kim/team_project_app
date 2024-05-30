package com.example.team_project.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Chat.adapter.UserListAdapter;
import com.example.team_project.Chat.ChatData.User_ChatData;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class FragmentUser extends Fragment {
    private RecyclerView recyclerView;
    private UserListAdapter adapter;
    private ArrayList<User_ChatData> users = new ArrayList<>(); // 사용자 목록

    private String email = "";
    private String name = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_user, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserListAdapter(users, user -> {
            Intent intent = new Intent(getContext(), ActivityChat.class);
            intent.putExtra("userEmail1", email);
            intent.putExtra("userEmail2", user.getEmail());
            intent.putExtra("user1", name);
            intent.putExtra("user2", user.getName());

            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        getMyInfo();

        return view;
    }

    private void getMyInfo() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        email = documentSnapshot.getString("email");
                        name = documentSnapshot.getString("name");

                        loadUsers();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "닉네임을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    private void loadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User_ChatData user = document.toObject(User_ChatData.class);

                    if (user.getEmail().equals(email)) continue;

                    users.add(user);
                }
                adapter.notifyDataSetChanged();
            } else {
                // 데이터 로드 실패 처리
            }
        });
    }
}