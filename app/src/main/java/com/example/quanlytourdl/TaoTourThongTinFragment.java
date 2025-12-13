package com.example.quanlytourdl;

import android.app.DatePickerDialog; // ⭐ ĐÃ THÊM: Import DatePickerDialog
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quanlytourdl.model.Tour;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar; // ⭐ ĐÃ THÊM: Import Calendar
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TaoTourThongTinFragment extends Fragment implements TaoTourDetailFullFragment.TourStepDataCollector {

    private static final String TAG = "TaoTourThongTinFragment";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // ⭐ ĐÃ THÊM: Danh sách các Loại Tour có sẵn
    private static final String[] LOAI_TOUR_OPTIONS = new String[] {
            "Tour Trong nước",
            "Tour Du lịch Bền vững",
            "Tour Tự túc (Free & Easy)",
            "Tour Khám phá (Adventure)"
    };

    // ⭐ ĐÃ THÊM: Danh sách ví dụ Điểm Khởi Hành/Điểm Đến
    private static final String[] DIEM_KHOI_HANH_OPTIONS = new String[] {"TP. Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Cần Thơ"};
    private static final String[] DIEM_DEN_OPTIONS = new String[] {"Hà Nội", "Đà Nẵng", "Phú Quốc", "Singapore", "Thái Lan", "Hà Giang"};

    // ⭐ Đối tượng Tour được chia sẻ từ Fragment cha
    private final Tour tour;

    // ⭐ THÀNH PHẦN UI THỰC TẾ (Khớp với layout XML)
    private TextInputEditText etTenTour;
    private AutoCompleteTextView actvLoaiTour;
    private TextInputEditText etNgayBatDau;
    private TextInputEditText etNgayKetThuc;
    private AutoCompleteTextView actvDiemKhoiHanh;
    private AutoCompleteTextView actvDiemDen;
    private TextInputEditText etSoLuongToiDa;
    private TextInputEditText etMoTa;
    private TextView tvThoiLuong;
    private TextInputEditText etMaTour;

    // Biến lưu trữ mã tour ngẫu nhiên (chỉ được tạo một lần)
    private String generatedTourCode = null;


    // Bắt buộc phải có constructor nhận đối tượng Tour
    public TaoTourThongTinFragment(Tour tour) {
        this.tour = tour;
    }

    // Constructor rỗng
    public TaoTourThongTinFragment() {
        this(new Tour()); // Khởi tạo Tour mặc định
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_tin_co_ban, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ⭐ ÁNH XẠ VIEWS VÀ KHẮC PHỤC LỖI ID
        etTenTour = view.findViewById(R.id.et_ten_tour);
        etMaTour = view.findViewById(R.id.et_ma_tour);
        actvLoaiTour = view.findViewById(R.id.actv_loai_tour);
        etNgayBatDau = view.findViewById(R.id.et_ngay_bat_dau);
        etNgayKetThuc = view.findViewById(R.id.et_ngay_ket_thuc);
        tvThoiLuong = view.findViewById(R.id.tv_thoi_luong);
        actvDiemKhoiHanh = view.findViewById(R.id.actv_diem_khoi_hanh);
        actvDiemDen = view.findViewById(R.id.actv_diem_den);
        etSoLuongToiDa = view.findViewById(R.id.et_so_luong_toi_da);
        etMoTa = view.findViewById(R.id.et_mo_ta);

        // ⭐ Thiết lập Adapter cho các trường AutoCompleteTextView
        setupAdapters();

        // ⭐ ĐÃ THÊM: Thiết lập Listener cho etNgayBatDau, etNgayKetThuc để mở DatePicker
        etNgayBatDau.setOnClickListener(v -> showDatePickerDialog(etNgayBatDau));
        etNgayKetThuc.setOnClickListener(v -> showDatePickerDialog(etNgayKetThuc));

        // Ngăn bàn phím hiện lên khi click vào trường ngày
        etNgayBatDau.setKeyListener(null);
        etNgayKetThuc.setKeyListener(null);


        // Cập nhật giao diện với dữ liệu hiện có (nếu là chỉnh sửa)
        updateDisplay();

        // Ngăn không cho người dùng sửa Mã Tour
        if (etMaTour != null) {
            etMaTour.setKeyListener(null);
            etMaTour.setFocusable(false);
            etMaTour.setClickable(false);
        }
    }

    /**
     * ⭐ ĐÃ THÊM: Phương thức hiển thị DatePickerDialog
     */
    private void showDatePickerDialog(final TextInputEditText editText) {
        if (getContext() == null) return;
        final Calendar calendar = Calendar.getInstance();

        // Thử parse ngày hiện có trong EditText (nếu có)
        try {
            String currentText = editText.getText().toString();
            if (!currentText.isEmpty()) {
                Date currentDate = dateFormat.parse(currentText);
                if (currentDate != null) {
                    calendar.setTime(currentDate);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi phân tích ngày hiện tại: ", e);
            // Mặc định sử dụng ngày hiện tại nếu parse thất bại
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Cập nhật Calendar với ngày mới
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    Date selectedDate = calendar.getTime();

                    // Hiển thị ngày đã chọn
                    editText.setText(dateFormat.format(selectedDate));

                    // Cập nhật thời lượng sau khi cả hai trường ngày được điền
                    Date start = null;
                    Date end = null;
                    try {
                        if (etNgayBatDau.getText() != null && !etNgayBatDau.getText().toString().isEmpty()) {
                            start = dateFormat.parse(etNgayBatDau.getText().toString());
                        }
                        if (etNgayKetThuc.getText() != null && !etNgayKetThuc.getText().toString().isEmpty()) {
                            end = dateFormat.parse(etNgayKetThuc.getText().toString());
                        }
                    } catch (Exception ignored) {
                        // Bỏ qua lỗi parse
                    }
                    updateThoiLuongDisplay(start, end);

                }, year, month, day);

        datePickerDialog.show();
    }


    /**
     * ⭐ ĐÃ THÊM: Thiết lập ArrayAdapter cho các AutoCompleteTextView (Loại Tour, Điểm Khởi hành/Đến)
     */
    private void setupAdapters() {
        if (getContext() == null) return;

        // 1. Loại Tour
        ArrayAdapter<String> loaiTourAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line, // Layout mặc định cho dropdown
                LOAI_TOUR_OPTIONS
        );
        actvLoaiTour.setAdapter(loaiTourAdapter);

        // 2. Điểm Khởi Hành
        ArrayAdapter<String> diemKhoiHanhAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                DIEM_KHOI_HANH_OPTIONS
        );
        actvDiemKhoiHanh.setAdapter(diemKhoiHanhAdapter);

        // 3. Điểm Đến
        ArrayAdapter<String> diemDenAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                DIEM_DEN_OPTIONS
        );
        actvDiemDen.setAdapter(diemDenAdapter);
    }

    /**
     * ⭐ ĐÃ SỬA: Cập nhật logic hiển thị Mã Tour để buộc hiển thị MT-XXXX nếu Mã cũ không hợp lệ (UUID)
     */
    private void updateDisplay() {
        if (tour != null) {
            etTenTour.setText(tour.getTenTour());

            // ⭐ LOGIC MỚI Xử lý Mã Tour tự động theo định dạng MT-XXXX
            String currentMaTour = tour.getMaTour() != null ? tour.getMaTour() : "";

            // Điều kiện để tạo mã mới:
            // 1. Mã tour rỗng (Tour mới hoàn toàn)
            // 2. Mã tour có định dạng không mong muốn (quá dài như UUID, hoặc không bắt đầu bằng "MT-")
            if (currentMaTour.isEmpty() || currentMaTour.length() > 10 || !currentMaTour.startsWith("MT-")) {

                // Đây là Tour mới, hoặc Tour cũ có mã không hợp lệ -> Tạo mã ngẫu nhiên
                if (generatedTourCode == null) {
                    generatedTourCode = generateTourCode();
                }
                etMaTour.setText(generatedTourCode);

            } else {
                // Đây là Tour đã có mã MT-XXXX hợp lệ (trường hợp chỉnh sửa)
                etMaTour.setText(currentMaTour);
            }
            // HẾT LOGIC MỚI

            // ⭐ Gán giá trị với 'false' để không tự động mở dropdown khi gán dữ liệu cũ
            actvLoaiTour.setText(tour.getLoaiTour(), false);
            actvDiemKhoiHanh.setText(tour.getDiemKhoiHanh(), false);
            actvDiemDen.setText(tour.getDiemDen(), false);

            etMoTa.setText(tour.getMoTa());

            if (tour.getSoLuongKhachToiDa() > 0) {
                etSoLuongToiDa.setText(String.valueOf(tour.getSoLuongKhachToiDa()));
            }

            // Hiển thị Ngày và Thời lượng
            if (tour.getNgayKhoiHanh() != null) {
                etNgayBatDau.setText(dateFormat.format(tour.getNgayKhoiHanh()));
            }
            if (tour.getNgayKetThuc() != null) {
                etNgayKetThuc.setText(dateFormat.format(tour.getNgayKetThuc()));
            }
            updateThoiLuongDisplay(tour.getNgayKhoiHanh(), tour.getNgayKetThuc());
        }
    }

    /**
     * ⭐ PHƯƠNG THỨC TẠO MÃ TOUR NGẪU NHIÊN: Định dạng MT-XXXX
     */
    private String generateTourCode() {
        Random random = new Random();
        int randomNumber = random.nextInt(10000); // Tạo số ngẫu nhiên từ 0 đến 9999

        // Sử dụng String.format để đảm bảo luôn có 4 chữ số, VD: 0045, 1234
        String randomDigits = String.format(Locale.getDefault(), "%04d", randomNumber);

        return "MT-" + randomDigits;
    }

    private void updateThoiLuongDisplay(Date start, Date end) {
        if (start != null && end != null && !end.before(start)) {
            long diff = end.getTime() - start.getTime();
            int soNgay = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
            int soDem = soNgay - 1;
            tvThoiLuong.setText(String.format("Thời lượng dự kiến: %d ngày %d đêm", soNgay, soDem));
        } else {
            tvThoiLuong.setText("Thời lượng dự kiến: 0 ngày 0 đêm");
        }
    }


    /**
     * ⭐ Thực hiện Thu thập và Validation dữ liệu của Bước 1.
     */
    @Override
    public boolean collectDataAndValidate(Tour tour) {
        // Đặt lại lỗi thành null/clear trước khi validation mới
        if (etTenTour != null) etTenTour.setError(null);
        if (actvLoaiTour != null) actvLoaiTour.setError(null);
        if (actvDiemKhoiHanh != null) actvDiemKhoiHanh.setError(null);
        if (actvDiemDen != null) actvDiemDen.setError(null);
        if (etNgayKetThuc != null) etNgayKetThuc.setError(null);
        if (etSoLuongToiDa != null) etSoLuongToiDa.setError(null);


        String tenTour = etTenTour.getText() != null ? etTenTour.getText().toString().trim() : "";
        String loaiTour = actvLoaiTour.getText() != null ? actvLoaiTour.getText().toString().trim() : "";
        String diemKhoiHanh = actvDiemKhoiHanh.getText() != null ? actvDiemKhoiHanh.getText().toString().trim() : "";
        String diemDen = actvDiemDen.getText() != null ? actvDiemDen.getText().toString().trim() : "";
        String moTa = etMoTa.getText() != null ? etMoTa.getText().toString().trim() : "";
        String soLuongToiDaStr = etSoLuongToiDa.getText() != null ? etSoLuongToiDa.getText().toString().trim() : "";

        Date ngayKhoiHanh = null;
        Date ngayKetThuc = null;

        // Xử lý Ngày
        try {
            if (etNgayBatDau.getText() != null && !etNgayBatDau.getText().toString().isEmpty()) {
                ngayKhoiHanh = dateFormat.parse(etNgayBatDau.getText().toString());
            }
            if (etNgayKetThuc.getText() != null && !etNgayKetThuc.getText().toString().isEmpty()) {
                ngayKetThuc = dateFormat.parse(etNgayKetThuc.getText().toString());
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi định dạng ngày tháng. Vui lòng kiểm tra lại.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Lỗi phân tích ngày tháng", e);
            return false;
        }

        // 1. Validation
        if (tenTour.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập Tên Tour.", Toast.LENGTH_SHORT).show();
            etTenTour.setError("Không được để trống");
            return false;
        }
        if (loaiTour.isEmpty() || loaiTour.equals("Chọn loại tour")) {
            Toast.makeText(getContext(), "Vui lòng chọn Loại Tour.", Toast.LENGTH_SHORT).show();
            actvLoaiTour.setError("Không được để trống");
            return false;
        }
        if (diemKhoiHanh.isEmpty() || diemDen.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ Điểm Khởi Hành và Điểm Đến.", Toast.LENGTH_SHORT).show();
            if (diemKhoiHanh.isEmpty()) actvDiemKhoiHanh.setError("Không được để trống");
            if (diemDen.isEmpty()) actvDiemDen.setError("Không được để trống");
            return false;
        }
        if (ngayKhoiHanh == null || ngayKetThuc == null) {
            Toast.makeText(getContext(), "Vui lòng chọn Ngày Khởi Hành và Ngày Kết Thúc.", Toast.LENGTH_SHORT).show();
            // Không setError cụ thể vì nó là 2 trường khác nhau
            return false;
        }
        if (ngayKetThuc.before(ngayKhoiHanh)) {
            Toast.makeText(getContext(), "Ngày kết thúc không thể trước ngày khởi hành.", Toast.LENGTH_SHORT).show();
            etNgayKetThuc.setError("Ngày không hợp lệ");
            return false;
        }

        int soLuongToiDa;
        try {
            if (soLuongToiDaStr.isEmpty()) {
                Toast.makeText(getContext(), "Số lượng khách tối đa không được để trống.", Toast.LENGTH_SHORT).show();
                etSoLuongToiDa.setError("Không được để trống");
                return false;
            }
            soLuongToiDa = Integer.parseInt(soLuongToiDaStr);
            if (soLuongToiDa <= 0) {
                Toast.makeText(getContext(), "Số lượng khách tối đa phải lớn hơn 0.", Toast.LENGTH_SHORT).show();
                etSoLuongToiDa.setError("Phải > 0");
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số lượng khách tối đa phải là số nguyên.", Toast.LENGTH_SHORT).show();
            etSoLuongToiDa.setError("Phải là số");
            return false;
        }

        // 2. Gán dữ liệu vào đối tượng Tour

        // ⭐ Gán Mã Tour TỰ ĐỘNG nếu đây là Tour mới (Logic giữ nguyên)
        if (tour.getMaTour() == null || tour.getMaTour().isEmpty() || tour.getMaTour().length() > 10) {
            // Sử dụng mã đã tạo trong updateDisplay
            if (generatedTourCode != null) {
                tour.setMaTour(generatedTourCode);
            } else {
                // Trường hợp an toàn (nếu collectDataAndValidate được gọi trước updateDisplay)
                tour.setMaTour(generateTourCode());
            }
        }

        tour.setTenTour(tenTour);
        tour.setLoaiTour(loaiTour);
        tour.setDiemKhoiHanh(diemKhoiHanh);
        tour.setDiemDen(diemDen);
        tour.setMoTa(moTa);
        tour.setNgayKhoiHanh(ngayKhoiHanh);
        tour.setNgayKetThuc(ngayKetThuc);
        tour.setSoLuongKhachToiDa(soLuongToiDa);

        // Tính toán tự động
        long diff = ngayKetThuc.getTime() - ngayKhoiHanh.getTime();
        int soNgay = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
        int soDem = soNgay - 1;
        tour.setSoNgay(soNgay);
        tour.setSoDem(soDem);

        updateThoiLuongDisplay(ngayKhoiHanh, ngayKetThuc);
        Log.d(TAG, "Bước 1 - Thông tin: Thu thập thành công.");
        return true;
    }
}