package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.quanlytourdl.model.Tour;
import com.example.quanlytourdl.model.TourDaySchedule;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaoTourLichTrinhFragment extends Fragment implements TaoTourDetailFullFragment.TourStepDataCollector {

    private static final String TAG = "TaoTourLichTrinhFragment";

    // ⭐ Danh sách lưu trữ DỮ LIỆU của các ngày
    private final List<TourDaySchedule> dayScheduleList = new ArrayList<>();

    private final Tour tour;

    // THÀNH PHẦN UI
    private LinearLayout llItineraryDaysContainer; // Container để thêm các ngày động
    private MaterialButton btnAddNewDay;
    private TextView tvEmptyState; // ⭐ TextView để hiển thị khi chưa có ngày nào

    public TaoTourLichTrinhFragment(Tour tour) {
        this.tour = tour;
    }

    public TaoTourLichTrinhFragment() {
        this(new Tour());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lich_trinh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ÁNH XẠ VIEWS
        llItineraryDaysContainer = view.findViewById(R.id.ll_itinerary_days_container);
        btnAddNewDay = view.findViewById(R.id.btn_add_new_day);

        // ⭐ TẠO VÀ THÊM TEXTVIEW EMPTY STATE VÀO CONTAINER
        tvEmptyState = createEmptyStateTextView(view.getContext());
        llItineraryDaysContainer.addView(tvEmptyState);

        // Thiết lập Listener cho btnAddNewDay
        btnAddNewDay.setOnClickListener(v -> {
            addNewDayItem(dayScheduleList.size() + 1);
        });

        // Tải lịch trình hiện có (hoặc thêm Ngày 1 mặc định)
        loadExistingItinerary();
        updateDisplay();
    }

    /**
     * Tạo TextView hiển thị thông báo trạng thái trống (Empty State)
     */
    private TextView createEmptyStateTextView(android.content.Context context) {
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        tv.setLayoutParams(params);
        tv.setText("Danh sách ngày tour sẽ hiển thị ở đây");
        tv.setTextColor(0xFFAAAAAA); // Màu xám nhạt
        tv.setPadding(16, 16, 16, 16);
        return tv;
    }


    /**
     * Tải lịch trình hiện có (hoặc thêm ngày mặc định cho tour mới).
     */
    private void loadExistingItinerary() {
        // Tải dữ liệu mẫu hoặc ngày mặc định (chỉ thêm 1 ngày nếu không có)
        int initialDays = tour.getSoNgay() > 0 ? tour.getSoNgay() : 1;

        // Khởi tạo schedule list (giả định tour không có dữ liệu schedule chi tiết)
        // Nếu tour có dữ liệu chi tiết, bạn cần loop qua dữ liệu đó
        for (int i = 1; i <= initialDays; i++) {
            TourDaySchedule schedule = new TourDaySchedule();
            schedule.dayNumber = i;
            dayScheduleList.add(schedule);

            // Tạo và hiển thị View
            createAndDisplayDayView(schedule);
        }
    }

    /**
     * Phương thức thêm một mục lịch trình ngày mới.
     */
    private void addNewDayItem(int dayNumber) {
        if (dayNumber > 10) {
            Toast.makeText(getContext(), "Số ngày tối đa là 10.", Toast.LENGTH_SHORT).show();
            return;
        }

        TourDaySchedule schedule = new TourDaySchedule();
        schedule.dayNumber = dayNumber;
        dayScheduleList.add(schedule);

        createAndDisplayDayView(schedule);

        Toast.makeText(getContext(), "Đã thêm Ngày " + dayNumber, Toast.LENGTH_SHORT).show();
        updateDisplay();
    }

    /**
     * Phương thức tạo và hiển thị View (CardView) cho một ngày.
     */
    private void createAndDisplayDayView(final TourDaySchedule schedule) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View dayView = inflater.inflate(R.layout.template_itinerary_day_card, llItineraryDaysContainer, false);

        // Ánh xạ các View con
        TextView tvDayTitleNumber = dayView.findViewById(R.id.tv_day_title_number);
        ImageButton btnDeleteDay = dayView.findViewById(R.id.btn_delete_day);
        TextInputEditText etDailySummary = dayView.findViewById(R.id.et_daily_summary);

        // 1. Cập nhật số ngày
        tvDayTitleNumber.setText(String.format("Ngày %d", schedule.dayNumber));

        // 2. Load dữ liệu nếu có
        etDailySummary.setText(schedule.summary);

        // 3. Thiết lập Listener XÓA NGÀY
        btnDeleteDay.setOnClickListener(v -> deleteDayItem(dayView, schedule));

        // Thêm View vào container chính
        // ⭐ CHÚ Ý: Thêm View ở vị trí cuối cùng (trước tvEmptyState)
        // Nếu tvEmptyState là View con cuối cùng, ta thêm ở vị trí llItineraryDaysContainer.getChildCount() - 1
        llItineraryDaysContainer.addView(dayView, llItineraryDaysContainer.getChildCount() - 1);
    }

    /**
     * Phương thức xóa một mục lịch trình ngày.
     */
    private void deleteDayItem(View dayView, TourDaySchedule schedule) {
        // 1. Xóa khỏi Container
        llItineraryDaysContainer.removeView(dayView);

        // 2. Xóa khỏi danh sách dữ liệu
        dayScheduleList.remove(schedule);

        // 3. Cập nhật lại số thứ tự
        reindexDayItems();

        Toast.makeText(getContext(), "Đã xóa Ngày " + schedule.dayNumber, Toast.LENGTH_SHORT).show();
        updateDisplay();
    }

    /**
     * Cập nhật lại số thứ tự (Ngày 1, Ngày 2,...) của các mục lịch trình còn lại
     */
    private void reindexDayItems() {
        int newDayCount = dayScheduleList.size();

        for (int i = 0; i < newDayCount; i++) {
            TourDaySchedule schedule = dayScheduleList.get(i);
            int newDayNumber = i + 1;

            // Cập nhật số ngày trong mô hình dữ liệu
            schedule.dayNumber = newDayNumber;

            // Lấy View tại vị trí tương ứng (Bỏ qua tvEmptyState nếu nó nằm ở cuối)
            View dayView = llItineraryDaysContainer.getChildAt(i);

            // ⭐ Đảm bảo View là MaterialCardView trước khi tìm TextView bên trong
            if (dayView instanceof MaterialCardView) {
                TextView tvDayTitleNumber = dayView.findViewById(R.id.tv_day_title_number);
                if (tvDayTitleNumber != null) {
                    tvDayTitleNumber.setText(String.format("Ngày %d", newDayNumber));
                }
            }
        }
    }


    /**
     * Cập nhật trạng thái hiển thị của Empty State TextView.
     */
    private void updateDisplay() {
        // Hiển thị/Ẩn Empty State
        if (tvEmptyState != null) {
            if (dayScheduleList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
            }
        }

        Log.d(TAG, "Lịch trình: Đang hiển thị " + dayScheduleList.size() + " ngày.");
    }

    /**
     * ⭐ Thực hiện Thu thập và Validation dữ liệu của Bước 2. (Đã sửa lỗi Null Safety)
     */
    @Override
    public boolean collectDataAndValidate(Tour tour) {

        if (dayScheduleList.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng thêm ít nhất một ngày lịch trình.", Toast.LENGTH_SHORT).show();
            return false;
        }

        StringBuilder detailedScheduleSummary = new StringBuilder();

        // Chỉ duyệt qua số lượng ngày thực tế trong danh sách dữ liệu
        for (int i = 0; i < dayScheduleList.size(); i++) {
            // Lấy View tại vị trí i. (tvEmptyState nằm ở vị trí cuối, sẽ không bị duyệt tới)
            View dayView = llItineraryDaysContainer.getChildAt(i);

            // Nếu View không phải là MaterialCardView, có lỗi nghiêm trọng về cấu trúc.
            if (!(dayView instanceof MaterialCardView)) {
                Log.e(TAG, "Lỗi nghiêm trọng: View tại index " + i + " không phải là lịch trình ngày.");
                Toast.makeText(getContext(), "Lỗi hệ thống: Cấu trúc lịch trình bị lỗi.", Toast.LENGTH_LONG).show();
                return false;
            }

            TourDaySchedule schedule = dayScheduleList.get(i);

            // LẤY DỮ LIỆU TỪ INPUT VÀ KIỂM TRA NULL (Đã giải quyết NullPointerException)
            TextInputEditText etDailySummary = dayView.findViewById(R.id.et_daily_summary);
            String summary;

            if (etDailySummary == null) {
                // Lỗi này chỉ xảy ra nếu ID trong XML bị đổi hoặc View bị inflate sai
                Log.e(TAG, "Không tìm thấy et_daily_summary trong View Ngày " + schedule.dayNumber);
                Toast.makeText(getContext(), String.format("Lỗi hệ thống: Không thể tìm thấy trường tóm tắt cho Ngày %d.", schedule.dayNumber), Toast.LENGTH_LONG).show();
                return false;
            } else {
                summary = Objects.requireNonNull(etDailySummary.getText()).toString().trim();
            }

            // Validation nội dung
            if (summary.isEmpty()) {
                Toast.makeText(getContext(), String.format("Vui lòng nhập tóm tắt cho Ngày %d.", schedule.dayNumber), Toast.LENGTH_SHORT).show();
                etDailySummary.setError("Tóm tắt ngày không được để trống.");
                return false;
            }

            // Cập nhật mô hình dữ liệu
            schedule.summary = summary;

            // Tổng hợp dữ liệu
            detailedScheduleSummary.append(String.format("Ngày %d: %s\n", schedule.dayNumber, summary));
        }

        // Gán dữ liệu vào Tour object
        tour.setSoNgay(dayScheduleList.size());
        tour.setLichTrinhChiTiet(detailedScheduleSummary.toString());

        Log.d(TAG, "Bước 2 - Lịch trình: Thu thập thành công cho " + tour.getSoNgay() + " ngày.");
        return true;
    }
}