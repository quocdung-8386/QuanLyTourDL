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
import java.util.ArrayList;
import java.util.List;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {

    private final Context context;
    private List<Object> resourceList; // Bỏ final để linh hoạt hơn trong việc cập nhật
    private final OnResourceSelectedListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnResourceSelectedListener {
        void onResourceSelected(Object resource);
    }

    public ResourceAdapter(Context context, List<Object> resourceList, OnResourceSelectedListener listener) {
        this.context = context;
        this.resourceList = (resourceList != null) ? new ArrayList<>(resourceList) : new ArrayList<>();
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

        if (resource instanceof Guide) {
            Guide guide = (Guide) resource;
            holder.tvName.setText(guide.getFullName());

            String lang = (guide.getLanguages() != null && !guide.getLanguages().isEmpty())
                    ? String.join(", ", guide.getLanguages()) : "Chưa cập nhật ngôn ngữ";

            holder.tvDetails.setText(String.format("%s • %d năm KN", lang, guide.getExperienceYears()));
            holder.tvRating.setText(String.format("%.1f", guide.getRating()));
            holder.tvRating.setVisibility(View.VISIBLE);
            holder.imgAvatar.setImageResource(R.drawable.ic_hdv_placeholder);

            boolean isReady = guide.isApproved();
            String statusText = isReady ? "Sẵn sàng" : "Chờ phê duyệt";
            updateStatusUI(holder, isReady, statusText);

        } else if (resource instanceof Vehicle) {
            Vehicle vehicle = (Vehicle) resource;
            holder.tvName.setText(vehicle.getBienSoXe());
            holder.tvDetails.setText(String.format("%s - %d chỗ", vehicle.getLoaiPhuongTien(), vehicle.getSoChoNgoi()));
            holder.tvRating.setVisibility(View.GONE);
            holder.imgAvatar.setImageResource(R.drawable.ic_bus);

            boolean isReady = "Hoạt động tốt".equalsIgnoreCase(vehicle.getTinhTrangBaoDuong());
            updateStatusUI(holder, isReady, vehicle.getTinhTrangBaoDuong());
        }

        // Cập nhật trạng thái RadioButton
        holder.radioButton.setChecked(position == selectedPosition);

        // Xử lý sự kiện click
        View.OnClickListener clickListener = v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            if (oldPos != RecyclerView.NO_POSITION) notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onResourceSelected(resource);
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.radioButton.setOnClickListener(clickListener);
    }

    private void updateStatusUI(ResourceViewHolder holder, boolean isReady, String statusText) {
        // LUÔN set text trước để đảm bảo chữ hiện ra
        holder.tvStatus.setText(statusText != null ? statusText : "N/A");
        holder.tvStatus.setVisibility(View.VISIBLE);

        // Kiểm tra background có hợp lệ để đổi màu không
        if (holder.tvStatus.getBackground() instanceof GradientDrawable) {
            GradientDrawable background = (GradientDrawable) holder.tvStatus.getBackground();

            if (isReady) {
                int colorGreen = Color.parseColor("#1B5E20");
                holder.tvStatus.setTextColor(colorGreen);
                background.setColor(Color.parseColor("#E8F5E9"));
                holder.imgStatusIcon.setImageResource(R.drawable.ic_check_circle);
                holder.imgStatusIcon.setColorFilter(colorGreen);
            } else {
                int colorRed = Color.parseColor("#B71C1C");
                holder.tvStatus.setTextColor(colorRed);
                background.setColor(Color.parseColor("#FFEBEE"));
                holder.imgStatusIcon.setImageResource(R.drawable.ic_warning);
                holder.imgStatusIcon.setColorFilter(colorRed);
            }
        }
    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    public void updateList(List<Object> newList) {
        this.resourceList.clear();
        if (newList != null) {
            this.resourceList.addAll(newList);
        }
        this.selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public static class ResourceViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgAvatar, imgStatusIcon;
        final TextView tvName, tvDetails, tvRating, tvStatus;
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