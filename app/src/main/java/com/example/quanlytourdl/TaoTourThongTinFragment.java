package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TaoTourThongTinFragment extends Fragment {

    private static final String TAG = "ThongTinChungFragment";
    private static final String DEFAULT_APP_ID = "default-tour-app-id";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // UI Components (Ánh xạ theo ID trong XML bạn cung cấp)
    private TextInputEditText etTourName; // et_ten_tour
    private AutoCompleteTextView actvTourType; // actv_loai_tour
    private TextInputEditText etStartDate; // et_ngay_bat_dau
    private TextInputEditText etEndDate; // et_ngay_ket_thuc
    private TextView tvDuration; // tv_thoi_luong
    private TextInputEditText etDescription; // et_mo_ta

    // Giả định các thành phần này nằm trong layout gốc hoặc parent Fragment
    private Button saveButton;
    private TextView statusTextView; // Cần được tìm thấy hoặc truyền vào từ Fragment cha
    private TextView userIdTextView; // Cần được tìm thấy hoặc truyền vào từ Fragment cha

    // Firebase instances
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String appId;
    private ListenerRegistration firestoreListener;

    private final List<String> tourTypes = Arrays.asList(
            "Tour Nội địa",
            "Tour Cá nhân",
            "Tour Đoàn/Công ty"
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFirebase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Giả định file layout là fragment_thong_tin_co_ban.xml (theo ID trong code)
        View view = inflater.inflate(R.layout.fragment_thong_tin_co_ban, container, false);

        // Ánh xạ các thành phần UI
        etTourName = view.findViewById(R.id.et_ten_tour);
        actvTourType = view.findViewById(R.id.actv_loai_tour);
        etStartDate = view.findViewById(R.id.et_ngay_bat_dau);
        etEndDate = view.findViewById(R.id.et_ngay_ket_thuc);
        tvDuration = view.findViewById(R.id.tv_thoi_luong);
        etDescription = view.findViewById(R.id.et_mo_ta);

        // TẠM THỜI gán statusTextView và userIdTextView bằng null
        // Do chúng không được ánh xạ trong đoạn code gốc.
        statusTextView = null;
        userIdTextView = null;

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Thiết lập Dropdown Loại Tour
        setupTourTypeDropdown();

        // 2. Thiết lập Date Pickers
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        // 3. Gắn Listener cho nút Lưu (Nếu nút tồn tại trong layout này - đã bị loại bỏ)
        // Nếu `saveButton` không được tìm thấy, nút "Lưu Nháp" sẽ được điều khiển bởi Fragment cha.

        // 4. Thiết lập Auth Listener Firebase
        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                Log.d(TAG, "Authenticated with UID: " + userId);
                if (userIdTextView != null) {
                    userIdTextView.setText("User ID: " + userId);
                }
                startRealtimeListener(userId);
            } else {
                Log.d(TAG, "User signed out or not yet signed in. Attempting sign-in.");
                stopRealtimeListener();
                signInUser();
            }
        });
    }

    /**
     * Khởi tạo Firebase Firestore và Auth.
     */
    private void setupFirebase() {
        try {
            // Sửa lỗi cú pháp JavaScript: Trong Java, ta phải sử dụng giá trị mặc định
            // hoặc System.getProperty() nếu biến được truyền qua VM arguments.
            // Để tương thích với môi trường Canvas, ta sẽ giả định biến này có thể được gán
            // thông qua một cơ chế nào đó (ví dụ: một class Constants), nhưng ở đây ta dùng DEFAULT_APP_ID.
            // Nếu bạn đang dùng Android Studio, hãy bỏ qua các biến __xxx__.

            // Giữ lại DEFAULT_APP_ID để biên dịch:
            appId = DEFAULT_APP_ID;

            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo Firebase: " + e.getMessage(), e);
            if (statusTextView != null) {
                statusTextView.setText("Lỗi khởi tạo Firebase.");
            }
        }
    }

    /**
     * Xử lý đăng nhập ẩn danh (do không có token tùy chỉnh).
     */
    private void signInUser() {
        if (auth.getCurrentUser() != null) return;

        auth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Signed in anonymously.");
                    } else {
                        Log.e(TAG, "Anonymous sign-in failed: " + task.getException().getMessage());
                        if (statusTextView != null) {
                            statusTextView.setText("Lỗi đăng nhập Firebase.");
                        }
                    }
                });
    }

    /**
     * Thiết lập danh sách tùy chọn cho Dropdown Loại Tour.
     */
    private void setupTourTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                tourTypes
        );
        actvTourType.setAdapter(adapter);
    }

    /**
     * Hiển thị DatePickerDialog và cập nhật trường ngày tháng.
     */
    private void showDatePickerDialog(final TextInputEditText dateField) {
        final Calendar c = Calendar.getInstance();
        if (!dateField.getText().toString().isEmpty()) {
            try {
                // Đảm bảo không bị crash nếu ngày tháng cũ không hợp lệ
                Date existingDate = DATE_FORMAT.parse(dateField.getText().toString());
                if (existingDate != null) {
                    c.setTime(existingDate);
                }
            } catch (ParseException e) {
                Log.e(TAG, "Lỗi phân tích cú pháp ngày: " + e.getMessage());
            }
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, y, m, d) -> {
                    c.set(y, m, d);
                    dateField.setText(DATE_FORMAT.format(c.getTime()));
                    calculateDuration(); // Tính lại thời lượng sau khi chọn ngày
                }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Tính toán và hiển thị thời lượng tour (Ngày/Đêm).
     */
    private void calculateDuration() {
        String startStr = etStartDate.getText().toString();
        String endStr = etEndDate.getText().toString();

        if (startStr.isEmpty() || endStr.isEmpty()) {
            tvDuration.setText("Thời lượng dự kiến: 0 ngày 0 đêm");
            return;
        }

        try {
            Date startDate = DATE_FORMAT.parse(startStr);
            Date endDate = DATE_FORMAT.parse(endStr);

            if (startDate != null && endDate != null && endDate.after(startDate)) {
                // Tính toán sự khác biệt về ngày
                long diff = endDate.getTime() - startDate.getTime();
                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1; // +1 để tính cả ngày bắt đầu
                long nights = days - 1;

                String durationText = String.format(Locale.getDefault(),
                        "Thời lượng dự kiến: %d ngày %d đêm", days, nights);
                tvDuration.setText(durationText);
            } else {
                tvDuration.setText("Thời lượng dự kiến: Ngày kết thúc phải sau Ngày bắt đầu");
            }

        } catch (ParseException e) {
            Log.e(TAG, "Lỗi tính toán thời lượng: " + e.getMessage());
        }
    }


    /**
     * Tạo đường dẫn Document Firestore.
     * Cấu trúc: /artifacts/{appId}/users/{userId}/tour_data/general_info
     */
    private DocumentReference getDocumentPath(String userId) {
        return db.collection("artifacts").document(appId)
                .collection("users").document(userId)
                .collection("tour_data").document("general_info");
    }

    /**
     * Bắt đầu lắng nghe dữ liệu Tour theo thời gian thực (onSnapshot).
     */
    private void startRealtimeListener(String userId) {
        stopRealtimeListener();

        DocumentReference docRef = getDocumentPath(userId);

        firestoreListener = docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Lỗi lắng nghe Firestore.", e);
                if (statusTextView != null) statusTextView.setText("Lỗi kết nối dữ liệu.");
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                // Tải dữ liệu và cập nhật UI
                etTourName.setText(snapshot.getString("tourName"));
                // false: không kích hoạt dropdown
                actvTourType.setText(snapshot.getString("tourType"), false);
                etStartDate.setText(snapshot.getString("startDate"));
                etEndDate.setText(snapshot.getString("endDate"));
                etDescription.setText(snapshot.getString("description"));

                calculateDuration(); // Tính lại thời lượng sau khi tải ngày tháng

                if (statusTextView != null) statusTextView.setText("Thông tin Tour đã được tải.");
                Log.d(TAG, "Dữ liệu thời gian thực được cập nhật.");
            } else {
                // Tài liệu không tồn tại
                if (statusTextView != null) statusTextView.setText("Hãy nhập và lưu thông tin Tour.");
                // Xóa các trường để chuẩn bị cho dữ liệu mới
                etTourName.setText("");
                actvTourType.setText("", false);
                etStartDate.setText("");
                etEndDate.setText("");
                etDescription.setText("");
                calculateDuration();
            }
        });
    }

    /**
     * Hủy bỏ Listener Firestore hiện tại.
     */
    private void stopRealtimeListener() {
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
            Log.d(TAG, "Firestore Listener đã được hủy.");
        }
    }

    /**
     * Phương thức PRIVATE: Chỉ thực hiện validation form.
     * @return true nếu form hợp lệ, false nếu ngược lại.
     */
    private boolean validateForm() {
        String tourName = etTourName.getText().toString().trim();
        String tourType = actvTourType.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();

        // 1. Kiểm tra các trường bắt buộc
        if (tourName.isEmpty() || tourType.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            if (statusTextView != null) statusTextView.setText("Vui lòng nhập đầy đủ các trường bắt buộc (*).");
            return false;
        }

        // 2. Kiểm tra ngày kết thúc có hợp lệ không
        try {
            Date start = DATE_FORMAT.parse(startDate);
            Date end = DATE_FORMAT.parse(endDate);
            if (end == null || start == null || end.before(start)) {
                if (statusTextView != null) statusTextView.setText("Lỗi: Ngày kết thúc phải sau Ngày bắt đầu.");
                return false;
            }
        } catch (ParseException e) {
            if (statusTextView != null) statusTextView.setText("Lỗi: Định dạng ngày không hợp lệ.");
            return false;
        }

        return true;
    }

    /**
     * Phương thức PRIVATE: Thực hiện logic lưu dữ liệu vào Firestore.
     */
    private void saveDataToFirestore(String userId) {
        String tourName = etTourName.getText().toString().trim();
        String tourType = actvTourType.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        Map<String, Object> tourInfo = new HashMap<>();
        tourInfo.put("tourName", tourName);
        tourInfo.put("tourType", tourType);
        tourInfo.put("startDate", startDate);
        tourInfo.put("endDate", endDate);
        tourInfo.put("description", description);
        tourInfo.put("lastUpdated", System.currentTimeMillis());

        // Mã Tour (Mặc định: Tự động tạo nếu chưa có)
        // Trong trường hợp này, ta tạo mới mỗi lần lưu nháp vì không có ID Tour cố định
        tourInfo.put("tourCode", "TOUR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT));

        DocumentReference docRef = getDocumentPath(userId);

        docRef.set(tourInfo)
                .addOnSuccessListener(aVoid -> {
                    // Listener Realtime sẽ cập nhật trạng thái UI
                    Log.d(TAG, "Thông tin chung Tour đã được lưu thành công.");
                    if (statusTextView != null) statusTextView.setText("Đã lưu thành công!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lưu thông tin chung Tour", e);
                    if (statusTextView != null) statusTextView.setText("Lỗi lưu dữ liệu: " + e.getMessage());
                });
    }

    // --- CÁC PHƯƠNG THỨC CÔNG KHAI DÀNH CHO FRAGMENT CHA ---

    /**
     * PUBLIC: Phương thức dùng cho nút "Lưu Nháp".
     * Chỉ kiểm tra User Auth và gọi hàm lưu. (Không cần validation nghiêm ngặt như khi chuyển bước).
     */
    public void saveGeneralInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (statusTextView != null) statusTextView.setText("Lỗi: Người dùng chưa đăng nhập. Đang thử đăng nhập lại.");
            signInUser();
            return;
        }

        // Vẫn thực hiện validation cơ bản trước khi lưu nháp
        if (validateForm()) {
            saveDataToFirestore(user.getUid());
        }
    }

    /**
     * PUBLIC: Phương thức dùng cho nút "Tiếp tục" (Next Step).
     * BẮT BUỘC phải có theo yêu cầu của Fragment cha.
     * Thực hiện Validation nghiêm ngặt. Nếu hợp lệ, tiến hành lưu và trả về TRUE.
     *
     * @return true nếu validation và lưu thành công, false nếu ngược lại.
     */
    public boolean isFormValidAndSave() {
        // 1. Kiểm tra validation
        if (!validateForm()) {
            return false;
        }

        // 2. Kiểm tra trạng thái User
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (statusTextView != null) statusTextView.setText("Lỗi: Người dùng chưa đăng nhập. Vui lòng thử lại.");
            signInUser(); // Cố gắng đăng nhập lại
            return false;
        }

        // 3. Tiến hành lưu dữ liệu
        saveDataToFirestore(user.getUid());

        // Trả về true ngay lập tức để cho phép chuyển bước.
        // LƯU Ý: Việc lưu Firestore là bất đồng bộ (async), nhưng ta chấp nhận chuyển bước
        // nếu validation thành công.
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopRealtimeListener();
    }
}