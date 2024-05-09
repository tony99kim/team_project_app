package com.example.team_project.Board;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.Board.BoardWriteFragment;
import com.example.team_project.Home.HomeSettingsFragment;
import com.example.team_project.R;
import com.example.team_project.Toolbar.NotificationsFragment;
import com.example.team_project.Toolbar.SearchFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.team_project.R;

public class BoardFragment extends Fragment {
    private ViewPager2 viewPager;
    private Button boardwriteButton;
    private androidx.appcompat.widget.Toolbar board_toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);

        viewPager = view.findViewById(R.id.board_view_pager);
        BoardFragmentPagerAdapter adapter = new BoardFragmentPagerAdapter(getActivity());
        viewPager.setAdapter(adapter);

        TextView newsTextView = view.findViewById(R.id.news_board_textview);
        TextView freeTextView = view.findViewById(R.id.free_board_textview);
        TextView eventTextView = view.findViewById(R.id.event_board_textview);
        TextView volunteerTextView = view.findViewById(R.id.volunteer_board_textview);

        TextView myPostTextView = view.findViewById(R.id.board_my_post_list_textview);
        TextView myCommentPostTextView = view.findViewById(R.id.board_my_comment_post_list_textview);
        TextView myFavoritesPostTextView = view.findViewById(R.id.board_my_favorites_post_list_textview);

        // 각 TextView의 클릭 이벤트 처리
        newsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newsTextView.setTypeface(Typeface.DEFAULT_BOLD);
                freeTextView.setTypeface(Typeface.DEFAULT);
                eventTextView.setTypeface(Typeface.DEFAULT);
                volunteerTextView.setTypeface(Typeface.DEFAULT);
                viewPager.setCurrentItem(0);
            }
        });

        freeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeTextView.setTypeface(Typeface.DEFAULT_BOLD);
                newsTextView.setTypeface(Typeface.DEFAULT);
                eventTextView.setTypeface(Typeface.DEFAULT);
                volunteerTextView.setTypeface(Typeface.DEFAULT);
                viewPager.setCurrentItem(1);
            }
        });

        eventTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventTextView.setTypeface(Typeface.DEFAULT_BOLD);
                newsTextView.setTypeface(Typeface.DEFAULT);
                freeTextView.setTypeface(Typeface.DEFAULT);
                volunteerTextView.setTypeface(Typeface.DEFAULT);
                viewPager.setCurrentItem(2);
            }
        });

        volunteerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volunteerTextView.setTypeface(Typeface.DEFAULT_BOLD);
                newsTextView.setTypeface(Typeface.DEFAULT);
                freeTextView.setTypeface(Typeface.DEFAULT);
                eventTextView.setTypeface(Typeface.DEFAULT);
                viewPager.setCurrentItem(3);
            }
        });



        // 사용자 맞춤 카테고리

        myPostTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 내가 쓴 글 Fragment로 이동하는 코드
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new BoardMyPostFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        myCommentPostTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 댓글 단 글 Fragment로 이동하는 코드
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new BoardMyCommentFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        myFavoritesPostTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 즐겨찾기 한 글 Fragment로 이동하는 코드
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new BoardMyFavoritesFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });









        // 게시판 작성 버튼 클릭 이벤트 처리
        boardwriteButton = view.findViewById(R.id.boardwriteButton);
        boardwriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBoardWriteFragment();
            }
        });

        // 툴바 설정
        board_toolbar = view.findViewById(R.id.board_toolbar);
        // 툴바 타이틀 설정
        board_toolbar.setTitle("게시판");
        board_toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        // 툴바 메뉴 설정
        board_toolbar.setOnMenuItemClickListener(item -> {
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

        return view;
    }

    private void openBoardWriteFragment() {
        BoardWriteFragment fragment = new BoardWriteFragment();

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
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