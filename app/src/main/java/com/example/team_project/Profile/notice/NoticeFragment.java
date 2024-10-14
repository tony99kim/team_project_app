package com.example.team_project.Profile.notice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoticeFragment extends Fragment {

    private FirebaseFirestore db; // Firestore 인스턴스
    private RecyclerView noticeRecyclerView; // RecyclerView
    private NoticeAdapter noticeAdapter; // Adapter
    private List<Notice> noticeList; // 공지사항 리스트
    private List<Notice> filteredList; // 필터링된 공지사항 리스트

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_notice, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("공지사항");
        }

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 공지사항 리스트 초기화
        noticeList = new ArrayList<>();
        filteredList = new ArrayList<>();
        noticeRecyclerView = view.findViewById(R.id.noticeRecyclerView);
        noticeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noticeAdapter = new NoticeAdapter(filteredList);
        noticeRecyclerView.setAdapter(noticeAdapter);

        // 공지사항 로드
        loadNotices();

        return view;
    }

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 메뉴 사용 가능 설정
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_toolbar, menu); // 메뉴 추가

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // SearchView의 텍스트 변경 리스너 설정
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotices(newText);
                return true;
            }
        });
    }

    private void loadNotices() {
        db.collection("notices") // Firestore의 공지사항 컬렉션 이름
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title"); // 제목 필드 이름
                            String content = document.getString("content"); // 내용 필드 이름
                            Date createdAt = document.getDate("createdAt"); // 생성 날짜 필드 이름
                            String imageUrl = document.getString("imageUrl"); // 이미지 URL 필드 이름

                            // 각 필드가 null이 아닐 때만 추가
                            if (title != null && content != null && createdAt != null) {
                                // 공지사항 추가
                                Notice notice = new Notice(title, content, createdAt, imageUrl);
                                noticeList.add(notice);
                            }
                        }
                        filteredList.addAll(noticeList); // 처음에는 모든 공지사항을 표시
                        noticeAdapter.notifyDataSetChanged(); // Adapter에 데이터 변경 알림
                    } else {
                        Toast.makeText(getContext(), "공지사항 로딩 실패: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterNotices(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(noticeList);
        } else {
            for (Notice notice : noticeList) {
                if (notice.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(notice);
                }
            }
        }
        noticeAdapter.notifyDataSetChanged();
    }
}