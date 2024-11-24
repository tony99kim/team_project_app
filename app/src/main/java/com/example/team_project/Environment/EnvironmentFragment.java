package com.example.team_project.Environment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.team_project.Environment.Point.PointFragment;
import com.example.team_project.Environment.Store.StoreFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.team_project.R;
import com.example.team_project.Toolbar.LocationSettingsFragment;
import com.example.team_project.Toolbar.NotificationsFragment;
import com.example.team_project.Toolbar.SearchFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EnvironmentFragment extends Fragment {

    private FirebaseFirestore db;
    private String userId;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        // 툴바 타이틀 설정
        toolbar.setTitle("지역 선택 ▼");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        // 툴바 메뉴 설정

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
        toolbar.setOnClickListener(v -> showLocationPopupMenu(v));

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.view_pager);

        EnvironmentPagerAdapter adapter = new EnvironmentPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 슬라이드를 통한 화면 전환 비활성화
        viewPager.setUserInputEnabled(false);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("탄소 중립 실천 인증");
            } else {
                tab.setText("스토어");
            }
        }).attach();

        // 탭 레이아웃의 탭 선택 리스너 설정
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 선택된 탭의 텍스트 색상 변경
                tabLayout.setTabTextColors(ContextCompat.getColor(getContext(), R.color.tab_unselected), ContextCompat.getColor(getContext(), R.color.tab_selected));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 선택되지 않은 탭의 처리
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 탭이 다시 선택된 경우의 처리
            }
        });

        // 추가된 코드: targetPage 인텐트 처리
        if (getArguments() != null) {
            int targetPage = getArguments().getInt("targetPage", 0);
            viewPager.setCurrentItem(targetPage);
        }

        return view;
    }

    private void showLocationPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_location, popupMenu.getMenu());

        // Fetch and display current address
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentAddress = documentSnapshot.getString("address");
                if (currentAddress != null) {
                    String neighborhood = currentAddress.split(" ")[1]; // Assuming the neighborhood is the second word
                    popupMenu.getMenu().findItem(R.id.action_set_neighborhood).setTitle(neighborhood);
                }
            }
        });

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_set_neighborhood) {
                Toast.makeText(getContext(), "동네가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_set_location) {
                replaceFragment(new LocationSettingsFragment());
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
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

    private static class EnvironmentPagerAdapter extends FragmentStateAdapter {
        public EnvironmentPagerAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new PointFragment(); // 포인트 페이지 프래그먼트
            } else {
                return new StoreFragment(); // 스토어 페이지 프래그먼트
            }
        }

        @Override
        public int getItemCount() {
            return 2; // 탭의 총 개수 반환
        }
    }
}