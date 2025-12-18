package com.example.quanlytourdl.adapter; // Package adapter

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R; // Import R từ package chính
import com.example.quanlytourdl.model.DanhGia; // Import Model

import java.util.List;

public class DanhGiaAdapter extends RecyclerView.Adapter<DanhGiaAdapter.ViewHolder> {

    private List<DanhGia> mList;

    public DanhGiaAdapter(List<DanhGia> list) {
        this.mList = list;
    }

    public void setList(List<DanhGia> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_danh_gia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DanhGia item = mList.get(position);
        if (item == null) return;

        holder.tvName.setText(item.getUserName());
        holder.tvDate.setText(item.getDate());
        holder.tvRating.setText(String.valueOf(item.getRating()));
        holder.tvComment.setText(item.getComment());
        holder.tvTour.setText(item.getTourName());
        holder.tvHDV.setText("HDV: " + item.getHdvName());

        // Logic set ảnh avatar (Mặc định hoặc theo resource ID)
        if (item.getAvatarResId() != 0) {
            holder.ivAvatar.setImageResource(item.getAvatarResId());
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_launcher_background); // Ảnh mặc định
        }
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvRating, tvComment, tvTour, tvHDV;
        ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_username);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvComment = itemView.findViewById(R.id.tv_comment);
            tvTour = itemView.findViewById(R.id.tv_tour_name);
            tvHDV = itemView.findViewById(R.id.tv_hdv_name);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}