package com.example.team_project.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.team_project.AddressPickerActivity;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationSettingsFragment extends Fragment {

    private static final int REQUEST_ADDRESS_PICKER = 1;
    private Toolbar toolbarLocationSettings;
    private FirebaseFirestore db;
    private TextView currentAddressTextView;
    private EditText newAddressEditText;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_toolbar_location_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbarLocationSettings = view.findViewById(R.id.toolbar_location_settings);
        currentAddressTextView = view.findViewById(R.id.currentAddressTextView);
        newAddressEditText = view.findViewById(R.id.newAddressEditText);
        Button btnSetLocation = view.findViewById(R.id.btnSetLocation);
        Button btnSaveAddress = view.findViewById(R.id.btnSaveAddress);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 액티비티의 액션바(툴바)로 설정
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbarLocationSettings);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        toolbarLocationSettings.setNavigationOnClickListener(v -> {
            // Fragment 스택에서 현재 Fragment를 제거하여 이전 화면으로 돌아감
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        // Fetch and display current address
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentAddress = documentSnapshot.getString("address");
                currentAddressTextView.setText(currentAddress != null ? currentAddress : "주소가 설정되지 않았습니다.");
            }
        });

        // Start AddressPickerActivity
        btnSetLocation.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddressPickerActivity.class);
            startActivityForResult(intent, REQUEST_ADDRESS_PICKER);
        });

        // Save new address
        btnSaveAddress.setOnClickListener(v -> {
            String newAddress = newAddressEditText.getText().toString();
            if (!newAddress.isEmpty()) {
                db.collection("users").document(userId).update("address", newAddress)
                        .addOnSuccessListener(aVoid -> {
                            currentAddressTextView.setText(newAddress);
                            newAddressEditText.setText("");
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure
                        });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADDRESS_PICKER && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            String selectedAddress = data.getStringExtra("selectedAddress");
            if (selectedAddress != null) {
                newAddressEditText.setText(selectedAddress);
            }
        }
    }
}