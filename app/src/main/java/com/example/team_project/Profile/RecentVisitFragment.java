package com.example.team_project.Profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.team_project.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecentVisitFragment extends Fragment {

    private ListView recentVisitListView;
    private ArrayAdapter<String> adapter;
    private List<String> recentVisitList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_recent_visit_fragment, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("최근방문");
        }

        recentVisitListView = view.findViewById(R.id.recentVisitListView);

        // 사용자의 최근 방문 상품 가져오기
        getRecentVisits();

        return view;
    }

    private void getRecentVisits() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("recent_visit", Context.MODE_PRIVATE);
        Set<String> visitSet = sharedPreferences.getStringSet("visit_list", new HashSet<>());
        recentVisitList = new ArrayList<>(visitSet);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, recentVisitList);
        recentVisitListView.setAdapter(adapter);
    }
}
