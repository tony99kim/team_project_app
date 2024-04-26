package com.example.team_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.team_project.Board.BoardFragment;
import com.example.team_project.Chat.FragmentChat;
import com.example.team_project.Home.HomeFragment;
import com.example.team_project.Home.HomeSettingsFragment;
import com.example.team_project.Toolbar.NotificationsFragment;
import com.example.team_project.Toolbar.SearchFragment;
import com.example.team_project.Profile.ProfileFragment;
import com.example.team_project.Store.StoreFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private static final int LOGIN_REQUEST_CODE = 100;

    private final String PREFERENCES_NAME = "team_project_preferences";
    private final String PREF_KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeSettingsFragment || currentFragment instanceof SearchFragment || currentFragment instanceof NotificationsFragment) {
                hideBottomNav();
            } else {
                showBottomNav();
            }
        });

        checkLoginState();
    }

    private void checkLoginState() {
        if (!isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    private boolean isLoggedIn() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        return preferences.getBoolean(PREF_KEY_IS_LOGGED_IN, false);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navigation_store) {
                    selectedFragment = new StoreFragment();
                } else if (itemId == R.id.navigation_board) {
                    selectedFragment = new BoardFragment();
                } else if (itemId == R.id.navigation_chat) {
                    selectedFragment = new FragmentChat();
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment();
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                return true;
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            }
        }
    }


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
}
