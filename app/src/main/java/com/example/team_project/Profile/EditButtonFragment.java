package com.example.team_project.Profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditButtonFragment extends Fragment {
    private EditText usernameEditText;
    private Button saveButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit_button_fragment, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("프로필 편집");
        }

        usernameEditText = view.findViewById(R.id.usernameEditText);
        saveButton = view.findViewById(R.id.saveButton);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "사용자 이름");
        usernameEditText.setText(savedUsername);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                if (!username.isEmpty()) {
                    saveProfile(username);
                } else {
                    Toast.makeText(getActivity(), "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void saveProfile(String username) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.apply();

        usernameEditText.setText(username);
        Toast.makeText(getActivity(), "이름이 변경되었습니다: " + username, Toast.LENGTH_SHORT).show();

        saveProfileToFirebase(username);
    }

    private void saveProfileToFirebase(String username) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "프로필 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show());
    }
}