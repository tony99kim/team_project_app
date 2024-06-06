package com.example.team_project.Environment.Store;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.viewpager2.widget.ViewPager2;

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
    private ProductImagesAdapter imagesAdapter;
    private TextView titleTextView, descriptionTextView, priceTextView; // priceTextView 추가

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
        priceTextView = priceToolBar.findViewById(R.id.textView_product_price); // priceTextView 초기화

        titleTextView.setText(title);
        descriptionTextView.setText(description);

        Toolbar toolbar = view.findViewById(R.id.toolbar_product_detail);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        loadProductPrice(); // 가격 불러오기 함수 호출

        return view;
    }
    /*
       @Override
       public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
           super.onViewCreated(view, savedInstanceState);

           // 초기 이미지 URL 리스트를 null 대신 빈 리스트로 설정
           imagesAdapter = new ProductImagesAdapter(new ArrayList<>());
           viewPager2.setAdapter(imagesAdapter);
           loadProductImages();
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
                           .filter(Objects::nonNull) // 널이 아닌 Uri 객체만 필터링
                           .map(Uri::toString)
                           .collect(Collectors.toList());
                   if (!imageUrls.isEmpty()) { // 이미지 URL 목록이 비어 있지 않은 경우에만 설정
                       imagesAdapter.setImageUrls(imageUrls);
                   }
               }).addOnFailureListener(exception -> {
                   Log.e("ProductDetailFragment", "Error getting all download URLs", exception);
               });
           });
       }
    */

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
}
