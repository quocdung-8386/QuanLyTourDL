package com.example.quanlytourdl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Tour;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat; // ⭐ ĐÃ THÊM IMPORT
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditTourFragment extends Fragment {

    private static final String TAG = "EditTourFragment";
    private static final String ARG_TOUR_ID = "tour_id";
    private String tourId;

    // UI Components
    private Toolbar toolbar;
    private TextInputEditText etTenTour, etDiemDen, etMaTour, etSoNgay, etSoDem, etMoTa;
    private AutoCompleteTextView atvLoaiTour, atvStatus;
    private AutoCompleteTextView atvDiemKhoiHanh;
    private TextInputEditText etSoLuongKhachToiDa;

    // Giá và chi phí
    private TextInputEditText etGiaNguoiLon, etGiaTreEm, etGiaEmBe, etGiaNuocNgoai;
    private TextInputEditText etTongGiaVon, etGiaVonPerPax, etTySuatLoiNhuan;

    // Lịch trình & Dịch vụ
    private Button btnChinhSuaLichTrinh, btnSaveTour;
    private TextInputEditText etDichVuBaoGom, etDichVuKhongBaoGom;

    // Phân công Tài nguyên
    private AutoCompleteTextView atvAssignedGuide, atvAssignedVehicle;

    // SEO & Khác
    private TextInputEditText etMoTaSeo;
    private Button btnQuanLyHinhAnh;
    private SwitchMaterial switchXuatBan, switchNoiBat;

    private FirebaseFirestore db;
    private Tour currentTour;

    // ⭐ FORMATTER ĐỂ HIỂN THỊ TIỀN TỆ THEO DẠNG HÀNG NGHÌN (ví dụ: 1.000.000)
    private final java.text.DecimalFormat currencyDisplayFormatter =
            new java.text.DecimalFormat("#,###", new java.text.DecimalFormatSymbols(new Locale("vi", "VN")));

    public static EditTourFragment newInstance(String tourId) {
        EditTourFragment fragment = new EditTourFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOUR_ID, tourId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tourId = getArguments().getString(ARG_TOUR_ID);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_tour, container, false);

        // 1. Ánh xạ View
        mapViews(view);

        // 2. Thiết lập Toolbar và Nút Quay lại
        setupToolbar();

        // 3. Thiết lập Dropdown (Trạng thái, Loại Tour)
        setupDropdowns();

        // 4. Tải dữ liệu và điền vào form (nếu có tourId)
        if (tourId != null) {
            loadTourDataAndBind(tourId);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ID Tour để chỉnh sửa.", Toast.LENGTH_LONG).show();
            if (getActivity() != null) getActivity().onBackPressed();
        }

        // 5. Xử lý sự kiện Lưu
        btnSaveTour.setOnClickListener(v -> handleSaveTour());

        // 6. Xử lý sự kiện cho các nút mở Fragment/Activity chi tiết
        btnChinhSuaLichTrinh.setOnClickListener(v -> Toast.makeText(getContext(), "Mở màn hình chỉnh sửa Lịch trình chi tiết", Toast.LENGTH_SHORT).show());
        btnQuanLyHinhAnh.setOnClickListener(v -> Toast.makeText(getContext(), "Mở màn hình Quản lý Hình ảnh & SEO", Toast.LENGTH_SHORT).show());


        return view;
    }

    private void mapViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_edit_tour);
        btnSaveTour = view.findViewById(R.id.btn_save_tour);

        // 1. Thông tin cơ bản
        etMaTour = view.findViewById(R.id.et_ma_tour);
        etTenTour = view.findViewById(R.id.et_ten_tour);
        atvLoaiTour = view.findViewById(R.id.atv_loai_tour);
        atvDiemKhoiHanh = view.findViewById(R.id.atv_diem_khoi_hanh);
        etDiemDen = view.findViewById(R.id.et_diem_den);
        etMoTa = view.findViewById(R.id.et_mo_ta);

        // 2. Thời gian & Khách
        etSoNgay = view.findViewById(R.id.et_so_ngay);
        etSoDem = view.findViewById(R.id.et_so_dem);
        etSoLuongKhachToiDa = view.findViewById(R.id.et_so_luong_khach_toi_da);

        // 3. Giá và chi phí
        etGiaNguoiLon = view.findViewById(R.id.et_gia_nguoi_lon);
        etGiaTreEm = view.findViewById(R.id.et_gia_tre_em);
        etGiaEmBe = view.findViewById(R.id.et_gia_em_be);
        etGiaNuocNgoai = view.findViewById(R.id.et_gia_nuoc_ngoai);
        etTongGiaVon = view.findViewById(R.id.et_tong_gia_von);
        etGiaVonPerPax = view.findViewById(R.id.et_gia_von_per_pax);
        etTySuatLoiNhuan = view.findViewById(R.id.et_ty_suat_loi_nhuan);

        // 4. Lịch trình & Dịch vụ
        btnChinhSuaLichTrinh = view.findViewById(R.id.btn_chinh_sua_lich_trinh);
        etDichVuBaoGom = view.findViewById(R.id.et_dich_vu_bao_gom);
        etDichVuKhongBaoGom = view.findViewById(R.id.et_dich_vu_khong_bao_gom);

        // 5. Phân công Tài nguyên & Trạng thái
        atvAssignedGuide = view.findViewById(R.id.atv_assigned_guide);
        atvAssignedVehicle = view.findViewById(R.id.atv_assigned_vehicle);
        atvStatus = view.findViewById(R.id.atv_status);
        switchXuatBan = view.findViewById(R.id.switch_xuat_ban);
        switchNoiBat = view.findViewById(R.id.switch_noi_bat);

        // 6. SEO & Hình ảnh
        btnQuanLyHinhAnh = view.findViewById(R.id.btn_quan_ly_hinh_anh);
        etMoTaSeo = view.findViewById(R.id.et_mo_ta_seo);
    }

    private void setupToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupDropdowns() {
        // Cập nhật Dropdown Trạng thái theo yêu cầu: DANG_MO_BAN, HET_HAN và các trạng thái khác
        String[] statusOptions = new String[]{
                "DANG_MO_BAN",
                "HET_HAN",
                "NHAP",
                "CHO_PHE_DUYET",
                "DANG_CHO_PHAN_CONG",
                "DA_GAN_NHAN_VIEN"
        };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                statusOptions
        );
        atvStatus.setAdapter(statusAdapter);

        // Thiết lập Dropdown Loại Tour (ví dụ)
        String[] loaiTourOptions = new String[]{"Tour Khám phá (Adventure)", "Tour Nghỉ dưỡng", "Tour Tham quan"};
        ArrayAdapter<String> loaiTourAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                loaiTourOptions
        );
        atvLoaiTour.setAdapter(loaiTourAdapter);

        // Dropdown Điểm khởi hành (Ví dụ)
        String[] diemKhoiHanhOptions = new String[]{"Hà Nội", "TP Hồ Chí Minh", "Đà Nẵng", "Cần Thơ"};
        ArrayAdapter<String> diemKhoiHanhAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                diemKhoiHanhOptions
        );
        atvDiemKhoiHanh.setAdapter(diemKhoiHanhAdapter);

        // Dropdown cho HDV và Phương tiện (Cần tải dữ liệu thật từ Firestore/API)
        // Hiện tại chỉ để trống hoặc dùng dữ liệu giả
        atvAssignedGuide.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new String[]{"Chưa phân công"}));
        atvAssignedVehicle.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new String[]{"Chưa phân công"}));
    }

    private void loadTourDataAndBind(String id) {
        db.collection("Tours").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentTour = documentSnapshot.toObject(Tour.class);
                        if (currentTour != null) {
                            currentTour.setMaTour(documentSnapshot.getId());
                            bindDataToForm(currentTour);
                        } else {
                            Toast.makeText(getContext(), "Lỗi mapping dữ liệu Tour.", Toast.LENGTH_LONG).show();
                            if (getActivity() != null) getActivity().onBackPressed();
                        }
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy Tour: " + id, Toast.LENGTH_LONG).show();
                        if (getActivity() != null) getActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải dữ liệu Tour", e);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if (getActivity() != null) getActivity().onBackPressed();
                });
    }

    private void bindDataToForm(Tour tour) {
        if (tour == null) return;

        // Định dạng Double dùng 2 chữ số thập phân (cho Giá nước ngoài và Tỷ suất LN)
        String doubleToStringFormat = "%.2f";

        // 1. Thông tin cơ bản
        etTenTour.setText(tour.getTenTour());
        atvLoaiTour.setText(tour.getLoaiTour(), false);
        atvDiemKhoiHanh.setText(tour.getDiemKhoiHanh(), false);
        etDiemDen.setText(tour.getDiemDen());
        etMaTour.setText(tour.getMaTour());
        etMoTa.setText(tour.getMoTa());
        etSoNgay.setText(String.valueOf(tour.getSoNgay()));
        etSoDem.setText(String.valueOf(tour.getSoDem()));
        etSoLuongKhachToiDa.setText(String.valueOf(tour.getSoLuongKhachToiDa()));

        // 2. Giá và chi phí
        // ⭐ SỬ DỤNG FORMATTER MỚI CHO HIỂN THỊ TIỀN TỆ (có dấu phân cách hàng nghìn)
        etGiaNguoiLon.setText(currencyDisplayFormatter.format(tour.getGiaNguoiLon()));
        etGiaTreEm.setText(currencyDisplayFormatter.format(tour.getGiaTreEm()));
        etGiaEmBe.setText(currencyDisplayFormatter.format(tour.getGiaEmBe()));

        // Giá nước ngoài và Tỷ suất lợi nhuận dùng double
        etGiaNuocNgoai.setText(String.format(Locale.getDefault(), doubleToStringFormat, tour.getGiaNuocNgoai()));
        etTongGiaVon.setText(currencyDisplayFormatter.format(tour.getTongGiaVon()));
        etGiaVonPerPax.setText(currencyDisplayFormatter.format(tour.getGiaVonPerPax()));
        etTySuatLoiNhuan.setText(String.format(Locale.getDefault(), doubleToStringFormat, tour.getTySuatLoiNhuan()));

        // 3. Dịch vụ
        etDichVuBaoGom.setText(tour.getDichVuBaoGom());
        etDichVuKhongBaoGom.setText(tour.getDichVuKhongBaoGom());

        // 4. Phân công
        atvAssignedGuide.setText(tour.getAssignedGuideName() != null ? tour.getAssignedGuideName() : "Chưa phân công", false);
        atvAssignedVehicle.setText(tour.getAssignedVehicleLicensePlate() != null ? tour.getAssignedVehicleLicensePlate() : "Chưa phân công", false);

        // 5. Trạng thái và SEO (Đã sử dụng Getter mới)
        atvStatus.setText(tour.getStatus(), false);
        switchXuatBan.setChecked(tour.getIsXuatBan());
        switchNoiBat.setChecked(tour.getIsNoiBat());
        etMoTaSeo.setText(tour.getMoTaSeo());
    }

    private void handleSaveTour() {
        if (tourId == null) return;

        // 1. Thu thập và kiểm tra dữ liệu bắt buộc
        String tenTour = etTenTour.getText() != null ? etTenTour.getText().toString() : "";
        String diemDen = etDiemDen.getText() != null ? etDiemDen.getText().toString() : "";
        String loaiTour = atvLoaiTour.getText() != null ? atvLoaiTour.getText().toString() : "";
        String diemKhoiHanh = atvDiemKhoiHanh.getText() != null ? atvDiemKhoiHanh.getText().toString() : "";

        if (tenTour.isEmpty() || diemDen.isEmpty() || loaiTour.isEmpty() || diemKhoiHanh.isEmpty()) {
            Toast.makeText(getContext(), "Tên Tour, Loại Tour, Điểm Khởi Hành và Điểm đến không được để trống.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Chuẩn bị Map để cập nhật Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("tenTour", tenTour);
        updates.put("diemDen", diemDen);
        updates.put("loaiTour", loaiTour);
        updates.put("diemKhoiHanh", diemKhoiHanh);
        updates.put("moTa", etMoTa.getText() != null ? etMoTa.getText().toString() : "");
        updates.put("dichVuBaoGom", etDichVuBaoGom.getText() != null ? etDichVuBaoGom.getText().toString() : "");
        updates.put("dichVuKhongBaoGom", etDichVuKhongBaoGom.getText() != null ? etDichVuKhongBaoGom.getText().toString() : "");
        updates.put("moTaSeo", etMoTaSeo.getText() != null ? etMoTaSeo.getText().toString() : "");
        updates.put("status", atvStatus.getText() != null ? atvStatus.getText().toString() : "");
        updates.put("isXuatBan", switchXuatBan.isChecked());
        updates.put("isNoiBat", switchNoiBat.isChecked());

        // LƯU Ý: Nếu chưa phân công (default), giá trị sẽ là null.
        // Logic phía sau cần đảm bảo assignedGuideId/assignedVehicleId được cập nhật
        updates.put("assignedGuideName", atvAssignedGuide.getText() != null ? atvAssignedGuide.getText().toString() : null);
        updates.put("assignedVehicleLicensePlate", atvAssignedVehicle.getText() != null ? atvAssignedVehicle.getText().toString() : null);

        // 3. Xử lý các trường số
        try {
            updates.put("soNgay", Integer.parseInt(etSoNgay.getText() != null ? etSoNgay.getText().toString() : "0"));
            updates.put("soDem", Integer.parseInt(etSoDem.getText() != null ? etSoDem.getText().toString() : "0"));
            updates.put("soLuongKhachToiDa", Integer.parseInt(etSoLuongKhachToiDa.getText() != null ? etSoLuongKhachToiDa.getText().toString() : "0"));

            // Chuyển sang long cho các trường giá VNĐ (parseAndClean sẽ loại bỏ dấu chấm/phẩy)
            updates.put("giaNguoiLon", parseAndClean(etGiaNguoiLon.getText() != null ? etGiaNguoiLon.getText().toString() : "0"));
            updates.put("giaTreEm", parseAndClean(etGiaTreEm.getText() != null ? etGiaTreEm.getText().toString() : "0"));
            updates.put("giaEmBe", parseAndClean(etGiaEmBe.getText() != null ? etGiaEmBe.getText().toString() : "0"));
            updates.put("tongGiaVon", parseAndClean(etTongGiaVon.getText() != null ? etTongGiaVon.getText().toString() : "0"));
            updates.put("giaVonPerPax", parseAndClean(etGiaVonPerPax.getText() != null ? etGiaVonPerPax.getText().toString() : "0"));

            // Chuyển sang double cho Giá nước ngoài và Tỷ suất LN
            // (Chấp nhận dấu chấm hoặc dấu phẩy là thập phân, sau đó chuẩn hóa thành chấm)
            updates.put("giaNuocNgoai", Double.parseDouble(
                    (etGiaNuocNgoai.getText() != null ? etGiaNuocNgoai.getText().toString() : "0")
                            .replace(",", ".").trim()
            ));
            updates.put("tySuatLoiNhuan", Double.parseDouble(
                    (etTySuatLoiNhuan.getText() != null ? etTySuatLoiNhuan.getText().toString() : "0")
                            .replace(",", ".").trim()
            ));


        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Lỗi định dạng số: Vui lòng kiểm tra lại tất cả các trường số và giá.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Lỗi parsing số:", e);
            return;
        }

        // 4. Thực hiện cập nhật lên Firestore
        db.collection("Tours").document(tourId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật Tour thành công!", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        // Trở về màn hình trước sau khi lưu
                        getActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi cập nhật Tour", e);
                    Toast.makeText(getContext(), "Lỗi cập nhật Tour: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private long parseAndClean(String s) throws NumberFormatException {
        if (s == null || s.trim().isEmpty()) return 0;
        // Loại bỏ tất cả dấu chấm và dấu phẩy, giả định chúng là dấu phân cách hàng nghìn.
        String cleanString = s.trim().replaceAll("[.,]", "");
        if (cleanString.isEmpty()) return 0;
        return Long.parseLong(cleanString);
    }
}