package com.example.team_project.Toolbar;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.example.team_project.Board.BoardKategorie.Post;
import com.example.team_project.Board.BoardKategorie.PostAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class BordSearchFragment extends Fragment {
    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private PostAdapter postAdapter;
    private ArrayList<Post> postList;
    private FirebaseFirestore firestore;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toobar_bord_search, container, false);

        // Firebase Firestore 초기화
        firestore = FirebaseFirestore.getInstance();

        // UI 요소 초기화
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_black); // 뒤로가기 아이콘 설정
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed()); // 뒤로가기 버튼 클릭 시 동작
        toolbar.setTitle("");

        searchEditText = view.findViewById(R.id.searchEditText);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        searchResultsRecyclerView.setAdapter(postAdapter);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // EditText에 TextWatcher 추가
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPosts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void searchPosts(String query) {
        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("posts")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff") // 유사한 제목을 모두 얻기 위해
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear(); // 이전 결과 클리어
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Post post = document.toObject(Post.class); // Document를 Post 객체로 변환
                            postList.add(post); // 결과 목록에 추가
                        }
                        postAdapter.notifyDataSetChanged(); // RecyclerView 업데이트
                    } else {
                        Toast.makeText(getContext(), "검색 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }
}