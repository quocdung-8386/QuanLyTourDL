package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Review; // Import model Review
// Giả định bạn đã thêm thư viện Glide/Picasso để tải ảnh
// import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private List<Review> reviewList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ⭐ Giả định layout item là item_review.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_card, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        // 1. Thông tin người đánh giá
        holder.tvReviewerName.setText(review.getTenNguoiDanhGia());

        // 2. Điểm và Nội dung
        holder.ratingBar.setRating(review.getRating());
        holder.tvReviewContent.setText(review.getComment());

        // 3. Ngày đánh giá
        if (review.getNgayDanhGia() != null) {
            holder.tvReviewDate.setText(dateFormat.format(review.getNgayDanhGia()));
        } else {
            holder.tvReviewDate.setText("");
        }

        // 4. Load Avatar (Sử dụng Glide/Picasso)
        if (review.getAvatarUrl() != null && !review.getAvatarUrl().isEmpty()) {
            // ⭐ BẬT COMMENT NẾU SỬ DỤNG THƯ VIỆN TẢI ẢNH
            /*
            Glide.with(context)
                 .load(review.getAvatarUrl())
                 .placeholder(R.drawable.ic_default_avatar) // Placeholder mặc định
                 .into(holder.imgReviewerAvatar);
            */
            holder.imgReviewerAvatar.setImageResource(R.drawable.ic_person); // Dùng tạm placeholder
        } else {
            holder.imgReviewerAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    /**
     * Phương thức cập nhật danh sách đánh giá.
     * Được gọi từ TourReviewFragment sau khi tải dữ liệu từ Firebase.
     */
    public void updateList(List<Review> newList) {
        this.reviewList = newList;
        notifyDataSetChanged();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgReviewerAvatar;
        final TextView tvReviewerName;
        final RatingBar ratingBar;
        final TextView tvReviewDate;
        final TextView tvReviewContent;
        // final RecyclerView rvReviewImages; // Nếu có ảnh đính kèm

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View từ item_review.xml
            imgReviewerAvatar = itemView.findViewById(R.id.img_reviewer_avatar);
            tvReviewerName = itemView.findViewById(R.id.tv_reviewer_name);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            tvReviewContent = itemView.findViewById(R.id.tv_review_content);
        }
    }
}