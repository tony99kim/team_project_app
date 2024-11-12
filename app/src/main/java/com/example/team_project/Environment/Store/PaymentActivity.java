package com.example.team_project.Environment.Store;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.team_project.R;

public class PaymentActivity extends AppCompatActivity {

    private String productId, price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Intent로 전달된 데이터 받기
        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        price = intent.getStringExtra("price");

        // 결제 관련 UI 구성 (예: 가격 표시, 결제 버튼 등)
        TextView priceTextView = findViewById(R.id.textView_price);
        priceTextView.setText(price);

        // 결제 로직을 여기에 추가 (예: 결제 API 연동 등)
    }
}
