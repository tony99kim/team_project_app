package com.example.team_project.Store;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.team_project.R;
import com.example.team_project.Toolbar.LocationSettingsFragment;
import com.example.team_project.Toolbar.NotificationsFragment;
import com.example.team_project.Toolbar.SearchFragment;

public class StoreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store, container, false);

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

        // 상품 등록 버튼 초기화 및 클릭 이벤트 설정
        Button addProductButton = view.findViewById(R.id.addProductButton);
        addProductButton.setText("상품 등록");
        addProductButton.setOnClickListener(v -> {
            // 상품 등록 페이지로 이동
            replaceFragment(new ProductRegistrationFragment());
        });

        return view;
    }

    private void showLocationPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_location, popupMenu.getMenu());

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
}
