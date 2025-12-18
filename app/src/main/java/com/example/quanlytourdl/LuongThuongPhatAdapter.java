package com.example.quanlytourdl;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class LuongThuongPhatAdapter extends RecyclerView.Adapter<LuongThuongPhatAdapter.LuongThuongPhatViewHolder> {

    private List<LuongThuongPhat> mList;
    private Context mContext;
    private OnActionClickListener listener;

    // Interface để gửi sự kiện click về Fragment xử lý
    public interface OnActionClickListener {
        void onApprove(LuongThuongPhat item);
        void onReject(LuongThuongPhat item);
    }

    public LuongThuongPhatAdapter(Context context, List<LuongThuongPhat> list, OnActionClickListener listener) {
        this.mContext = context;
        this.mList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LuongThuongPhatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_luong_thuong_phat, parent, false);
        return new LuongThuongPhatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LuongThuongPhatViewHolder holder, int position) {
        LuongThuongPhat item = mList.get(position);
        if (item == null) return;

        DecimalFormat df = new DecimalFormat("#,### VNĐ");

        // 1. Gán dữ liệu
        holder.tvTitle.setText(item.getType() + " - " + item.getEmployeeName());
        holder.tvDepartment.setText("Phòng ban: " + item.getDepartment());
        holder.tvReason.setText("Lý do: " + item.getReason()); // Sử dụng getReason() khớp với Model
        holder.tvAmount.setText("Số tiền: " + df.format(item.getAmount()));
        holder.tvStatus.setText(item.getStatus());

        // 2. Xử lý màu sắc và ẩn/hiện nút dựa trên trạng thái
        String status = item.getStatus();

        if ("Đã duyệt".equals(status) || "Đã phê duyệt".equals(status)) {
            // Màu Xanh lá
            holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
            holder.layoutActions.setVisibility(View.GONE); // Ẩn nút

        } else if ("Đã từ chối".equals(status)) {
            // Màu Đỏ
            holder.tvStatus.setBackgroundColor(Color.parseColor("#F44336"));
            holder.layoutActions.setVisibility(View.GONE); // Ẩn nút

        } else {
            // Chờ duyệt -> Màu Vàng
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFC107"));
            holder.layoutActions.setVisibility(View.VISIBLE); // Hiện nút
        }

        // 3. Bắt sự kiện click
        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(item);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(item);
        });
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public static class LuongThuongPhatViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus, tvDepartment, tvReason, tvAmount, tvReport;
        LinearLayout layoutActions; // Layout chứa 2 nút
        Button btnReject, btnApprove;

        public LuongThuongPhatViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID chuẩn theo file item_luong_thuong_phat.xml mới nhất
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDepartment = itemView.findViewById(R.id.tv_department);
            tvReason = itemView.findViewById(R.id.tv_reason);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvReport = itemView.findViewById(R.id.tv_report);

            layoutActions = itemView.findViewById(R.id.layout_actions);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnApprove = itemView.findViewById(R.id.btn_approve);
        }
    }
}