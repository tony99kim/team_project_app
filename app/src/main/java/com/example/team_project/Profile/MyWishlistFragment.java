package com.example.team_project.Profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.team_project.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyWishlistFragment extends Fragment {

    private ListView wishlistListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> wishlistItems;
    private ArrayList<String> wishlistDetails; // 추가된 리스트

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_wishlist, container, false);
        wishlistListView = view.findViewById(R.id.wishlistListView);
        wishlistItems = loadWishlistItems(); // 찜한 상품 목록 로드
        wishlistDetails = loadWishlistDetails(); // 찜한 상품의 상세 정보 로드

        List<String> displayItems = new ArrayList<>();
        for (int i = 0; i < wishlistItems.size(); i++) {
            displayItems.add(wishlistDetails.get(i)); // 상세 정보를 리스트에 추가
        }

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, displayItems);
        wishlistListView.setAdapter(adapter);
        return view;
    }

    private ArrayList<String> loadWishlistItems() {
        SharedPreferences prefs = getActivity().getSharedPreferences("wishlist", Context.MODE_PRIVATE);
        Set<String> wishlistSet = prefs.getStringSet("wishlistItems", new HashSet<>());
        return new ArrayList<>(wishlistSet);
    }

    private ArrayList<String> loadWishlistDetails() {
        SharedPreferences prefs = getActivity().getSharedPreferences("wishlist", Context.MODE_PRIVATE);
        Set<String> wishlistDetailSet = prefs.getStringSet("wishlistDetails", new HashSet<>());
        return new ArrayList<>(wishlistDetailSet);
    }
}
