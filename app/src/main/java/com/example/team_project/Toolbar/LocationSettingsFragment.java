package com.example.team_project.Toolbar;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.team_project.R;

public class LocationSettingsFragment extends Fragment {

    private Toolbar toolbarLocationSettings;

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
    }
}
