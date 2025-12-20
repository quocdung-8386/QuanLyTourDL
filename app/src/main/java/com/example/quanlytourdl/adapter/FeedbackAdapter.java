package com.example.quanlytourdl.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Feedback;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    private List<Feedback> mList;

    public FeedbackAdapter(List<Feedback> list) {
        this.mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Liên kết với file layout item_feedback.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Feedback item = mList.get(position);
        if (item == null) return;

        holder.tvTour.setText(item.getTourName());
        holder.tvContent.setText("\"" + item.getContent() + "\"");
        holder.tvDate.setText(item.getDate());
        holder.tvSentiment.setText(item.getSentiment());

        // Hiển thị số sao
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < item.getRating(); i++) {
            stars.append("⭐");
        }
        holder.tvRating.setText(stars.toString());

        // Xử lý màu sắc nền cho nhãn Cảm xúc
        setupSentimentColor(holder.tvSentiment, item.getSentiment());
    }

    private void setupSentimentColor(TextView textView, String sentiment) {
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(16); // Bo góc tròn

        if ("Tích cực".equals(sentiment)) {
            background.setColor(Color.parseColor("#E8F5E9")); // Nền xanh nhạt
            textView.setTextColor(Color.parseColor("#4CAF50")); // Chữ xanh
        } else if ("Tiêu cực".equals(sentiment)) {
            background.setColor(Color.parseColor("#FFEBEE")); // Nền đỏ nhạt
            textView.setTextColor(Color.parseColor("#F44336")); // Chữ đỏ
        } else {
            // Trung lập
            background.setColor(Color.parseColor("#FFF8E1")); // Nền vàng nhạt
            textView.setTextColor(Color.parseColor("#FFA000")); // Chữ vàng
        }
        textView.setBackground(background);
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTour, tvContent, tvSentiment, tvDate, tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ đúng ID từ file item_feedback.xml
            tvTour = itemView.findViewById(R.id.tv_fb_tour);
            tvContent = itemView.findViewById(R.id.tv_fb_content);
            tvSentiment = itemView.findViewById(R.id.tv_fb_sentiment);
            tvDate = itemView.findViewById(R.id.tv_fb_date);
            tvRating = itemView.findViewById(R.id.tv_fb_rating);
        }
    }
}