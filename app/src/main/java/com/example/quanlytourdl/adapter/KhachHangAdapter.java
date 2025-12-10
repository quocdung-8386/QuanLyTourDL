package com.example.quanlytourdl.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.model.KhachHang;
import com.example.quanlytourdl.R;

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

        holder.tvTen.setText(kh.getTen());
        holder.tvSdt.setText(kh.getSdt());
        // Set ảnh mặc định (hoặc logic load ảnh nếu có)
        holder.imgAvatar.setImageResource(R.drawable.ic_launcher_background);

        // Click vào item -> Xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(kh);
            }
        });

        // Click nút Xóa
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
        TextView btnDelete;
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Ánh xạ View
            tvTen = itemView.findViewById(R.id.tvTenKhachHang);
            tvSdt = itemView.findViewById(R.id.tvSoDienThoai);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);

            // Ánh xạ nút Xóa (Bây giờ là TextView)
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}