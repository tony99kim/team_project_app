package com.example.team_project.Environment.Store;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PaymentFragment extends AppCompatActivity {

    private String productId, price, deliveryDestination, request;
    private int accountBalance, environmentPoint;
    private TextView priceTextView, deliveryDestinationTextView, totalPriceTextView, environmentPointTextView;
    private EditText requestEditText, usePointsEditText;
    private Button selectDeliveryDestinationButton, payButton;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_environment_store_product_payment);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar_payment);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Intent로 전달된 데이터 받기
        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        price = intent.getStringExtra("price");

        // 결제 관련 UI 구성 (예: 가격 표시, 결제 버튼 등)
        priceTextView = findViewById(R.id.textView_price);
        priceTextView.setText(price);

        deliveryDestinationTextView = findViewById(R.id.textView_delivery_destination);
        requestEditText = findViewById(R.id.editText_request);
        totalPriceTextView = findViewById(R.id.textView_total_price);
        environmentPointTextView = findViewById(R.id.textView_environment_point);
        usePointsEditText = findViewById(R.id.editText_use_points);
        selectDeliveryDestinationButton = findViewById(R.id.button_select_delivery_destination);
        payButton = findViewById(R.id.button_pay);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loadUserInfo();

        selectDeliveryDestinationButton.setOnClickListener(v -> {
            Intent intent1 = new Intent(PaymentFragment.this, DeliveryDestinationActivity.class);
            startActivityForResult(intent1, 1);
        });

        usePointsEditText.setOnEditorActionListener((v, actionId, event) -> {
            updateTotalPrice();
            return false;
        });

        payButton.setOnClickListener(v -> processPayment());
    }

    private void loadUserInfo() {
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                accountBalance = documentSnapshot.getLong("accountBalance").intValue();
                environmentPoint = documentSnapshot.getLong("environmentPoint").intValue();
                deliveryDestination = documentSnapshot.getString("deliveryDestination");
                deliveryDestinationTextView.setText(deliveryDestination);
                environmentPointTextView.setText(String.valueOf(environmentPoint));
                updateTotalPrice();
            }
        });
    }

    private void updateTotalPrice() {
        int totalPrice = Integer.parseInt(price);
        String usePointsStr = usePointsEditText.getText().toString();
        int usePoints = usePointsStr.isEmpty() ? 0 : Integer.parseInt(usePointsStr);
        totalPrice -= usePoints;
        totalPriceTextView.setText(String.valueOf(totalPrice));
    }

    private void processPayment() {
        int totalPrice = Integer.parseInt(totalPriceTextView.getText().toString());
        String usePointsStr = usePointsEditText.getText().toString();
        int usePoints = usePointsStr.isEmpty() ? 0 : Integer.parseInt(usePointsStr);

        if (totalPrice > accountBalance) {
            Toast.makeText(this, "잔액이 부족합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 결제 정보 저장
        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("productId", productId);
        paymentInfo.put("price", price);
        paymentInfo.put("deliveryDestination", deliveryDestination);
        paymentInfo.put("request", requestEditText.getText().toString());
        paymentInfo.put("usePoints", usePoints);
        paymentInfo.put("userId", user.getUid());

        db.collection("payments").add(paymentInfo).addOnSuccessListener(documentReference -> {
            // 잔액 차감
            accountBalance -= totalPrice;
            environmentPoint -= usePoints;
            db.collection("users").document(user.getUid()).update("accountBalance", accountBalance);
            db.collection("users").document(user.getUid()).update("environmentPoint", environmentPoint);

            // 결제 완료 페이지로 이동
            Intent intent = new Intent(PaymentFragment.this, PaymentCompleteActivity.class);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "결제에 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            deliveryDestination = data.getStringExtra("deliveryDestination");
            deliveryDestinationTextView.setText(deliveryDestination);
        }
    }
}