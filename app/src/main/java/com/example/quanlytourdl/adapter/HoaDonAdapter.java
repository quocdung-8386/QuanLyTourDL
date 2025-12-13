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

    // Interface để xử lý sự kiện click
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_hoa_don, parent, false);
        return new HoaDonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoaDonViewHolder holder, int position) {
        HoaDon hd = hoaDonList.get(position);

        holder.tvMaHoaDon.setText(hd.getMaHoaDon());
        holder.tvNgayTao.setText(hd.getNgayTao());
        holder.tvTenKhach.setText(hd.getTenKhachHang());
        holder.tvTongTien.setText(formatter.format(hd.getTongTien()) + "đ");

        // Xử lý màu sắc trạng thái
        switch (hd.getTrangThai()) {
            case 1: // Đã thanh toán
                holder.tvTrangThai.setText("Đã thanh toán");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_paid);
                holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_paid_text));
                holder.imgIcon.setImageResource(R.drawable.ic_receipt);
                break;
            case 2: // Chờ thanh toán
                holder.tvTrangThai.setText("Chờ thanh toán");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_pending);
                holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));
                holder.imgIcon.setImageResource(R.drawable.ic_more_horiz);
                break;
            case 3: // Quá hạn
                holder.tvTrangThai.setText("Quá hạn");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_overdue);
                holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_overdue_text));
                holder.imgIcon.setImageResource(R.drawable.ic_warning);
                break;
        }

        // Bắt sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(hd);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hoaDonList.size();
    }

    public static class HoaDonViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaHoaDon, tvNgayTao, tvTenKhach, tvTongTien, tvTrangThai;
        ImageView imgIcon;

        public HoaDonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaHoaDon = itemView.findViewById(R.id.tvMaHoaDon);
            tvNgayTao = itemView.findViewById(R.id.tvNgayTao);
            tvTenKhach = itemView.findViewById(R.id.tvTenKhach);
            tvTongTien = itemView.findViewById(R.id.tvTongTien);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}