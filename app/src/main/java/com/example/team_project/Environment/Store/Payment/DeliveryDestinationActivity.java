package com.example.team_project.Environment.Store.Payment;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DeliveryDestinationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser user;
    private ListView deliveryDestinationListView;
    private Button addDeliveryDestinationButton;
    private EditText newDeliveryDestinationEditText;
    private ArrayList<String> deliveryDestinationList;
    private DeliveryDestinationAdapter deliveryDestinationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_environment_store_product_payment_delivery_destination);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar_delivery_destination);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        deliveryDestinationListView = findViewById(R.id.delivery_destination_list_view);
        addDeliveryDestinationButton = findViewById(R.id.add_delivery_destination_button);
        newDeliveryDestinationEditText = findViewById(R.id.new_delivery_destination_edit_text);

        deliveryDestinationList = new ArrayList<>();
        deliveryDestinationAdapter = new DeliveryDestinationAdapter(this, deliveryDestinationList);
        deliveryDestinationListView.setAdapter(deliveryDestinationAdapter);

        loadDeliveryDestinations();

        addDeliveryDestinationButton.setOnClickListener(v -> addDeliveryDestination());
    }

    private void loadDeliveryDestinations() {
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                deliveryDestinationList.clear();
                ArrayList<String> destinations = (ArrayList<String>) documentSnapshot.get("deliveryDestinations");
                if (destinations != null) {
                    deliveryDestinationList.addAll(destinations);
                }
                deliveryDestinationAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addDeliveryDestination() {
        String newDeliveryDestination = newDeliveryDestinationEditText.getText().toString();
        if (newDeliveryDestination.isEmpty()) {
            Toast.makeText(this, "배송지를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        deliveryDestinationList.add(newDeliveryDestination);
        db.collection("users").document(user.getUid()).update("deliveryDestinations", deliveryDestinationList).addOnSuccessListener(aVoid -> {
            deliveryDestinationAdapter.notifyDataSetChanged();
            newDeliveryDestinationEditText.setText("");
            Toast.makeText(this, "배송지가 추가되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }
}