package com.example.team_project.Chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.team_project.R;

public class FragmentChat_room extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_chat.xml 파일을 인플레이트하여 뷰를 생성합니다.
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }
}