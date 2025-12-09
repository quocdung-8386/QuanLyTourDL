package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

// KHÔNG sử dụng View Binding (Sử dụng findViewById truyền thống)
public class AddPhuongTienFragment extends Fragment {

    // Khai báo các thành phần UI
    private TextInputLayout inputVehicleType;
    private TextInputLayout inputBrand;
    private TextInputLayout inputLicensePlate;
    private TextInputLayout inputSeatCount;
    private TextInputLayout inputProductionYear;
    private TextInputLayout inputMaintenanceStatus;

    private View layoutUploadDocuments; // Dùng View cho khu vực tải lên
    private MaterialButton btnBack;
    private MaterialButton btnCancel;
    private MaterialButton btnAddNew;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng inflate layout truyền thống. Đảm bảo R.layout.fragment_add_phuongtien tồn tại
        return inflater.inflate(R.layout.fragment_add_phuongtien, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ các View bằng findViewById
        // LƯU Ý: Vui lòng kiểm tra lại các ID này phải trùng khớp với ID trong file XML của bạn
        inputVehicleType = view.findViewById(R.id.input_vehicle_type);
        inputBrand = view.findViewById(R.id.input_brand);
        inputLicensePlate = view.findViewById(R.id.input_license_plate);
        inputSeatCount = view.findViewById(R.id.input_seat_count);
        inputProductionYear = view.findViewById(R.id.input_production_year);
        inputMaintenanceStatus = view.findViewById(R.id.input_maintenance_status);

        layoutUploadDocuments = view.findViewById(R.id.layout_upload_documents);
        btnBack = view.findViewById(R.id.btn_back);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnAddNew = view.findViewById(R.id.btn_add_new);

        // 2. Setup Dropdown Menus (Loại phương tiện & Tình trạng bảo dưỡng)
        setupVehicleTypeDropdown();
        setupMaintenanceStatusDropdown();

        // 3. Handle Events
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());

        btnAddNew.setOnClickListener(v -> validateAndSaveVehicle());

        // 4. Handle Year Picker click (Mở DatePicker khi nhấn vào End Icon)
        inputProductionYear.setEndIconOnClickListener(v -> showYearPickerDialog());

        // 5. Handle Upload Area click (Giấy tờ xe)
        layoutUploadDocuments.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Mở giao diện chọn giấy tờ xe", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupVehicleTypeDropdown() {
        List<String> vehicleTypes = Arrays.asList("Xe 4 chỗ", "Xe 7 chỗ", "Xe 16 chỗ", "Xe 29 chỗ", "Xe mô tô");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, vehicleTypes);

        // Ép kiểu sang AutoCompleteTextView để thiết lập Adapter
        if (inputVehicleType.getEditText() instanceof AutoCompleteTextView) {
            AutoCompleteTextView dropdown = (AutoCompleteTextView) inputVehicleType.getEditText();
            dropdown.setAdapter(adapter);

            dropdown.setOnItemClickListener((parent, view, position, id) -> {
                String selectedType = parent.getItemAtPosition(position).toString();
                Toast.makeText(requireContext(), "Đã chọn: " + selectedType, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupMaintenanceStatusDropdown() {
        List<String> statuses = Arrays.asList("Hoạt động tốt", "Cần bảo trì nhỏ", "Cần sửa chữa lớn", "Đang bảo dưỡng");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses);

        if (inputMaintenanceStatus.getEditText() instanceof AutoCompleteTextView) {
            AutoCompleteTextView dropdown = (AutoCompleteTextView) inputMaintenanceStatus.getEditText();
            dropdown.setAdapter(adapter);
        }
    }

    private void showYearPickerDialog() {
        // Trong ứng dụng thực tế, bạn sẽ hiển thị DatePickerDialog
        Toast.makeText(requireContext(), "Mở DatePicker để chọn năm sản xuất", Toast.LENGTH_SHORT).show();

        // Ví dụ: Sau khi chọn, cập nhật EditText
        if (inputProductionYear.getEditText() != null) {
            ((TextInputEditText) inputProductionYear.getEditText()).setText("2024");
        }
    }

    private void validateAndSaveVehicle() {
        // Đảm bảo không có lỗi validation trước khi lưu
        clearAllErrors();

        // Lấy dữ liệu
        String vehicleType = "";
        if (inputVehicleType.getEditText() instanceof AutoCompleteTextView) {
            vehicleType = ((AutoCompleteTextView) inputVehicleType.getEditText()).getText().toString().trim();
        }
        String brand = inputBrand.getEditText() != null ? inputBrand.getEditText().getText().toString().trim() : "";
        String licensePlate = inputLicensePlate.getEditText() != null ? inputLicensePlate.getEditText().getText().toString().trim() : "";
        String seatCountStr = inputSeatCount.getEditText() != null ? inputSeatCount.getEditText().getText().toString().trim() : "";
        String productionYearStr = inputProductionYear.getEditText() != null ? inputProductionYear.getEditText().getText().toString().trim() : "";
        String maintenanceStatus = "";
        if (inputMaintenanceStatus.getEditText() instanceof AutoCompleteTextView) {
            maintenanceStatus = ((AutoCompleteTextView) inputMaintenanceStatus.getEditText()).getText().toString().trim();
        }

        boolean isValid = true;

        // Kiểm tra các trường bắt buộc
        if (vehicleType.isEmpty()) {
            inputVehicleType.setError("Vui lòng chọn loại phương tiện");
            isValid = false;
        }

        if (brand.isEmpty()) {
            inputBrand.setError("Hãng xe không được để trống");
            isValid = false;
        }

        if (licensePlate.isEmpty()) {
            inputLicensePlate.setError("Biển số xe không được để trống");
            isValid = false;
        }

        // Kiểm tra số chỗ ngồi
        Integer seats = null;
        try {
            seats = Integer.parseInt(seatCountStr);
        } catch (NumberFormatException ignored) { }

        if (seats == null || seats <= 0) {
            inputSeatCount.setError("Vui lòng nhập số chỗ hợp lệ");
            isValid = false;
        }

        // Kiểm tra năm sản xuất
        Integer year = null;
        try {
            year = Integer.parseInt(productionYearStr);
        } catch (NumberFormatException ignored) { }

        if (productionYearStr.length() != 4 || year == null || year < 1900 || year > 2100) { // Giả định khoảng năm hợp lệ
            inputProductionYear.setError("Vui lòng nhập năm sản xuất hợp lệ (VD: 2023)");
            isValid = false;
        }

        // Kiểm tra Tình trạng bảo dưỡng
        if (maintenanceStatus.isEmpty()) {
            inputMaintenanceStatus.setError("Vui lòng chọn tình trạng bảo dưỡng");
            isValid = false;
        }


        if (isValid) {
            // Dữ liệu hợp lệ, thực hiện lưu
            String message = String.format("Đang lưu: Loại: %s, Hãng: %s, BS: %s", vehicleType, brand, licensePlate);
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

            // TODO: Triển khai logic lưu vào Firestore/API ở đây

            // Sau khi lưu thành công:
            // requireActivity().onBackPressed();
        } else {
            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ và chính xác các thông tin bắt buộc", Toast.LENGTH_LONG).show();
        }
    }

    // Hàm xóa tất cả các lỗi trước khi xác thực mới
    private void clearAllErrors() {
        inputVehicleType.setError(null);
        inputBrand.setError(null);
        inputLicensePlate.setError(null);
        inputSeatCount.setError(null);
        inputProductionYear.setError(null);
        inputMaintenanceStatus.setError(null);
    }
}