package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter; // Thêm import này

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Import các lớp Material UI cần thiết
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Thêm import này
import android.widget.AutoCompleteTextView;

// Giả định sử dụng Firebase Firestore (Chỉ để mô phỏng logic lưu trữ)
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddHdvFragment extends Fragment {

    // Khai báo các thành phần UI
    private ImageButton btnBack;
    private ShapeableImageView imgAvatar;
    private FloatingActionButton btnUploadAvatar; // Thêm FloatingActionButton

    // Các trường nhập liệu (TextInputLayouts)
    private TextInputLayout inputFullName;
    private TextInputLayout inputGuideCode; // Mã HDV
    private TextInputLayout inputBirthDate;
    private TextInputLayout inputPhoneNumber;
    private TextInputLayout inputEmail;
    private TextInputLayout inputAddress;
    private TextInputLayout inputGender;

    private TextInputEditText edittextGuideCode; // EditText Mã HDV
    private AutoCompleteTextView dropdownGender;
    private ChipGroup chipGroupLanguages;
    private TextInputEditText inputAddLanguage;
    private MaterialButton btnDecrementExp;
    private MaterialButton btnIncrementExp;
    private TextView textExperienceValue;
    private LinearLayout layoutUploadFile;
    private MaterialButton btnAddNew;
    private MaterialButton btnCancel;

    private FirebaseFirestore db;
    private int experienceYears = 0; // Khởi tạo giá trị kinh nghiệm (số năm)

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
        btnUploadAvatar = view.findViewById(R.id.btn_upload_avatar); // Ánh xạ FloatingActionButton

        // 2. Thông tin cá nhân
        inputFullName = view.findViewById(R.id.input_full_name);
        inputGuideCode = view.findViewById(R.id.input_guide_code);
        edittextGuideCode = view.findViewById(R.id.edittext_guide_code);
        inputBirthDate = view.findViewById(R.id.input_birth_date);
        inputGender = view.findViewById(R.id.input_gender);
        dropdownGender = view.findViewById(R.id.dropdown_gender);

        // 3. Thông tin liên hệ
        inputPhoneNumber = view.findViewById(R.id.input_phone_number);
        inputEmail = view.findViewById(R.id.input_email);
        inputAddress = view.findViewById(R.id.input_address);

        // 4. Kỹ năng & Kinh nghiệm
        chipGroupLanguages = view.findViewById(R.id.chip_group_languages);
        inputAddLanguage = view.findViewById(R.id.input_add_language);
        btnDecrementExp = view.findViewById(R.id.btn_decrement_exp);
        btnIncrementExp = view.findViewById(R.id.btn_increment_exp);
        textExperienceValue = view.findViewById(R.id.text_experience_value);

        // 5. Chứng chỉ & Giấy tờ
        layoutUploadFile = view.findViewById(R.id.layout_upload_file);

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

        // Tải lên Chứng chỉ/File
        if (layoutUploadFile != null) layoutUploadFile.setOnClickListener(v -> showToast("Mở bộ chọn File Chứng chỉ", false));
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

    // --- 3. Thu thập và Xác thực Dữ liệu ---

    private boolean validateInput() {
        boolean isValid = true;

        // Đặt lại lỗi trước khi kiểm tra
        if (inputFullName != null) inputFullName.setError(null);
        if (inputBirthDate != null) inputBirthDate.setError(null);
        if (inputPhoneNumber != null) inputPhoneNumber.setError(null);
        if (inputGender != null) inputGender.setError(null);

        // Hàm tiện ích để lấy giá trị EditText an toàn
        // (Để tránh lặp lại getEditText().getText().toString().trim())
        String fullName = inputFullName != null && inputFullName.getEditText() != null ? inputFullName.getEditText().getText().toString().trim() : "";
        String birthDate = inputBirthDate != null && inputBirthDate.getEditText() != null ? inputBirthDate.getEditText().getText().toString().trim() : "";
        String phoneNumber = inputPhoneNumber != null && inputPhoneNumber.getEditText() != null ? inputPhoneNumber.getEditText().getText().toString().trim() : "";
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
        }
        if (gender.isEmpty()) {
            if (inputGender != null) inputGender.setError("Vui lòng chọn giới tính");
            isValid = false;
        }

        // TODO: Thêm logic xác thực format Ngày sinh, Số điện thoại (RegEx)

        return isValid;
    }

    private Map<String, Object> getFormData() {
        // Thu thập dữ liệu
        String fullName = inputFullName != null && inputFullName.getEditText() != null ? inputFullName.getEditText().getText().toString().trim() : "";
        String birthDate = inputBirthDate != null && inputBirthDate.getEditText() != null ? inputBirthDate.getEditText().getText().toString().trim() : "";
        String phoneNumber = inputPhoneNumber != null && inputPhoneNumber.getEditText() != null ? inputPhoneNumber.getEditText().getText().toString().trim() : "";
        String email = inputEmail != null && inputEmail.getEditText() != null ? inputEmail.getEditText().getText().toString().trim() : "";
        String address = inputAddress != null && inputAddress.getEditText() != null ? inputAddress.getEditText().getText().toString().trim() : "";
        String gender = dropdownGender != null ? dropdownGender.getText().toString().trim() : "";
        String guideCode = edittextGuideCode != null ? edittextGuideCode.getText().toString() : "N/A";

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

        Map<String, Object> guideData = getFormData();

        // Cập nhật trạng thái nút
        if (btnAddNew != null) {
            btnAddNew.setEnabled(false);
            btnAddNew.setText("Đang lưu...");
        }

        // Giả định sử dụng collection "tour_guides"
        db.collection("tour_guides")
                .add(guideData)
                .addOnSuccessListener(documentReference -> {
                    showToast("Thêm HDV thành công! ID: " + documentReference.getId(), false);
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    Log.e("AddHDV", "Lỗi khi thêm HDV: " + e.getMessage(), e);
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
        if (inputFullName != null && inputFullName.getEditText() != null) inputFullName.getEditText().setText("");
        if (inputBirthDate != null && inputBirthDate.getEditText() != null) inputBirthDate.getEditText().setText("");
        if (inputPhoneNumber != null && inputPhoneNumber.getEditText() != null) inputPhoneNumber.getEditText().setText("");
        if (inputEmail != null && inputEmail.getEditText() != null) inputEmail.getEditText().setText("");
        if (inputAddress != null && inputAddress.getEditText() != null) inputAddress.getEditText().setText("");

        if (dropdownGender != null) dropdownGender.setText("", false);
        if (inputAddLanguage != null) inputAddLanguage.setText("");

        // Reset lỗi (nếu có)
        if (inputFullName != null) inputFullName.setError(null);
        if (inputBirthDate != null) inputBirthDate.setError(null);
        if (inputPhoneNumber != null) inputPhoneNumber.setError(null);
        if (inputGender != null) inputGender.setError(null);


        // Reset kinh nghiệm
        experienceYears = 0;
        if (textExperienceValue != null) textExperienceValue.setText(String.valueOf(experienceYears));

        // TODO: Xóa tất cả chips ngôn ngữ đã thêm
        // TODO: Xóa danh sách file đã upload
    }

    private void showToast(String message, boolean isError) {
        if (getContext() != null) {
            Toast.makeText(getContext(), (isError ? "LỖI: " : "") + message, Toast.LENGTH_LONG).show();
        }
    }
}