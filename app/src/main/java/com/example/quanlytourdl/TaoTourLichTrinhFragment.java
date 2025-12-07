package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TaoTourLichTrinhFragment extends Fragment {

    private static final String TAG = "LichTrinhFragment";
    private static final String DEFAULT_APP_ID = "default-tour-app-id";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // UI Components
    private LinearLayout llItineraryDaysContainer;
    private Button btnAddNewDay;
    // statusTextView: Dùng để hiển thị trạng thái/lỗi (Nếu có trong layout cha)

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String appId;
    private ListenerRegistration itineraryListener;
    private ListenerRegistration generalInfoListener;

    // Data Holders
    private int totalDays = 0;
    private List<DayData> dayDataList = new ArrayList<>();

    /**
     * Lớp nội bộ để giữ tham chiếu UI và dữ liệu cho mỗi ngày.
     */
    private static class DayData {
        int dayNumber;
        MaterialCardView cardView;
        TextView tvDayTitle;
        TextInputEditText etDayTitle;
        TextInputEditText etDayActivities;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFirebase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đã cập nhật để sử dụng R.layout.fragment_lich_trinh theo yêu cầu của bạn
        View view = inflater.inflate(R.layout.fragment_lich_trinh, container, false);

        llItineraryDaysContainer = view.findViewById(R.id.ll_itinerary_days_container);
        btnAddNewDay = view.findViewById(R.id.btn_add_new_day);

        // Loại bỏ nút Thêm Ngày Mới
        if (btnAddNewDay != null) {
            btnAddNewDay.setVisibility(View.GONE);
        }

        // statusTextView (Giả định rằng nó không nằm trong layout này)
        // Nếu bạn muốn dùng nó, bạn cần ánh xạ nó từ Activity hoặc Fragment cha.

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "Authenticated with UID: " + user.getUid());
                fetchGeneralInfoAndStartListeners(user.getUid());
            } else {
                Log.d(TAG, "User signed out or not yet signed in.");
                stopListeners();
                signInUser();
            }
        });
    }

    /**
     * Khởi tạo Firebase Firestore và Auth.
     */
    private void setupFirebase() {
        try {
            appId = DEFAULT_APP_ID;
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo Firebase: " + e.getMessage(), e);
            showToast("Lỗi khởi tạo Firebase.");
        }
    }

    /**
     * Đăng nhập ẩn danh nếu chưa đăng nhập.
     */
    private void signInUser() {
        if (auth.getCurrentUser() != null) return;
        auth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Signed in anonymously.");
                    } else {
                        Log.e(TAG, "Anonymous sign-in failed: " + task.getException().getMessage());
                        showToast("Lỗi đăng nhập Firebase.");
                    }
                });
    }

    /**
     * Lấy đường dẫn document Thông tin chung.
     */
    private DocumentReference getGeneralInfoPath(String userId) {
        return db.collection("artifacts").document(appId)
                .collection("users").document(userId)
                .collection("tour_data").document("general_info");
    }

    /**
     * Lấy đường dẫn document Chi tiết Lịch trình.
     */
    private DocumentReference getItineraryDetailsPath(String userId) {
        return db.collection("artifacts").document(appId)
                .collection("users").document(userId)
                .collection("tour_data").document("itinerary_details");
    }

    /**
     * Phương thức chính: Lấy Thông tin chung để xác định số ngày, sau đó bắt đầu Listener Lịch trình.
     */
    private void fetchGeneralInfoAndStartListeners(String userId) {
        stopListeners();

        // 1. Lắng nghe Thông tin chung (để tính toán số ngày)
        generalInfoListener = getGeneralInfoPath(userId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Lỗi lắng nghe Thông tin chung.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                String startDateStr = snapshot.getString("startDate");
                String endDateStr = snapshot.getString("endDate");
                calculateTotalDays(startDateStr, endDateStr);
            } else {
                calculateTotalDays(null, null);
                showToast("Chưa có Thông tin chung. Mặc định 1 ngày.");
            }

            // 2. Bắt đầu lắng nghe Chi tiết lịch trình
            startItineraryListener(userId);
        });
    }

    /**
     * Dừng tất cả các Listener Firestore.
     */
    private void stopListeners() {
        if (generalInfoListener != null) {
            generalInfoListener.remove();
            generalInfoListener = null;
        }
        if (itineraryListener != null) {
            itineraryListener.remove();
            itineraryListener = null;
        }
    }

    /**
     * Tính toán số ngày tour dựa trên Ngày bắt đầu và Ngày kết thúc.
     */
    private void calculateTotalDays(@Nullable String startDateStr, @Nullable String endDateStr) {
        int newTotalDays = 0;
        if (startDateStr != null && endDateStr != null) {
            try {
                Date startDate = DATE_FORMAT.parse(startDateStr);
                Date endDate = DATE_FORMAT.parse(endDateStr);

                if (startDate != null && endDate != null && !endDate.before(startDate)) {
                    long diff = endDate.getTime() - startDate.getTime();
                    // Thêm 1 ngày vì cả ngày bắt đầu và kết thúc đều được tính
                    newTotalDays = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
                }
            } catch (ParseException e) {
                Log.e(TAG, "Lỗi phân tích cú pháp ngày: " + e.getMessage());
                newTotalDays = 1;
            }
        }

        if (newTotalDays <= 0) {
            newTotalDays = 1;
        }

        if (newTotalDays != totalDays) {
            totalDays = newTotalDays;
            Log.d(TAG, "Total days updated to: " + totalDays);
            // Giao diện sẽ được render trong startItineraryListener
        }
    }

    /**
     * Bắt đầu lắng nghe dữ liệu Chi tiết Lịch trình theo thời gian thực (onSnapshot).
     */
    private void startItineraryListener(String userId) {
        if (itineraryListener != null) {
            itineraryListener.remove();
        }

        DocumentReference docRef = getItineraryDetailsPath(userId);

        itineraryListener = docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Lỗi lắng nghe Chi tiết Lịch trình.", e);
                showToast("Lỗi kết nối dữ liệu Lịch trình.");
                return;
            }

            List<Map<String, Object>> savedDays = new ArrayList<>();
            if (snapshot != null && snapshot.exists() && snapshot.contains("days")) {
                try {
                    // Phải đảm bảo type casting an toàn
                    savedDays = (List<Map<String, Object>>) snapshot.get("days");
                    if (savedDays == null) savedDays = new ArrayList<>();
                } catch (Exception ex) {
                    Log.e(TAG, "Lỗi chuyển đổi dữ liệu lịch trình", ex);
                    savedDays = new ArrayList<>();
                }
            }

            renderItineraryDays(savedDays);
        });
    }

    /**
     * Tạo hoặc cập nhật giao diện người dùng cho tất cả các ngày.
     * @param savedDays Dữ liệu lịch trình đã lưu từ Firestore.
     */
    private void renderItineraryDays(List<Map<String, Object>> savedDays) {
        llItineraryDaysContainer.removeAllViews();
        dayDataList.clear();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (int i = 1; i <= totalDays; i++) {
            final int dayNum = i;

            // 1. Tạo View mới từ template_itinerary_day_card.xml
            // Giả định file này tồn tại và chứa các ID cần thiết
            View dayView = inflater.inflate(R.layout.template_itinerary_day_card, llItineraryDaysContainer, false);
            llItineraryDaysContainer.addView(dayView);

            // 2. Tạo đối tượng DayData và ánh xạ UI
            DayData dayData = new DayData();
            dayData.dayNumber = dayNum;
            dayData.cardView = (MaterialCardView) dayView;

            // Ánh xạ các ID cố định trong template_itinerary_day_card.xml
            dayData.tvDayTitle = dayView.findViewById(R.id.tv_day_number);
            dayData.etDayTitle = dayView.findViewById(R.id.et_day_title);
            dayData.etDayActivities = dayView.findViewById(R.id.et_day_activities);

            // Đặt tiêu đề Ngày
            if (dayData.tvDayTitle != null) {
                dayData.tvDayTitle.setText(String.format(Locale.getDefault(), "NGÀY %d", dayNum));
            }

            // Gắn Listener Mock cho nút chi tiết hoạt động
            Button btnAddActivity = dayView.findViewById(R.id.btn_add_activity);
            if (btnAddActivity != null) {
                btnAddActivity.setOnClickListener(v ->
                        showToast("Chức năng chi tiết hoạt động ngày " + dayNum + " chưa triển khai."));
            }

            // 3. Tải dữ liệu đã lưu (nếu có)
            for (Map<String, Object> savedDay : savedDays) {
                if (savedDay.get("dayNumber") instanceof Number &&
                        ((Number) savedDay.get("dayNumber")).intValue() == dayNum) {

                    if (savedDay.containsKey("title")) {
                        dayData.etDayTitle.setText((CharSequence) savedDay.get("title"));
                    }
                    if (savedDay.containsKey("activities")) {
                        dayData.etDayActivities.setText((CharSequence) savedDay.get("activities"));
                    }
                    break;
                }
            }

            dayDataList.add(dayData);
        }
        Log.d(TAG, "Rendered " + totalDays + " days.");
    }

    /**
     * Phương thức PUBLIC: Dùng cho nút "Tiếp tục" (Next Step).
     * @return true nếu validation và lưu thành công, false nếu ngược lại.
     */
    public boolean isFormValidAndSave() {
        if (!validateForm()) {
            return false;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showToast("Lỗi: Người dùng chưa đăng nhập. Vui lòng thử lại.");
            signInUser();
            return false;
        }

        // Tiến hành lưu dữ liệu
        saveItineraryData(user.getUid());

        return true;
    }

    /**
     * Phương thức PUBLIC: Dùng cho nút "Lưu Nháp" (Draft Save).
     */
    public void saveDraft() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showToast("Lỗi: Người dùng chưa đăng nhập. Vui lòng thử lại.");
            signInUser();
            return;
        }

        // Lưu dữ liệu mà không cần validation nghiêm ngặt
        saveItineraryData(user.getUid());
    }

    /**
     * Phương thức PRIVATE: Chỉ thực hiện validation form.
     */
    private boolean validateForm() {
        for (DayData day : dayDataList) {
            String title = day.etDayTitle.getText().toString().trim();
            String activities = day.etDayActivities.getText().toString().trim();

            if (title.isEmpty() || activities.isEmpty()) {
                showToast("Ngày " + day.dayNumber + ": Vui lòng nhập đầy đủ Tiêu đề và Mô tả chi tiết.");
                // Yêu cầu focus vào trường lỗi đầu tiên
                if (title.isEmpty()) {
                    day.etDayTitle.requestFocus();
                } else {
                    day.etDayActivities.requestFocus();
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Phương thức PRIVATE: Thực hiện logic lưu dữ liệu Lịch trình vào Firestore.
     */
    private void saveItineraryData(String userId) {
        List<Map<String, Object>> daysToSave = new ArrayList<>();

        for (DayData day : dayDataList) {
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("dayNumber", day.dayNumber);
            dayMap.put("title", day.etDayTitle.getText().toString().trim());
            dayMap.put("activities", day.etDayActivities.getText().toString().trim());

            daysToSave.add(dayMap);
        }

        Map<String, Object> itineraryDetails = new HashMap<>();
        itineraryDetails.put("days", daysToSave);
        itineraryDetails.put("totalDays", totalDays);
        itineraryDetails.put("lastUpdated", System.currentTimeMillis());

        DocumentReference docRef = getItineraryDetailsPath(userId);

        docRef.set(itineraryDetails, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chi tiết Lịch trình đã được lưu thành công.");
                    showToast("Đã lưu Lịch trình!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lưu Chi tiết Lịch trình", e);
                    showToast("Lỗi lưu dữ liệu Lịch trình: " + e.getMessage());
                });
    }

    // Phương thức tiện ích để hiển thị Toast
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        // Giả định không thể truy cập statusTextView, nên chỉ dùng Toast
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopListeners();
    }
}