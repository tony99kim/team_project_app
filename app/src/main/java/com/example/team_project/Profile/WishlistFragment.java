package com.example.team_project.Profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.team_project.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WishlistFragment extends Fragment {

    private ListView wishlistListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> wishlistItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_wishlist, container, false);

        wishlistListView = view.findViewById(R.id.wishlistListView);
        wishlistItems = loadWishlistItems(); // 찜한 상품 제목 목록 로드

        adapter = new WishlistAdapter(getContext(), wishlistItems);
        wishlistListView.setAdapter(adapter);

        return view;
    }

    private ArrayList<String> loadWishlistItems() {
        SharedPreferences prefs = getActivity().getSharedPreferences("wishlist", Context.MODE_PRIVATE);
        Set<String> wishlistSet = prefs.getStringSet("wishlistItems", new HashSet<>());
        return new ArrayList<>(wishlistSet);
    }

    private class WishlistAdapter extends ArrayAdapter<String> {
        private ArrayList<String> titles;

        public WishlistAdapter(Context context, ArrayList<String> titles) {
            super(context, R.layout.wishlist_item, titles);
            this.titles = titles;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // 뷰 재사용
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.wishlist_item, parent, false);
            }

            TextView titleTextView = convertView.findViewById(R.id.wishlistTitleTextView);
            Button cancelButton = convertView.findViewById(R.id.cancelButton);

            titleTextView.setText(titles.get(position));

            // 취소 버튼 클릭 리스너 설정
            cancelButton.setOnClickListener(v -> {
                String itemToRemove = titles.get(position);
                titles.remove(position);
                notifyDataSetChanged();
                removeFromWishlist(itemToRemove);
            });

            return convertView;
        }

        private void removeFromWishlist(String item) {
            SharedPreferences prefs = getContext().getSharedPreferences("wishlist", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // 기존 찜한 상품 목록 가져오기
            Set<String> wishlistSet = prefs.getStringSet("wishlistItems", new HashSet<>());
            wishlistSet.remove(item); // 아이템 제거

            editor.putStringSet("wishlistItems", wishlistSet);
            editor.apply();
        }
    }
}
