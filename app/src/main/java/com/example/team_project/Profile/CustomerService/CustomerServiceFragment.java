package com.example.team_project.Profile.CustomerService;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CustomerServiceFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private InquiryAdapter adapter;
    private ArrayList<Inquiry> inquiries = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_customerservice, container, false);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("고객 문의 내역");
            toolbar.setNavigationOnClickListener(v -> {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                } else {
                    activity.onBackPressed();
                }
            });
        }

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InquiryAdapter(inquiries, inquiry -> openInquiryDetailFragment(inquiry));
        recyclerView.setAdapter(adapter);

        loadInquiries();

        view.findViewById(R.id.btn_inquire).setOnClickListener(v -> openInquiryFragment());

        return view;
    }

    private void loadInquiries() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 사용자 ID 가져오기

        db.collection("inquiries")
                .whereEqualTo("userId", userId) // 사용자 ID로 필터링
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        inquiries.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Inquiry inquiry = document.toObject(Inquiry.class);
                            inquiry.setId(document.getId()); // Firestore 문서 ID 설정
                            inquiries.add(inquiry);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "문의 내역 로딩 실패: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openInquiryFragment() {
        InquiryFragment inquiryFragment = new InquiryFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, inquiryFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openInquiryDetailFragment(Inquiry inquiry) {
        InquiryDetailFragment inquiryDetailFragment = InquiryDetailFragment.newInstance(inquiry);
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, inquiryDetailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}