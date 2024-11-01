package com.example.team_project.Toolbar;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.example.team_project.Board.BoardKategorie.Post;
import com.example.team_project.Board.BoardKategorie.PostAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class BordSearchFragment extends AppCompatActivity {
    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private PostAdapter postAdapter;
    private ArrayList<Post> postList;
    private FirebaseFirestore firestore;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_toobar_bord_search); // 새로 만든 레이아웃 파일

        // Firebase Firestore 초기화
        firestore = FirebaseFirestore.getInstance();

        // UI 요소 초기화
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 표시
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_black); // 뒤로가기 아이콘 설정
        getSupportActionBar().setTitle("");

        searchEditText = findViewById(R.id.searchEditText);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        searchResultsRecyclerView.setAdapter(postAdapter);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // 뒤로가기 버튼 클릭 시 이전 액티비티로 이동
        return true;
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
                        Toast.makeText(this, "검색 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }
}
