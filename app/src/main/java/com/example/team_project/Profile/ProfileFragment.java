package com.example.team_project.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.team_project.LoginActivity;
import com.example.team_project.R;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private TextView usernameTextView, environmentPointsTextView;
    private ImageView profileImageView;
    private Button editButton, recentVisitButton, noticeButton, customerServiceButton, logoutButton, withdrawButton;
    private androidx.appcompat.widget.Toolbar toolbar;

    private ListView recentVisitListView;
    private ArrayList<String> recentVisitList;
    private ArrayAdapter<String> adapter;
    private SharedPreferences sharedPreferences;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        recentVisitList = new ArrayList<>();

        profileImageView = view.findViewById(R.id.profileImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        recentVisitButton = view.findViewById(R.id.recentVisitButton);
        noticeButton = view.findViewById(R.id.noticeButton);
        customerServiceButton = view.findViewById(R.id.customerServiceButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        withdrawButton = view.findViewById(R.id.withdrawButton);
        editButton = view.findViewById(R.id.editButton);
        recentVisitListView = view.findViewById(R.id.recentVisitListView);
        // 툴바 설정
        toolbar = view.findViewById(R.id.toolbar);
        environmentPointsTextView = view.findViewById(R.id.environmentPointsTextView);


        sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        // setUsername() 호출
        setUsername();
        // 최근 방문 기록을 담을 리스트 초기화
        recentVisitList = new ArrayList<>();
        recentVisitListView = view.findViewById(R.id.recentVisitListView);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, recentVisitList);
        recentVisitListView.setAdapter(adapter);

        // 최근 방문 버튼 클릭 리스너 설정
        // 여기에 최근 방문 기록을 추가하는 코드를 넣으세요.
        // 예시로 아래와 같이 리스트에 데이터를 추가할 수 있습니다.
        recentVisitList.add("상품 1");
        recentVisitList.add("상품 2");
        recentVisitList.add("상품 3");
        adapter.notifyDataSetChanged(); // 어댑터에 데이터가 변경되었음을 알려줍니다.

        // 사용자의 환경 포인트 설정 (예시로 1000이라고 가정)
        int environmentPoints = 1000;
        environmentPointsTextView.setText("환경 포인트: " + environmentPoints);



        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setTitle("마이 페이지");
        }

        // 로그아웃 버튼 클릭 리스너 설정
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // 최근 방문 버튼 클릭 리스너 설정
        recentVisitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRecentVisitFragment();
            }
        });

        // 공지사항 버튼 클릭 리스너 설정
        noticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoticeFragment();
            }
        });

        // 고객센터 버튼 클릭 리스너 설정
        customerServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCustomerServiceFragment();
            }
        });

        // 회원탈퇴 버튼 클릭 리스너 설정
        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWithdrawFragment();
            }
        });

        // 수정하기 버튼 클릭 리스너 설정
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditButtonFragment();
            }
        });



        return view;
    }





    private  void logout() {
        Intent intent = new Intent(getActivity() , LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
    private void openRecentVisitFragment() {
        // 최근 방문 프래그먼트 객체 생성
        RecentVisitFragment fragment = new RecentVisitFragment();

        // 현재 액티비티가 AppCompatActivity 인지 확인
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        // 현재 액티비티가 AppCompatActivity 인 경우에만 프래그먼트 트랜잭션 수행
        if (activity != null) {
            // 프래그먼트 트랜잭션을 사용하여 최근 방문 프래그먼트를 화면에 표시
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment) // R.id.fragment_container는 프래그먼트를 표시할 레이아웃의 ID입니다.
                    .addToBackStack(null) // 백 스택에 프래그먼트를 추가하여 뒤로 가기 기능을 지원합니다.
                    .commit();
        }
    }

    private void openNoticeFragment() {
        NoticeFragment fragment = new NoticeFragment();

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openCustomerServiceFragment() {
        CustomerServiceFragment fragment = new CustomerServiceFragment();

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openWithdrawFragment() {
        WithdrawFragment fragment = new WithdrawFragment();

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openEditButtonFragment() {
        EditButtonFragment fragment = new EditButtonFragment();

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // onResume()에서 setUsername() 호출하여 사용자 이름 업데이트
        setUsername();
    }

    private void setUsername() {
        String savedUsername = sharedPreferences.getString("username", "사용자 이름");
        usernameTextView.setText(savedUsername);
    }






}


