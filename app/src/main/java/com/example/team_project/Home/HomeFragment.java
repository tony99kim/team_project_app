package com.example.team_project.Home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.team_project.Board.BoardKategorie.Post;
import com.example.team_project.Board.BoardKategorie.PostDetailFragment;
import com.example.team_project.LoginActivity;
import com.example.team_project.Profile.event.Event;
import com.example.team_project.Profile.event.EventDetailFragment;
import com.example.team_project.R;
import com.example.team_project.Toolbar.NotificationsFragment;
import com.example.team_project.Toolbar.SearchFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {
    private TextView tvEnvironmentPoints;
    private TextView popularPost1, popularPost2, popularPost3;
    private TextView event1, event2, event3;
    private FirebaseFirestore db;
    private String userId;
    private ListenerRegistration registration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        Button settingsButton = view.findViewById(R.id.btn_home_settings);
        tvEnvironmentPoints = view.findViewById(R.id.tvEnvironmentPoints);
        popularPost1 = view.findViewById(R.id.popular_post_1);
        popularPost2 = view.findViewById(R.id.popular_post_2);
        popularPost3 = view.findViewById(R.id.popular_post_3);
        event1 = view.findViewById(R.id.event_1);
        event2 = view.findViewById(R.id.event_2);
        event3 = view.findViewById(R.id.event_3);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Redirect to LoginActivity if the user is not logged in
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return view;
        } else {
            userId = currentUser.getUid();
        }

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

        loadEnvironmentPoints();
        loadPopularPosts();
        loadLatestEvents();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLayoutBasedOnSettings();
    }

    private void loadEnvironmentPoints() {
        DocumentReference docRef = db.collection("users").document(userId);

        registration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Firestore 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    Long environmentPoint = snapshot.getLong("environmentPoint");
                    if (environmentPoint != null) {
                        tvEnvironmentPoints.setText(String.valueOf(environmentPoint));
                    } else {
                        tvEnvironmentPoints.setText("0");
                    }
                } else {
                    tvEnvironmentPoints.setText("0");
                }
            }
        });
    }

    private void loadPopularPosts() {
        db.collection("posts")
                .orderBy("viewCount", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    AtomicInteger index = new AtomicInteger();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String postId = document.getId();
                        String title = document.getString("title");
                        String content = document.getString("content");
                        String authorId = document.getString("authorId");
                        List<String> imageUrls = (List<String>) document.get("imageUrls");
                        long viewCount = document.getLong("viewCount") != null ? document.getLong("viewCount") : 0;
                        long likes = document.getLong("likes") != null ? document.getLong("likes") : 0;

                        if (authorId != null) {
                            db.collection("users").document(authorId).get().addOnSuccessListener(userSnapshot -> {
                                String authorName = userSnapshot.getString("username");

                                Post post = new Post(postId, title, content, authorId, authorName, imageUrls, viewCount, likes);

                                if (index.get() == 0) {
                                    popularPost1.setText(authorName + ": " + title + "\n" + content);
                                    popularPost1.setEllipsize(TextUtils.TruncateAt.END);
                                    popularPost1.setMaxLines(1);
                                    popularPost1.setOnClickListener(v -> openPostDetail(post));
                                } else if (index.get() == 1) {
                                    popularPost2.setText(authorName + ": " + title + "\n" + content);
                                    popularPost2.setEllipsize(TextUtils.TruncateAt.END);
                                    popularPost2.setMaxLines(1);
                                    popularPost2.setOnClickListener(v -> openPostDetail(post));
                                } else if (index.get() == 2) {
                                    popularPost3.setText(authorName + ": " + title + "\n" + content);
                                    popularPost3.setEllipsize(TextUtils.TruncateAt.END);
                                    popularPost3.setMaxLines(1);
                                    popularPost3.setOnClickListener(v -> openPostDetail(post));
                                }
                                index.getAndIncrement();
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "인기 게시글을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    private void openPostDetail(Post post) {
        Fragment postDetailFragment = PostDetailFragment.newInstance(post);
        replaceFragment(postDetailFragment);
    }

    private void loadLatestEvents() {
        db.collection("events")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    AtomicInteger index = new AtomicInteger();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String eventId = document.getId();
                        String title = document.getString("title");
                        String content = document.getString("content");
                        Date createdAtDate = document.getDate("createdAt");
                        String imageUrl = document.getString("imageUrl");

                        if (createdAtDate != null) {
                            String createdAt = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.getDefault()).format(createdAtDate);

                            Event event = new Event(eventId, title, content, createdAt, imageUrl);

                            if (index.get() == 0) {
                                event1.setText(title);
                                event1.setEllipsize(TextUtils.TruncateAt.END);
                                event1.setMaxLines(1);
                                event1.setOnClickListener(v -> openEventDetail(event));
                            } else if (index.get() == 1) {
                                event2.setText(title);
                                event2.setEllipsize(TextUtils.TruncateAt.END);
                                event2.setMaxLines(1);
                                event2.setOnClickListener(v -> openEventDetail(event));
                            } else if (index.get() == 2) {
                                event3.setText(title);
                                event3.setEllipsize(TextUtils.TruncateAt.END);
                                event3.setMaxLines(1);
                                event3.setOnClickListener(v -> openEventDetail(event));
                            }
                            index.getAndIncrement();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "이벤트 공지를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    private void openEventDetail(Event event) {
        Fragment eventDetailFragment = EventDetailFragment.newInstance(event);
        replaceFragment(eventDetailFragment);
    }

    private void updateLayoutBasedOnSettings() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("HomeSettingsPrefs", getContext().MODE_PRIVATE);
        boolean showPopularPosts = sharedPref.getBoolean("popularPosts", true);
        boolean showEvents = sharedPref.getBoolean("events", true);

        View popularPostsSection = getView().findViewById(R.id.fragment_home_section_popular_posts);
        View eventsSection = getView().findViewById(R.id.fragment_home_section_events);

        popularPostsSection.setVisibility(showPopularPosts ? View.VISIBLE : View.GONE);
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
        if (registration != null) {
            registration.remove();
        }
    }
}