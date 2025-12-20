package com.example.quanlytourdl.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Warning;
import java.util.List;

public class WarningAdapter extends RecyclerView.Adapter<WarningAdapter.ViewHolder> {

    private List<Warning> list;

    public WarningAdapter(List<Warning> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_warning, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Warning item = list.get(position);
        if (item == null) return;

        holder.tvTitle.setText(item.getTitle());
        holder.tvContent.setText(item.getContent());
        holder.tvTime.setText(item.getTimestamp());
        holder.tvTarget.setText("Gửi đến: " + item.getTargetType());

        // Xử lý hiển thị mức độ
        String level = item.getLevel();
        holder.tvLevel.setText(level);

        if (level.contains("KHẨN CẤP") || level.contains("🔴")) {
            holder.tvLevel.setBackgroundColor(android.graphics.Color.parseColor("#D32F2F")); // Đỏ
        } else if (level.contains("Cảnh báo") || level.contains("🟠")) {
            holder.tvLevel.setBackgroundColor(android.graphics.Color.parseColor("#F57C00")); // Cam
        } else {
            holder.tvLevel.setBackgroundColor(android.graphics.Color.parseColor("#1976D2")); // Xanh
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvLevel, tvTime, tvTarget;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_warn_title);
            tvContent = itemView.findViewById(R.id.tv_warn_content);
            tvLevel = itemView.findViewById(R.id.tv_warn_level);
            tvTime = itemView.findViewById(R.id.tv_warn_time);
            tvTarget = itemView.findViewById(R.id.tv_warn_target);
        }
    }
}