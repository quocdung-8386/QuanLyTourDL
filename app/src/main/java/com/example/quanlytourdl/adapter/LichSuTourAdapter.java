package com.example.quanlytourdl.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.DonDatTour;

import java.util.List;

public class LichSuTourAdapter extends RecyclerView.Adapter<LichSuTourAdapter.ViewHolder> {

    private List<DonDatTour> listDonDat;
    private OnItemClickListener listener;
    public LichSuTourAdapter(List<DonDatTour> listDonDat) {
        this.listDonDat = listDonDat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo bạn đã có file item_tour_history.xml như hướng dẫn trước
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_history, parent, false);
        return new ViewHolder(view);
    }
    public interface OnItemClickListener {
        void onItemClick(DonDatTour donDatTour);
    }
    public LichSuTourAdapter(List<DonDatTour> listDonDat, OnItemClickListener listener) {
        this.listDonDat = listDonDat;
        this.listener = listener; // Gán listener
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonDatTour item = listDonDat.get(position);

        holder.tvTourName.setText(item.getTenTourSnapshot());
        holder.tvDateRange.setText(item.getThoiGianKhoiHanh());
        holder.tvTourCode.setText("Mã tour: " + item.getMaTourCode());
        holder.tvGuestCount.setText("Số khách: " + item.getSoLuongKhach());
        holder.tvStatus.setText(item.getTrangThai());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));

        // Logic đổi màu trạng thái
        String status = item.getTrangThai();
        if (status != null) {
            switch (status) {
                case "Hoàn thành":
                    holder.cardStatus.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Xanh lá nhạt
                    holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                    break;
                case "Sắp diễn ra":
                    holder.cardStatus.setCardBackgroundColor(Color.parseColor("#E3F2FD")); // Xanh dương nhạt
                    holder.tvStatus.setTextColor(Color.parseColor("#1565C0"));
                    break;
                case "Đã hủy":
                    holder.cardStatus.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Đỏ nhạt
                    holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
                    break;
                default:
                    holder.cardStatus.setCardBackgroundColor(Color.LTGRAY);
                    holder.tvStatus.setTextColor(Color.BLACK);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return listDonDat != null ? listDonDat.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTourName, tvDateRange, tvTourCode, tvGuestCount, tvStatus;
        CardView cardStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID khớp với file item_tour_history.xml
            tvTourName = itemView.findViewById(R.id.tvTourName);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            tvTourCode = itemView.findViewById(R.id.tvTourCode);
            tvGuestCount = itemView.findViewById(R.id.tvGuestCount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cardStatus = itemView.findViewById(R.id.cardStatus);
        }
    }
}