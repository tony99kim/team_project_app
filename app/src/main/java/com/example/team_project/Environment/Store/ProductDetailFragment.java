package com.example.team_project.Environment.Store;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.team_project.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductDetailFragment extends Fragment {

    private String productId, userId, title, price, description;
    private ViewPager2 viewPager2;
    private TextView titleTextView, descriptionTextView, priceTextView;

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
        titleTextView = view.findViewById(R.id.textView_product_title);
        descriptionTextView = view.findViewById(R.id.textView_product_description);
        priceToolBar = view.findViewById(R.id.toolbar_bottom);
        priceTextView = priceToolBar.findViewById(R.id.textView_product_price);

        titleTextView.setText(title);
        descriptionTextView.setText(description);

        Toolbar toolbar = view.findViewById(R.id.toolbar_product_detail);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        loadProductPrice(); // 가격 불러오기 함수 호출
        loadProductImages(); // 이미지 로드 함수 호출

        return view;
    }

    private void loadProductImages() {
        String directoryPath = "ProductImages/" + productId;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(directoryPath);

        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<Task<Uri>> tasks = new ArrayList<>();
            for (StorageReference item : listResult.getItems()) {
                Task<Uri> downloadTask = item.getDownloadUrl();
                tasks.add(downloadTask);
            }

            // 모든 다운로드 URL Task가 완료될 때까지 기다림
            Task<List<Uri>> allTasks = Tasks.whenAllSuccess(tasks);
            allTasks.addOnSuccessListener(uris -> {
                List<String> imageUrls = uris.stream()
                        .filter(Objects::nonNull)
                        .map(Uri::toString)
                        .collect(Collectors.toList());

                if (!imageUrls.isEmpty()) {
                    setupViewPager(imageUrls);
                }
            }).addOnFailureListener(exception -> {
                Log.e("ProductDetailFragment", "Error getting all download URLs", exception);
            });
        }).addOnFailureListener(exception -> {
            Log.e("ProductDetailFragment", "Error listing items in storage", exception);
        });
    }

    private void setupViewPager(List<String> imageUrls) {
        viewPager2.setAdapter(new ViewPagerAdapter(imageUrls));
    }

    private void loadProductPrice() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference productRef = db.collection("products").document(productId);

        productRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String fetchedPrice = document.getString("price");
                    if (fetchedPrice != null) {
                        priceTextView.setText(fetchedPrice);
                    } else {
                        Toast.makeText(getContext(), "Price not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Product not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error getting product: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {
        private final List<String> imageUrls;

        public ViewPagerAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_environment_store_productdetail_item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);
            Glide.with(holder.imageView.getContext())
                    .load(imageUrl)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.productImageView);
            }
        }
    }
}

