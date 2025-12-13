package com.example.quanlytourdl.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    private static final String TAG = "TourAdapter";

    // ⭐ HẰNG SỐ TRẠNG THÁI: Dùng để Fragment/Activity gọi
    public static final String STATUS_APPROVED = "DANG_MO_BAN";
    public static final String STATUS_REJECTED = "DA_TU_CHOI";
    public static final String STATUS_PENDING = "CHO_PHE_DUYET"; // Nếu cần dùng trong Adapter

    // ⭐ Interface Callback ĐÃ SỬA TÊN
    public interface OnTourActionListener {
        void onApproveReject(String tourId, String tourName, String newStatus, int position);
        void onViewDetails(Tour tour);
        void onImageLoad(String imageUrl, ImageView targetView);
    }

    private final List<Tour> tourList;
    private final Context context;
    private final OnTourActionListener listener;
    private final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, new java.util.Locale("vi", "VN"));

    public TourAdapter(Context context, List<Tour> tourList, OnTourActionListener listener) {
        this.context = context;
        this.tourList = tourList;
        this.listener = listener;
    }

    public void updateList(List<Tour> newList) {
        tourList.clear();
        tourList.addAll(newList);
        notifyDataSetChanged();
        Log.d(TAG, "Danh sách tour đã được cập nhật. Số lượng: " + tourList.size());
    }

    public void removeItem(int position) {
        if (position >= 0 && position < tourList.size()) {
            tourList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, tourList.size());
        }
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_cho_phe_duyet, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        final Tour tour = tourList.get(position);

        // --- 1. Hiển thị thông tin cơ bản ---
        holder.tourNameTextView.setText(tour.getTenTour());

        Date ngayKhoiHanh = tour.getNgayKhoiHanh();
        String ngayKhoiHanhStr = (ngayKhoiHanh != null) ? dateFormatter.format(ngayKhoiHanh) : "Chưa xác định";

        String details = tour.getDiemKhoiHanh() + " -> " + tour.getDiemDen() + " | Khởi hành: " + ngayKhoiHanhStr;
        holder.tourDetailsTextView.setText(details);

        Date ngayTao = tour.getNgayTao();
        String ngayTaoStr = (ngayTao != null) ? dateFormatter.format(ngayTao) : "N/A";

        String creatorInfo = String.format("Người tạo ID: %s - Ngày tạo: %s",
                tour.getNguoiTao() != null ? tour.getNguoiTao() : "Unknown",
                ngayTaoStr);
        holder.creatorInfoTextView.setText(creatorInfo);

        // --- 2. Tải hình ảnh ---
        if (tour.getHinhAnhChinhUrl() != null && !tour.getHinhAnhChinhUrl().isEmpty()) {
            listener.onImageLoad(tour.getHinhAnhChinhUrl(), holder.thumbnailImageView);
        } else {
            holder.thumbnailImageView.setImageResource(R.drawable.tour_placeholder_danang);
        }

        // --- 3. Xử lý sự kiện click ---
        holder.approveButton.setOnClickListener(v ->
                showActionDialog(tour, STATUS_APPROVED, "Phê duyệt", position)
        );

        holder.rejectButton.setOnClickListener(v ->
                showActionDialog(tour, STATUS_REJECTED, "Từ chối", position)
        );

        holder.viewDetailsTextView.setOnClickListener(v -> {
            listener.onViewDetails(tour);
        });
    }

    private void showActionDialog(Tour tour, String newStatus, String action, int position) {
        if (tour.getMaTour() == null) {
            Toast.makeText(context, "Lỗi: Không tìm thấy mã tour để cập nhật.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Lỗi: Mã tour rỗng khi thực hiện hành động " + action);
            return;
        }

        String message = "Bạn có chắc chắn muốn " + action + " tour \"" + tour.getTenTour() + "\" không?";

        new AlertDialog.Builder(context)
                .setTitle("Xác nhận " + action + " Tour")
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    listener.onApproveReject(tour.getMaTour(), tour.getTenTour(), newStatus, position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView tourNameTextView;
        TextView tourDetailsTextView;
        TextView creatorInfoTextView;
        TextView viewDetailsTextView;
        MaterialButton rejectButton;
        MaterialButton approveButton;

        public TourViewHolder(View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.img_tour_thumbnail);
            tourNameTextView = itemView.findViewById(R.id.text_tour_name);
            tourDetailsTextView = itemView.findViewById(R.id.text_tour_details);
            creatorInfoTextView = itemView.findViewById(R.id.text_creator_info);
            viewDetailsTextView = itemView.findViewById(R.id.text_view_details);
            rejectButton = itemView.findViewById(R.id.btn_reject);
            approveButton = itemView.findViewById(R.id.btn_approve);
        }
    }
}