package com.example.team_project.Board;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.example.team_project.Board.BoardKategorie.Post;
import com.example.team_project.Board.BoardKategorie.PostAdapter;
import com.example.team_project.Board.BoardKategorie.PostRegistrationFragment;
import com.example.team_project.Toolbar.NotificationsFragment;
import com.example.team_project.Toolbar.BordSearchFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class BoardFragment extends Fragment {
    private Button boardWriteButton;
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private ArrayList<Post> postList;
    private FirebaseFirestore firestore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 메뉴 활성화
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);

        // RecyclerView 설정
        postsRecyclerView = view.findViewById(R.id.postListRecyclerView);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        postsRecyclerView.setAdapter(postAdapter);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Firestore 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance();

        // 게시물 데이터 로드
        loadPostsFromFirestore();

        // 게시물 등록 버튼 초기화 및 클릭 이벤트 설정
        boardWriteButton = view.findViewById(R.id.boardWriteButton);
        boardWriteButton.setText("게시물 쓰기");
        boardWriteButton.setOnClickListener(v -> {
            // 게시물 등록 페이지로 이동
            replaceFragment(new PostRegistrationFragment());
        });

        // 툴바 설정
        androidx.appcompat.widget.Toolbar boardToolbar = view.findViewById(R.id.board_toolbar);
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(boardToolbar);
            boardToolbar.setTitle("게시판");
            boardToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home_toolbar, menu); // 메뉴 인플레이트
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            // 검색 아이콘 클릭 시 BordSearchFragment로 이동
            replaceFragment(new BordSearchFragment());
            return true;
        } else if (id == R.id.action_notifications) {
            replaceFragment(new NotificationsFragment());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadPostsFromFirestore() {
        firestore.collection("posts") // Firestore에서 "posts" 컬렉션 참조
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear(); // 기존 목록을 클리어
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Post post = document.toObject(Post.class); // Document를 Post 객체로 변환
                            postList.add(post); // 목록에 추가
                        }
                        postAdapter.notifyDataSetChanged(); // 데이터 변경 알림
                    } else {
                        // 에러 처리
                    }
                });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}