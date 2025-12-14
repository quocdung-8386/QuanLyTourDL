package com.example.quanlytourdl.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.TimelineEvent;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private final Context context;
    // ⭐ Thay đổi thành private List<TimelineEvent> để cho phép cập nhật
    private List<TimelineEvent> eventList;
    private final int primaryColor;

    public TimelineAdapter(Context context, List<TimelineEvent> eventList) {
        this.context = context;
        this.eventList = eventList;
        this.primaryColor = ContextCompat.getColor(context, R.color.colorPrimary);
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_event, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        TimelineEvent event = eventList.get(position);

        // 1. Cập nhật nội dung sự kiện
        // Xử lý Time/Title Style
        if (event.getTime() == null || event.getTime().isEmpty()) {
            holder.textEventTime.setVisibility(View.GONE);
            // Giả định R.style.App_TextAppearance_TimelineSubhead là style cho tiêu đề lớn (Day Header)
            holder.textEventTitle.setTextAppearance(context, R.style.App_TextAppearance_TimelineSubhead);
        } else {
            holder.textEventTime.setVisibility(View.VISIBLE);
            holder.textEventTime.setText(event.getTime());
            // Giả định R.style.App_TextAppearance_TimelineBody1 là style cho nội dung sự kiện
            holder.textEventTitle.setTextAppearance(context, R.style.App_TextAppearance_TimelineBody1);
        }

        holder.textEventTitle.setText(event.getTitle());
        holder.textEventDescription.setText(event.getDescription());

        // Ẩn mô tả nếu rỗng
        holder.textEventDescription.setVisibility((event.getDescription() == null || event.getDescription().isEmpty()) ? View.GONE : View.VISIBLE);

        // 2. Xử lý logic Timeline (Đường kẻ và Icon)
        holder.timelineLineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.timelineLineBottom.setVisibility(position == getItemCount() - 1 ? View.INVISIBLE : View.VISIBLE);

        int iconResId = getIconForEventType(event.getIconType());
        holder.timelineDot.setImageResource(iconResId);
        // ⭐ Đảm bảo sử dụng ContextCompat.getColor() nếu ColorFilter yêu cầu int
        holder.timelineDot.setColorFilter(primaryColor);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ⭐ Phương thức để cập nhật dữ liệu danh sách TimelineEvents.
     * Đây là phương thức mà TourItineraryFragment sẽ gọi sau khi parse JSON thành công.
     */
    public void updateList(List<TimelineEvent> newList) {
        // Kiểm tra null để đảm bảo an toàn
        if (newList != null) {
            this.eventList = newList;
            notifyDataSetChanged(); // Yêu cầu RecyclerView vẽ lại toàn bộ danh sách
        }
    }

    private int getIconForEventType(String iconType) {
        if (iconType == null) return R.drawable.ic_timeline_default;

        switch (iconType.toLowerCase()) {
            case "transfer":
                return R.drawable.ic_timeline_transfer;
            case "hotel":
                return R.drawable.ic_timeline_hotel;
            case "food":
                return R.drawable.ic_timeline_food;
            case "attraction":
                return R.drawable.ic_timeline_attraction;
            case "day_header": // Icon đặc biệt cho Tiêu đề Ngày
                return R.drawable.ic_timeline_day_header;
            default:
                return R.drawable.ic_timeline_default;
        }
    }

    public static class TimelineViewHolder extends RecyclerView.ViewHolder {
        final View timelineLineTop;
        final ImageView timelineDot;
        final View timelineLineBottom;
        final TextView textEventTime;
        final TextView textEventTitle;
        final TextView textEventDescription;
        final LinearLayout eventMediaContainer;
        final TextView textExpandDetails;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            timelineLineTop = itemView.findViewById(R.id.timeline_line_top);
            timelineDot = itemView.findViewById(R.id.timeline_dot);
            timelineLineBottom = itemView.findViewById(R.id.timeline_line_bottom);
            textEventTime = itemView.findViewById(R.id.text_event_time);
            textEventTitle = itemView.findViewById(R.id.text_event_title);
            textEventDescription = itemView.findViewById(R.id.text_event_description);
            eventMediaContainer = itemView.findViewById(R.id.event_media_container);
            textExpandDetails = itemView.findViewById(R.id.text_expand_details);

            // Xử lý click (Ví dụ: mở rộng chi tiết hoặc hiển thị Toast)
            itemView.setOnClickListener(v -> {
                // Lấy vị trí click
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // TODO: Mở Dialog/Fragment chi tiết cho sự kiện này
                    Toast.makeText(itemView.getContext(), "Chi tiết sự kiện: " + textEventTitle.getText(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}