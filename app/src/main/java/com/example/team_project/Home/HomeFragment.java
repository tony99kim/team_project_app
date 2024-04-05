package com.example.team_project.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_project.R;

public class HomeFragment extends Fragment {

    //태엽
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_search) {
                // 검색 화면으로 전환하는 로직 구현
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.slide_in_right,  // 들어올 때 애니메이션
                        R.anim.fade_out,        // 현재 Fragment가 사라질 때 애니메이션
                        R.anim.fade_in,         // BackStack에서 돌아올 때 애니메이션
                        R.anim.slide_out_right  // BackStack으로 돌아갈 때 애니메이션
                );
                transaction.replace(R.id.fragment_container, new SearchFragment()); // SearchFragment로 전환
                transaction.addToBackStack(null);
                transaction.commit();
                return true;
            } else if (id == R.id.action_notifications) {
                // NotificationsFragment로 화면 전환
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_right
                );
                transaction.replace(R.id.fragment_container, new NotificationsFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                return true;
            }
            return false;
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


}
