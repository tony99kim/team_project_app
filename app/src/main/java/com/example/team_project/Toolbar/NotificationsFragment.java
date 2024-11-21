package com.example.team_project.Toolbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
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
        View view = inflater.inflate(R.layout.fragment_toolbar_notification, container, false);

        // 현재 사용자 정보 가져오기
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            myEmail = currentUser.getEmail(); // 현재 사용자 이메일
        } else {
            myEmail = "unknown@example.com"; // 기본값 또는 에러 처리
        }

        Toolbar toolbar = view.findViewById(R.id.toolbar_notification);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("알림");
        }

        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Context와 빈 알림 리스트를 전달
        adapter = new NotificationsAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // 스와이프 삭제 기능 설정
        setupSwipeToDelete();

        // 알림 데이터 로드
        loadNotifications();

        return view;
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // 드래그 동작 비활성화
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.removeNotification(position); // 항목 삭제
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void loadNotifications() {
        db.collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NotificationItem> notificationList = new ArrayList<>();

                    for (var doc : queryDocumentSnapshots) {
                        String chatId = doc.getString("chatId");
                        String sender = doc.getString("sender");
                        String content = doc.getString("content");
                        String documentId = doc.getId(); // Firebase 문서 ID 추가

                        if ("system".equals(sender)) {
                            continue;
                        }

                        if (chatId != null && chatId.contains(myEmail)) {
                            String[] participants = chatId.split("_");
                            String otherEmail = participants[0].equals(myEmail) ? participants[1] : participants[0];

                            if (!sender.equals(myEmail)) {
                                db.collection("users")
                                        .whereEqualTo("email", sender)
                                        .get()
                                        .addOnSuccessListener(userSnapshots -> {
                                            if (!userSnapshots.isEmpty()) {
                                                String username = userSnapshots.getDocuments().get(0).getString("username");
                                                notificationList.add(new NotificationItem(username, "새 메시지: " + content, documentId));
                                                adapter.updateNotifications(notificationList);
                                            }
                                        })
                                        .addOnFailureListener(e -> e.printStackTrace());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
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
}
