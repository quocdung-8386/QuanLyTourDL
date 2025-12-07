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
import com.example.quanlytourdl.firebase.FirebaseRepository; // Import Repository
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    private final List<Tour> tourList;
    private final Context context;
    // Sử dụng Repository trực tiếp
    private final FirebaseRepository repository;
    private final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, new java.util.Locale("vi", "VN"));

    // Constructor đơn giản
    public TourAdapter(Context context, List<Tour> tourList) {
        this.context = context;
        this.tourList = tourList;
        // Khởi tạo Repository tại đây
        this.repository = new FirebaseRepository();
        Log.d("TourAdapter", "FirebaseRepository đã được khởi tạo trong Adapter.");
    }

    // Phương thức giúp cập nhật danh sách tour
    public void updateList(List<Tour> newList) {
        tourList.clear();
        tourList.addAll(newList);
        // Bắt buộc gọi để RecyclerView hiển thị dữ liệu mới
        notifyDataSetChanged();
        Log.d("TourAdapter", "Danh sách tour đã được cập nhật.");
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_cho_phe_duyet, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tourList.get(position);

        // Hiển thị thông tin
        holder.tourNameTextView.setText(tour.getTenTour());
        String details = tour.getDiemKhoiHanh() + " -> " + tour.getDiemDen() + " | Khởi hành: " + dateFormatter.format(tour.getNgayKhoiHanh());
        holder.tourDetailsTextView.setText(details);

        // Giả định tour.getNguoiTao() và tour.getNgayTao() không null
        String creatorInfo = "Người tạo ID: " + tour.getNguoiTao() + " - Ngày tạo: " + dateFormatter.format(tour.getNgayTao());
        holder.creatorInfoTextView.setText(creatorInfo);

        // Xử lý sự kiện Phê duyệt
        holder.approveButton.setOnClickListener(v ->
                // Gửi trạng thái đã được phê duyệt (DANG_MO_BAN)
                showActionDialog(tour, FirebaseRepository.STATUS_APPROVED, "Phê duyệt")
        );

        // Xử lý sự kiện Từ chối
        holder.rejectButton.setOnClickListener(v ->
                // Gửi trạng thái đã bị từ chối (DA_TU_CHOI)
                showActionDialog(tour, FirebaseRepository.STATUS_REJECTED, "Từ chối")
        );

        // Xử lý Xem chi tiết
        holder.viewDetailsTextView.setOnClickListener(v -> {
            Toast.makeText(context, "Mở màn hình chi tiết tour " + tour.getMaTour(), Toast.LENGTH_SHORT).show();
            // TODO: Triển khai logic điều hướng đến màn hình chi tiết
        });
    }

    // Đã thay đổi tham số thành String newStatus
    private void showActionDialog(Tour tour, String newStatus, String action) {
        String message = "Bạn có chắc chắn muốn " + action + " tour \"" + tour.getTenTour() + "\" không?";

        new AlertDialog.Builder(context)
                .setTitle("Xác nhận " + action + " Tour")
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    if (tour.getMaTour() != null) {
                        // **ĐÃ SỬA:** Gọi hàm updateTourStatus và truyền chuỗi trạng thái
                        repository.updateTourStatus(tour.getMaTour(), newStatus);
                        Toast.makeText(context, action + " tour " + tour.getMaTour() + " thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Lỗi: Không tìm thấy mã tour để cập nhật.", Toast.LENGTH_SHORT).show();
                    }
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