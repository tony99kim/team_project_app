package com.example.team_project.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.team_project.LoginActivity;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private TextView usernameTextView, environmentPointsTextView;
    private ImageView profileImageView;
    private Button editButton, recentVisitButton, noticeButton, customerServiceButton, logoutButton, withdrawButton;
    private androidx.appcompat.widget.Toolbar toolbar;

    private ListView recentVisitListView;
    private ArrayList<String> recentVisitList;
    private ArrayAdapter<String> adapter;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        recentVisitList = new ArrayList<>();
        recentVisitListView = view.findViewById(R.id.recentVisitListView);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, recentVisitList);
        recentVisitListView.setAdapter(adapter);

        profileImageView = view.findViewById(R.id.profileImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        recentVisitButton = view.findViewById(R.id.recentVisitButton);
        noticeButton = view.findViewById(R.id.noticeButton);
        customerServiceButton = view.findViewById(R.id.customerServiceButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        withdrawButton = view.findViewById(R.id.withdrawButton);
        editButton = view.findViewById(R.id.editButton);
        toolbar = view.findViewById(R.id.toolbar);
        environmentPointsTextView = view.findViewById(R.id.environmentPointsTextView);

        setUsername();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setTitle("마이 페이지");
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        recentVisitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRecentVisitFragment();
            }
        });

        noticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoticeFragment();
            }
        });

        customerServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCustomerServiceFragment();
            }
        });

        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWithdrawFragment();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditButtonFragment();
            }
        });

        return view;
    }

    private void logout() {
        SharedPreferences prefs = getActivity().getSharedPreferences("team_project_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("autoLogin", false);
        editor.apply();

        mAuth.signOut();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void openRecentVisitFragment() {
        RecentVisitFragment fragment = new RecentVisitFragment();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openNoticeFragment() {
        NoticeFragment fragment = new NoticeFragment();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openCustomerServiceFragment() {
        CustomerServiceFragment fragment = new CustomerServiceFragment();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openWithdrawFragment() {
        WithdrawFragment fragment = new WithdrawFragment();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openEditButtonFragment() {
        EditButtonFragment fragment = new EditButtonFragment();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUsernameFromFirebase(); // Firebase에서 닉네임을 가져와서 설정
    }

    private void setUsernameFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        if (username != null && !username.isEmpty()) {
                            // 가져온 닉네임을 SharedPreferences에 저장
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", username);
                            editor.apply();

                            // 화면에 표시
                            usernameTextView.setText(username);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "닉네임을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    private void setUsername() {
        String savedUsername = sharedPreferences.getString("username", "사용자 이름");
        usernameTextView.setText(savedUsername);
    }
}


