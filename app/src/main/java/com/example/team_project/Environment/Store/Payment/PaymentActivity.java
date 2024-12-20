package com.example.team_project.Environment.Store.Payment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.team_project.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.bootpay.android.Bootpay;
import kr.co.bootpay.android.events.BootpayEventListener;
import kr.co.bootpay.android.models.BootExtra;
import kr.co.bootpay.android.models.BootItem;
import kr.co.bootpay.android.models.BootUser;
import kr.co.bootpay.android.models.Payload;

public class PaymentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Intent로부터 결제 정보 가져오기
        String productId = getIntent().getStringExtra("productId");
        String price = getIntent().getStringExtra("price");
        String title = getIntent().getStringExtra("title");
        String phone = getIntent().getStringExtra("phone");

        // Bootpay 결제 요청
        BootUser user = new BootUser().setPhone(phone); // 구매자 정보

        BootExtra extra = new BootExtra()
                .setCardQuota("0,2,3"); // 일시불, 2개월, 3개월 할부 허용

        List<BootItem> items = new ArrayList<>();
        BootItem item = new BootItem().setName(title).setId(productId).setQty(1).setPrice(Double.parseDouble(price));
        items.add(item);

        Payload payload = new Payload();
        payload.setApplicationId("5b8f6a4d396fa665fdc2b5e8")
                .setOrderName(title)
                .setPg("kcp")
                .setMethod("card")
                .setOrderId("order_" + System.currentTimeMillis())
                .setPrice(Double.parseDouble(price))
                .setUser(user)
                .setExtra(extra)
                .setItems(items);

        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("title", title);
        map.put("price", price);
        payload.setMetadata(map);

        Bootpay.init(getSupportFragmentManager(), getApplicationContext())
                .setPayload(payload)
                .setEventListener(new BootpayEventListener() {
                    @Override
                    public void onCancel(String data) {
                        Log.d("bootpay", "cancel: " + data);
                    }

                    @Override
                    public void onError(String data) {
                        Log.d("bootpay", "error: " + data);
                    }

                    @Override
                    public void onClose() {
                        Log.d("bootpay", "close");
                        Bootpay.removePaymentWindow();
                    }

                    @Override
                    public void onIssued(String data) {
                        Log.d("bootpay", "issued: " + data);
                    }

                    @Override
                    public boolean onConfirm(String data) {
                        Log.d("bootpay", "confirm: " + data);
                        return true; // 재고가 있어서 결제를 진행하려 할 때 true
                    }

                    @Override
                    public void onDone(String data) {
                        Log.d("done", data);
                        // 결제 완료 후 PaymentCompleteActivity로 이동
                        Intent intent = new Intent(PaymentActivity.this, PaymentCompleteActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).requestPayment();
    }
}