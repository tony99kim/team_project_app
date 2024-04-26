package com.example.team_project.Board;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.team_project.Board.BoardWriteFragment;
import com.example.team_project.R;

public class BoardFragment extends Fragment {
    private Button boardwriteButton;
    private androidx.appcompat.widget.Toolbar board_toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);

        boardwriteButton = view.findViewById(R.id.boardwriteButton);
        // 툴바 설정
        board_toolbar = view.findViewById(R.id.board_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(board_toolbar);
            activity.getSupportActionBar().setTitle("게시판");
        }
        boardwriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBoardWriteFragment();
            }
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


}
