package com.example.quanlytourdl.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.KhachHang;

import java.util.List;

public class KhachHangAdapter extends RecyclerView.Adapter<KhachHangAdapter.ViewHolder> {

    private List<KhachHang> listKhachHang;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(KhachHang khachHang);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(KhachHang khachHang, int position);
    }

    public KhachHangAdapter(List<KhachHang> listKhachHang, OnItemClickListener listener, OnDeleteClickListener deleteListener) {
        this.listKhachHang = listKhachHang;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_khach_hang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        KhachHang kh = listKhachHang.get(position);

        // Hiển thị Tên và SĐT
        holder.tvTen.setText(kh.getTen() != null ? kh.getTen() : "Không tên");
        holder.tvSdt.setText(kh.getSdt() != null ? kh.getSdt() : "---");

        // --- XỬ LÝ ẢNH ĐẠI DIỆN THEO GIỚI TÍNH ---
        String gender = kh.getGioiTinh();

        if (gender != null) {
            // So sánh không phân biệt hoa thường (Nam/nam/NAM đều được)
            if (gender.trim().equalsIgnoreCase("Nam")) {
                holder.imgAvatar.setImageResource(R.drawable.ic_avatar_male); // Icon Nam
            } else if (gender.trim().equalsIgnoreCase("Nữ")) {
                holder.imgAvatar.setImageResource(R.drawable.ic_avatar_female); // Icon Nữ
            } else {
                // Giới tính khác hoặc nhập sai -> Hiện icon mặc định
                holder.imgAvatar.setImageResource(R.drawable.ic_avatar_default);
            }
        } else {
            // Chưa có dữ liệu giới tính -> Hiện icon mặc định
            holder.imgAvatar.setImageResource(R.drawable.ic_avatar_default);
        }
        // -------------------------------------------

        // Sự kiện Click xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(kh);
            }
        });

        // Sự kiện Click nút Xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    deleteListener.onDeleteClick(kh, currentPos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (listKhachHang != null) ? listKhachHang.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvSdt;
        TextView btnDelete; // Đã đổi thành TextView cho khớp với XML của bạn
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTen = itemView.findViewById(R.id.tvTenKhachHang);
            tvSdt = itemView.findViewById(R.id.tvSoDienThoai);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}