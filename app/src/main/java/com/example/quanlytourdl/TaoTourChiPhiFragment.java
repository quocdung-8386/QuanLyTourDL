package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Fragment xử lý bước 3: Quản lý Chi phí & Định giá Tour.
 * Bao gồm tính toán giá vốn, áp dụng lợi nhuận và quản lý dịch vụ bao gồm/không bao gồm.
 */
public class TaoTourChiPhiFragment extends Fragment
        implements BottomSheetTinhGiaVon.OnCostUpdateListener, BottomSheetApDungLoiNhuan.OnProfitUpdateListener { // Triển khai các interface listener

    // --- Data Model Mock-up ---
    // Trong ứng dụng thực tế, lớp này nên được di chuyển ra ngoài hoặc sử dụng ViewModel/Repository
    static class TourPricingData {
        public long totalCost = 0;
        public int paxCount = 0; // Số lượng khách dự kiến
        public double profitMargin = 0.0; // Tỷ suất lợi nhuận (ví dụ: 0.25 cho 25%)
        public String inclusions = "";
        public String exclusions = "";

        /** Tính toán Giá vốn/khách (Cost Per Pax) */
        public long getCostPerPax() {
            return paxCount > 0 ? totalCost / paxCount : 0;
        }

        /** Tính toán Giá bán/khách (Selling Price) dựa trên tỷ suất lợi nhuận */
        public long getSellingPrice() {
            long costPerPax = getCostPerPax();
            if (costPerPax == 0 || profitMargin >= 1.0) return 0;
            // Giá bán/khách = Giá vốn/(1 - Tỷ suất lợi nhuận)
            // (Làm tròn để hiển thị tốt hơn)
            return (long) Math.round(costPerPax / (1.0 - profitMargin));
        }
    }
    // --------------------------

    private TourPricingData tourPricingData;

    // UI Elements
    private TextView tvTotalCostPrice;
    private TextView tvCostPerPax;
    private TextView tvSellingPrice;
    private TextView tvProfitMargin;
    private MaterialButton btnOpenCostDialog;
    private MaterialButton btnOpenProfitDialog;
    private TextInputEditText etInclusions;
    private TextInputEditText etExclusions;

    // Format utility
    // Sử dụng Locale.US để đảm bảo dấu phẩy hàng nghìn (ví dụ: 10,000,000)
    private final NumberFormat currencyFormatter = new DecimalFormat("#,###");

    /**
     * Định dạng số tiền sang chuỗi VNĐ
     */
    private String formatCurrency(long amount) {
        return currencyFormatter.format(amount) + " VNĐ";
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Sử dụng R.layout.fragment_chi_phi (Tên bạn cung cấp trong code cũ)
        return inflater.inflate(R.layout.fragment_chi_phi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo dữ liệu mẫu
        tourPricingData = new TourPricingData();
        // Đã loại bỏ dữ liệu giả lập ban đầu để bắt đầu với các giá trị rỗng (0).
        // tourPricingData.totalCost = 82500000;
        // tourPricingData.paxCount = 20;
        // tourPricingData.profitMargin = 0.30;
        // tourPricingData.inclusions = "• Vé máy bay khứ hồi (Hạng phổ thông).\n• Khách sạn 4 sao (2 đêm).\n• 6 bữa ăn theo chương trình.";
        // tourPricingData.exclusions = "• Chi phí cá nhân, mua sắm.\n• Đồ uống gọi thêm trong bữa ăn.\n• Tiền tips cho hướng dẫn viên/tài xế.";


        // 2. Ánh xạ UI
        tvTotalCostPrice = view.findViewById(R.id.tv_total_cost_price);
        tvCostPerPax = view.findViewById(R.id.tv_cost_per_pax);
        tvSellingPrice = view.findViewById(R.id.tv_selling_price);
        tvProfitMargin = view.findViewById(R.id.tv_profit_margin);
        btnOpenCostDialog = view.findViewById(R.id.btn_open_cost_dialog);
        btnOpenProfitDialog = view.findViewById(R.id.btn_open_profit_dialog);
        etInclusions = view.findViewById(R.id.et_inclusions);
        etExclusions = view.findViewById(R.id.et_exclusions);

        // 3. Thiết lập Listener cho các nút mở Dialog
        btnOpenCostDialog.setOnClickListener(v -> openCostCalculationDialog());
        btnOpenProfitDialog.setOnClickListener(v -> openProfitMarginDialog());

        // 4. Cập nhật UI với dữ liệu ban đầu
        updateUI();
    }

    /**
     * Cập nhật toàn bộ các TextView hiển thị Giá vốn và Giá bán.
     */
    private void updateUI() {
        // --- Giá Vốn ---
        tvTotalCostPrice.setText(formatCurrency(tourPricingData.totalCost));
        tvCostPerPax.setText(
                String.format(Locale.getDefault(), "Giá vốn/khách dự kiến: %s (SL: %d)",
                        formatCurrency(tourPricingData.getCostPerPax()),
                        tourPricingData.paxCount
                )
        );

        // --- Giá Bán & Lợi Nhuận ---
        tvSellingPrice.setText(formatCurrency(tourPricingData.getSellingPrice()));
        tvProfitMargin.setText(
                String.format(Locale.getDefault(), "Tỷ suất lợi nhuận dự kiến: %d%%",
                        (int) (tourPricingData.profitMargin * 100)
                )
        );

        // --- Dịch vụ ---
        etInclusions.setText(tourPricingData.inclusions);
        etExclusions.setText(tourPricingData.exclusions);
    }

    /**
     * Mở dialog Tính Chi Phí Chi Tiết.
     */
    private void openCostCalculationDialog() {
        BottomSheetTinhGiaVon bottomSheet = new BottomSheetTinhGiaVon();

        // Truyền dữ liệu cũ vào dialog
        Bundle bundle = new Bundle();
        // Khi mở dialog, truyền dữ liệu hiện tại (có thể là 0 nếu chưa nhập)
        bundle.putLong("currentCost", tourPricingData.totalCost);
        bundle.putInt("currentPax", tourPricingData.paxCount);
        bottomSheet.setArguments(bundle);

        bottomSheet.setOnCostUpdateListener(this); // Thiết lập listener là Fragment này
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }

    /**
     * Mở dialog Áp Dụng Lợi Nhuận.
     */
    private void openProfitMarginDialog() {
        BottomSheetApDungLoiNhuan bottomSheet = new BottomSheetApDungLoiNhuan();

        // Truyền dữ liệu cũ vào dialog
        Bundle bundle = new Bundle();
        // QUAN TRỌNG: Truyền Giá vốn/khách và Tỷ suất lợi nhuận cũ
        bundle.putLong("costPerPax", tourPricingData.getCostPerPax());
        bundle.putDouble("currentProfit", tourPricingData.profitMargin);
        bottomSheet.setArguments(bundle);

        bottomSheet.setOnProfitUpdateListener(this); // Thiết lập listener là Fragment này
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }


    /**
     * Hàm công khai để kiểm tra tính hợp lệ và lưu dữ liệu.
     * Thường được gọi từ Activity/Fragment chứa nó khi người dùng bấm "Tiếp tục".
     * @return true nếu dữ liệu hợp lệ và đã lưu, false nếu không hợp lệ.
     */
    public boolean isValidAndSave() {
        // 1. Lấy dữ liệu từ UI
        tourPricingData.inclusions = etInclusions.getText() != null ? etInclusions.getText().toString().trim() : "";
        tourPricingData.exclusions = etExclusions.getText() != null ? etExclusions.getText().toString().trim() : "";

        // 2. Thực hiện Validation
        // Yêu cầu: Tour phải có giá vốn và số lượng khách dự kiến để tính giá bán.
        if (tourPricingData.totalCost <= 0 || tourPricingData.paxCount <= 0) {
            Toast.makeText(getContext(), "Vui lòng hoàn tất tính Giá Vốn Tour và số lượng khách dự kiến trước khi tiếp tục.", Toast.LENGTH_LONG).show();
            return false;
        }

        // 3. Lưu dữ liệu (Mock up)
        saveTourPricingData();
        return true;
    }

    /**
     * Xử lý logic lưu thông tin chi phí và giá cả vào ViewModel hoặc Firestore.
     */
    private void saveTourPricingData() {
        // Đây là nơi logic lưu dữ liệu TourPricingData vào hệ thống được thực hiện.
        // Ví dụ: tourViewModel.savePricingData(tourPricingData);
        Toast.makeText(getContext(), "Đã lưu dữ liệu Chi phí & Định giá thành công! Giá bán: " + formatCurrency(tourPricingData.getSellingPrice()), Toast.LENGTH_SHORT).show();
    }


    // --- IMPLEMENTATION CỦA CÁC INTERFACE LISTENER TỪ BOTTOM SHEET ---

    @Override
    public void onCostUpdated(long totalCost, int paxCount) {
        tourPricingData.totalCost = totalCost;
        tourPricingData.paxCount = paxCount;
        updateUI(); // Cập nhật giao diện sau khi có dữ liệu mới
        Toast.makeText(getContext(), "Đã cập nhật Giá Vốn Tour.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProfitUpdated(double profitMargin) {
        tourPricingData.profitMargin = profitMargin;
        updateUI(); // Cập nhật giao diện sau khi có dữ liệu mới
        Toast.makeText(getContext(), "Đã áp dụng lợi nhuận mới.", Toast.LENGTH_SHORT).show();
    }
}