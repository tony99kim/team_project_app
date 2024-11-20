package com.example.team_project.Profile.Pay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.team_project.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TransactionHistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_pay_transaction_history, container, false);

        // 툴바 설정
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("페이 내역");
        }

        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        TabLayout tabLayout = view.findViewById(R.id.tabs);

        TransactionPagerAdapter adapter = new TransactionPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("충전/환전 내역");
                    break;
                case 1:
                    tab.setText("송금 내역");
                    break;
                case 2:
                    tab.setText("결제 내역");
                    break;
            }
        }).attach();

        return view;
    }

    private static class TransactionPagerAdapter extends FragmentStateAdapter {
        public TransactionPagerAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new RechargeExchangeHistoryFragment();
                case 1:
                    return new TransferHistoryFragment();
                case 2:
                    return new PaymentHistoryFragment();
                default:
                    return new RechargeExchangeHistoryFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}