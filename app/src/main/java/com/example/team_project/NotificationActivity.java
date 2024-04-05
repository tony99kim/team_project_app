package com.example.team_project;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recycler_view_notification);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 데이터 로딩 및 어댑터 설정
        loadNotifications();
    }

    private void loadNotifications() {
        // 여기서 알림 데이터를 로드합니다.
        // 예제를 위해 임시 데이터를 생성하겠습니다.
        ArrayList<NotificationItem> items = new ArrayList<>();
        // 예시 데이터 추가
        items.add(new NotificationItem("새로운 알림", "방금"));
        items.add(new NotificationItem("이것은 예시 알림입니다", "5분 전"));

        adapter = new NotificationAdapter(items);
        recyclerView.setAdapter(adapter);
    }
}
