package com.example.quanlytourdl;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Import các lớp Material UI cần thiết
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Giả định sử dụng Firebase Firestore (Chỉ để mô phỏng logic lưu trữ)
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class AddHdvFragment extends Fragment {

    private static final String TAG = "AddHdvFragment";

    // Khai báo các thành phần UI
    private ImageButton btnBack;
    private ShapeableImageView imgAvatar;
    private FloatingActionButton btnUploadAvatar;

    // Các trường nhập liệu (TextInputLayouts)
    private TextInputLayout inputFullName;
    private TextInputLayout inputGuideCode;
    private TextInputLayout inputBirthDate;
    private TextInputLayout inputPhoneNumber;
    private TextInputLayout inputEmail;
    private TextInputLayout inputAddress;
    private TextInputLayout inputGender;

    // Các TextInputEditText tương ứng (cần thiết để lấy giá trị)
    private TextInputEditText edittextFullName;
    private TextInputEditText edittextGuideCode;
    private TextInputEditText edittextBirthDate;
    private TextInputEditText edittextPhoneNumber;
    private TextInputEditText edittextEmail;
    private TextInputEditText edittextAddress;
    private AutoCompleteTextView dropdownGender;

    private ChipGroup chipGroupLanguages;
    private TextInputEditText inputAddLanguage;
    private MaterialButton btnDecrementExp;
    private MaterialButton btnIncrementExp;
    private TextView textExperienceValue;

    // THÀNH PHẦN MỚI CHO UPLOAD FILE (Giấy phép/Chứng chỉ)
    private LinearLayout layoutUploadFile;
    private TextView uploadedFileNameTextView; // TextView hiển thị tên file đã upload
    private String localDocumentUriString = null; // Biến lưu trữ Uri cục bộ

    private MaterialButton btnAddNew;
    private MaterialButton btnCancel;

    private FirebaseFirestore db;
    private int experienceYears = 0;

    // ActivityResultLauncher cho việc chọn file
    private final ActivityResultLauncher<String[]> documentPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            this::handleDocumentPickerResult
    );


    // --- 1. Cấu trúc Fragment ---

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_hdv, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Database (Mô phỏng)
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
        mapViews(view);

        // Thiết lập UI ban đầu
        if (textExperienceValue != null) {
            experienceYears = 2; // Đặt giá trị mặc định cho dễ kiểm tra
            textExperienceValue.setText(String.valueOf(experienceYears));
        }
        setupGenderDropdown();
        setupListeners();
    }

    // --- 1.1 Ánh xạ View (Cập nhật theo ID trong XML) ---
    private void mapViews(View view) {
        // TIÊU ĐỀ
        btnBack = view.findViewById(R.id.btn_back);

        // 1. Ảnh đại diện
        imgAvatar = view.findViewById(R.id.img_avatar);
        btnUploadAvatar = view.findViewById(R.id.btn_upload_avatar);

        // 2. Thông tin cá nhân
        inputFullName = view.findViewById(R.id.input_full_name);
        edittextFullName = view.findViewById(R.id.edittext_full_name);
        inputGuideCode = view.findViewById(R.id.input_guide_code);
        edittextGuideCode = view.findViewById(R.id.edittext_guide_code);
        inputBirthDate = view.findViewById(R.id.input_birth_date);
        edittextBirthDate = view.findViewById(R.id.edittext_birth_date);
        inputGender = view.findViewById(R.id.input_gender);
        dropdownGender = view.findViewById(R.id.dropdown_gender);

        // 3. Thông tin liên hệ
        inputPhoneNumber = view.findViewById(R.id.input_phone_number);
        edittextPhoneNumber = view.findViewById(R.id.edittext_phone_number);
        inputEmail = view.findViewById(R.id.input_email);
        edittextEmail = view.findViewById(R.id.edittext_email);
        inputAddress = view.findViewById(R.id.input_address);
        edittextAddress = view.findViewById(R.id.edittext_address);

        // 4. Kỹ năng & Kinh nghiệm
        chipGroupLanguages = view.findViewById(R.id.chip_group_languages);
        inputAddLanguage = view.findViewById(R.id.input_add_language);
        btnDecrementExp = view.findViewById(R.id.btn_decrement_exp);
        btnIncrementExp = view.findViewById(R.id.btn_increment_exp);
        textExperienceValue = view.findViewById(R.id.text_experience_value);

        // 5. Chứng chỉ & Giấy tờ
        layoutUploadFile = view.findViewById(R.id.layout_upload_file);
        uploadedFileNameTextView = view.findViewById(R.id.tv_uploaded_file_name);

        // CHÂN TRANG
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnAddNew = view.findViewById(R.id.btn_add_new);
    }

    // --- 1.2 Thiết lập Dropdown Giới tính ---
    private void setupGenderDropdown() {
        if (dropdownGender != null) {
            String[] genders = new String[]{"Nam", "Nữ", "Khác"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    genders
            );
            dropdownGender.setAdapter(adapter);
        }
    }


    // --- 2. Xử lý Listeners và Logic Giao diện ---

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Stepper Kinh nghiệm
        if (btnDecrementExp != null) btnDecrementExp.setOnClickListener(v -> updateExperience(-1));
        if (btnIncrementExp != null) btnIncrementExp.setOnClickListener(v -> updateExperience(1));

        // Nút Lưu/Thêm mới
        if (btnAddNew != null) btnAddNew.setOnClickListener(v -> saveTourGuide());

        // Nút Hủy
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                showToast("Đã hủy thao tác.", false);
                // Thoát Fragment
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Tải lên ảnh đại diện & Nút tải lên
        if (imgAvatar != null) imgAvatar.setOnClickListener(v -> showToast("Mở bộ chọn ảnh HDV", false));
        if (btnUploadAvatar != null) btnUploadAvatar.setOnClickListener(v -> showToast("Mở bộ chọn ảnh HDV", false));

        // Tải lên Chứng chỉ/File (Tích hợp logic chọn file)
        if (layoutUploadFile != null) layoutUploadFile.setOnClickListener(v -> pickDocumentFile());
    }

    private void updateExperience(int delta) {
        experienceYears += delta;
        if (experienceYears < 0) {
            experienceYears = 0; // Giới hạn tối thiểu
        }
        if (textExperienceValue != null) {
            textExperienceValue.setText(String.valueOf(experienceYears));
        }
    }

    /**
     * Tạo mã HDV độc đáo ngẫu nhiên theo format HDV-UUID
     */
    private String generateUniqueGuideCode() {
        // Lấy 8 ký tự đầu của UUID và chuyển thành chữ in hoa, thêm prefix "HDV-"
        return "HDV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
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

    // --- Logic Upload File (Giống bên Phương tiện) ---

    private void pickDocumentFile() {
        Toast.makeText(requireContext(), "Mở bộ chọn file để tải lên Giấy phép HDV...", Toast.LENGTH_SHORT).show();
        // Giả định Giấy phép/Chứng chỉ HDV là PDF hoặc hình ảnh
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
        return "GiayPhepHDV.pdf";
    }

    /**
     * Hàm mô phỏng việc tải file lên Firebase Storage (hoặc server).
     */
    private String performActualFileUpload(String localUriString) {
        if (localUriString == null || localUriString.isEmpty()) {
            Log.e(TAG, "Không thể upload: URI cục bộ là null hoặc trống.");
            return "UPLOAD_FAILED";
        }
        String fileName = getFileNameFromUri(Uri.parse(localUriString));
        Log.d(TAG, "Mô phỏng quá trình upload file: " + fileName);

        // GIẢ ĐỊNH UPLOAD THÀNH CÔNG VÀ TRẢ VỀ URL CỦA FILE TRÊN SERVER/STORAGE
        return "https://firebase.storage.com/guide_licenses/" + fileName;
    }


    // --- 3. Thu thập và Xác thực Dữ liệu ---

    private boolean validateInput() {
        boolean isValid = true;

        // Đặt lại lỗi trước khi kiểm tra
        if (inputFullName != null) inputFullName.setError(null);
        if (inputBirthDate != null) inputBirthDate.setError(null);
        if (inputPhoneNumber != null) inputPhoneNumber.setError(null);
        if (inputGender != null) inputGender.setError(null);
        if (inputEmail != null) inputEmail.setError(null);

        // Thu thập giá trị
        String fullName = getEditTextValue(edittextFullName);
        String birthDate = getEditTextValue(edittextBirthDate);
        String phoneNumber = getEditTextValue(edittextPhoneNumber);
        String email = getEditTextValue(edittextEmail);
        String gender = dropdownGender != null ? dropdownGender.getText().toString().trim() : "";

        // Kiểm tra các trường bắt buộc
        if (fullName.isEmpty()) {
            if (inputFullName != null) inputFullName.setError("Không được để trống");
            isValid = false;
        }
        if (birthDate.isEmpty()) {
            if (inputBirthDate != null) inputBirthDate.setError("Không được để trống");
            isValid = false;
        }
        if (phoneNumber.isEmpty()) {
            if (inputPhoneNumber != null) inputPhoneNumber.setError("Không được để trống");
            isValid = false;
        } else if (!isValidPhone(phoneNumber)) {
            if (inputPhoneNumber != null) inputPhoneNumber.setError("Số điện thoại không hợp lệ (9-15 số)");
            isValid = false;
        }
        // Email là bắt buộc và phải hợp lệ
        if (email.isEmpty()) {
            if (inputEmail != null) inputEmail.setError("Email không được để trống");
            isValid = false;
        } else if (!isValidEmail(email)) {
            if (inputEmail != null) inputEmail.setError("Email không hợp lệ");
            isValid = false;
        }
        if (gender.isEmpty()) {
            if (inputGender != null) inputGender.setError("Vui lòng chọn giới tính");
            isValid = false;
        }

        // Kiểm tra file đã được chọn chưa (BẮT BUỘC)
        if (localDocumentUriString == null) {
            showToast("Vui lòng tải lên Giấy phép/Chứng chỉ HDV.", true);
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        // Regex kiểm tra email cơ bản
        Pattern emailPattern = Pattern.compile("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$");
        return emailPattern.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        // Cho phép 9-15 số, có thể có dấu cách hoặc dấu gạch ngang
        Pattern phonePattern = Pattern.compile("^[\\d\\s-]{9,15}$");
        return phonePattern.matcher(phone).matches();
    }


    private Map<String, Object> getFormData(String guideCode, String documentServerPath) {
        // Thu thập dữ liệu
        String fullName = getEditTextValue(edittextFullName);
        String birthDate = getEditTextValue(edittextBirthDate);
        String phoneNumber = getEditTextValue(edittextPhoneNumber);
        String email = getEditTextValue(edittextEmail);
        String address = getEditTextValue(edittextAddress);
        String gender = dropdownGender != null ? dropdownGender.getText().toString().trim() : "";

        // Ngôn ngữ (Giả định, trong thực tế cần lấy từ ChipGroup)
        List<String> languages = Arrays.asList("Tiếng Việt", "Tiếng Anh");

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", fullName);
        data.put("guideCode", guideCode);
        data.put("birthDate", birthDate);
        data.put("gender", gender);
        data.put("phoneNumber", phoneNumber);
        data.put("email", email);
        data.put("address", address);
        data.put("languages", languages);
        data.put("experienceYears", experienceYears);
        data.put("duongDanGiayPhep", documentServerPath); // LƯU ĐƯỜNG DẪN TỪ UPLOAD
        data.put("isApproved", false);
        data.put("createdAt", Timestamp.now());

        return data;
    }

    // --- 4. Logic Lưu trữ Firebase Firestore (Mô phỏng) ---

    private void saveTourGuide() {
        if (!validateInput()) {
            showToast("Vui lòng điền đầy đủ thông tin bắt buộc.", true);
            return;
        }

        // Tắt nút và đổi text
        if (btnAddNew != null) {
            btnAddNew.setEnabled(false);
            btnAddNew.setText("Đang xử lý...");
        }

        // 1. TẠO MÃ HDV VÀ GHI VÀO UI
        String guideCode = generateUniqueGuideCode();
        if (edittextGuideCode != null) {
            edittextGuideCode.setText(guideCode);
        }

        // 2. UPLOAD FILE GIẤY PHÉP (Mô phỏng)
        String documentServerPath = performActualFileUpload(localDocumentUriString);

        if ("UPLOAD_FAILED".equals(documentServerPath)) {
            showToast("Lỗi khi tải lên Giấy phép HDV, vui lòng thử lại.", true);
            if (btnAddNew != null) {
                btnAddNew.setEnabled(true);
                btnAddNew.setText("Thêm mới");
            }
            return;
        }

        // 3. THU THẬP DỮ LIỆU VÀ LƯU VÀO FIRESTORE
        Map<String, Object> guideData = getFormData(guideCode, documentServerPath);

        db.collection("tour_guides")
                .add(guideData)
                .addOnSuccessListener(documentReference -> {
                    showToast("Thêm HDV thành công! Mã HDV: " + guideCode, false);
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi thêm HDV: " + e.getMessage(), e);
                    showToast("Lỗi khi thêm HDV: " + e.getMessage(), true);
                })
                .addOnCompleteListener(task -> {
                    // Khôi phục trạng thái nút
                    if (btnAddNew != null) {
                        btnAddNew.setEnabled(true);
                        btnAddNew.setText("Thêm mới");
                    }
                });
    }

    // --- 5. Hàm Tiện ích & Đặt lại ---

    private void resetForm() {
        // Reset các trường nhập liệu
        if (edittextFullName != null) edittextFullName.setText("");
        if (edittextBirthDate != null) edittextBirthDate.setText("");
        if (edittextPhoneNumber != null) edittextPhoneNumber.setText("");
        if (edittextEmail != null) edittextEmail.setText("");
        if (edittextAddress != null) edittextAddress.setText("");
        if (edittextGuideCode != null) edittextGuideCode.setText("");

        if (dropdownGender != null) dropdownGender.setText("", false);
        if (inputAddLanguage != null) inputAddLanguage.setText("");

        // Reset file upload
        localDocumentUriString = null;
        if (uploadedFileNameTextView != null) uploadedFileNameTextView.setText("Chưa có file nào được chọn");

        // Reset lỗi (nếu có)
        if (inputFullName != null) inputFullName.setError(null);
        if (inputBirthDate != null) inputBirthDate.setError(null);
        if (inputPhoneNumber != null) inputPhoneNumber.setError(null);
        if (inputGender != null) inputGender.setError(null);
        if (inputEmail != null) inputEmail.setError(null);


        // Reset kinh nghiệm
        experienceYears = 0;
        if (textExperienceValue != null) textExperienceValue.setText(String.valueOf(experienceYears));

        // TODO: Xóa tất cả chips ngôn ngữ đã thêm
    }

    private void showToast(String message, boolean isError) {
        if (getContext() != null) {
            Toast.makeText(getContext(), (isError ? "LỖI: " : "") + message, (isError ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)).show();
        }
    }
}