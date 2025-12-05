package com.example.quanlytourdl.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.firebase.FirebaseRepository;
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    private final List<Tour> tourList;
    private final Context context;
    private final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, new java.util.Locale("vi", "VN"));
    private final FirebaseRepository repository; // Thêm Repository

    public TourAdapter(Context context, List<Tour> tourList) {
        this.context = context;
        this.tourList = tourList;
        this.repository = new FirebaseRepository(); // Khởi tạo Repository
    }

    // Phương thức giúp cập nhật danh sách tour
    public void updateList(List<Tour> newList) {
        tourList.clear();
        tourList.addAll(newList);
        notifyDataSetChanged();
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
        holder.tourDetailsTextView.setText("Mã tour: " + tour.getMaTour());

        String creatorInfo = tour.getNguoiTao() + " - " + dateFormatter.format(tour.getNgayTao());
        holder.creatorInfoTextView.setText(creatorInfo);

        // Xử lý sự kiện Phê duyệt
        holder.approveButton.setOnClickListener(v -> showApprovalDialog(tour, true));

        // Xử lý sự kiện Từ chối
        holder.rejectButton.setOnClickListener(v -> showApprovalDialog(tour, false));

        // Xử lý Xem chi tiết
        holder.viewDetailsTextView.setOnClickListener(v -> {
            Toast.makeText(context, "Mở màn hình chi tiết tour " + tour.getMaTour(), Toast.LENGTH_SHORT).show();
        });

        // Ghi chú: Sử dụng thư viện ngoài (Glide/Picasso) để load holder.thumbnailImageView
    }

    private void showApprovalDialog(Tour tour, boolean isApproval) {
        String action = isApproval ? "Phê duyệt" : "Từ chối";
        String message = "Bạn có chắc chắn muốn " + action + " tour \"" + tour.getTenTour() + "\" không?";

        new AlertDialog.Builder(context)
                .setTitle("Xác nhận " + action + " Tour")
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Gọi Repository để cập nhật trạng thái trên Firebase
                    repository.updateTourApprovalStatus(tour.getMaTour(), isApproval);
                    Toast.makeText(context, action + " thành công!", Toast.LENGTH_SHORT).show();
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