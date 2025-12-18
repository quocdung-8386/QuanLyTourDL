package com.example.quanlytourdl;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.List;

public class BangLuongAdapter extends RecyclerView.Adapter<BangLuongAdapter.BangLuongViewHolder> {

    private List<BangLuong> mListBangLuong;
    private OnSalaryActionListener listener;

    public interface OnSalaryActionListener {
        void onEdit(BangLuong bangLuong);
    }

    public BangLuongAdapter(List<BangLuong> mListBangLuong, OnSalaryActionListener listener) {
        this.mListBangLuong = mListBangLuong;
        this.listener = listener;
    }

    public void setList(List<BangLuong> list) {
        this.mListBangLuong = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BangLuongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bang_luong, parent, false);
        return new BangLuongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BangLuongViewHolder holder, int position) {
        BangLuong item = mListBangLuong.get(position);
        if (item == null) return;

        DecimalFormat df = new DecimalFormat("#,### VNĐ");

        holder.tvSalaryPeriod.setText("Bảng lương " + item.getSalaryPeriod());
        holder.tvLuongCoBan.setText(df.format(item.getLuongCoBan()));
        holder.tvTongPhuCap.setText(df.format(item.getTongPhuCap()));
        holder.tvThuongHoaHong.setText("+ " + df.format(item.getThuongHoaHong()));
        holder.tvPhatKhauTru.setText("- " + df.format(item.getPhatKhauTru()));
        holder.tvTongThuNhap.setText(df.format(item.getTongThuNhap()));

        if (item.isPaid()) {
            holder.tvStatus.setText("Đã thanh toán");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_paid);
            holder.tvStatus.setTextColor(Color.parseColor("#155724"));
        } else {
            holder.tvStatus.setText("Chưa thanh toán");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvStatus.setTextColor(Color.parseColor("#856404"));
        }

        // Bấm vào Item để Sửa
        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
    }

    @Override
    public int getItemCount() { return mListBangLuong != null ? mListBangLuong.size() : 0; }

    public class BangLuongViewHolder extends RecyclerView.ViewHolder {
        TextView tvSalaryPeriod, tvStatus, tvLuongCoBan, tvTongPhuCap, tvThuongHoaHong, tvPhatKhauTru, tvTongThuNhap;
        public BangLuongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSalaryPeriod = itemView.findViewById(R.id.tv_salary_period);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvLuongCoBan = itemView.findViewById(R.id.tv_luong_co_ban);
            tvTongPhuCap = itemView.findViewById(R.id.tv_tong_phu_cap);
            tvThuongHoaHong = itemView.findViewById(R.id.tv_thuong_hoa_hong);
            tvPhatKhauTru = itemView.findViewById(R.id.tv_phat_khau_tru);
            tvTongThuNhap = itemView.findViewById(R.id.tv_tong_thu_nhap);
        }
    }
}