package com.example.quanlytourdl.adapter;

import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter chuyên biệt để hiển thị danh sách Tour cần phân công hoặc đã được phân công.
 */
public class TourAssignmentAdapter extends RecyclerView.Adapter<TourAssignmentAdapter.TourAssignmentViewHolder> {

    private static final String TAG = "TourAssignmentAdapter";

    // Interface để giao tiếp ngược với Fragment
    public interface AssignmentListener {
        void onAssignTourClick(Tour tour);
    }

    // ⭐ Hằng số Trạng thái Tour: Đảm bảo khớp với DB
    private static final String STATUS_AWAITING_ASSIGNMENT = "DANG_CHO_PHAN_CONG";
    private static final String STATUS_ASSIGNED = "DA_GAN_NHAN_VIEN"; // Hoặc bất kỳ trạng thái nào bạn dùng

    private final List<Tour> tourList;
    private final Context context;
    private final AssignmentListener listener;
    // Sử dụng SimpleDateFormat để kiểm soát định dạng tốt hơn
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    public TourAssignmentAdapter(Context context, List<Tour> tourList, AssignmentListener listener) {
        this.context = context;
        this.tourList = tourList;
        this.listener = listener;
        Log.d(TAG, "Adapter cho Phân công Tour đã được khởi tạo.");
    }

    public void updateList(List<Tour> newList) {
        tourList.clear();
        tourList.addAll(newList);
        notifyDataSetChanged();
        Log.d(TAG, "Danh sách tour cho phân công đã được cập nhật: " + newList.size());
    }

    @NonNull
    @Override
    public TourAssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng R.layout.item_tour_assignment
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_assignment, parent, false);
        return new TourAssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourAssignmentViewHolder holder, int position) {
        final Tour tour = tourList.get(position);

        // 1. Hiển thị thông tin cơ bản
        holder.tourCodeTextView.setText(String.format("Mã Tour: %s", tour.getMaTour()));
        holder.tourNameTextView.setText(tour.getTenTour());

        // Ngày khởi hành
        String ngayKhoiHanhText = tour.getNgayKhoiHanh() != null ? dateFormatter.format(tour.getNgayKhoiHanh()) : "Chưa xác định";
        holder.departureDateTextView.setText(String.format("Ngày khởi hành: %s", ngayKhoiHanhText));

        String customerCountText = String.format("Số khách: %d/%d",
                tour.getSoLuongKhachHienTai(),
                tour.getSoLuongKhachToiDa());
        holder.customerCountTextView.setText(customerCountText);

        // 2. Xử lý Trạng thái Phân công
        boolean isAssigned = tour.getStatus() != null && tour.getStatus().equals(STATUS_ASSIGNED);

        if (isAssigned) {
            // Đã Phân Công
            holder.statusTextView.setText("ĐÃ PHÂN CÔNG (Xem chi tiết)");
            holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.green_700)); // Màu xanh cho Đã gán

            holder.assignButton.setText("Xem/Sửa Gán");
            holder.assignButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_700)); // Màu xanh cho nút
        } else {
            // Chưa Phân Công (ĐANG_CHO_PHAN_CONG)
            holder.statusTextView.setText("CHƯA PHÂN CÔNG (Cần gán HDV/PT)");
            holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.red_700)); // Màu đỏ/cảnh báo

            holder.assignButton.setText("Gán Ngay");
            holder.assignButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.orange_700)); // Màu cam cho nút
        }
        // Giả định các màu (green_700, red_700, blue_500, orange_600) đã tồn tại trong colors.xml

        // 3. Xử lý sự kiện Gán/Xem Chi Tiết
        holder.assignButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAssignTourClick(tour);
            } else {
                Toast.makeText(context, "Lỗi: AssignmentListener không được thiết lập.", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Tải ảnh (Chức năng này thường được xử lý bởi thư viện như Glide/Picasso)
        // Hiện tại chỉ dùng placeholder
        holder.thumbnailImageView.setImageResource(R.drawable.tour_placeholder_danang);
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public static class TourAssignmentViewHolder extends RecyclerView.ViewHolder {

        // ⭐ Ánh xạ đã được sửa để khớp với item_tour_assignment.xml ⭐
        ImageView thumbnailImageView;
        TextView tourCodeTextView;      // R.id.text_tour_code
        TextView tourNameTextView;      // R.id.text_tour_name
        TextView departureDateTextView; // R.id.text_departure_date
        TextView customerCountTextView; // R.id.text_customer_count
        TextView statusTextView;        // R.id.text_assignment_status
        MaterialButton assignButton;    // R.id.btn_assign_now

        public TourAssignmentViewHolder(View itemView) {
            super(itemView);

            // --- Ánh xạ các trường thông tin ---
            thumbnailImageView = itemView.findViewById(R.id.img_tour); // Đã sửa từ img_tour_thumbnail
            tourCodeTextView = itemView.findViewById(R.id.text_tour_code);
            tourNameTextView = itemView.findViewById(R.id.text_tour_name);
            departureDateTextView = itemView.findViewById(R.id.text_departure_date);
            customerCountTextView = itemView.findViewById(R.id.text_customer_count);

            // --- Ánh xạ các trường hành động/trạng thái ---
            statusTextView = itemView.findViewById(R.id.text_assignment_status);
            assignButton = itemView.findViewById(R.id.btn_assign_now);
        }
    }
}