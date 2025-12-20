package com.example.quanlytourdl;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class ChiTietKhieuNaiFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_khieu_nai, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_detail);
        TextView tvId = view.findViewById(R.id.tv_detail_id);
        TextView tvStatus = view.findViewById(R.id.tv_detail_status);
        TextView tvPriority = view.findViewById(R.id.tv_detail_priority);
        TextView tvCustomer = view.findViewById(R.id.tv_detail_customer);
        TextView tvTour = view.findViewById(R.id.tv_detail_tour);
        TextView tvDate = view.findViewById(R.id.tv_detail_date);
        TextView tvContent = view.findViewById(R.id.tv_detail_content);
        ImageView ivEvidence = view.findViewById(R.id.iv_detail_evidence);
        CardView cardEvidence = view.findViewById(R.id.card_evidence);

        // 1. Nút Back
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // 2. Nhận dữ liệu
        Bundle args = getArguments();
        if (args != null) {
            String id = args.getString("ID");
            String status = args.getString("STATUS");
            String priority = args.getString("PRIORITY");
            String customer = args.getString("CUSTOMER");
            String tour = args.getString("TOUR");
            String date = args.getString("DATE");
            String content = args.getString("CONTENT");
            String imgUri = args.getString("IMG_URI");

            tvId.setText(id);
            tvStatus.setText(status);
            tvPriority.setText("Ưu tiên: " + priority);
            tvCustomer.setText(customer);
            tvTour.setText(tour);
            tvDate.setText(date);
            tvContent.setText(content);

            // Xử lý màu sắc trạng thái (Tương tự Adapter)
            if ("Đã giải quyết".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_status_green);
                tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else if ("Hủy".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_status_red);
                tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
            } else {
                tvStatus.setBackgroundResource(R.drawable.bg_status_orange);
                tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"));
            }

            // Xử lý ảnh
            if (imgUri != null && !imgUri.isEmpty()) {
                try {
                    ivEvidence.setImageURI(Uri.parse(imgUri));
                } catch (Exception e) {
                    ivEvidence.setImageResource(R.drawable.ic_upload_cloud); // Ảnh lỗi hoặc không load được
                }
            } else {
                cardEvidence.setVisibility(View.GONE); // Ẩn card nếu không có ảnh
            }
        }
    }
}