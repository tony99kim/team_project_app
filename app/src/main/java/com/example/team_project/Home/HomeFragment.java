package com.example.team_project.Home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.R;
import com.example.team_project.Toolbar.NotificationsFragment;
import com.example.team_project.Toolbar.SearchFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class HomeFragment extends Fragment {

    private TextView tvEnvironmentPoints;
    private FirebaseFirestore db;
    private String userId;
    private ListenerRegistration registration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        Button settingsButton = view.findViewById(R.id.btn_home_settings);
        tvEnvironmentPoints = view.findViewById(R.id.tvEnvironmentPoints); // 환경 포인트 TextView 초기화

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 사용자 ID 가져오기

        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_search) {
                replaceFragment(new SearchFragment());
                return true;
            } else if (id == R.id.action_notifications) {
                replaceFragment(new NotificationsFragment());
                return true;
            }
            return false;
        });

        settingsButton.setOnClickListener(v -> replaceFragment(new HomeSettingsFragment()));

        // Firestore에서 환경 포인트 값 가져오기
        loadenvironmentPoints();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 설정 변경 반영
        updateLayoutBasedOnSettings();
    }

    private void loadenvironmentPoints() {
        DocumentReference docRef = db.collection("users").document(userId);

        registration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // 오류 처리
                    Toast.makeText(getContext(), "Firestore 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    // 환경 포인트 값 가져오기
                    Long environmentPoint = snapshot.getLong("environmentPoint");

                    // null 체크
                    if (environmentPoint != null) {
                        tvEnvironmentPoints.setText(String.valueOf(environmentPoint)); // 환경 포인트 표시
                    } else {
                        tvEnvironmentPoints.setText("0"); // 환경 포인트가 없을 경우 기본값으로 0 표시
                    }
                } else {
                    tvEnvironmentPoints.setText("0"); // 문서가 없을 경우 기본값으로 0 표시
                }
            }
        });
    }


    private void updateLayoutBasedOnSettings() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("HomeSettingsPrefs", getContext().MODE_PRIVATE);
        boolean showPopularPosts = sharedPref.getBoolean("popularPosts", true);
        boolean showEvents = sharedPref.getBoolean("events", true);

        View popularPostsSection = getView().findViewById(R.id.fragment_home_section_popular_posts);
        View eventsSection = getView().findViewById(R.id.fragment_home_section_events);

        // 인기 게시글 섹션의 표시 여부 설정
        popularPostsSection.setVisibility(showPopularPosts ? View.VISIBLE : View.GONE);

        // 이벤트 공지 섹션의 표시 여부 설정
        eventsSection.setVisibility(showEvents ? View.VISIBLE : View.GONE);
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

    @Override
    public void onStop() {
        super.onStop();
        // 리스너 해제
        if (registration != null) {
            registration.remove();
        }
    }
}
