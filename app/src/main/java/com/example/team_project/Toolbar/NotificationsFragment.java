package com.example.team_project.Toolbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private FirebaseFirestore db;
    private String myEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        // 현재 사용자 정보 가져오기
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            myEmail = currentUser.getEmail(); // 현재 사용자 이메일
        } else {
            // 사용자 로그인 안됨 처리
            myEmail = "unknown@example.com"; // 기본값 또는 에러 처리
        }

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        // 뒤로가기 버튼 설정
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("알림");
        }

        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NotificationsAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadNotifications();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNotifications() {
        db.collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING) // 최신 순 정렬
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NotificationItem> notificationList = new ArrayList<>();

                    for (var doc : queryDocumentSnapshots) {
                        String chatId = doc.getString("chatId");
                        String sender = doc.getString("sender"); // 이메일 형식
                        String content = doc.getString("content");

                        // "system" 메시지는 표시하지 않음
                        if ("system".equals(sender)) {
                            continue; // sender가 "system"일 경우 다음 반복으로 넘어감
                        }

                        if (chatId != null && chatId.contains(myEmail)) {
                            // 나와 관련된 메시지인 경우
                            String[] participants = chatId.split("_");
                            String otherEmail = participants[0].equals(myEmail) ? participants[1] : participants[0];

                            // 보낸 사람이 현재 사용자와 다를 경우에만 알림 추가
                            if (!sender.equals(myEmail)) {
                                // 사용자의 username을 가져오기 위한 쿼리
                                db.collection("users")
                                        .whereEqualTo("email", sender)
                                        .get()
                                        .addOnSuccessListener(userSnapshots -> {
                                            if (!userSnapshots.isEmpty()) {
                                                String username = userSnapshots.getDocuments().get(0).getString("username");

                                                // username과 메시지 내용을 NotificationItem에 설정
                                                notificationList.add(new NotificationItem(username, "새 메시지: " + content));

                                                // 데이터를 업데이트하고 RecyclerView 반영
                                                adapter.updateNotifications(notificationList);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // 사용자 정보 가져오기 실패 처리
                                            e.printStackTrace();
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // 메시지 쿼리 실패 처리
                    e.printStackTrace();
                });
    }


}