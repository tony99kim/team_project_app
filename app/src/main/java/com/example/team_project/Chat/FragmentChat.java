package com.example.team_project.Chat;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.R;
import java.util.ArrayList;
import android.util.TypedValue;

public class FragmentChat extends Fragment {

    private LinearLayout layout; // 뷰 참조를 유지
    private ArrayList<String> chatRooms = new ArrayList<>(); // 채팅방 리스트

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        layout = view.findViewById(R.id.linear_layout_chat_rooms);

        Button addButton = view.findViewById(R.id.button_add_chat_room);
        addButton.setOnClickListener(v -> showAddRoomDialog());

        return view;
    }

    private void showAddRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("새 채팅방 이름 입력");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("추가", (dialog, which) -> {
            String roomName = input.getText().toString();
            if (!roomName.isEmpty()) {
                chatRooms.add(roomName);
                updateUI();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUI() {
        if (getContext() == null) return;
        layout.removeAllViews();

        for (String roomName : chatRooms) {
            LinearLayout roomLayout = new LinearLayout(getContext());
            roomLayout.setOrientation(LinearLayout.HORIZONTAL);
            roomLayout.setPadding(10, 10, 10, 10);

            TextView textView = new TextView(getContext());
            textView.setText(roomName);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            roomLayout.addView(textView);

            Button enterButton = new Button(getContext());
            enterButton.setText("입장");
            enterButton.setOnClickListener(v -> enterChatRoom(roomName));
            roomLayout.addView(enterButton);

            Button deleteButton = new Button(getContext());
            deleteButton.setText("삭제");
            deleteButton.setOnClickListener(v -> deleteChatRoom(roomName));
            roomLayout.addView(deleteButton);

            layout.addView(roomLayout);
        }
    }

    private void deleteChatRoom(String roomName) {
        chatRooms.remove(roomName);
        updateUI();
    }

    private void enterChatRoom(String roomName) {
        FragmentChat_room fragmentChatRoom = new FragmentChat_room();
        Bundle args = new Bundle();
        args.putString("room_name", roomName);
        fragmentChatRoom.setArguments(args);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragmentChatRoom);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}