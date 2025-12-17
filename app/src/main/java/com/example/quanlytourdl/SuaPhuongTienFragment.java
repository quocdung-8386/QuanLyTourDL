package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quanlytourdl.model.Vehicle;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SuaPhuongTienFragment extends Fragment {

    private static final String TAG = "SuaPhuongTienFragment";
    // ⭐ ĐÃ SỬA: Dựa trên hình ảnh Firestore, tên collection là "phuongtien"
    private static final String COLLECTION_VEHICLES = "phuongtien";

    // UI Components
    private ImageButton btnCloseDialog, btnAssignDriver;
    private TextInputEditText etBienSoXe, etHangXe, etSoChoNgoi;
    private AutoCompleteTextView actvLoaiPhuongTien, actvTinhTrangBaoDuong;
    private TextView tvVehicleIdStatic, tvDriverName;
    private MaterialButton btnResetPt, btnSavePt;

    // Data
    private FirebaseFirestore db;
    private String vehicleId;
    private Vehicle currentVehicle; // Lưu trữ đối tượng Phương tiện hiện tại

    // Danh sách mẫu cho Dropdown
    private static final String[] LOAI_PT = {"Xe 4 chỗ", "Xe 7 chỗ", "Xe 16 chỗ", "Xe 29 chỗ", "Xe 45 chỗ"};
    private static final String[] TINH_TRANG = {"Hoạt động tốt", "Cần bảo trì nhỏ", "Đang bảo dưỡng", "Cần sửa chữa lớn"};


    public SuaPhuongTienFragment() {
        // Required empty public constructor
    }

    // Phương thức để tạo Fragment và truyền ID qua Bundle
    public static SuaPhuongTienFragment newInstance(String vehicleId) {
        SuaPhuongTienFragment fragment = new SuaPhuongTienFragment();
        Bundle args = new Bundle();
        args.putString("vehicle_id", vehicleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            vehicleId = getArguments().getString("vehicle_id");
            Log.d(TAG, "Đang chỉnh sửa Phương tiện với ID: " + vehicleId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sua_phuong_tien_dialog, container, false);
        mapViews(view);
        setupDropdowns();
        setupListeners();

        if (vehicleId != null) {
            loadVehicleData(vehicleId);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID Phương tiện.", Toast.LENGTH_LONG).show();
            closeFragment();
        }

        return view;
    }

    private void mapViews(View view) {
        btnCloseDialog = view.findViewById(R.id.btn_close_dialog);

        // Thông tin cơ bản
        tvVehicleIdStatic = view.findViewById(R.id.tv_vehicle_id_static);
        etBienSoXe = view.findViewById(R.id.et_bien_so_xe);
        actvLoaiPhuongTien = view.findViewById(R.id.actv_loai_phuong_tien);
        etHangXe = view.findViewById(R.id.et_hang_xe);
        etSoChoNgoi = view.findViewById(R.id.et_so_cho_ngoi);

        // Tình trạng & Tài xế
        actvTinhTrangBaoDuong = view.findViewById(R.id.actv_tinh_trang_bao_duong);
        tvDriverName = view.findViewById(R.id.tv_driver_name);
        btnAssignDriver = view.findViewById(R.id.btn_assign_driver);

        // Nút hành động
        btnResetPt = view.findViewById(R.id.btn_reset_pt);
        btnSavePt = view.findViewById(R.id.btn_save_pt);
    }

    private void setupDropdowns() {
        // Thiết lập Adapter cho Loại Phương tiện
        ArrayAdapter<String> adapterLoaiPT = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                LOAI_PT
        );
        actvLoaiPhuongTien.setAdapter(adapterLoaiPT);

        // Thiết lập Adapter cho Tình trạng Bảo dưỡng
        ArrayAdapter<String> adapterTinhTrang = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                TINH_TRANG
        );
        actvTinhTrangBaoDuong.setAdapter(adapterTinhTrang);
    }

    private void setupListeners() {
        // Nút đóng Fragment
        btnCloseDialog.setOnClickListener(v -> closeFragment());

        // Nút Reset
        btnResetPt.setOnClickListener(v -> {
            if (currentVehicle != null) {
                displayVehicleData(currentVehicle); // Load lại dữ liệu gốc
                Toast.makeText(getContext(), "Đã đặt lại dữ liệu gốc.", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Lưu
        btnSavePt.setOnClickListener(v -> validateAndSaveData());

        // Nút Gán tài xế (Mở dialog chọn Tài xế)
        btnAssignDriver.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng Gán Tài xế sẽ được thêm sau.", Toast.LENGTH_SHORT).show();
            // TODO: Mở Dialog/Fragment để chọn và gán driverId/driverName mới
        });
    }

    /**
     * Tải dữ liệu Phương tiện hiện tại từ Firestore.
     */
    private void loadVehicleData(String id) {
        // ⭐ SỬA LỖI TÊN COLLECTION
        db.collection(COLLECTION_VEHICLES).document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentVehicle = documentSnapshot.toObject(Vehicle.class);
                        if (currentVehicle != null) {
                            // ⭐ Đảm bảo gán ID từ DocumentSnapshot vào đối tượng Vehicle
                            currentVehicle.setId(documentSnapshot.getId());
                            displayVehicleData(currentVehicle);
                        } else {
                            Toast.makeText(getContext(), "Lỗi chuyển đổi dữ liệu Phương tiện.", Toast.LENGTH_LONG).show();
                            closeFragment();
                        }
                    } else {
                        // Đây là nơi lỗi "Không tìm thấy Phương tiện này" đã xảy ra trước đó.
                        Toast.makeText(getContext(), "Không tìm thấy Phương tiện này.", Toast.LENGTH_LONG).show();
                        closeFragment();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải dữ liệu Phương tiện", e);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    closeFragment();
                });
    }

    /**
     * Hiển thị dữ liệu lên UI.
     */
    private void displayVehicleData(Vehicle vehicle) {
        tvVehicleIdStatic.setText(String.format("ID: %s (Không thể chỉnh sửa)", vehicle.getId()));

        // Thông tin cơ bản
        etBienSoXe.setText(vehicle.getBienSoXe());
        // ⭐ Quan trọng: Dùng 'false' để không kích hoạt AutoCompleteTextView dropdown
        actvLoaiPhuongTien.setText(vehicle.getLoaiPhuongTien(), false);
        etHangXe.setText(vehicle.getHangXe());
        etSoChoNgoi.setText(String.valueOf(vehicle.getSoChoNgoi()));

        // Tình trạng & Tài xế
        actvTinhTrangBaoDuong.setText(vehicle.getTinhTrangBaoDuong(), false);

        String driver = (vehicle.getDriverName() != null && !vehicle.getDriverName().isEmpty()) ?
                vehicle.getDriverName() :
                "Chưa gán tài xế";

        tvDriverName.setText(driver);
    }

    /**
     * Xác thực dữ liệu và gọi hàm lưu.
     */
    private void validateAndSaveData() {
        String bienSoXe = Objects.requireNonNull(etBienSoXe.getText()).toString().trim();
        String loaiPhuongTien = Objects.requireNonNull(actvLoaiPhuongTien.getText()).toString().trim();
        String hangXe = Objects.requireNonNull(etHangXe.getText()).toString().trim();
        String soChoStr = Objects.requireNonNull(etSoChoNgoi.getText()).toString().trim();
        String tinhTrangBaoDuong = Objects.requireNonNull(actvTinhTrangBaoDuong.getText()).toString().trim();

        // 1. Xác thực cơ bản
        if (bienSoXe.isEmpty() || loaiPhuongTien.isEmpty() || hangXe.isEmpty() || soChoStr.isEmpty() || tinhTrangBaoDuong.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ các trường bắt buộc.", Toast.LENGTH_SHORT).show();
            return;
        }

        int soChoNgoi;
        try {
            soChoNgoi = Integer.parseInt(soChoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số chỗ ngồi không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Tạo Map chứa các trường cần cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("bienSoXe", bienSoXe);
        updates.put("loaiPhuongTien", loaiPhuongTien);
        updates.put("hangXe", hangXe);
        updates.put("soChoNgoi", soChoNgoi);
        updates.put("tinhTrangBaoDuong", tinhTrangBaoDuong);

        // Cập nhật các trường không có trong UI (năm sản xuất, link ảnh...) nếu cần
        // Ví dụ: updates.put("namSanXuat", 2024); // Nếu bạn có trường này
        // updates.put("duongDanGiayTo", currentVehicle.getDuongDanGiayTo()); // Giữ nguyên

        // Gọi hàm cập nhật
        saveVehicleData(updates);
    }

    /**
     * Cập nhật dữ liệu lên Firestore.
     */
    private void saveVehicleData(Map<String, Object> updates) {
        if (vehicleId == null) {
            Toast.makeText(getContext(), "Lỗi hệ thống: Không có ID để cập nhật.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection(COLLECTION_VEHICLES).document(vehicleId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật Phương tiện thành công!", Toast.LENGTH_SHORT).show();
                    closeFragment();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi cập nhật Phương tiện", e);
                    Toast.makeText(getContext(), "Lỗi khi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Đóng Fragment (Quay lại màn hình trước).
     */
    private void closeFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}