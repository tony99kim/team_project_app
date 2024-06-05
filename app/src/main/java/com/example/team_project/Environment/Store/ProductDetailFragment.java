package com.example.team_project.Environment.Store;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.team_project.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailFragment extends Fragment {

    private String productId, userId, title, price, description;
    private ViewPager2 viewPager2;
    private ProductImagesAdapter imagesAdapter;
    private TextView titleTextView, descriptionTextView;

    private Toolbar priceToolBar;

    public static ProductDetailFragment newInstance(String productId, String userId, String title, String price, String description) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString("productId", productId);
        args.putString("userId", userId);
        args.putString("title", title);
        args.putString("price", price);
        args.putString("description", description);
        fragment.setArguments(args);
        return fragment;
    }

    public static ProductDetailFragment newInstance(Product product) {
        return newInstance(product.productId, product.userId, product.title, product.price, product.description);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            userId = getArguments().getString("userId");
            title = getArguments().getString("title");
            price = getArguments().getString("price");
            description = getArguments().getString("description");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment_store_productdetail, container, false);

        viewPager2 = view.findViewById(R.id.viewPager_images);
        imagesAdapter = new ProductImagesAdapter(getContext());

        titleTextView = view.findViewById(R.id.textView_product_title);
        descriptionTextView = view.findViewById(R.id.textView_product_description);
        priceToolBar = view.findViewById(R.id.toolbar_bottom);

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        priceToolBar.setTextDirection(Integer.parseInt(price));

        Toolbar toolbar = view.findViewById(R.id.toolbar_product_detail);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        loadProductImages();

        return view;
    }


    private void loadProductImages() {
        String directoryPath = "ProductImages/" + productId;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(directoryPath);
        List<String> imageUrls = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            storageRef.child(productId + "_" + i + ".jpg").getDownloadUrl().addOnSuccessListener(uri -> {
                imageUrls.add(uri.toString());
                if (finalI == 9) {
                    imagesAdapter.setImageUrls(imageUrls);
                    viewPager2.setAdapter(imagesAdapter);
                    imagesAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(exception -> {
                // error handling
            });
        }
    }

}
