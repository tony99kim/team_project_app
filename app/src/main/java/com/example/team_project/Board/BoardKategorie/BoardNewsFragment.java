package com.example.team_project.Board.BoardKategorie;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.Board.BoardPostFragment;
import com.example.team_project.R;

public class BoardNewsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board_news_post_list, container, false);

        // LinearLayout 찾기
        LinearLayout boardNewsPost1 = view.findViewById(R.id.board_news_post1);

        // LinearLayout에 클릭 이벤트 리스너 추가
        boardNewsPost1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 시 fragment_board_post.xml로 이동하는 코드
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new BoardPostFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
}