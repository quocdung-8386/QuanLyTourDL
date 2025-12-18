package com.example.quanlytourdl;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- IMPORT TỪ PACKAGE CON ---
import com.example.quanlytourdl.adapter.DanhGiaAdapter;
import com.example.quanlytourdl.model.DanhGia;

import java.util.ArrayList;
import java.util.List;

public class PhanHoiFragment extends Fragment {

    private RecyclerView rvReviews;
    private DanhGiaAdapter adapter;
    private List<DanhGia> mListGoc;

    // UI Thống kê
    private TextView tvAvgRating, tvTotalReviews;
    private RatingBar rbAvg;
    private View row5, row4, row3, row2, row1;

    // Filter Buttons
    private Button btnAll, btnTour, btnHDV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phan_hoi, container, false);

        initViews(view);
        createDummyData(); // Tạo dữ liệu mẫu
        setupRecyclerView();
        setupStatistics(); // Tính toán sao
        setupFilters();    // Xử lý bộ lọc

        return view;
    }

    private void initViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        rvReviews = view.findViewById(R.id.rv_reviews);

        // Thống kê Views
        tvAvgRating = view.findViewById(R.id.tv_avg_rating);
        tvTotalReviews = view.findViewById(R.id.tv_total_reviews);
        rbAvg = view.findViewById(R.id.rb_avg);

        // Ánh xạ các dòng include
        row5 = view.findViewById(R.id.row_5);
        row4 = view.findViewById(R.id.row_4);
        row3 = view.findViewById(R.id.row_3);
        row2 = view.findViewById(R.id.row_2);
        row1 = view.findViewById(R.id.row_1);

        // Buttons
        btnAll = view.findViewById(R.id.btn_filter_all);
        btnTour = view.findViewById(R.id.btn_filter_tour);
        btnHDV = view.findViewById(R.id.btn_filter_hdv);

        // Nút Back
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // Nút Chuông thông báo
        RelativeLayout btnNoti = view.findViewById(R.id.btn_notification);
        View viewBadge = view.findViewById(R.id.view_badge);

        btnNoti.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Không có thông báo mới.", Toast.LENGTH_SHORT).show();
            viewBadge.setVisibility(View.GONE);
        });
    }

    private void createDummyData() {
        mListGoc = new ArrayList<>();
        // Tạo dữ liệu giả lập để hiển thị
        mListGoc.add(new DanhGia("1", "Nguyễn Thị Lan", "2 ngày trước", 5.0f, "Tuyệt vời, sẽ quay lại!", "Tour Đà Nẵng", "Nguyễn Văn A", 0));
        mListGoc.add(new DanhGia("2", "Trần Văn Bình", "5 ngày trước", 4.0f, "Hơi mệt nhưng vui.", "Tour Hạ Long", "Lê Thị Cẩm", 0));
        mListGoc.add(new DanhGia("3", "Phạm Minh Anh", "1 tuần trước", 3.0f, "Xe di chuyển hơi cũ.", "Tour Miền Tây", "Nguyễn Văn A", 0));
        mListGoc.add(new DanhGia("4", "Lê Tuấn", "2 tuần trước", 5.0f, "HDV siêu nhiệt tình!", "Tour Đà Lạt", "Trần X", 0));
        mListGoc.add(new DanhGia("5", "Hoàng Yến", "1 tháng trước", 1.0f, "Thất vọng về dịch vụ.", "Tour Sapa", "Lê Thị Cẩm", 0));
    }

    private void setupRecyclerView() {
        adapter = new DanhGiaAdapter(mListGoc);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(adapter);
    }

    private void setupStatistics() {
        if (mListGoc == null || mListGoc.isEmpty()) return;

        int total = mListGoc.size();
        tvTotalReviews.setText(total + " reviews");

        // Tính trung bình cộng và đếm số lượng từng sao
        float sum = 0;
        int[] counts = new int[6]; // index 1-5 dùng để đếm

        for (DanhGia d : mListGoc) {
            sum += d.getRating();
            int star = Math.round(d.getRating());
            if (star >= 1 && star <= 5) {
                counts[star]++;
            }
        }

        float avg = total > 0 ? sum / total : 0;
        tvAvgRating.setText(String.format("%.1f", avg));
        rbAvg.setRating(avg);

        // Cập nhật giao diện từng thanh progress
        updateRow(row5, 5, counts[5], total);
        updateRow(row4, 4, counts[4], total);
        updateRow(row3, 3, counts[3], total);
        updateRow(row2, 2, counts[2], total);
        updateRow(row1, 1, counts[1], total);
    }

    private void updateRow(View row, int star, int count, int total) {
        TextView tvLabel = row.findViewById(R.id.tv_star_label);
        ProgressBar pb = row.findViewById(R.id.progress_bar);
        TextView tvPercent = row.findViewById(R.id.tv_percent);

        tvLabel.setText(String.valueOf(star));

        int percent = total > 0 ? (count * 100 / total) : 0;
        pb.setProgress(percent);
        tvPercent.setText(percent + "%");
    }

    private void setupFilters() {
        btnAll.setOnClickListener(v -> {
            updateFilterUI(btnAll);
            adapter.setList(mListGoc); // Hiển thị tất cả
        });

        btnTour.setOnClickListener(v -> {
            updateFilterUI(btnTour);
            // Demo lọc
            Toast.makeText(getContext(), "Đang lọc theo Tour...", Toast.LENGTH_SHORT).show();
            // Logic lọc thật sẽ thêm sau
        });

        btnHDV.setOnClickListener(v -> {
            updateFilterUI(btnHDV);
            Toast.makeText(getContext(), "Đang lọc theo HDV...", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFilterUI(Button activeBtn) {
        // Màu sắc Active / Inactive
        int activeColor = Color.parseColor("#BBDEFB");
        int activeText = Color.parseColor("#1565C0");
        int inactiveColor = Color.parseColor("#EEEEEE");
        int inactiveText = Color.BLACK;

        // Reset tất cả về Inactive
        btnAll.setBackgroundColor(inactiveColor); btnAll.setTextColor(inactiveText);
        btnTour.setBackgroundColor(inactiveColor); btnTour.setTextColor(inactiveText);
        btnHDV.setBackgroundColor(inactiveColor); btnHDV.setTextColor(inactiveText);

        // Set nút được chọn thành Active
        activeBtn.setBackgroundColor(activeColor);
        activeBtn.setTextColor(activeText);
    }
}