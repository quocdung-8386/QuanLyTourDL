package com.example.quanlytourdl.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.HoanTien;
import java.util.List;

public class HoanTienAdapter extends RecyclerView.Adapter<HoanTienAdapter.ViewHolder> {

    private List<HoanTien> list;
    // Interface để xử lý sự kiện click nút
    private OnActionClickListener listener;

    public interface OnActionClickListener {
        void onApprove(HoanTien item);
        void onReject(HoanTien item);
    }

    public HoanTienAdapter(List<HoanTien> list, OnActionClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hoan_tien, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HoanTien item = list.get(position);

        holder.tvTenKhach.setText(item.getTenKhach());
        holder.tvMaDon.setText("Mã đơn: " + item.getMaDon());
        holder.tvSoTien.setText(item.getSoTien());
        holder.tvTenTour.setText(item.getTenTour());
        holder.tvNgay.setText(item.getNgayYeuCau());

        // Xử lý Logic màu sắc và hiển thị nút theo trạng thái
        String status = item.getTrangThai(); 
        if (status == null) status = "";

        switch (status) {
            case "cho_xu_ly":
                holder.tvTrangThai.setText("Chờ xử lý");
                holder.tvTrangThai.setTextColor(Color.parseColor("#F57F17")); // Vàng
                holder.layoutButtons.setVisibility(View.VISIBLE); // Hiện nút
                break;
            case "da_hoan_tien":
                holder.tvTrangThai.setText("Đã hoàn tiền");
                holder.tvTrangThai.setTextColor(Color.parseColor("#388E3C")); // Xanh lá
                holder.layoutButtons.setVisibility(View.GONE); // Ẩn nút
                break;
            case "da_tu_choi":
                holder.tvTrangThai.setText("Đã từ chối");
                holder.tvTrangThai.setTextColor(Color.parseColor("#D32F2F")); // Đỏ
                holder.layoutButtons.setVisibility(View.GONE); // Ẩn nút
                break;
            default:
                holder.tvTrangThai.setText("Không xác định");
                holder.layoutButtons.setVisibility(View.GONE);
        }

        holder.btnPheDuyet.setOnClickListener(v -> listener.onApprove(item));
        holder.btnTuChoi.setOnClickListener(v -> listener.onReject(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenKhach, tvMaDon, tvSoTien, tvTenTour, tvNgay, tvTrangThai;
        LinearLayout layoutButtons;
        Button btnPheDuyet, btnTuChoi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenKhach = itemView.findViewById(R.id.tvTenKhach);
            tvMaDon = itemView.findViewById(R.id.tvMaDon);
            tvSoTien = itemView.findViewById(R.id.tvSoTien);
            tvTenTour = itemView.findViewById(R.id.tvTenTour);
            tvNgay = itemView.findViewById(R.id.tvNgay);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            layoutButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnPheDuyet = itemView.findViewById(R.id.btnPheDuyet);
            btnTuChoi = itemView.findViewById(R.id.btnTuChoi);
        }
    }
}