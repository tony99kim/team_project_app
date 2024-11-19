// MainActivity.java
package com.example.team_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.team_project.Board.BoardFragment;
import com.example.team_project.Chat.ChatFragment;
import com.example.team_project.Environment.EnvironmentFragment;
import com.example.team_project.Home.HomeFragment;
import com.example.team_project.Profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private static final int LOGIN_REQUEST_CODE = 100;

    private final String PREFERENCES_NAME = "team_project_preferences";
    private final String PREF_KEY_IS_LOGGED_IN = "isLoggedIn";
    private final String PREF_KEY_USER_ID = "userId"; // 사용자 ID를 저장하기 위한 키

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeFragment || currentFragment instanceof EnvironmentFragment || currentFragment instanceof BoardFragment || currentFragment instanceof ChatFragment || currentFragment instanceof ProfileFragment) {
                showBottomNav();
            } else {
                hideBottomNav();
            }
        });

        // 로그인 상태를 확인하고, 필요한 경우 LoginActivity로 이동합니다.
        checkLoginState();
    }

    private void checkLoginState() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean(PREF_KEY_IS_LOGGED_IN, false);
        String userId = preferences.getString(PREF_KEY_USER_ID, null);

        if (!isLoggedIn || userId == null) {
            redirectToLogin();
        } else {
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    // 로그인 상태이므로 메인 화면 구성
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                } else {
                    // 사용자 ID가 존재하지 않으면 로그인 페이지로 이동
                    redirectToLogin();
                }
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST_CODE); // 로그인 액티비티로 이동
    }

    // LoginActivity로부터 결과를 받음
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // 로그인 성공 처리, 필요한 경우 메인 화면 구성
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            } else {
                // 로그인 실패 처리, 필요한 경우 추가 작업
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navigation_environment) {
                    selectedFragment = new EnvironmentFragment();
                } else if (itemId == R.id.navigation_board) {
                    selectedFragment = new BoardFragment();
                } else if (itemId == R.id.navigation_chat) {
                    selectedFragment = new ChatFragment();
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }

                return true;
            };

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    private void hideBottomNav() {
        bottomNav.setVisibility(BottomNavigationView.GONE);
    }

    private void showBottomNav() {
        bottomNav.setVisibility(BottomNavigationView.VISIBLE);
    }

    private void navigateToStoreFragment(int targetPage) {
        EnvironmentFragment environmentFragment = new EnvironmentFragment();
        Bundle args = new Bundle();
        args.putInt("targetPage", targetPage);
        environmentFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, environmentFragment).commit();
    }

    public void onRegisterProductClicked(View view) {
        // 클릭 이벤트 처리 로직
    }
}