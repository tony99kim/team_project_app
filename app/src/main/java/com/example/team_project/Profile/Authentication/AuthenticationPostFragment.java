package com.example.team_project.Profile.Authentication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.Environment.Point.PointAuthentication;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationPostFragment extends Fragment {

    private RecyclerView recyclerView;
    private AuthenticationPostAdapter adapter;
    private List<PointAuthentication> authenticationPosts = new ArrayList<>();
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_authentication_post, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("내 인증글");
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        recyclerView = view.findViewById(R.id.recyclerViewAuthenticationPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new AuthenticationPostAdapter(authenticationPosts);
        recyclerView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            loadAuthenticationPosts();
        } else {
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadAuthenticationPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pointAuthentications")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    authenticationPosts.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PointAuthentication post = document.toObject(PointAuthentication.class);
                        authenticationPosts.add(post);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "인증글을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show());
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

    private class AuthenticationPostAdapter extends RecyclerView.Adapter<AuthenticationPostAdapter.ViewHolder> {

        private List<PointAuthentication> posts;

        AuthenticationPostAdapter(List<PointAuthentication> posts) {
            this.posts = posts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_authentication_post_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PointAuthentication post = posts.get(position);
            holder.titleTextView.setText(post.getTitle());
            holder.timestampTextView.setText(post.getTimestamp());
            holder.statusTextView.setText(post.getStatus());

            holder.itemView.setOnClickListener(v -> {
                AuthenticationPostDetailFragment detailFragment = AuthenticationPostDetailFragment.newInstance(post);
                replaceFragment(detailFragment);
            });
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView timestampTextView;
            TextView statusTextView;

            ViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.textViewTitle);
                timestampTextView = itemView.findViewById(R.id.textViewTimestamp);
                statusTextView = itemView.findViewById(R.id.textViewStatus);
            }
        }
    }
}