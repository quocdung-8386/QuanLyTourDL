package com.example.quanlytourdl;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BangLuongAdapter extends RecyclerView.Adapter<BangLuongAdapter.BangLuongViewHolder> {

    private List<BangLuong> mListBangLuong;

    public BangLuongAdapter(List<BangLuong> mListBangLuong) {
        this.mListBangLuong = mListBangLuong;
    }

    @NonNull
    @Override
    public BangLuongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bang_luong, parent, false);
        return new BangLuongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BangLuongViewHolder holder, int position) {
        BangLuong bangLuong = mListBangLuong.get(position);
        if (bangLuong == null) {
            return;
        }

        holder.tvSalaryPeriod.setText(bangLuong.getSalaryPeriod());
        holder.tvLuongCoBan.setText(bangLuong.getLuongCoBan());
        holder.tvTongPhuCap.setText(bangLuong.getTongPhuCap());
        holder.tvThuongHoaHong.setText(bangLuong.getThuongHoaHong());
        holder.tvPhatKhauTru.setText(bangLuong.getPhatKhauTru());
        holder.tvTongThuNhap.setText(bangLuong.getTongThuNhap());

        if (bangLuong.isPaid()) {
            holder.tvStatus.setText("Đã thanh toán");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_paid);
            holder.tvStatus.setTextColor(Color.parseColor("#155724"));
        } else {
            holder.tvStatus.setText("Chưa thanh toán");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvStatus.setTextColor(Color.parseColor("#856404"));
        }
    }

    @Override
    public int getItemCount() {
        if (mListBangLuong != null) {
            return mListBangLuong.size();
        }
        return 0;
    }

    public class BangLuongViewHolder extends RecyclerView.ViewHolder {

        private TextView tvSalaryPeriod;
        private TextView tvStatus;
        private TextView tvLuongCoBan;
        private TextView tvTongPhuCap;
        private TextView tvThuongHoaHong;
        private TextView tvPhatKhauTru;
        private TextView tvTongThuNhap;

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