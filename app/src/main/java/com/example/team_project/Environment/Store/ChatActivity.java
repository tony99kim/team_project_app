package com.example.team_project.Environment.Store;
import com.example.team_project.R;
// AndroidX AppCompatActivity import 추가
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat_fragment_user);  // 채팅 화면 레이아웃
    }
}