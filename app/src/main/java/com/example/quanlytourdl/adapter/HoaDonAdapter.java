package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.HoaDon;
import java.text.DecimalFormat;
import java.util.List;

public class HoaDonAdapter extends RecyclerView.Adapter<HoaDonAdapter.HoaDonViewHolder> {

    private Context context;
    private List<HoaDon> hoaDonList;
    private DecimalFormat formatter = new DecimalFormat("#,###");
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HoaDon hoaDon);
    }

    public HoaDonAdapter(Context context, List<HoaDon> hoaDonList, OnItemClickListener listener) {
        this.context = context;
        this.hoaDonList = hoaDonList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HoaDonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layout đã được cập nhật là item_hoa_don.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_hoa_don, parent, false);
        return new HoaDonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoaDonViewHolder holder, int position) {
        HoaDon hd = hoaDonList.get(position);

        // Gán dữ liệu cơ bản
        holder.tvMaHoaDon.setText("#" + (hd.getMaHoaDon().length() > 8 ? hd.getMaHoaDon().substring(0, 8).toUpperCase() : hd.getMaHoaDon()));
        holder.tvNgayTao.setText(hd.getNgayTao());
        holder.tvTenKhach.setText(hd.getTenKhachHang());
        holder.tvTongTien.setText(formatter.format(hd.getTongTien()) + "đ");

        // Xử lý logic màu sắc trạng thái (Background & Text Color)
        switch (hd.getTrangThai()) {
            case 1: // Đã thanh toán
                holder.tvTrangThai.setText("Thành công");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_paid);
                holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_paid_text));
                break;
            case 2: // Chờ thanh toán
                holder.tvTrangThai.setText("Đang chờ");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_pending);
                holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));
                break;
            case 3: // Quá hạn/Hủy
                holder.tvTrangThai.setText("Đã hủy");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_overdue);
                holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_overdue_text));
                break;
            default:
                holder.tvTrangThai.setText("Khác");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_pending);
                holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));
        }

        // Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(hd);
        });
    }

    @Override
    public int getItemCount() {
        return hoaDonList.size();
    }

    public static class HoaDonViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaHoaDon, tvNgayTao, tvTrangThai, tvTenKhach, tvTongTien;
        ImageView imgIcon;

        public HoaDonViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ đúng các ID trong file XML mới
            tvMaHoaDon = itemView.findViewById(R.id.tvMaHoaDon);
            tvNgayTao = itemView.findViewById(R.id.tvNgayTao);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvTenKhach = itemView.findViewById(R.id.tvTenKhach);
            tvTongTien = itemView.findViewById(R.id.tvTongTien);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}