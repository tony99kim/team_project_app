package com.example.team_project.Board;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.team_project.R;

public class BoardWriteFragment extends Fragment {

    private Button postButton;
    private CheckBox board_free_checkbox, board_news_checkbox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board_board_write, container, false);

        Toolbar toolbar = view.findViewById(R.id.board_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("글쓰기");
        }

        postButton = view.findViewById(R.id.buttonPost);
        postButton.setOnClickListener(v -> {
            // 버튼을 클릭했을 때의 작업을 처리합니다.
            // 다음 fragment로 전환하고 3초 후에 이전 fragment로 이동하도록 설정합니다.
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new BoardPostCompleteFragment());
            transaction.addToBackStack(null);
            transaction.commit();

            // 3초 후에 이전 fragment로 이동하는 작업을 합니다.
            new Handler().postDelayed(() -> {
                // 여기서는 BoardFragment로 이동합니다.
                FragmentTransaction newTransaction = getParentFragmentManager().beginTransaction();
                newTransaction.replace(R.id.fragment_container, new BoardFragment());
                newTransaction.addToBackStack(null);
                newTransaction.commit();
            }, 2000); // 2초 후 게시판 페이지로 이동
        });

        // 체크박스 초기화 및 이벤트 리스너 설정
        board_news_checkbox = view.findViewById(R.id.board_news_checkbox);
        board_free_checkbox = view.findViewById(R.id.board_free_checkbox);

        board_news_checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onNewsCheckboxClicked(buttonView, isChecked);
        });

        board_free_checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onFreeCheckboxClicked(buttonView, isChecked);
        });

        return view;
    }

    public void onNewsCheckboxClicked(View view, boolean isChecked) {
        // 체크박스 상태 확인
        boolean newsChecked = isChecked;

        // 두 체크박스가 모두 선택되었는지 검사
        board_free_checkbox = getView().findViewById(R.id.board_free_checkbox);
        boolean freeChecked = board_free_checkbox.isChecked();

        if (newsChecked && freeChecked) {
            // 경고 메시지 표시
            Toast.makeText(getContext(), "한 개의 카테고리만 선택해주세요", Toast.LENGTH_SHORT).show();
            // 체크 해제
            board_news_checkbox.setChecked(false);
        }
    }

    public void onFreeCheckboxClicked(View view, boolean isChecked) {
        // 체크박스 상태 확인
        boolean freeChecked = isChecked;

        // 두 체크박스가 모두 선택되었는지 검사
        board_news_checkbox = getView().findViewById(R.id.board_news_checkbox);
        boolean newsChecked = board_news_checkbox.isChecked();

        if (freeChecked && newsChecked) {
            // 경고 메시지 표시
            Toast.makeText(getContext(), "한 개의 카테고리만 선택해주세요", Toast.LENGTH_SHORT).show();
            // 체크 해제
            board_free_checkbox.setChecked(false);
        }
    }
}
