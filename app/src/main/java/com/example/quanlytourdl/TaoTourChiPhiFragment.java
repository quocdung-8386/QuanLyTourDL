package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.quanlytourdl.model.Tour;
// ⭐ KHAI BÁO CÁC LỚP BOTTOM SHEET ĐÃ CÓ VÀ SẼ SỬ DỤNG
// Đảm bảo các file này tồn tại trong package com.example.quanlytourdl
// import com.example.quanlytourdl.BottomSheetTinhGiaVon; // Cần import nếu nằm trong package khác
// import com.example.quanlytourdl.BottomSheetApDungLoiNhuan; // Cần import nếu nằm trong package khác
// Hiện tại tôi giả định chúng nằm trong cùng package nên không cần explicit import nếu IDE tự tìm thấy

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

// ⭐ ĐÃ THÊM: Implement hai Interface Listeners từ Bottom Sheet
public class TaoTourChiPhiFragment extends Fragment
        implements TaoTourDetailFullFragment.TourStepDataCollector,
        BottomSheetTinhGiaVon.OnCostUpdateListener,
        BottomSheetApDungLoiNhuan.OnProfitUpdateListener {

    private static final String TAG = "TaoTourChiPhiFragment";

    // Định dạng số tiền (Ví dụ: 1,500,000)
    private final DecimalFormat priceFormatter = new DecimalFormat("#,###", new DecimalFormatSymbols(new Locale("vi", "VN")));

    private final Tour tour;

    // ... (Khai báo UI giữ nguyên) ...
    private TextView tvTotalCostPrice;
    private TextView tvCostPerPax;
    private MaterialButton btnOpenCostDialog;

    private TextInputEditText etProfitPercent;
    private TextView tvProfitAbsolute;
    private MaterialButton btnCalculateSellingPrice;

    private TextInputEditText etGiaNguoiLon;
    private TextInputEditText etGiaTreEm;
    private TextInputEditText etGiaEmBe;
    private TextInputEditText etGiaNuocNgoai;

    private TextInputEditText etInclusions;
    private TextInputEditText etExclusions;

    public TaoTourChiPhiFragment(Tour tour) {
        this.tour = tour;
    }

    public TaoTourChiPhiFragment() {
        this(new Tour());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chi_phi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ⭐ ÁNH XẠ VIEWS TỪ LAYOUT XML (Giữ nguyên)
        tvTotalCostPrice = view.findViewById(R.id.tv_total_cost_price);
        tvCostPerPax = view.findViewById(R.id.tv_cost_per_pax);
        btnOpenCostDialog = view.findViewById(R.id.btn_open_cost_dialog);

        etProfitPercent = view.findViewById(R.id.et_profit_percent);
        tvProfitAbsolute = view.findViewById(R.id.tv_profit_absolute);
        btnCalculateSellingPrice = view.findViewById(R.id.btn_calculate_selling_price);

        etGiaNguoiLon = view.findViewById(R.id.et_gia_nguoi_lon);
        etGiaTreEm = view.findViewById(R.id.et_gia_tre_em);
        etGiaEmBe = view.findViewById(R.id.et_gia_em_be);
        etGiaNuocNgoai = view.findViewById(R.id.et_gia_nuoc_ngoai);

        etInclusions = view.findViewById(R.id.et_inclusions);
        etExclusions = view.findViewById(R.id.et_exclusions);

        // ⭐ ĐÃ THÊM: Thiết lập Listener cho các nút
        btnOpenCostDialog.setOnClickListener(v -> openCostDetailSheet());
        btnCalculateSellingPrice.setOnClickListener(v -> openProfitSheet());

        // TODO: Thêm TextWatcher cho etProfitPercent nếu muốn tính toán tự động khi thay đổi *trực tiếp*
        // Hiện tại, việc tính toán chính xảy ra khi mở Bottom Sheet Lợi nhuận.

        updateDisplay();
    }

    /**
     * ⭐ PHƯƠNG THỨC: Mở Bottom Sheet để TÍNH TOÁN GIÁ VỐN
     */
    private void openCostDetailSheet() {
        FragmentManager fm = getParentFragmentManager();
        if (fm == null) return;

        // ⭐ CÁCH MỞ BOTTOM SHEET CHUẨN: Dùng hàm tạo mặc định và setArguments
        Bundle args = new Bundle();
        args.putInt("currentPax", tour.getSoLuongKhachToiDa());
        // Nếu bạn đã lưu chi phí chi tiết, bạn cần putLongArray/putStringArray chi phí chi tiết vào đây

        BottomSheetTinhGiaVon sheet = new BottomSheetTinhGiaVon();
        sheet.setArguments(args);
        sheet.setOnCostUpdateListener(this); // ⭐ Thiết lập callback
        sheet.show(fm, sheet.getTag());
    }

    /**
     * ⭐ IMPLEMENT CALLBACK: Nhận kết quả từ BottomSheetTinhGiaVon
     */
    @Override
    public void onCostUpdated(long totalCost, int paxCount) {
        // Cập nhật đối tượng Tour với dữ liệu Giá vốn mới
        tour.setTongGiaVon(totalCost);
        tour.setSoLuongKhachToiDa(paxCount);

        // Tính toán Giá vốn/khách
        long costPerPax = (paxCount > 0) ? totalCost / paxCount : 0;
        tour.setGiaVonPerPax(costPerPax);

        Toast.makeText(getContext(), "Đã cập nhật Tổng giá vốn và Số lượng khách.", Toast.LENGTH_SHORT).show();
        updateDisplay(); // Cập nhật lại UI chính
    }


    /**
     * ⭐ PHƯƠNG THỨC: Mở Bottom Sheet để ÁP DỤNG LỢI NHUẬN
     */
    private void openProfitSheet() {
        // Yêu cầu phải có Giá vốn/khách > 0 trước khi tính lợi nhuận
        if (tour.getGiaVonPerPax() <= 0) {
            Toast.makeText(getContext(), "Vui lòng tính Giá vốn chi tiết trước.", Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentManager fm = getParentFragmentManager();
        if (fm == null) return;

        // ⭐ CÁCH MỞ BOTTOM SHEET CHUẨN: Dùng hàm tạo mặc định và setArguments
        Bundle args = new Bundle();
        args.putLong("costPerPax", tour.getGiaVonPerPax());
        args.putDouble("currentProfit", tour.getTySuatLoiNhuan());

        BottomSheetApDungLoiNhuan sheet = new BottomSheetApDungLoiNhuan();
        sheet.setArguments(args);
        sheet.setOnProfitUpdateListener(this); // ⭐ Thiết lập callback
        sheet.show(fm, sheet.getTag());
    }

    /**
     * ⭐ IMPLEMENT CALLBACK: Nhận kết quả từ BottomSheetApDungLoiNhuan
     */
    @Override
    public void onProfitUpdated(double profitMargin) {
        // Cập nhật tỷ suất lợi nhuận vào Tour object
        tour.setTySuatLoiNhuan(profitMargin);

        // TÍNH TOÁN LẠI GIÁ BÁN
        if (tour.getGiaVonPerPax() > 0 && profitMargin < 1.0) {
            // Giá bán = Giá vốn / (1 - Tỷ suất lợi nhuận)
            long giaBanMoi = (long) Math.round(tour.getGiaVonPerPax() / (1.0 - profitMargin));
            // Cập nhật giá bán Người Lớn đã tính toán tự động
            tour.setGiaNguoiLon(giaBanMoi);
        }

        Toast.makeText(getContext(), String.format("Đã áp dụng lợi nhuận %.1f%%. Giá người lớn được tính lại.", profitMargin * 100), Toast.LENGTH_LONG).show();
        updateDisplay(); // Cập nhật lại UI chính
    }


    private void updateDisplay() {
        if (tour == null) return;

        // --- Cập nhật Giá Vốn/Khách và Tổng Chi Phí ---
        long giaVonPerPax = tour.getGiaVonPerPax() > 0 ? tour.getGiaVonPerPax() : 0;
        long totalCost = tour.getTongGiaVon() > 0 ? tour.getTongGiaVon() : 0;
        int maxPax = tour.getSoLuongKhachToiDa() > 0 ? tour.getSoLuongKhachToiDa() : 0;

        tvTotalCostPrice.setText(String.format("%s VNĐ", formatLongOrZero(totalCost)));
        tvCostPerPax.setText(String.format("Giá vốn/khách dự kiến: %s VNĐ (SL: %d)",
                formatLongOrZero(giaVonPerPax),
                maxPax));

        // --- Cập nhật Lợi Nhuận và Giá Bán Dự Kiến ---
        double profitMargin = tour.getTySuatLoiNhuan() > 0 ? tour.getTySuatLoiNhuan() : 0.0;

        // Hiển thị lợi nhuận dưới dạng phần trăm (0.25 -> 25.0)
        etProfitPercent.setText(String.format(Locale.getDefault(), "%.1f", profitMargin * 100));

        // Lợi nhuận tuyệt đối = Giá bán NL - Giá vốn/khách
        long giaBanNguoiLon = tour.getGiaNguoiLon() > 0 ? tour.getGiaNguoiLon() : 0;
        long absoluteProfit = giaBanNguoiLon > giaVonPerPax ? (giaBanNguoiLon - giaVonPerPax) : 0;
        tvProfitAbsolute.setText(String.format("Lợi nhuận dự kiến: %s VNĐ / khách",
                formatLongOrZero(absoluteProfit)));

        // --- Cập nhật Giá Bán và Dịch Vụ ---
        etGiaNguoiLon.setText(formatLongOrEmpty(tour.getGiaNguoiLon()));
        etGiaTreEm.setText(formatLongOrEmpty(tour.getGiaTreEm()));
        etGiaEmBe.setText(formatLongOrEmpty(tour.getGiaEmBe()));

        // Giả sử etGiaNuocNgoai là giá không định dạng VNĐ (ví dụ: USD)
        if (tour.getGiaNuocNgoai() > 0) {
            etGiaNuocNgoai.setText(String.valueOf(tour.getGiaNuocNgoai()));
        } else {
            etGiaNuocNgoai.setText("");
        }

        etInclusions.setText(tour.getDichVuBaoGom());
        etExclusions.setText(tour.getDichVuKhongBaoGom());
    }

    private String formatLongOrEmpty(long value) {
        return value > 0 ? priceFormatter.format(value) : "";
    }

    private String formatLongOrZero(long value) {
        // Luôn hiển thị 0 nếu giá trị bằng 0
        return priceFormatter.format(value);
    }


    /**
     * ⭐ Thực hiện Thu thập và Validation dữ liệu của Bước 3.
     */
    @Override
    public boolean collectDataAndValidate(Tour tour) {

        // ⭐ Lấy dữ liệu từ các View thực tế
        String giaNLStr = Objects.requireNonNull(etGiaNguoiLon.getText()).toString().trim().replace(",", "").replace(".", "");
        String giaTEStr = Objects.requireNonNull(etGiaTreEm.getText()).toString().trim().replace(",", "").replace(".", "");
        String giaEBStr = Objects.requireNonNull(etGiaEmBe.getText()).toString().trim().replace(",", "").replace(".", "");
        String giaNNStr = Objects.requireNonNull(etGiaNuocNgoai.getText()).toString().trim();
        String inclusions = Objects.requireNonNull(etInclusions.getText()).toString().trim();
        String exclusions = Objects.requireNonNull(etExclusions.getText()).toString().trim();

        long giaNguoiLon = 0;
        long giaTreEm = 0;
        long giaEmBe = 0;
        double giaNuocNgoai = 0.0;

        try {
            if (!giaNLStr.isEmpty()) giaNguoiLon = Long.parseLong(giaNLStr);
            if (!giaTEStr.isEmpty()) giaTreEm = Long.parseLong(giaTEStr);
            if (!giaEBStr.isEmpty()) giaEmBe = Long.parseLong(giaEBStr);
            if (!giaNNStr.isEmpty()) giaNuocNgoai = Double.parseDouble(giaNNStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Giá bán nhập vào không hợp lệ.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 1. Validation Logic
        if (tour.getGiaVonPerPax() <= 0) {
            Toast.makeText(getContext(), "Vui lòng tính toán Giá vốn chi tiết (Bấm nút 'Chi tiết') trước.", Toast.LENGTH_LONG).show();
            return false;
        }

        if (giaNguoiLon <= tour.getGiaVonPerPax()) {
            Toast.makeText(getContext(), "Giá bán Người Lớn phải lớn hơn Giá vốn/khách dự kiến.", Toast.LENGTH_SHORT).show();
            etGiaNguoiLon.setError("Giá bán phải > Giá vốn");
            return false;
        }

        if (inclusions.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng liệt kê dịch vụ bao gồm.", Toast.LENGTH_SHORT).show();
            etInclusions.setError("Không được để trống");
            return false;
        }

        // 2. Gán dữ liệu
        tour.setGiaNguoiLon(giaNguoiLon);
        tour.setGiaTreEm(giaTreEm);
        tour.setGiaEmBe(giaEmBe);
        tour.setGiaNuocNgoai(giaNuocNgoai);
        tour.setDichVuBaoGom(inclusions);
        tour.setDichVuKhongBaoGom(exclusions);

        // ⭐ QUAN TRỌNG: Cập nhật lại Tỷ suất lợi nhuận nếu người dùng chỉnh tay Giá người lớn
        if (giaNguoiLon > 0 && tour.getGiaVonPerPax() > 0) {
            double newProfitMargin = (double) (giaNguoiLon - tour.getGiaVonPerPax()) / giaNguoiLon;
            tour.setTySuatLoiNhuan(newProfitMargin);
        }


        // Các trường tính toán (TongGiaVon, GiaVonPerPax, TySuatLoiNhuan) đã được cập nhật qua callbacks.

        Log.d(TAG, "Bước 3 - Chi phí: Thu thập thành công.");
        return true;
    }
}