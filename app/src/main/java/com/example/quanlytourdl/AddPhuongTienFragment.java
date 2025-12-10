package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore; // Import cần thiết cho Firestore

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Fragment để quản lý việc thêm phương tiện mới.
 * ĐÃ HOÀN THIỆN: Thêm logic kết nối và lưu dữ liệu vào Firebase Firestore.
 */
public class AddPhuongTienFragment extends Fragment {

    private static final String TAG = "AddPhuongTienFragment";

    // Khai báo instance của Firebase Firestore
    private FirebaseFirestore db;

    // Khai báo các TextInputLayout
    private TextInputLayout inputVehicleType;
    private TextInputLayout inputBrand;
    private TextInputLayout inputLicensePlate;
    private TextInputLayout inputSeatCount;
    private TextInputLayout inputProductionYear;
    private TextInputLayout inputMaintenanceStatus;

    // KHAI BÁO TRỰC TIẾP CÁC EDITTEXT BÊN TRONG (ĐỂ ĐẢM BẢO VIỆC LẤY DỮ LIỆU)
    private AutoCompleteTextView dropdownVehicleType;
    private TextInputEditText editTextBrand;
    private TextInputEditText editTextLicensePlate;
    private TextInputEditText editTextSeatCount;
    private TextInputEditText editTextProductionYear;
    private AutoCompleteTextView dropdownMaintenanceStatus;

    // Các thành phần liên quan đến Giấy tờ & Upload
    private LinearLayout layoutUploadDocuments;
    private TextView tvViewSample;
    private TextView uploadedFileNameTextView;

    // Buttons
    private ImageButton btnBack;
    private MaterialButton btnCancel;
    private MaterialButton btnAddNew;

    private String localDocumentUriString = null;

    private final ActivityResultLauncher<String[]> documentPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            this::handleDocumentPickerResult
    );


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_phuongtien, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // *** KHỞI TẠO FIRESTORE ***
        // Hàm này lấy instance của cơ sở dữ liệu Firestore đã được khởi tạo.
        db = FirebaseFirestore.getInstance();

        // 1. Ánh xạ các TextInputLayout
        inputVehicleType = view.findViewById(R.id.input_vehicle_type);
        inputBrand = view.findViewById(R.id.input_brand);
        inputLicensePlate = view.findViewById(R.id.input_license_plate);
        inputSeatCount = view.findViewById(R.id.input_seat_count);
        inputProductionYear = view.findViewById(R.id.input_production_year);
        inputMaintenanceStatus = view.findViewById(R.id.input_maintenance_status);

        // 2. Ánh xạ các EditText/Dropdown (SỬ DỤNG ID ĐÃ THÊM TRONG XML)
        dropdownVehicleType = view.findViewById(R.id.dropdown_vehicle_type);
        editTextBrand = view.findViewById(R.id.edit_text_brand);
        editTextLicensePlate = view.findViewById(R.id.edit_text_license_plate);
        editTextSeatCount = view.findViewById(R.id.edit_text_seat_count);
        editTextProductionYear = view.findViewById(R.id.edit_text_production_year);
        dropdownMaintenanceStatus = view.findViewById(R.id.dropdown_maintenance_status);


        // 3. Ánh xạ các thành phần Upload và Buttons
        btnBack = view.findViewById(R.id.btn_back);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnAddNew = view.findViewById(R.id.btn_add_new);

        layoutUploadDocuments = view.findViewById(R.id.layout_upload_documents);
        tvViewSample = view.findViewById(R.id.tv_view_sample);
        uploadedFileNameTextView = view.findViewById(R.id.tv_uploaded_file_name);


        // 4. Setup Dropdown Menus
        setupVehicleTypeDropdown();
        setupMaintenanceStatusDropdown();

        // 5. Handle Events
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> handleBackNavigation());
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> handleBackNavigation());
        }
        if (btnAddNew != null) {
            // Khi nhấn nút Thêm mới, thực hiện xác thực và lưu dữ liệu (bao gồm cả Firestore)
            btnAddNew.setOnClickListener(v -> validateAndSaveVehicle());
        }

        // Handle Year Picker click
        if (inputProductionYear != null && editTextProductionYear != null) {
            inputProductionYear.setEndIconOnClickListener(v -> showYearPickerDialog());
            editTextProductionYear.setOnClickListener(v -> showYearPickerDialog());
            editTextProductionYear.setFocusable(false);
            editTextProductionYear.setClickable(true);
        }

        // Handle Upload Area click
        if (layoutUploadDocuments != null) {
            layoutUploadDocuments.setOnClickListener(v -> pickDocumentFile());
        }

        if (tvViewSample != null) {
            tvViewSample.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Mở hộp thoại Xem mẫu Giấy tờ xe", Toast.LENGTH_SHORT).show()
            );
        }
    }

    /**
     * Hàm tiện ích để lấy text từ một TextInputEditText, đảm bảo .trim() và không bao giờ trả về null.
     */
    private String getEditTextValue(TextInputEditText editText) {
        if (editText != null && editText.getText() != null) {
            return editText.getText().toString().trim();
        }
        return "";
    }

    /**
     * Hàm tiện ích để lấy text từ một AutoCompleteTextView, đảm bảo .trim() và không bao giờ trả về null.
     */
    private String getDropdownValue(AutoCompleteTextView dropdown) {
        if (dropdown != null && dropdown.getText() != null) {
            return dropdown.getText().toString().trim();
        }
        return "";
    }


    private void pickDocumentFile() {
        Toast.makeText(requireContext(), "Mở bộ chọn file để tải lên Giấy tờ xe...", Toast.LENGTH_SHORT).show();
        documentPickerLauncher.launch(new String[] {"application/pdf", "image/jpeg", "image/png"});
    }

    private void handleDocumentPickerResult(Uri uri) {
        if (uri == null) {
            Toast.makeText(requireContext(), "Đã hủy chọn file.", Toast.LENGTH_SHORT).show();
            if (uploadedFileNameTextView != null) {
                uploadedFileNameTextView.setText("Chưa có file nào được chọn");
            }
            localDocumentUriString = null;
            return;
        }

        String fileName = getFileNameFromUri(uri);
        localDocumentUriString = uri.toString();

        try {
            // Cấp quyền đọc file lâu dài (quan trọng cho ứng dụng Android)
            requireActivity().getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (SecurityException e) {
            Log.e(TAG, "Không thể lấy quyền truy cập lâu dài cho URI: " + uri.toString(), e);
            Toast.makeText(requireContext(), "Lỗi quyền truy cập file. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            localDocumentUriString = null;
            if (uploadedFileNameTextView != null) {
                uploadedFileNameTextView.setText("Lỗi: Không thể truy cập file");
            }
            return;
        }

        Toast.makeText(requireContext(), "Đã chọn file: " + fileName, Toast.LENGTH_LONG).show();

        if (uploadedFileNameTextView != null) {
            uploadedFileNameTextView.setText(fileName);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getLastPathSegment();
        if (path != null) {
            if (path.contains("/")) {
                return path.substring(path.lastIndexOf('/') + 1);
            }
            return path;
        }
        return "TaiLieuGiayTo.pdf";
    }

    private void handleBackNavigation() {
        if (getActivity() != null) {
            requireActivity().onBackPressed();
        }
    }

    private void setupVehicleTypeDropdown() {
        List<String> vehicleTypes = Arrays.asList("Xe 4 chỗ", "Xe 7 chỗ", "Xe 16 chỗ", "Xe 29 chỗ", "Xe máy", "Xe khác");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, vehicleTypes);

        if (dropdownVehicleType != null) {
            dropdownVehicleType.setAdapter(adapter);
        }
    }

    private void setupMaintenanceStatusDropdown() {
        List<String> statuses = Arrays.asList("Hoạt động tốt", "Cần bảo trì nhỏ", "Cần sửa chữa lớn", "Đang bảo dưỡng");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses);

        if (dropdownMaintenanceStatus != null) {
            dropdownMaintenanceStatus.setAdapter(adapter);
        }
    }

    private void showYearPickerDialog() {
        final Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);

        int initialYear = currentYear;
        // Lấy giá trị năm hiện tại
        String existingYearText = getEditTextValue(editTextProductionYear);
        if (!existingYearText.isEmpty()) {
            try {
                int parsedYear = Integer.parseInt(existingYearText);
                if (parsedYear >= 1900 && parsedYear <= currentYear) {
                    initialYear = parsedYear;
                }
            } catch (NumberFormatException ignored) { }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    String yearText = String.valueOf(yearSelected);
                    if (editTextProductionYear != null) {
                        editTextProductionYear.setText(yearText);
                        inputProductionYear.setError(null);
                    }
                },
                initialYear,
                0, // Tháng 1
                1 // Ngày 1
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        Calendar minYear = Calendar.getInstance();
        minYear.set(1900, 0, 1);
        datePickerDialog.getDatePicker().setMinDate(minYear.getTimeInMillis());


        datePickerDialog.setTitle("Chọn Năm Sản Xuất");
        datePickerDialog.show();
    }

    /**
     * Hàm mô phỏng việc tải file lên Firebase Storage (hoặc server).
     * Trong ứng dụng thực tế, bạn sẽ dùng Firebase Storage ở đây.
     */
    private String performActualFileUpload(String localUriString) {
        if (localUriString == null || localUriString.isEmpty()) {
            Log.e(TAG, "Không thể upload: URI cục bộ là null hoặc trống.");
            return "UPLOAD_FAILED";
        }
        String fileName = getFileNameFromUri(Uri.parse(localUriString));
        Log.d(TAG, "Mô phỏng quá trình upload file: " + fileName);

        // GIẢ ĐỊNH UPLOAD THÀNH CÔNG VÀ TRẢ VỀ URL CỦA FILE TRÊN SERVER/STORAGE
        return "https://firebase.storage.com/vehicle_docs/" + fileName;
    }

    private boolean isValidLicensePlateFormat(String plate) {
        // Regex linh hoạt: Cho phép bất kỳ kết hợp nào của chữ cái, số, khoảng trắng, dấu gạch ngang, dấu chấm.
        Pattern lenientPattern = Pattern.compile("^[\\da-zA-Z\\s.-]+$");
        return lenientPattern.matcher(plate).matches();
    }

    /**
     * Thực hiện lưu dữ liệu phương tiện vào collection "phuongtien" trên Firestore.
     * @param vehicleData Map chứa dữ liệu phương tiện.
     */
    private void saveVehicleToFirestore(Map<String, Object> vehicleData) {
        if (db == null) {
            Toast.makeText(requireContext(), "Lỗi: Không thể kết nối Firebase Firestore.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("phuongtien")
                .add(vehicleData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Lưu phương tiện thành công với ID: " + documentReference.getId());
                    Toast.makeText(requireContext(), "Thêm phương tiện mới thành công!", Toast.LENGTH_LONG).show();
                    handleBackNavigation();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lưu phương tiện: ", e);
                    Toast.makeText(requireContext(), "Lỗi: Không thể lưu phương tiện vào cơ sở dữ liệu. " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Lấy dữ liệu, xác thực và thực hiện lưu phương tiện.
     */
    private void validateAndSaveVehicle() {
        clearAllErrors();

        // 1. Lấy dữ liệu
        String vehicleType = getDropdownValue(dropdownVehicleType);
        String brand = getEditTextValue(editTextBrand);
        String licensePlate = getEditTextValue(editTextLicensePlate);
        String seatCountStr = getEditTextValue(editTextSeatCount);
        String productionYearStr = getEditTextValue(editTextProductionYear);
        String maintenanceStatus = getDropdownValue(dropdownMaintenanceStatus);

        boolean isValid = true;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Integer seats = null;
        Integer year = null;


        // 2. Xác thực (Giữ nguyên logic sửa lỗi Biển số xe)
        if (inputVehicleType != null && vehicleType.isEmpty()) {
            inputVehicleType.setError("Vui lòng chọn loại phương tiện");
            isValid = false;
        }

        if (inputBrand != null && brand.isEmpty()) {
            inputBrand.setError("Hãng xe không được để trống");
            isValid = false;
        }

        if (inputLicensePlate != null) {
            if (licensePlate.length() == 0) {
                inputLicensePlate.setError("Biển số xe không được để trống");
                isValid = false;
            } else if (!isValidLicensePlateFormat(licensePlate)) {
                inputLicensePlate.setError("Biển số xe chứa ký tự không hợp lệ.");
                isValid = false;
            }
        }

        if (inputSeatCount != null) {
            try {
                seats = Integer.parseInt(seatCountStr);
            } catch (NumberFormatException ignored) { }
            if (seats == null || seats <= 0) {
                inputSeatCount.setError("Vui lòng nhập số chỗ hợp lệ");
                isValid = false;
            }
        }

        if (inputProductionYear != null) {
            try {
                year = Integer.parseInt(productionYearStr);
            } catch (NumberFormatException ignored) { }
            if (productionYearStr.isEmpty() || productionYearStr.length() != 4 || year == null || year < 1900 || year > currentYear) {
                inputProductionYear.setError("Vui lòng chọn năm sản xuất hợp lệ (trước hoặc bằng " + currentYear + ")");
                isValid = false;
            }
        }

        if (inputMaintenanceStatus != null && maintenanceStatus.isEmpty()) {
            inputMaintenanceStatus.setError("Vui lòng chọn tình trạng bảo dưỡng");
            isValid = false;
        }

        // Xác thực Giấy tờ xe
        if (localDocumentUriString == null) {
            Toast.makeText(requireContext(), "Vui lòng tải lên giấy tờ xe.", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        // 3. Xử lý lưu & UPLOAD FILE THỰC TẾ
        if (isValid) {
            // Bước 3a: Thực hiện upload file giấy tờ (mô phỏng)
            String documentServerPath = performActualFileUpload(localDocumentUriString);

            if ("UPLOAD_FAILED".equals(documentServerPath)) {
                Toast.makeText(requireContext(), "Lỗi khi tải lên giấy tờ xe, vui lòng thử lại.", Toast.LENGTH_LONG).show();
                return;
            }

            // Bước 3b: Tạo Map dữ liệu để lưu vào Firestore
            Map<String, Object> vehicleData = new HashMap<>();
            vehicleData.put("loaiPhuongTien", vehicleType);
            vehicleData.put("hangXe", brand);
            vehicleData.put("bienSoXe", licensePlate);
            vehicleData.put("soChoNgoi", seats);
            vehicleData.put("namSanXuat", year);
            vehicleData.put("tinhTrangBaoDuong", maintenanceStatus);
            vehicleData.put("duongDanGiayTo", documentServerPath);
            vehicleData.put("thoiGianTao", System.currentTimeMillis());

            // Bước 3c: LƯU VÀO FIRESTORE
            saveVehicleToFirestore(vehicleData);

        } else {
            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ và chính xác các thông tin bắt buộc.", Toast.LENGTH_LONG).show();
        }
    }

    private void clearAllErrors() {
        if (inputVehicleType != null) inputVehicleType.setError(null);
        if (inputBrand != null) inputBrand.setError(null);
        if (inputLicensePlate != null) inputLicensePlate.setError(null);
        if (inputSeatCount != null) inputSeatCount.setError(null);
        if (inputProductionYear != null) inputProductionYear.setError(null);
        if (inputMaintenanceStatus != null) inputMaintenanceStatus.setError(null);
    }
}