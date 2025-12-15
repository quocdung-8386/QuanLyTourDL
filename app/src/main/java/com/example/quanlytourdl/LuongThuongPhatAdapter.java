package com.example.quanlytourdl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LuongThuongPhatAdapter extends RecyclerView.Adapter<LuongThuongPhatAdapter.LuongThuongPhatViewHolder> {

    private List<LuongThuongPhat> mList;
    private Context mContext;

    public LuongThuongPhatAdapter(Context context, List<LuongThuongPhat> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public LuongThuongPhatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_luong_thuong_phat, parent, false);
        return new LuongThuongPhatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LuongThuongPhatViewHolder holder, int position) {
        LuongThuongPhat item = mList.get(position);

        holder.tvTitle.setText(item.getType() + " - " + item.getEmployeeName());
        holder.tvDescription.setText(item.getDescription());
        holder.tvStatus.setText(item.getStatus());

        switch (item.getStatus()) {
            case "Chờ phê duyệt":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.color_pending));
                break;
            case "Đã phê duyệt":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_approve_button);
                holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.color_approved));
                break;
            case "Đã từ chối":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_reject_button);
                holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.color_rejected));
                break;
        }

        if (item.isShowButtons()) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class LuongThuongPhatViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvStatus;
        TextView tvDescription;
        TextView tvReport;
        Button btnReject;
        Button btnApprove;

        public LuongThuongPhatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvReport = itemView.findViewById(R.id.tv_report);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnApprove = itemView.findViewById(R.id.btn_approve);
        }
    }
}