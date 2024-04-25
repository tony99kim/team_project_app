package com.example.team_project.Home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.team_project.R;
import com.example.team_project.Toolbar.NotificationsFragment;
import com.example.team_project.Toolbar.SearchFragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);

        Button settingsButton = view.findViewById(R.id.btn_home_settings);

        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_search) {
                replaceFragment(new SearchFragment());
                return true;
            } else if (id == R.id.action_notifications) {
                replaceFragment(new NotificationsFragment());
                return true;
            }
            return false;
        });

        settingsButton.setOnClickListener(v -> replaceFragment(new HomeSettingsFragment()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 설정 변경 반영
        updateLayoutBasedOnSettings();
    }

    private void updateLayoutBasedOnSettings() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("HomeSettingsPrefs", getContext().MODE_PRIVATE);
        boolean showPopularPosts = sharedPref.getBoolean("popularPosts", true);
        boolean showEvents = sharedPref.getBoolean("events", true);

        View popularPostsSection = getView().findViewById(R.id.fragment_home_section_popular_posts);
        View eventsSection = getView().findViewById(R.id.fragment_home_section_events);

        // 인기 게시글 섹션의 표시 여부 설정
        popularPostsSection.setVisibility(showPopularPosts ? View.VISIBLE : View.GONE);

        // 이벤트 공지 섹션의 표시 여부 설정
        eventsSection.setVisibility(showEvents ? View.VISIBLE : View.GONE);
    }


    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
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
