package com.example.team_project.Home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.team_project.R;

public class HomeSettingsFragment extends Fragment {

    private CheckBox checkboxPopularPosts;
    private CheckBox checkboxEvents;

    private static final String PREFS_NAME = "HomeSettingsPrefs";
    private static final String POPULAR_POSTS = "popularPosts";
    private static final String EVENTS = "events";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_settings, container, false);

        checkboxPopularPosts = view.findViewById(R.id.checkbox_popular_posts);
        checkboxEvents = view.findViewById(R.id.checkbox_events);

        loadSettings();

        checkboxPopularPosts.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        checkboxEvents.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());

        Toolbar toolbar = view.findViewById(R.id.toolbar_home_settings);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("홈 화면 설정");
        }

        return view;
    }

    private void saveSettings() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(POPULAR_POSTS, checkboxPopularPosts.isChecked());
        editor.putBoolean(EVENTS, checkboxEvents.isChecked());

        editor.apply();
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        checkboxPopularPosts.setChecked(sharedPreferences.getBoolean(POPULAR_POSTS, true));
        checkboxEvents.setChecked(sharedPreferences.getBoolean(EVENTS, true));
    }
}
