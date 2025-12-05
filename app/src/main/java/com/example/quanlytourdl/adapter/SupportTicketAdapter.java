package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.SupportTicket;

import java.util.List;

public class SupportTicketAdapter extends RecyclerView.Adapter<SupportTicketAdapter.ViewHolder> {

    private final List<SupportTicket> ticketList;
    private final Context context;

    public SupportTicketAdapter(List<SupportTicket> ticketList) {
        this.ticketList = ticketList;
        this.context = null; // Hoặc bạn có thể truyền context từ Fragment
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_support_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SupportTicket ticket = ticketList.get(position);

        // Hiển thị dữ liệu
        holder.ticketIdTitle.setText("#" + ticket.getId() + " - " + ticket.getTitle());
        holder.customerInfo.setText("KHT: " + ticket.getCustomerInfo());
        holder.updatedTime.setText("Cập nhật " + ticket.getTime());
        holder.statusText.setText(ticket.getStatus());

        // Xử lý màu sắc Trạng thái (Quan trọng để giống hình ảnh)
        setStatusBackground(holder.statusText, ticket.getStatus());
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    // Hàm tùy chỉnh màu sắc trạng thái
    private void setStatusBackground(TextView statusText, String status) {
        Context context = statusText.getContext();

        // Đặt màu chữ mặc định là trắng
        statusText.setTextColor(Color.WHITE);

        // Dựa vào trạng thái để chọn background Drawable
        if (status.equalsIgnoreCase("Đã giải quyết")) {
            statusText.setBackgroundResource(R.drawable.bg_status_solved); // Xanh lá
        } else if (status.equalsIgnoreCase("Đang xử lý")) {
            statusText.setBackgroundResource(R.drawable.bg_status_processing); // Vàng cam
        } else {
            // Trạng thái khác (ví dụ: Mới)
            statusText.setBackgroundResource(R.drawable.bg_status_new); // Cần tạo bg_status_new.xml (màu xanh dương)
            statusText.setTextColor(ContextCompat.getColor(context, android.R.color.black)); // Đặt màu chữ đen nếu nền nhạt
        }
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ticketIdTitle;
        TextView customerInfo;
        TextView updatedTime;
        TextView statusText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketIdTitle = itemView.findViewById(R.id.text_ticket_id_title);
            customerInfo = itemView.findViewById(R.id.text_customer_info);
            updatedTime = itemView.findViewById(R.id.text_updated_time);
            statusText = itemView.findViewById(R.id.text_status);

            // Xử lý sự kiện nhấp vào item
            itemView.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "Xem chi tiết phiếu hỗ trợ", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
