package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.HopDong;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách HopDong trong RecyclerView.
 */
public class HopDongAdapter extends RecyclerView.Adapter<HopDongAdapter.HopDongViewHolder> {

    private final Context context;
    private final List<HopDong> hopDongList;
    private final OnItemActionListener listener;
    // Đã loại bỏ SimpleDateFormat vì dữ liệu ngày tháng được xử lý dưới dạng String.

    // Interface để xử lý các sự kiện click từ Fragment
    public interface OnItemActionListener {
        void onViewClick(HopDong hopDong);
        void onEditClick(HopDong hopDong);
        void onDeleteClick(HopDong hopDong);
    }

    public HopDongAdapter(Context context, List<HopDong> hopDongList, OnItemActionListener listener) {
        this.context = context;
        this.hopDongList = hopDongList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HopDongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_hopdong
        View view = LayoutInflater.from(context).inflate(R.layout.item_hopdong, parent, false);
        return new HopDongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HopDongViewHolder holder, int position) {
        HopDong hopDong = hopDongList.get(position);

        holder.tvTenNhaCungCap.setText(hopDong.getTenNhaCungCap());

        // Giả sử MaHD là MaHD: [Mã NCC viết tắt]-[Năm ký]-[Số thứ tự]
        holder.tvMaHopDong.setText("Mã HĐ: " + hopDong.getMaHopDong());

        // FIX: Giả định getNgayKy() và getNgayHetHan() trả về chuỗi String (dd/MM/yyyy).
        // Hiển thị trực tiếp chuỗi, tránh lỗi format Date/String.
        String ngayKy = hopDong.getNgayKy();
        String ngayHetHan = hopDong.getNgayHetHan();

        holder.tvNgayKy.setText("Ngày ký: " + (ngayKy != null ? ngayKy : "N/A"));
        holder.tvNgayHetHan.setText("Hết hạn: " + (ngayHetHan != null ? ngayHetHan : "N/A"));

        // Thiết lập Trạng thái và màu sắc
        // LƯU Ý: Trạng thái này đã được tính toán và CẬP NHẬT vào object 'hopDong'
        // trong Fragment (QuanLyHopDongFragment) ở lần sửa cuối.
        String trangThai = hopDong.getTrangThai();

        // Nếu trạng thái rỗng, gán mặc định (chỉ là dự phòng)
        if (trangThai == null || trangThai.isEmpty()) {
            trangThai = "Đang xử lý";
        }
        holder.tvTrangThai.setText(trangThai);

        int badgeColor = ContextCompat.getColor(context, R.color.color_default_status);
        int badgeTextColor = Color.WHITE;

        // Thiết lập màu sắc dựa trên trạng thái (cần đảm bảo R.color.xxx tồn tại)
        if ("Đang hiệu lực".equals(trangThai)) {
            badgeColor = ContextCompat.getColor(context, R.color.green_active);
        } else if ("Sắp hết hạn".equals(trangThai)) {
            badgeColor = ContextCompat.getColor(context, R.color.orange_warning);
        } else if ("Đã hết hạn".equals(trangThai) || "Đã chấm dứt".equals(trangThai)) {
            badgeColor = ContextCompat.getColor(context, R.color.red_expired);
        }

        holder.tvTrangThai.setBackgroundColor(badgeColor);
        holder.tvTrangThai.setTextColor(badgeTextColor);

        // Thiết lập sự kiện click
        holder.btnView.setOnClickListener(v -> listener.onViewClick(hopDong));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(hopDong));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(hopDong));
    }

    @Override
    public int getItemCount() {
        return hopDongList.size();
    }

    /**
     * ViewHolder chứa các view của mỗi item Hợp đồng.
     */
    public static class HopDongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenNhaCungCap, tvMaHopDong, tvNgayKy, tvNgayHetHan, tvTrangThai;
        ImageButton btnView, btnEdit, btnDelete;

        public HopDongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenNhaCungCap = itemView.findViewById(R.id.tv_supplier_name);
            tvMaHopDong = itemView.findViewById(R.id.tv_contract_id);
            tvNgayKy = itemView.findViewById(R.id.tv_contract_start_date);
            tvNgayHetHan = itemView.findViewById(R.id.tv_contract_end_date);
            tvTrangThai = itemView.findViewById(R.id.tv_status_badge);

            btnView = itemView.findViewById(R.id.btn_view_contract);
            btnEdit = itemView.findViewById(R.id.btn_edit_contract);
            btnDelete = itemView.findViewById(R.id.btn_delete_contract);
        }
    }
}