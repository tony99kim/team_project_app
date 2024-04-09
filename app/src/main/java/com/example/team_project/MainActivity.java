package com.example.team_project;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.team_project.Home.HomeFragment;
import com.example.team_project.Home.HomeSettingsFragment;
import com.example.team_project.Home.NotificationsFragment;
import com.example.team_project.Home.SearchFragment;
import com.example.team_project.Profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;


//민지 test1
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 액션바 설정
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 활성화
        }

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // 프래그먼트 매니저에 리스너 추가
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeSettingsFragment || currentFragment instanceof SearchFragment || currentFragment instanceof NotificationsFragment) {
                // 특정 프래그먼트에서 하단 메뉴바 숨기기
                hideBottomNav();
            } else {
                // 그 외의 경우 하단 메뉴바 보이기
                showBottomNav();
            }
        });

        // 앱이 시작될 때 기본으로 보여줄 프래그먼트를 설정합니다.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    int itemId = item.getItemId();
                    if (itemId == R.id.navigation_home) {
                        selectedFragment = new HomeFragment();
                    } else if (itemId == R.id.navigation_store) {
                        selectedFragment = new StoreFragment();
                    } else if (itemId == R.id.navigation_board) {
                        selectedFragment = new BoardFragment();
                    } else if (itemId == R.id.navigation_chat) {
                        selectedFragment = new ChatFragment();
                    } else if (itemId == R.id.navigation_profile) {
                        selectedFragment = new ProfileFragment();
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                    return true;
                }
            };

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack(); // 뒤로가기 스택에서 이전 프래그먼트로 돌아가기
        return true;
    }

    private void hideBottomNav() {
        bottomNav.setVisibility(BottomNavigationView.GONE); // 하단 메뉴바 숨기기
    }

    private void showBottomNav() {
        bottomNav.setVisibility(BottomNavigationView.VISIBLE); // 하단 메뉴바 보이기
    }
}
