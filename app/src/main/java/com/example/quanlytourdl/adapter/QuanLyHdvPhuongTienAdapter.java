package com.example.quanlytourdl.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Vehicle;
import com.example.quanlytourdl.model.Guide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class QuanLyHdvPhuongTienAdapter extends RecyclerView.Adapter<QuanLyHdvPhuongTienAdapter.ItemViewHolder> {

    public interface OnItemActionListener {
        void onEditItem(Object item);
        void onDeleteItem(Object item);
        void onViewDetails(Object item);
    }

    private final List<Object> dataList = new ArrayList<>();
    private final boolean isGuideList;
    private final OnItemActionListener listener;

    public QuanLyHdvPhuongTienAdapter(boolean isGuideList, OnItemActionListener listener) {
        this.isGuideList = isGuideList;
        this.listener = listener;
    }

    public void updateData(List<?> newData) {
        dataList.clear();
        if (newData != null) {
            dataList.addAll(newData);
        }
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

        if (isGuideList && item instanceof Guide) {
            bindGuide(holder, (Guide) item);
        } else if (!isGuideList && item instanceof Vehicle) {
            bindVehicle(holder, (Vehicle) item);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetails(item);
        });

        holder.btnMoreOptions.setOnClickListener(v -> showPopupMenu(v, item));
    }

    private void bindGuide(ItemViewHolder holder, Guide guide) {
        holder.imgAvatar.setImageResource(R.drawable.ic_hdv_placeholder);
        holder.textName.setText(guide.getFullName());
        holder.textIdDetail.setText("Mã HDV: " + guide.getGuideCode());

        // HIỂN THỊ TRẠNG THÁI HDV
        if (guide.isApproved()) {
            holder.textStatusBadge.setText("Sẵn sàng");
            setStatusStyle(holder.textStatusBadge, android.R.color.white, R.drawable.bg_status_solved);
        } else {
            holder.textStatusBadge.setText("Tạm nghỉ");
            setStatusStyle(holder.textStatusBadge, android.R.color.white, R.drawable.bg_status_maintenance);
        }
    }

    private void bindVehicle(ItemViewHolder holder, Vehicle vehicle) {
        holder.imgAvatar.setImageResource(R.drawable.ic_phuongtien_placeholder);
        holder.textName.setText(vehicle.getBienSoXe());
        holder.textIdDetail.setText(vehicle.getHangXe() + " | " + vehicle.getSoChoNgoi() + " chỗ");

        String status = vehicle.getTinhTrangBaoDuong();

        // HIỂN THỊ TRẠNG THÁI PHƯƠNG TIỆN
        if ("Hoạt động tốt".equalsIgnoreCase(status)) {
            holder.textStatusBadge.setText("Sẵn sàng");
            setStatusStyle(holder.textStatusBadge, android.R.color.white, R.drawable.bg_status_solved);
        } else {
            String displayStatus = (status != null && !status.isEmpty()) ? status : "Tạm nghỉ";
            holder.textStatusBadge.setText(displayStatus);
            setStatusStyle(holder.textStatusBadge, android.R.color.white, R.drawable.bg_status_default);
        }
    }

    private void setStatusStyle(TextView badge, int textColorRes, int bgRes) {
        // 1. Đảm bảo View luôn hiển thị
        badge.setVisibility(View.VISIBLE);

        // 2. Ép màu chữ sang màu trắng (hoặc màu tương phản) để thấy được chữ trên nền màu
        badge.setTextColor(ContextCompat.getColor(badge.getContext(), textColorRes));

        // 3. Set nền badge
        badge.setBackgroundResource(bgRes);

        // 4. (Quan trọng) Ép lại nội dung chữ một lần nữa để tránh lỗi render
        badge.invalidate();
    }

    private void showPopupMenu(View anchorView, Object item) {
        PopupMenu popup = new PopupMenu(anchorView.getContext(), anchorView);
        popup.getMenu().add("Chỉnh sửa");
        popup.getMenu().add("Xóa");

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle().equals("Chỉnh sửa")) {
                listener.onEditItem(item);
            } else if (menuItem.getTitle().equals("Xóa")) {
                listener.onDeleteItem(item);
            }
            return true;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgAvatar;
        TextView textName, textIdDetail, textStatusBadge;
        ImageButton btnMoreOptions;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            textName = itemView.findViewById(R.id.text_name);
            textIdDetail = itemView.findViewById(R.id.text_id_detail);
            textStatusBadge = itemView.findViewById(R.id.text_status_badge);
            btnMoreOptions = itemView.findViewById(R.id.btn_more_options);
        }
    }
}