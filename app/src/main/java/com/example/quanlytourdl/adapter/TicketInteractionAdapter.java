package com.example.quanlytourdl.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.TicketInteraction;
import java.util.List;

public class TicketInteractionAdapter extends RecyclerView.Adapter<TicketInteractionAdapter.ViewHolder> {

    private List<TicketInteraction> list;

    public TicketInteractionAdapter(List<TicketInteraction> list) {
        this.list = list;
    }

    public void addMessage(TicketInteraction message) {
        list.add(message);
        notifyItemInserted(list.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket_interaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TicketInteraction item = list.get(position);
        holder.tvName.setText(item.getName());
        holder.tvTime.setText(item.getTime());
        holder.tvContent.setText(item.getContent());

        // Logic đổi icon nếu là Hệ thống hoặc Người
        if (item.isSystem()) {
            holder.ivAvatar.setImageResource(R.drawable.ic_settings); // Cần icon bánh răng
            holder.ivAvatar.setBackgroundResource(R.drawable.bg_circle_blue_light); // Nền xanh nhạt
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person);
            holder.ivAvatar.setBackgroundResource(R.drawable.bg_circle_gray);
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvContent;
        ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}