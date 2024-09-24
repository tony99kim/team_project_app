package com.example.team_project.Environment.Point;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.team_project.R;

import java.util.List;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.PointViewHolder> {
    private List<PointItem> items;
    private View.OnClickListener onItemClickListener;

    public PointAdapter(List<PointItem> items) {
        this.items = items;
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        this.onItemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public PointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_environment_point_authentication_item, parent, false);
        view.setOnClickListener(onItemClickListener);
        return new PointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PointViewHolder holder, int position) {
        PointItem item = items.get(position);

        // 고정된 제목 설정
        holder.title.setText(item.getTitle()); // 이미 설정된 title 사용

        // 이미지 URL 가져오기
        String imageUrl = item.getImageUrl();

        // Glide를 사용하여 이미지 설정
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher)) // 로딩 중 표시될 이미지
                .error(R.mipmap.ic_launcher) // 로딩 실패 시 표시될 이미지
                .into(holder.iconImageView); // 이미지를 설정할 ImageView

        // 클릭 리스너
        holder.itemView.setOnClickListener(v -> {
            // 클릭 시 Fragment로 이동
            PointAuthenticationFragment fragment = PointAuthenticationFragment.newInstance(item);
            ((AppCompatActivity) holder.itemView.getContext()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment) // 적절한 프래그먼트 컨테이너 ID 사용
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class PointViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView iconImageView;

        public PointViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.authenticationTitleTextView); // 인증 제목 TextView ID
            iconImageView = itemView.findViewById(R.id.authenticationIconImageView); // 인증 아이콘 ImageView ID
        }
    }
}
