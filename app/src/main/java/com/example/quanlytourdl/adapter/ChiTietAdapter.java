package com.example.quanlytourdl.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.ChiTietItem;
import java.text.DecimalFormat;
import java.util.List;

public class ChiTietAdapter extends RecyclerView.Adapter<ChiTietAdapter.ViewHolder> {

    private List<ChiTietItem> listItems;
    private DecimalFormat formatter = new DecimalFormat("#,###");

    public ChiTietAdapter(List<ChiTietItem> listItems) {
        this.listItems = listItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo bạn đã tạo file layout item_chi_tiet.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chi_tiet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietItem item = listItems.get(position);
        holder.tvTenDichVu.setText(item.getTenDichVu());
        holder.tvMoTa.setText(item.getMoTa());
        holder.tvSoLuong.setText(item.getSoLuong());
        holder.tvGiaTien.setText(formatter.format(item.getGiaTien()) + "đ");
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenDichVu, tvMoTa, tvSoLuong, tvGiaTien;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID theo file item_chi_tiet.xml của bạn
            tvTenDichVu = itemView.findViewById(R.id.tvItemTenDichVu);
            tvMoTa = itemView.findViewById(R.id.tvItemMoTa);
            tvSoLuong = itemView.findViewById(R.id.tvItemSoLuong);
            tvGiaTien = itemView.findViewById(R.id.tvItemGiaTien);
        }
    }
}