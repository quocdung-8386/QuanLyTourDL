package com.example.quanlytourdl.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.model.KhachHang; // Import Model
import com.example.quanlytourdl.R;         // Import Tài nguyên (quan trọng)

import java.util.List;

public class KhachHangAdapter extends RecyclerView.Adapter<KhachHangAdapter.ViewHolder> {

    private List<KhachHang> listKhachHang;
    private OnItemClickListener listener; // Khai báo listener

    // 1. Tạo Interface để Fragment có thể implement hành động click
    public interface OnItemClickListener {
        void onItemClick(KhachHang khachHang);
    }

    // 2. Cập nhật Constructor để nhận thêm Listener
    public KhachHangAdapter(List<KhachHang> listKhachHang, OnItemClickListener listener) {
        this.listKhachHang = listKhachHang;
        this.listener = listener;
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

        // Gán dữ liệu lên View
        holder.tvTen.setText(kh.getTen());
        holder.tvSdt.setText(kh.getSdt());
        holder.imgAvatar.setImageResource(kh.getAvatarResId());

        // 3. Bắt sự kiện click vào item (dòng khách hàng)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(kh); // Gửi đối tượng khách hàng ra ngoài Fragment
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listKhachHang != null) {
            return listKhachHang.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvSdt;
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID từ file item_khach_hang.xml
            tvTen = itemView.findViewById(R.id.tvTenKhachHang);
            tvSdt = itemView.findViewById(R.id.tvSoDienThoai);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}