package com.example.team_project.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.team_project.MainActivity;
import com.example.team_project.R;

public class IntroActiviy extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        moveMain(1);	//1초 후 main activity 로 넘어감
    }

    private void moveMain(int sec) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);  // intent 에 명시된 액티비티로 이동

                finish();  // 현재 액티비티 종료
            }
        }, 1000 * sec);  // sec초 정도 딜레이를 준 후 시작
    }

}
