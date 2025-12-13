package com.example.quanlytourdl.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
 * Cấu trúc ViewHolder đã được cập nhật để khớp với item_common_list.xml mới.
 */
public class QuanLyHdvPhuongTienAdapter extends RecyclerView.Adapter<QuanLyHdvPhuongTienAdapter.ItemViewHolder> {

    private final List<Object> dataList = new ArrayList<>();
    private final boolean isGuideList; // true nếu đang hiển thị HDV, false nếu hiển thị PT

    public QuanLyHdvPhuongTienAdapter(boolean isGuideList) {
        this.isGuideList = isGuideList;
    }

    public void updateData(List<?> newData) {
        dataList.clear();
        dataList.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Giả định item_hdv_card.xml đã được định nghĩa
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

                // SỬA LỖI TIỀM NĂNG: Thay getTen() bằng getFullName() (tên trường Firestore là fullName)
                // và sử dụng Mã HDV cho chi tiết
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

                // Xử lý sự kiện click item
                holder.itemView.setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "Chi tiết HDV: " + guide.getFullName(), Toast.LENGTH_SHORT).show();
                    // Mở Fragment chi tiết HDV
                });
                holder.btnMoreOptions.setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "Tùy chọn cho HDV: " + guide.getFullName(), Toast.LENGTH_SHORT).show();
                });
            }
        } else {
            // Hiển thị dữ liệu Phương tiện
            if (item instanceof Vehicle) {
                Vehicle vehicle = (Vehicle) item;
                holder.imgAvatar.setImageResource(R.drawable.ic_phuongtien_placeholder); // Ảnh mặc định cho PT (Cần định nghĩa icon này)
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


                // Xử lý sự kiện click item
                holder.itemView.setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "Chi tiết PT: " + vehicle.getBienSoXe(), Toast.LENGTH_SHORT).show();
                    // Mở Fragment chi tiết PT
                });
                holder.btnMoreOptions.setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "Tùy chọn cho PT: " + vehicle.getBienSoXe(), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    /**
     * Hàm giả lập thiết lập màu sắc cho trạng thái dựa trên chuỗi trạng thái.
     * Cần đảm bảo các drawable bg_status_... và color... được định nghĩa.
     */
    private void setStatusStyle(TextView statusBadge, String status) {
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
     * ViewHolder (Đã cập nhật ID)
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgAvatar; // Sử dụng ShapeableImageView
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