package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Guide;
import com.example.quanlytourdl.model.Vehicle;
import java.util.List;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {

    private final Context context;
    private final List<Object> resourceList; // Dùng Object để chứa cả Guide và Vehicle
    private final OnResourceSelectedListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnResourceSelectedListener {
        /**
         * Được gọi khi người dùng chọn một tài nguyên (HDV hoặc Xe).
         * @param resource Đối tượng Guide hoặc Vehicle đã được chọn.
         */
        void onResourceSelected(Object resource);
    }

    public ResourceAdapter(Context context, List<Object> resourceList, OnResourceSelectedListener listener) {
        this.context = context;
        this.resourceList = resourceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_resource_assignment, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        Object resource = resourceList.get(position);

        // --- 1. Cấu hình nội dung dựa trên loại đối tượng ---
        if (resource instanceof Guide) {
            Guide guide = (Guide) resource;

            // Tên và chi tiết
            holder.tvName.setText(guide.getFullName());
            // Hiển thị Ngôn ngữ và Kinh nghiệm
            String lang = guide.getLanguages() != null && !guide.getLanguages().isEmpty()
                    ? String.join(", ", guide.getLanguages()) : "Không rõ";
            holder.tvDetails.setText(String.format("%s • %d năm KN", lang, guide.getExperienceYears()));

            // Rating
            holder.tvRating.setText(String.valueOf(guide.getRating()));
            holder.tvRating.setVisibility(View.VISIBLE);

            // Ảnh/Icon
            holder.imgAvatar.setImageResource(R.drawable.ic_hdv_placeholder); // Thay bằng logic load ảnh thực tế

            // Trạng thái Lịch
            updateStatusUI(holder, guide.isAvailable(), "GUIDE");

        } else if (resource instanceof Vehicle) {
            Vehicle vehicle = (Vehicle) resource;

            // Tên và chi tiết (Biển số và Tên tài xế/Số chỗ)
            holder.tvName.setText(vehicle.getBienSoXe());
            holder.tvDetails.setText(String.format("%s - %d chỗ (%s)",
                    vehicle.getLoaiPhuongTien(),
                    vehicle.getSoChoNgoi(),
                    vehicle.getDriverName() != null ? vehicle.getDriverName() : "Chưa gán tài xế"));

            // Rating (Ẩn đi cho xe)
            holder.tvRating.setVisibility(View.GONE);

            // Ảnh/Icon
            holder.imgAvatar.setImageResource(R.drawable.ic_bus); // Giả định có ic_car trong drawable

            // Trạng thái Lịch
            updateStatusUI(holder, vehicle.isAvailable(), "VEHICLE");
        }

        // --- 2. Cấu hình Radio Button và Sự kiện click ---
        holder.radioButton.setChecked(position == selectedPosition);

        // Cả ItemView đều có thể click
        holder.itemView.setOnClickListener(v -> {
            int oldSelectedPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Cập nhật trạng thái cho mục cũ và mục mới
            notifyItemChanged(oldSelectedPosition);
            notifyItemChanged(selectedPosition);

            listener.onResourceSelected(resource);
        });
    }

    /**
     * Cập nhật UI Trạng thái (Trống lịch/Vướng lịch).
     * @param holder ViewHolder
     * @param isAvailable true nếu trống lịch
     * @param type "GUIDE" hoặc "VEHICLE"
     */
    private void updateStatusUI(ResourceViewHolder holder, boolean isAvailable, String type) {
        GradientDrawable background = (GradientDrawable) holder.tvStatus.getBackground();

        if (isAvailable) {
            holder.tvStatus.setText("Trống lịch");
            holder.tvStatus.setTextColor(Color.parseColor("#1B5E20")); // Màu xanh đậm
            background.setColor(Color.parseColor("#E8F5E9")); // Màu xanh nhạt (bg_status_green)
            holder.imgStatusIcon.setImageResource(R.drawable.ic_check_circle);
            holder.imgStatusIcon.setColorFilter(Color.parseColor("#1B5E20"));
        } else {
            holder.tvStatus.setText("Vướng lịch");
            holder.tvStatus.setTextColor(Color.parseColor("#B71C1C")); // Màu đỏ đậm
            background.setColor(Color.parseColor("#FFEBEE")); // Màu đỏ nhạt (bg_status_red)
            holder.imgStatusIcon.setImageResource(R.drawable.ic_warning);
            holder.imgStatusIcon.setColorFilter(Color.parseColor("#B71C1C"));
        }
    }


    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    public void updateList(List<Object> newList) {
        resourceList.clear();
        resourceList.addAll(newList);
        selectedPosition = RecyclerView.NO_POSITION; // Reset lựa chọn khi đổi danh sách
        notifyDataSetChanged();
    }

    public static class ResourceViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgAvatar;
        final ImageView imgStatusIcon;
        final TextView tvName;
        final TextView tvDetails;
        final TextView tvRating;
        final TextView tvStatus;
        final RadioButton radioButton;

        public ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            imgStatusIcon = itemView.findViewById(R.id.img_status_icon);
            tvName = itemView.findViewById(R.id.tv_resource_name);
            tvDetails = itemView.findViewById(R.id.tv_resource_details);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvStatus = itemView.findViewById(R.id.tv_schedule_status);
            radioButton = itemView.findViewById(R.id.radio_select);
        }
    }
}