package com.example.team_project.Environment.Store.Payment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
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
    private FirebaseFirestore db;
    private String productId, title, phone, deliveryDestination, request;
    private double price, totalPrice;
    private int usePoints;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Intent로부터 결제 정보 가져오기
        productId = getIntent().getStringExtra("productId");
        price = Double.parseDouble(getIntent().getStringExtra("price"));
        totalPrice = Double.parseDouble(getIntent().getStringExtra("totalPrice"));
        title = getIntent().getStringExtra("title");
        phone = getIntent().getStringExtra("phone");
        deliveryDestination = getIntent().getStringExtra("deliveryDestination");
        request = getIntent().getStringExtra("request");
        usePoints = getIntent().getIntExtra("usePoints", 0);

        // Bootpay 결제 요청
        BootUser user = new BootUser().setPhone(phone); // 구매자 정보

        BootExtra extra = new BootExtra()
                .setCardQuota("0,2,3"); // 일시불, 2개월, 3개월 할부 허용

        List<BootItem> items = new ArrayList<>();
        BootItem item = new BootItem().setName(title).setId(productId).setQty(1).setPrice(totalPrice);
        items.add(item);

        Payload payload = new Payload();
        payload.setApplicationId("6641c2390a4877c5eb277af5")
                .setOrderName(title)
                .setPg("kcp")
                .setMethod("card")
                .setOrderId("order_" + System.currentTimeMillis())
                .setPrice(totalPrice)
                .setUser(user)
                .setExtra(extra)
                .setItems(items);

        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("title", title);
        map.put("price", price);
        map.put("totalPrice", totalPrice);
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
                        // 결제 완료 후 PaymentCompleteFragment로 이동
                        savePaymentInfo();
                        Intent intent = new Intent(PaymentActivity.this, PaymentCompleteActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).requestPayment();
    }

    private void savePaymentInfo() {
        // 결제 정보 저장
        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("productId", productId);
        paymentInfo.put("price", price);
        paymentInfo.put("finalPrice", totalPrice); // 최종 결제 금액
        paymentInfo.put("deliveryDestination", deliveryDestination);
        paymentInfo.put("request", request);
        paymentInfo.put("usePoints", usePoints);
        paymentInfo.put("userId", auth.getCurrentUser().getUid());
        paymentInfo.put("createdAt", new Date());
        paymentInfo.put("type", "결제"); // 결제 타입 추가
        paymentInfo.put("productTitle", title); // 상품명 추가

        db.collection("payments").add(paymentInfo).addOnSuccessListener(documentReference -> {
            // 잔액 차감
            db.collection("users").document(auth.getCurrentUser().getUid()).update("accountBalance", FieldValue.increment(-totalPrice));
            db.collection("users").document(auth.getCurrentUser().getUid()).update("environmentPoint", FieldValue.increment(-usePoints));
        }).addOnFailureListener(e -> Log.e("PaymentActivity", "결제 정보 저장 실패", e));
    }
}