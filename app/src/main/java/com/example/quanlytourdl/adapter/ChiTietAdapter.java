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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chi_tiet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietItem item = listItems.get(position);

        // --- SỬA LẠI DÒNG NÀY ---
        // Dùng getTenDichVu() thay vì getTenSanPham()
        holder.tvTenDichVu.setText(item.getTenDichVu());

        // Hiển thị mô tả (nếu có)
        if (item.getMoTa() != null && !item.getMoTa().isEmpty()) {
            holder.tvMoTa.setText(item.getMoTa());
            holder.tvMoTa.setVisibility(View.VISIBLE);
        } else {
            holder.tvMoTa.setVisibility(View.GONE);
        }

        // Hiển thị số lượng
        // Model của bạn trả về String ở hàm getSoLuong(), nên ta cộng chuỗi cho an toàn
        holder.tvSoLuong.setText("x" + item.getSoLuong());

        // Hiển thị giá tiền
        if (item.getGiaTien() > 0) {
            holder.tvGiaTien.setText(formatter.format(item.getGiaTien()) + "đ");
        } else {
            holder.tvGiaTien.setText("");
        }
    }

    @Override
    public int getItemCount() {
        if (listItems != null) {
            return listItems.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenDichVu, tvMoTa, tvSoLuong, tvGiaTien;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenDichVu = itemView.findViewById(R.id.tvItemTenDichVu);
            tvMoTa = itemView.findViewById(R.id.tvItemMoTa);
            tvSoLuong = itemView.findViewById(R.id.tvItemSoLuong);
            tvGiaTien = itemView.findViewById(R.id.tvItemGiaTien);
        }
    }
}