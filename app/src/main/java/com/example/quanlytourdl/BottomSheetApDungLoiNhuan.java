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
import java.util.Locale;
import java.util.Objects;

/**
 * BottomSheet để nhập tỷ suất lợi nhuận (Profit Margin) mong muốn.
 * Tự động tính toán Giá bán dự kiến dựa trên Giá vốn/khách đã cung cấp.
 */
public class BottomSheetApDungLoiNhuan extends BottomSheetDialogFragment {

    // Interface để truyền dữ liệu về Fragment chứa nó
    public interface OnProfitUpdateListener {
        void onProfitUpdated(double profitMargin);
    }

    private OnProfitUpdateListener listener;

    // UI Elements
    private TextInputEditText etProfitMargin;
    private TextView tvPredictedSellingPrice;
    private MaterialButton btnSaveProfit;
    private ImageButton btnCloseDialog;

    // Data
    private long costPerPax = 0; // Giá vốn trên mỗi khách (được truyền từ Fragment mẹ)

    // Format utility
    private final NumberFormat currencyFormatter = new DecimalFormat("#,###");

    public void setOnProfitUpdateListener(OnProfitUpdateListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout mới
        return inflater.inflate(R.layout.bottom_sheet_ap_dung_loi_nhuan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ UI (sử dụng ID mới)
        etProfitMargin = view.findViewById(R.id.et_profit_margin);
        tvPredictedSellingPrice = view.findViewById(R.id.tv_predicted_selling_price);
        btnSaveProfit = view.findViewById(R.id.btn_save_profit_margin); // ID mới
        btnCloseDialog = view.findViewById(R.id.btn_close_profit_dialog); // ID mới

        // 2. Load dữ liệu ban đầu
        loadInitialData();

        // 3. Thiết lập Listener
        btnCloseDialog.setOnClickListener(v -> dismiss());
        btnSaveProfit.setOnClickListener(v -> saveProfitData());

        // TextWatcher để tính toán giá bán theo thời gian thực
        etProfitMargin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                calculateAndDisplaySellingPrice();
            }
        });

        // 4. Tính toán và hiển thị lần đầu
        calculateAndDisplaySellingPrice();
    }

    /**
     * Load Giá vốn/khách và tỷ suất lợi nhuận cũ từ Fragment mẹ.
     */
    private void loadInitialData() {
        if (getArguments() != null) {
            // Lấy giá vốn/khách (Cost Per Pax) để tính giá bán
            costPerPax = getArguments().getLong("costPerPax", 0);

            // Lấy tỷ suất lợi nhuận hiện tại (dạng thập phân: 0.x)
            double currentProfitDecimal = getArguments().getDouble("currentProfit", 0);

            // Chuyển đổi sang phần trăm để hiển thị (ví dụ: 0.25 -> 25.0)
            if (currentProfitDecimal > 0) {
                String profitPercentStr = String.format(Locale.getDefault(), "%.1f", currentProfitDecimal * 100);
                etProfitMargin.setText(profitPercentStr);
            }
        }
    }

    /**
     * Tính toán Giá bán dự kiến dựa trên Giá vốn và Tỷ suất lợi nhuận hiện tại.
     */
    private void calculateAndDisplaySellingPrice() {
        if (costPerPax <= 0) {
            tvPredictedSellingPrice.setText("Chưa có Giá Vốn");
            return;
        }

        String profitStr = Objects.requireNonNull(etProfitMargin.getText()).toString().trim();
        double profitPercentage = 0;

        try {
            if (!profitStr.isEmpty()) {
                profitPercentage = Double.parseDouble(profitStr);
            }
        } catch (NumberFormatException e) {
            // Bỏ qua lỗi định dạng khi người dùng đang nhập
        }

        // Chuyển từ % sang decimal (0-1)
        double profitMarginDecimal = profitPercentage / 100.0;

        if (profitMarginDecimal >= 1.0) {
            tvPredictedSellingPrice.setText("Lợi nhuận quá cao!");
            return;
        }

        long sellingPrice = 0;
        if (profitMarginDecimal >= 0) {
            // Giá bán = Giá vốn / (1 - Tỷ suất lợi nhuận)
            sellingPrice = (long) Math.round(costPerPax / (1.0 - profitMarginDecimal));
        }

        tvPredictedSellingPrice.setText(formatCurrency(sellingPrice) + " VNĐ");
    }

    /**
     * Định dạng số tiền sang chuỗi VNĐ.
     */
    private String formatCurrency(long amount) {
        return currencyFormatter.format(amount);
    }

    /**
     * Lưu tỷ suất lợi nhuận đã chọn và đóng dialog.
     */
    private void saveProfitData() {
        String profitStr = Objects.requireNonNull(etProfitMargin.getText()).toString().trim();

        if (profitStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập Tỷ suất lợi nhuận.", Toast.LENGTH_SHORT).show();
            return;
        }

        double profitPercentage;
        try {
            profitPercentage = Double.parseDouble(profitStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Dữ liệu nhập không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profitPercentage < 0 || profitPercentage >= 100) {
            Toast.makeText(getContext(), "Lợi nhuận phải nằm trong khoảng 0% đến 99.9%.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi từ % sang double (ví dụ: 25 -> 0.25)
        double profitMargin = profitPercentage / 100.0;

        if (listener != null) {
            listener.onProfitUpdated(profitMargin);
        }

        dismiss(); // Đóng Bottom Sheet
    }
}