package com.example.quanlytourdl.adapter; // Đổi package về thư mục adapter

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

// Import các class từ package chính
import com.example.quanlytourdl.NhanVien;
import com.example.quanlytourdl.R;

import java.util.List;

public class PhanQuyenTruyCapAdapter extends RecyclerView.Adapter<PhanQuyenTruyCapAdapter.PhanQuyenViewHolder> {

    private List<NhanVien> mListNhanVien;

    public PhanQuyenTruyCapAdapter(List<NhanVien> mListNhanVien) {
        this.mListNhanVien = mListNhanVien;
    }

    public void setData(List<NhanVien> list) {
        this.mListNhanVien = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhanQuyenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_phan_quyen_truy_cap, parent, false);
        return new PhanQuyenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhanQuyenViewHolder holder, int position) {
        NhanVien nv = mListNhanVien.get(position);
        if (nv == null) return;

        holder.tvName.setText(nv.getFullName());
        holder.tvRole.setText("Vai trò: " + nv.getRole());

        // Reset listener
        holder.swTour.setOnCheckedChangeListener(null);
        holder.swCustomer.setOnCheckedChangeListener(null);
        holder.swReport.setOnCheckedChangeListener(null);

        // Hiển thị trạng thái
        holder.swTour.setChecked(nv.isAccessTour());
        holder.swCustomer.setChecked(nv.isAccessCustomer());
        holder.swReport.setChecked(nv.isAccessReport());

        // Sự kiện click
        holder.swTour.setOnCheckedChangeListener((v, isChecked) -> nv.setAccessTour(isChecked));
        holder.swCustomer.setOnCheckedChangeListener((v, isChecked) -> nv.setAccessCustomer(isChecked));
        holder.swReport.setOnCheckedChangeListener((v, isChecked) -> nv.setAccessReport(isChecked));
    }

    @Override
    public int getItemCount() {
        return mListNhanVien != null ? mListNhanVien.size() : 0;
    }

    public static class PhanQuyenViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole;
        SwitchCompat swTour, swCustomer, swReport;

        public PhanQuyenViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRole = itemView.findViewById(R.id.tv_role);
            swTour = itemView.findViewById(R.id.sw_tour);
            swCustomer = itemView.findViewById(R.id.sw_customer);
            swReport = itemView.findViewById(R.id.sw_report);
        }
    }
}