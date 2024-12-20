package com.example.team_project.Environment.Store.Payment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentFragment extends Fragment {

    private String productId, price, deliveryDestination, request, productTitle, phone;
    private int accountBalance, environmentPoint;
    private TextView priceTextView, deliveryDestinationTextView, totalPriceTextView, environmentPointTextView;
    private EditText requestEditText, usePointsEditText;
    private Button selectDeliveryDestinationButton, payButton;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment_store_product_payment, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar_payment);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> navigateBack());

        // Intent로 전달된 데이터 받기
        Bundle args = getArguments();
        if (args != null) {
            productId = args.getString("productId");
            price = args.getString("price");
            productTitle = args.getString("title"); // 상품명 받기
            phone = args.getString("phone"); // 전화번호 받기
        }

        // 결제 관련 UI 구성 (예: 가격 표시, 결제 버튼 등)
        priceTextView = view.findViewById(R.id.textView_price);
        priceTextView.setText(price);

        deliveryDestinationTextView = view.findViewById(R.id.textView_delivery_destination);
        requestEditText = view.findViewById(R.id.editText_request);
        totalPriceTextView = view.findViewById(R.id.textView_total_price);
        environmentPointTextView = view.findViewById(R.id.textView_environment_point);
        usePointsEditText = view.findViewById(R.id.editText_use_points);
        selectDeliveryDestinationButton = view.findViewById(R.id.button_select_delivery_destination);
        payButton = view.findViewById(R.id.button_pay);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loadUserInfo();

        selectDeliveryDestinationButton.setOnClickListener(v -> {
            try {
                Intent intent1 = new Intent(getActivity(), DeliveryDestinationActivity.class);
                startActivityForResult(intent1, 1);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "배송지 선택 페이지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        usePointsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalPrice();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        payButton.setOnClickListener(v -> processPayment());

        return view;
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

        // 사용 가능한 포인트보다 많이 입력하지 않도록 제한
        if (usePoints > environmentPoint) {
            usePointsEditText.setText(String.valueOf(environmentPoint));
            usePoints = environmentPoint;
        }

        totalPrice -= usePoints;
        totalPriceTextView.setText(String.valueOf(totalPrice));
    }

    private void processPayment() {
        int totalPrice = Integer.parseInt(totalPriceTextView.getText().toString());
        String usePointsStr = usePointsEditText.getText().toString();
        int usePoints = usePointsStr.isEmpty() ? 0 : Integer.parseInt(usePointsStr);

        if (totalPrice > accountBalance) {
            Toast.makeText(getActivity(), "잔액이 부족합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // PaymentActivity로 이동
        Intent intent = new Intent(getActivity(), PaymentActivity.class);
        intent.putExtra("productId", productId);
        intent.putExtra("price", String.valueOf(totalPrice)); // 최종 결제 금액 전달
        intent.putExtra("title", productTitle);
        intent.putExtra("phone", phone);
        intent.putExtra("deliveryDestination", deliveryDestination);
        intent.putExtra("request", requestEditText.getText().toString());
        intent.putExtra("usePoints", usePoints);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            deliveryDestination = data.getStringExtra("deliveryDestination");
            deliveryDestinationTextView.setText(deliveryDestination);
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right);
        }
    }

    private void navigateBack() {
        getActivity().getSupportFragmentManager().popBackStack();
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}