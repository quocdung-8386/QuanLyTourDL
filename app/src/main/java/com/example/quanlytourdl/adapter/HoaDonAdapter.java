package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.graphics.Color;
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

    public HoaDonAdapter(Context context, List<HoaDon> hoaDonList) {
        this.context = context;
        this.hoaDonList = hoaDonList;
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

        // Xử lý trạng thái giao diện
        switch (hd.getTrangThai()) {
            case 1: // Đã thanh toán (Xanh)
                holder.tvTrangThai.setText("Đã thanh toán");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_paid); // Bạn cần tạo file drawable này
                holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_paid_text));
                holder.imgIcon.setImageResource(R.drawable.ic_receipt); // Icon hóa đơn xanh
                holder.imgIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.icon_blue_bg));
                break;
            case 2: // Chờ thanh toán (Cam)
                holder.tvTrangThai.setText("Chờ thanh toán");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_pending); // Tạo file xml bg cam
                holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));
                holder.imgIcon.setImageResource(R.drawable.ic_more_horiz); // Icon 3 chấm
                // Set màu nền icon tương ứng...
                break;
            case 3: // Quá hạn (Đỏ)
                holder.tvTrangThai.setText("Quá hạn");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_overdue); // Tạo file xml bg đỏ
                holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_overdue_text));
                holder.imgIcon.setImageResource(R.drawable.ic_warning); // Icon cảnh báo
                break;
        }
    }

    @Override
    public int getItemCount() {
        return hoaDonList.size();
    }

    public class HoaDonViewHolder extends RecyclerView.ViewHolder {
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