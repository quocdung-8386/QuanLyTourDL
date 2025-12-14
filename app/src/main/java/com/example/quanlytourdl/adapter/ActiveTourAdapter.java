package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class ActiveTourAdapter extends RecyclerView.Adapter<ActiveTourAdapter.ActiveTourViewHolder> {

    private static final String TAG = "ActiveTourAdapter";

    // ⭐ Interface Callback (Định nghĩa các hành động)
    public interface OnActiveTourActionListener {
        void onUpdateTour(Tour tour);
        void onViewDetails(Tour tour);
        void onImageLoad(String imageUrl, ImageView targetView);
    }

    private final List<Tour> tourList;
    private final Context context;
    private final OnActiveTourActionListener listener;

    // ⭐ Định dạng tiền tệ Việt Nam (Ví dụ: 3.250.000₫)
    private final DecimalFormat currencyFormatter = new DecimalFormat("#,###₫", new java.text.DecimalFormatSymbols(new Locale("vi", "VN")));

    public ActiveTourAdapter(Context context, List<Tour> tourList, OnActiveTourActionListener listener) {
        this.context = context;
        this.tourList = tourList;
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách tour từ Firebase
     * @param newList Danh sách Tour mới
     */
    public void updateList(List<Tour> newList) {
        tourList.clear();
        tourList.addAll(newList);
        notifyDataSetChanged();
        Log.d(TAG, "Danh sách tour đang mở bán đã được cập nhật. Số lượng: " + tourList.size());
    }

    @NonNull
    @Override
    public ActiveTourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ánh xạ đến layout item bạn đã cung cấp
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_card, parent, false);
        return new ActiveTourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveTourViewHolder holder, int position) {
        final Tour tour = tourList.get(position);

        // --- 1. Header (Mã Tour & Giá bán) ---
        holder.tourCodeTextView.setText(tour.getMaTour() != null ? tour.getMaTour() : "N/A");

        // Hiển thị giá tour người lớn
        if (tour.getGiaNguoiLon() > 0) {
            String formattedPrice = currencyFormatter.format(tour.getGiaNguoiLon());
            holder.tourPriceTextView.setText(formattedPrice);
        } else {
            holder.tourPriceTextView.setText("Liên hệ");
        }

        // --- 2. Tên Tour và Badge ---
        holder.tourNameTextView.setText(tour.getTenTour() != null ? tour.getTenTour() : "Tour không tên");

        // Hiển thị HOT badge dựa trên isNoiBat
        // DÒNG ĐÃ SỬA LỖI: Sử dụng getIsNoiBat() thay vì isNoiBat()
        if (tour.getIsNoiBat()) {
            holder.hotBadgeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.hotBadgeTextView.setVisibility(View.GONE);
        }

        // --- 3. Chi tiết (Thời lượng, Địa điểm, Sức chứa) ---
        String duration = tour.getSoNgay() + "N" + tour.getSoDem() + "Đ";
        holder.tourDurationTextView.setText(duration);

        holder.tourLocationTextView.setText(tour.getDiemDen() != null ? tour.getDiemDen() : "N/A");

        // Cần tích hợp logic đếm khách thực tế ở đây, hiện tại giả lập 0
        String capacity = "0/" + tour.getSoLuongKhachToiDa();
        holder.tourCapacityTextView.setText(capacity);

        // --- 4. Đánh giá ---
        // Giả lập Rating và Review Count (vì Tour Model không có trường này)
        // Trong thực tế: Bạn cần truy vấn collection Ratings/Reviews
        // holder.tourRatingTextView.setText(String.format(Locale.US, "%.1f", tour.getAverageRating()));
        holder.tourRatingTextView.setText("5.0");
        holder.reviewCountTextView.setText("(0 đánh giá)");

        // --- 5. Trạng thái và Hành động ---
        updateStatusDisplay(holder.statusTextView, tour.getStatus());

        // Bắt sự kiện Click cho nút Cập nhật
        holder.btnUpdate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateTour(tour);
            }
        });

        // Bắt sự kiện Click cho toàn bộ item để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(tour);
            }
        });

        // Tải ảnh chính (Nếu có Glide/Picasso)
        if (listener != null && tour.getHinhAnhChinhUrl() != null) {
            // listener.onImageLoad(tour.getHinhAnhChinhUrl(), holder.mainImageView);
            // Hiện tại không có ImageView chính trong layout, nhưng giữ lại logic nếu bạn thêm sau.
        }
    }

    /**
     * Cập nhật hiển thị trạng thái (màu sắc và chữ) dựa trên trường status của Tour
     * (Giả định rằng adapter này chỉ nhận các tour đang mở bán)
     */
    private void updateStatusDisplay(TextView statusTextView, String status) {
        // Dựa trên logic getDbStatus() trong Fragment
        if (status != null) {
            if (status.equals("DANG_MO_BAN")) {
                statusTextView.setText("Đang mở bán");
                // Đặt màu xanh lá (Green 500)
                statusTextView.setTextColor(ContextCompat.getColor(context, R.color.green_700));
                // Cần drawable ic_status_dot_green. Nếu không có, bạn có thể tự thay đổi màu drawable.
            } else if (status.equals("HET_HAN")) {
                statusTextView.setText("Hết hạn");
                statusTextView.setTextColor(ContextCompat.getColor(context, R.color.red_700));
            }
            // Thêm các trạng thái khác nếu cần
        }
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    // ⭐ ViewHolder: Ánh xạ các View từ item_tour_dang_mo_ban.xml
    public static class ActiveTourViewHolder extends RecyclerView.ViewHolder {
        final TextView tourCodeTextView;
        final TextView tourPriceTextView;
        final TextView tourNameTextView;
        final TextView hotBadgeTextView;
        final TextView tourDurationTextView;
        final TextView tourLocationTextView;
        final TextView tourCapacityTextView;
        final TextView tourRatingTextView;
        final TextView reviewCountTextView;
        final TextView statusTextView;
        final MaterialButton btnUpdate;

        // Nếu layout có ImageView chính, bạn nên thêm nó vào đây

        public ActiveTourViewHolder(@NonNull View itemView) {
            super(itemView);
            tourCodeTextView = itemView.findViewById(R.id.text_tour_code);
            tourPriceTextView = itemView.findViewById(R.id.text_tour_price);
            tourNameTextView = itemView.findViewById(R.id.text_tour_name);
            hotBadgeTextView = itemView.findViewById(R.id.text_hot_badge);
            tourDurationTextView = itemView.findViewById(R.id.text_tour_duration);
            tourLocationTextView = itemView.findViewById(R.id.text_tour_location);
            tourCapacityTextView = itemView.findViewById(R.id.text_tour_capacity);
            tourRatingTextView = itemView.findViewById(R.id.text_tour_rating);
            reviewCountTextView = itemView.findViewById(R.id.text_review_count);
            statusTextView = itemView.findViewById(R.id.text_status);
            btnUpdate = itemView.findViewById(R.id.btn_update);
        }
    }
}