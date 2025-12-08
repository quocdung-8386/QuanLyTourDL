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
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.util.List;

/**
 * Adapter chuyên biệt để hiển thị danh sách Tour cần phân công hoặc đã được phân công.
 * Logic click sẽ được truyền ngược lại Fragment qua AssignmentListener.
 */
public class TourAssignmentAdapter extends RecyclerView.Adapter<TourAssignmentAdapter.TourAssignmentViewHolder> {

    // Interface để giao tiếp ngược với Fragment
    public interface AssignmentListener {
        void onAssignTourClick(Tour tour);
    }

    private final List<Tour> tourList;
    private final Context context;
    private final AssignmentListener listener;
    private final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, new java.util.Locale("vi", "VN"));

    public TourAssignmentAdapter(Context context, List<Tour> tourList, AssignmentListener listener) {
        this.context = context;
        this.tourList = tourList;
        this.listener = listener;
        Log.d("TourAssignmentAdapter", "Adapter cho Phân công Tour đã được khởi tạo.");
    }

    public void updateList(List<Tour> newList) {
        tourList.clear();
        tourList.addAll(newList);
        notifyDataSetChanged();
        Log.d("TourAssignmentAdapter", "Danh sách tour cho phân công đã được cập nhật: " + newList.size());
    }

    @NonNull
    @Override
    public TourAssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout được khuyến nghị cho màn hình này.
        // BẠN CẦN ĐẢM BẢO FILE R.layout.item_tour_assignment TỒN TẠI VÀ CHỨA CÁC ID CẦN THIẾT.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_assignment, parent, false);
        return new TourAssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourAssignmentViewHolder holder, int position) {
        Tour tour = tourList.get(position);

        // Hiển thị thông tin
        holder.tourNameTextView.setText(tour.getTenTour());
        // Kiểm tra null cho Date trước khi format
        String ngayKhoiHanhText = tour.getNgayKhoiHanh() != null ? dateFormatter.format(tour.getNgayKhoiHanh()) : "Chưa xác định";
        String details = tour.getDiemKhoiHanh() + " -> " + tour.getDiemDen() + " | Khởi hành: " + ngayKhoiHanhText;
        holder.tourDetailsTextView.setText(details);

        // Hiển thị trạng thái phân công
        String statusInfo;
        // Kiểm tra trạng thái an toàn
        boolean isAssigned = tour.getStatus() != null && (tour.getStatus().equals("DA_GAN_NHAN_VIEN") || tour.getStatus().equals("DA_GAN"));

        if (isAssigned) {
            statusInfo = "Đã Phân Công. Bấm để xem chi tiết.";
            holder.assignButton.setText("Xem Chi Tiết");
            holder.assignButton.setIconResource(R.drawable.ic_eye); // Thay thế bằng icon thực tế của bạn
            // Sử dụng màu sắc tương ứng
            holder.statusTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        } else {
            statusInfo = "Chưa Phân Công. Cần gán nhân viên/hướng dẫn viên.";
            holder.assignButton.setText("Gán Nhân Viên");
            holder.assignButton.setIconResource(R.drawable.ic_assignment); // Thay thế bằng icon thực tế của bạn
            // Sử dụng màu sắc tương ứng
            holder.statusTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        holder.statusTextView.setText(statusInfo);

        // Xử lý sự kiện Gán/Xem Chi Tiết
        holder.assignButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAssignTourClick(tour);
            } else {
                Toast.makeText(context, "Lỗi: Listener không được thiết lập.", Toast.LENGTH_SHORT).show();
            }
        });

        // Đảm bảo rằng các nút không liên quan đến phân công (nếu dùng chung layout) đều bị ẩn
        if (holder.rejectButton != null) holder.rejectButton.setVisibility(View.GONE);

        // KHÔNG CẦN ẩn holder.approveButton vì nó đã được sử dụng làm assignButton
        // Nếu layout mới có các nút này, chúng sẽ bị ẩn.

    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public static class TourAssignmentViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView tourNameTextView;
        TextView tourDetailsTextView;
        TextView statusTextView;
        MaterialButton assignButton;

        // Giữ lại các view không liên quan đến Assignment để ẩn đi an toàn
        MaterialButton rejectButton;

        public TourAssignmentViewHolder(View itemView) {
            super(itemView);
            // Ánh xạ các trường thông tin cơ bản
            thumbnailImageView = itemView.findViewById(R.id.img_tour_thumbnail);
            tourNameTextView = itemView.findViewById(R.id.text_tour_name);
            tourDetailsTextView = itemView.findViewById(R.id.text_tour_details);

            // Re-map các view cũ sang mục đích mới
            statusTextView = itemView.findViewById(R.id.text_creator_info);

            // Cần sửa lại ID trong layout mới thành R.id.btn_assign_action nếu có thể.
            // Nếu không, tạm thời sử dụng lại R.id.btn_approve (hoặc R.id.btn_action_primary)
            assignButton = itemView.findViewById(R.id.btn_approve);

            // Ánh xạ các nút Phê duyệt/Từ chối để ẩn đi (nếu dùng chung layout cũ)
            // Cần kiểm tra null an toàn.
            rejectButton = itemView.findViewById(R.id.btn_reject);

            // Ẩn TextView "Xem chi tiết" nếu nó có tồn tại trong layout
            TextView viewDetailsTextView = itemView.findViewById(R.id.text_view_details);
            if (viewDetailsTextView != null) viewDetailsTextView.setVisibility(View.GONE);
        }
    }
}