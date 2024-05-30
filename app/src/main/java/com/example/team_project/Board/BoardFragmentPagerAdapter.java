package com.example.team_project.Board;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.team_project.Board.BoardKategorie.BoardEventFragment;
import com.example.team_project.Board.BoardKategorie.BoardFreeFragment;
import com.example.team_project.Board.BoardKategorie.BoardNewsFragment;
import com.example.team_project.Board.BoardKategorie.BoardVolunteerFragment;

public class BoardFragmentPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 4; // 페이지 수

    public BoardFragmentPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BoardNewsFragment(); // 첫 번째 탭의 프래그먼트
            case 1:
                return new BoardFreeFragment(); // 두 번째 탭의 프래그먼트
            case 2:
                return new BoardEventFragment(); // 세 번째 탭의 프래그먼트
            case 3:
                return new BoardVolunteerFragment(); // 네 번째 탭의 프래그먼트
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES; // 페이지 수 반환
    }
}
