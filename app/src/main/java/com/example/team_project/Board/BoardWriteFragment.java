package com.example.team_project.Board;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.team_project.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class BoardWriteFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button postButton, board_post_pic_button;
    private CheckBox board_free_checkbox, board_news_checkbox;
    private EditText editTextTitle, editTextContent;

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

        board_post_pic_button = view.findViewById(R.id.board_post_pic_button);
        board_post_pic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(); // 버튼을 클릭할 때 파일 선택기를 열도록 openFileChooser() 메서드를 호출합니다.
            }
        });

        postButton = view.findViewById(R.id.buttonPost);
        postButton.setOnClickListener(v -> {
            // 버튼을 클릭했을 때의 작업을 처리합니다.
            validateAndProceed();
        });

        // 체크박스 초기화 및 이벤트 리스너 설정
        board_news_checkbox = view.findViewById(R.id.board_news_checkbox);
        board_free_checkbox = view.findViewById(R.id.board_free_checkbox);

        board_news_checkbox.setOnCheckedChangeListener(this::onNewsCheckboxClicked);
        board_free_checkbox.setOnCheckedChangeListener(this::onFreeCheckboxClicked);

        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextContent = view.findViewById(R.id.editTextContent);

        return view;
    }

    public void onNewsCheckboxClicked(View view, boolean isChecked) {
        checkMultipleCategories(board_news_checkbox, board_free_checkbox, isChecked);
    }

    public void onFreeCheckboxClicked(View view, boolean isChecked) {
        checkMultipleCategories(board_free_checkbox, board_news_checkbox, isChecked);
    }

    private void checkMultipleCategories(CheckBox checkBox1, CheckBox checkBox2, boolean isChecked) {
        // 체크박스 상태 확인
        boolean categoryChecked = isChecked;

        // 다른 체크박스의 상태 확인
        boolean otherCategoryChecked = checkBox2.isChecked();

        if (categoryChecked && otherCategoryChecked) {
            // 경고 메시지 표시
            Toast.makeText(getContext(), "한 개의 카테고리만 선택해주세요", Toast.LENGTH_SHORT).show();
            // 체크 해제
            checkBox1.setChecked(false);
        }
    }

    private void validateAndProceed() {
        boolean isCheckBoxChecked = board_news_checkbox.isChecked() || board_free_checkbox.isChecked();
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (!isCheckBoxChecked) {
            Toast.makeText(getContext(), "하나 이상의 체크박스를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty()) {
            Toast.makeText(getContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 모든 조건이 충족되면 다음 fragment로 이동
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new BoardPostCompleteFragment());
        transaction.addToBackStack(null);
        transaction.commit();

        // 2초 후에 이전 fragment로 이동하는 작업을 합니다.
        new Handler().postDelayed(() -> {
            FragmentTransaction newTransaction = getParentFragmentManager().beginTransaction();
            newTransaction.replace(R.id.fragment_container, new BoardFragment());
            newTransaction.addToBackStack(null);
            newTransaction.commit();
        }, 2000); // 2초 후 게시판 페이지로 이동
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

}
