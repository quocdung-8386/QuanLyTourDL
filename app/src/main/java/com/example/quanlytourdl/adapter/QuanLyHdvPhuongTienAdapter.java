package com.example.quanlytourdl.adapter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Vehicle;
import com.example.quanlytourdl.model.Guide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter chung để hiển thị danh sách Hướng dẫn viên hoặc Phương tiện.
 */
public class QuanLyHdvPhuongTienAdapter extends RecyclerView.Adapter<QuanLyHdvPhuongTienAdapter.ItemViewHolder> {

    // ⭐ 1. CẬP NHẬT: ĐỊNH NGHĨA INTERFACE BAO GỒM VIEW DETAILS
    public interface OnItemActionListener {
        void onEditItem(Object item);
        void onDeleteItem(Object item);
        void onViewDetails(Object item); // ⭐ PHƯƠNG THỨC MỚI CHO SỰ KIỆN CLICK ITEM
    }

    private final List<Object> dataList = new ArrayList<>();
    private final boolean isGuideList;
    private final OnItemActionListener listener;

    // 2. CONSTRUCTOR (GIỮ NGUYÊN)
    public QuanLyHdvPhuongTienAdapter(boolean isGuideList, OnItemActionListener listener) {
        this.isGuideList = isGuideList;
        this.listener = listener;
    }

    public void updateData(List<?> newData) {
        dataList.clear();
        dataList.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hdv_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Object item = dataList.get(position);

        if (isGuideList) {
            // Hiển thị dữ liệu Hướng dẫn viên
            if (item instanceof Guide) {
                Guide guide = (Guide) item;
                holder.imgAvatar.setImageResource(R.drawable.ic_hdv_placeholder);

                holder.textName.setText(guide.getFullName());
                holder.textIdDetail.setText("Mã HDV: " + guide.getGuideCode());

                String status = guide.getTrangThai();

                // Xử lý trạng thái
                if (status != null && !status.isEmpty()) {
                    holder.textStatusBadge.setText(status);
                    setStatusStyle(holder.textStatusBadge, status);
                } else {
                    holder.textStatusBadge.setText("Chưa có trạng thái");
                    setStatusStyle(holder.textStatusBadge, "Default");
                }

                // ⭐ CẬP NHẬT: Xử lý sự kiện click item (Xem Chi tiết)
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewDetails(guide);
                    }
                });

                // GÁN LISTENER CHO NÚT TÙY CHỌN (3 chấm)
                holder.btnMoreOptions.setOnClickListener(v -> {
                    showPopupMenu(v, guide);
                });
            }
        } else {
            // Hiển thị dữ liệu Phương tiện
            if (item instanceof Vehicle) {
                Vehicle vehicle = (Vehicle) item;
                holder.imgAvatar.setImageResource(R.drawable.ic_phuongtien_placeholder);
                holder.textName.setText(vehicle.getBienSoXe());
                holder.textIdDetail.setText("Loại: " + vehicle.getLoaiPhuongTien() + " | " + vehicle.getSoChoNgoi() + " chỗ");

                String status = vehicle.getTinhTrangBaoDuong();

                // Xử lý trạng thái
                if (status != null && !status.isEmpty()) {
                    holder.textStatusBadge.setText(status);
                    setStatusStyle(holder.textStatusBadge, status);
                } else {
                    holder.textStatusBadge.setText("Chưa có trạng thái");
                    setStatusStyle(holder.textStatusBadge, "Default");
                }


                // ⭐ CẬP NHẬT: Xử lý sự kiện click item (Xem Chi tiết)
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewDetails(vehicle);
                    }
                });

                // GÁN LISTENER CHO NÚT TÙY CHỌN (3 chấm)
                holder.btnMoreOptions.setOnClickListener(v -> {
                    showPopupMenu(v, vehicle);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // 3. PHƯƠNG THỨC HIỂN THỊ POPUP MENU (Chỉ Chỉnh sửa/Xóa)
    private void showPopupMenu(View anchorView, Object item) {
        if (listener == null) {
            Toast.makeText(anchorView.getContext(), "Listener chưa được thiết lập.", Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu popup = new PopupMenu(anchorView.getContext(), anchorView);

        // Thêm tùy chọn "Chỉnh sửa"
        popup.getMenu().add("Chỉnh sửa").setOnMenuItemClickListener(menuItem -> {
            listener.onEditItem(item);
            return true;
        });

        // Thêm tùy chọn "Xóa"
        popup.getMenu().add("Xóa").setOnMenuItemClickListener(menuItem -> {
            listener.onDeleteItem(item);
            return true;
        });

        popup.show();
    }

    /**
     * Hàm giả lập thiết lập màu sắc cho trạng thái dựa trên chuỗi trạng thái.
     */
    private void setStatusStyle(TextView statusBadge, String status) {
        // ... (Giữ nguyên logic setStatusStyle) ...
        // Đảm bảo status là chữ thường hoặc dùng hàm equalsIgnoreCase nếu cần
        switch (status) {
            case "Sẵn sàng": // HDV
            case "Hoạt động tốt": // PT
                // Giả định R.drawable.bg_status_ready và R.color.green_700 tồn tại
                statusBadge.setBackgroundResource(R.drawable.bg_status_ready);
                statusBadge.setTextColor(ContextCompat.getColor(statusBadge.getContext(), R.color.green_700));
                break;
            case "Đang đi tour": // HDV
                // Giả định R.drawable.bg_status_in_use và R.color.blue_700 tồn tại
                statusBadge.setBackgroundResource(R.drawable.bg_status_in_use);
                statusBadge.setTextColor(ContextCompat.getColor(statusBadge.getContext(), R.color.blue_700));
                break;
            case "Tạm nghỉ": // HDV
            case "Cần sửa chữa lớn": // PT
                // Giả định R.drawable.bg_status_unavailable và R.color.red_700 tồn tại
                statusBadge.setBackgroundResource(R.drawable.bg_status_unavailable);
                statusBadge.setTextColor(ContextCompat.getColor(statusBadge.getContext(), R.color.red_700));
                break;
            case "Cần bảo trì nhỏ": // PT
            case "Đang bảo dưỡng": // PT
                // Giả định R.drawable.bg_status_maintenance và R.color.orange_700 tồn tại
                statusBadge.setBackgroundResource(R.drawable.bg_status_maintenance);
                statusBadge.setTextColor(ContextCompat.getColor(statusBadge.getContext(), R.color.orange_700));
                break;
            case "Default": // Trạng thái mặc định hoặc chưa cập nhật
            default:
                // Giả định R.drawable.bg_status_default và R.color.gray_700 tồn tại
                statusBadge.setBackgroundResource(R.drawable.bg_status_default);
                statusBadge.setTextColor(ContextCompat.getColor(statusBadge.getContext(), R.color.gray_700));
                break;
        }
    }


    /**
     * ViewHolder (Giữ nguyên)
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgAvatar;
        TextView textName;
        TextView textIdDetail;
        TextView textStatusBadge;
        ImageButton btnMoreOptions;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các TextView theo ID mới
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            textName = itemView.findViewById(R.id.text_name);
            textIdDetail = itemView.findViewById(R.id.text_id_detail);
            textStatusBadge = itemView.findViewById(R.id.text_status_badge);
            btnMoreOptions = itemView.findViewById(R.id.btn_more_options);
        }
    }
}