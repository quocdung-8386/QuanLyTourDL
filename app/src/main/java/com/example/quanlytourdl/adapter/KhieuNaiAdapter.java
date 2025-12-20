package com.example.quanlytourdl.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.KhieuNai;

import java.util.List;

public class KhieuNaiAdapter extends RecyclerView.Adapter<KhieuNaiAdapter.ViewHolder> {

    private List<KhieuNai> mList;
    private OnItemClickListener mListener; // [MỚI] Khai báo listener

    // [MỚI] Interface để bắn sự kiện click ra ngoài
    public interface OnItemClickListener {
        void onItemClick(KhieuNai item);
    }

    // [MỚI] Cập nhật Constructor nhận thêm listener
    public KhieuNaiAdapter(List<KhieuNai> list, OnItemClickListener listener) {
        this.mList = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_support_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        KhieuNai item = mList.get(position);
        if (item == null) return;

        holder.tvId.setText(item.getId() + " - " + item.getPriority());
        holder.tvInfo.setText("KH: " + item.getCustomerName());
        holder.tvTime.setText("Ngày: " + item.getDateIncident());
        holder.tvStatus.setText(item.getStatus());

        // Màu sắc trạng thái
        String status = item.getStatus();
        if ("Đã giải quyết".equals(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green);
        } else if ("Hủy".equals(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_red);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_orange);
        }

        // [MỚI] Bắt sự kiện click vào toàn bộ item
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvInfo, tvTime, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_ticket_title);
            tvInfo = itemView.findViewById(R.id.tv_ticket_customer);
            tvTime = itemView.findViewById(R.id.tv_ticket_time);
            tvStatus = itemView.findViewById(R.id.tv_ticket_status);
        }
    }
}