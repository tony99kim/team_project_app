package com.example.team_project.Environment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.team_project.Environment.Point.PointFragment;
import com.example.team_project.Environment.Store.StoreFragment;

public class EnvironmentPagerAdapter extends FragmentStateAdapter {

    public EnvironmentPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 포인트 페이지와 스토어 페이지로 분기
        switch (position) {
            case 0:
                return new PointFragment(); // 포인트 페이지 프래그먼트
            case 1:
                return new StoreFragment(); // 스토어 페이지 프래그먼트
            default:
                throw new IllegalStateException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        // 탭의 총 개수 반환
        return 2;
    }
}
