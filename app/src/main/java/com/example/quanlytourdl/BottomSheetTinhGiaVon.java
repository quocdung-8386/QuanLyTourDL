package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * BottomSheet để nhập chi tiết các hạng mục chi phí tour và số lượng khách dự kiến.
 */
public class BottomSheetTinhGiaVon extends BottomSheetDialogFragment {

    // Interface để truyền dữ liệu về Fragment chứa nó
    public interface OnCostUpdateListener {
        void onCostUpdated(long totalCost, int paxCount);
    }

    private OnCostUpdateListener listener;

    // UI Elements
    private TextInputEditText etPaxCount;
    private TextView tvTotalCostCalculated;
    private MaterialButton btnSaveCost;
    private MaterialButton btnResetCost;
    private ImageButton btnCloseDialog;

    // Map để quản lý tất cả các trường chi phí (ID -> EditText)
    private Map<Integer, TextInputEditText> costEditTexts;

    // Format utility
    private final NumberFormat currencyFormatter = new DecimalFormat("#,###",
            new java.text.DecimalFormatSymbols(Locale.getDefault()));

    // Biến tạm để lưu trữ chi phí mặc định (cho chức năng Reset về 0)
    private final Map<Integer, Long> defaultCosts = new HashMap<>();

    public void setOnCostUpdateListener(OnCostUpdateListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Layout đã được đổi tên trong file XML mới
        return inflater.inflate(R.layout.bottom_sheet_tinh_gia_von, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ các thành phần chính
        etPaxCount = view.findViewById(R.id.et_so_luong_khach);
        tvTotalCostCalculated = view.findViewById(R.id.tv_total_cost_calculated);
        btnSaveCost = view.findViewById(R.id.btn_save_cost);
        btnResetCost = view.findViewById(R.id.btn_reset_cost);
        btnCloseDialog = view.findViewById(R.id.btn_close_dialog);

        // 2. Ánh xạ và quản lý các trường chi phí con
        initializeCostFields(view);

        // 3. Thiết lập Listener
        btnCloseDialog.setOnClickListener(v -> dismiss());
        btnSaveCost.setOnClickListener(v -> saveCostData());
        btnResetCost.setOnClickListener(v -> resetCostData());

        // 4. Load dữ liệu ban đầu (từ Bundle, nhưng nếu Bundle rỗng thì giá trị sẽ rỗng)
        loadInitialData();

        // 5. Tính toán ban đầu và thiết lập TextWatcher
        applyTextWatchers();
        calculateTotalCost(); // Tính toán lần đầu với dữ liệu đã load (sẽ là 0 nếu không có data cũ)
    }

    /**
     * Ánh xạ tất cả các trường chi phí con, lưu vào Map và ĐẶT GIÁ TRỊ RỖNG.
     */
    private void initializeCostFields(View view) {
        costEditTexts = new HashMap<>();
        costEditTexts.put(R.id.et_cost_guide, view.findViewById(R.id.et_cost_guide));
        costEditTexts.put(R.id.et_cost_transport, view.findViewById(R.id.et_cost_transport));
        costEditTexts.put(R.id.et_cost_stay, view.findViewById(R.id.et_cost_stay));
        costEditTexts.put(R.id.et_cost_food, view.findViewById(R.id.et_cost_food));
        costEditTexts.put(R.id.et_cost_tickets, view.findViewById(R.id.et_cost_tickets));
        costEditTexts.put(R.id.et_cost_insurance, view.findViewById(R.id.et_cost_insurance));
        costEditTexts.put(R.id.et_cost_other, view.findViewById(R.id.et_cost_other));

        // Đảm bảo tất cả các trường chi phí đều rỗng khi khởi tạo
        for (TextInputEditText et : costEditTexts.values()) {
            et.setText(""); // Đặt giá trị rỗng
        }

        // Lưu giá trị mặc định là 0 cho chức năng Reset
        for (Map.Entry<Integer, TextInputEditText> entry : costEditTexts.entrySet()) {
            defaultCosts.put(entry.getKey(), 0L);
        }
    }

    /**
     * Load Số lượng khách và các chi phí ban đầu (nếu được truyền từ Fragment mẹ).
     */
    private void loadInitialData() {
        if (getArguments() != null) {
            int currentPax = getArguments().getInt("currentPax", 0);
            if (currentPax > 0) {
                etPaxCount.setText(String.valueOf(currentPax));
            } else {
                etPaxCount.setText(""); // Đảm bảo rỗng nếu 0
            }

            // TODO: Nếu Fragment mẹ truyền chi phí chi tiết, cần cập nhật logic tại đây.
            // Hiện tại, các trường chi phí sẽ giữ nguyên là rỗng.
        } else {
            // Đảm bảo các trường rỗng nếu không có Bundle
            etPaxCount.setText("");
        }
    }

    /**
     * Thiết lập TextWatcher cho tất cả các trường chi phí.
     */
    private void applyTextWatchers() {
        // TextWatcher cho Số lượng khách
        etPaxCount.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Không cần tính toán lại tổng chi phí, chỉ cần đảm bảo người dùng nhập số
            }
        });

        // TextWatcher cho các trường chi phí (tự động format và tính tổng)
        for (TextInputEditText et : costEditTexts.values()) {
            et.addTextChangedListener(new CurrencyTextWatcher(et));
        }
    }

    /**
     * Tính toán tổng chi phí từ tất cả các trường nhập liệu và cập nhật TextView.
     */
    private void calculateTotalCost() {
        long totalCost = 0;
        for (TextInputEditText et : costEditTexts.values()) {
            String text = Objects.requireNonNull(et.getText()).toString();
            totalCost += parseCurrency(text);
        }

        tvTotalCostCalculated.setText(String.format("Tổng: %s VNĐ", formatCurrency(totalCost)));
    }

    /**
     * Chuyển đổi chuỗi tiền tệ đã định dạng ("1.500.000") thành giá trị Long (1500000).
     */
    private long parseCurrency(String formattedString) {
        if (formattedString == null || formattedString.isEmpty()) return 0;
        try {
            // Xóa dấu chấm, phẩy, và ký tự tiền tệ
            String cleanString = formattedString.replaceAll("[., VNĐ]", "");
            if (cleanString.isEmpty()) return 0; // Quan trọng: Trả về 0 nếu chuỗi rỗng sau khi làm sạch
            return Long.parseLong(cleanString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Định dạng số Long thành chuỗi tiền tệ ("1.500.000").
     */
    private String formatCurrency(long amount) {
        return currencyFormatter.format(amount);
    }

    /**
     * Lớp TextWatcher tùy chỉnh để tự động định dạng tiền tệ.
     */
    private class CurrencyTextWatcher extends SimpleTextWatcher {
        private final TextInputEditText editText;
        private String current = "";

        CurrencyTextWatcher(TextInputEditText editText) {
            this.editText = editText;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Loại bỏ logic này trong onTextChanged để tránh các vấn đề về vòng lặp định dạng
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!s.toString().equals(current)) {
                editText.removeTextChangedListener(this);

                String cleanString = s.toString().replaceAll("[., VNĐ]", "");

                if (!cleanString.isEmpty()) {
                    long parsed = parseCurrency(cleanString);
                    String formatted = formatCurrency(parsed);

                    current = formatted;
                    editText.setText(formatted);
                    // Đặt con trỏ về cuối văn bản đã định dạng
                    try {
                        editText.setSelection(formatted.length());
                    } catch (Exception e) {
                        // Bỏ qua lỗi IndexOutOfBounds khi nhập liệu nhanh
                    }
                } else {
                    current = "";
                    editText.setText("");
                }
                editText.addTextChangedListener(this);
            }
            // Luôn tính toán lại tổng sau khi input thay đổi
            calculateTotalCost();
        }
    }

    /**
     * Lớp TextWatcher đơn giản chỉ triển khai 1 phương thức.
     */
    private static abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* do nothing */ }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { /* do nothing */ }
        @Override
        public abstract void afterTextChanged(Editable s);
    }

    // --- Hành động Nút ---

    private void resetCostData() {
        // Đặt lại số lượng khách về rỗng
        etPaxCount.setText("");

        // Đặt lại các trường chi phí về rỗng (giá trị 0)
        for (TextInputEditText et : costEditTexts.values()) {
            et.setText("");
        }

        calculateTotalCost(); // Tổng chi phí sẽ là 0
        Toast.makeText(getContext(), "Đã đặt lại các hạng mục chi phí.", Toast.LENGTH_SHORT).show();
    }

    private void saveCostData() {
        String paxStr = Objects.requireNonNull(etPaxCount.getText()).toString().trim();
        long totalCost = 0;
        int paxCount = 0;

        // 1. Validation Số lượng khách
        if (paxStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập Số lượng khách dự kiến.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            paxCount = Integer.parseInt(paxStr);
            if (paxCount <= 0) {
                Toast.makeText(getContext(), "Số lượng khách phải lớn hơn 0.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số lượng khách không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validation Tổng chi phí
        // Lấy tổng chi phí đã được tính và hiển thị trên tvTotalCostCalculated
        String totalCostStr = tvTotalCostCalculated.getText().toString();
        // Loại bỏ tiền tố "Tổng: " và hậu tố " VNĐ" để lấy chuỗi số đã định dạng
        if (totalCostStr.contains("Tổng: ") && totalCostStr.contains(" VNĐ")) {
            String cleanedTotalCostStr = totalCostStr.replace("Tổng: ", "").replace(" VNĐ", "").trim();
            totalCost = parseCurrency(cleanedTotalCostStr);
        } else {
            // Fallback: Tính lại tổng chi phí từ các trường nhập liệu nếu chuỗi TextView bị lỗi
            totalCost = 0;
            for (TextInputEditText et : costEditTexts.values()) {
                String text = Objects.requireNonNull(et.getText()).toString();
                totalCost += parseCurrency(text);
            }
        }

        if (totalCost <= 0) {
            Toast.makeText(getContext(), "Tổng Chi phí phải lớn hơn 0.", Toast.LENGTH_LONG).show();
            return;
        }

        // Nếu tất cả đều hợp lệ
        if (listener != null) {
            listener.onCostUpdated(totalCost, paxCount);
        }

        dismiss(); // Đóng Bottom Sheet
    }
}