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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Import các lớp Material UI cần thiết
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.chip.ChipGroup;
import android.widget.AutoCompleteTextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddHdvFragment extends Fragment {

    // Khai báo các thành phần UI (Ánh xạ bằng findViewById)
    private ImageButton btnBack;
    private ShapeableImageView imgAvatar;

    // !!! CẦN THÊM ID CHO CÁC TextInputLayout NÀY TRONG XML !!!
    private TextInputLayout inputFullName;
    private TextInputLayout inputBirthDate;
    private TextInputLayout inputPhoneNumber;
    private TextInputLayout inputEmail;
    private TextInputLayout inputAddress;

    private AutoCompleteTextView dropdownGender;
    private ChipGroup chipGroupLanguages;
    private TextInputEditText inputAddLanguage;
    private MaterialButton btnDecrementExp;
    private MaterialButton btnIncrementExp;
    private TextView textExperienceValue;
    private LinearLayout layoutUploadFile;
    private MaterialButton btnAddNew;
    private MaterialButton btnCancel; // Thêm nút Hủy

    // Khởi tạo Firestore
    private FirebaseFirestore db;
    private int experienceYears = 2; // Khởi tạo giá trị kinh nghiệm

    // --- 1. Cấu trúc Fragment ---

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout XML. Bạn cần thay thế ID layout giả định này bằng ID layout thực tế của mình.
        View view = inflater.inflate(R.layout.fragment_add_hdv, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Database
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View (findViewById)
        mapViews(view);

        // Thiết lập UI ban đầu
        if (textExperienceValue != null) {
            textExperienceValue.setText(String.valueOf(experienceYears));
        }
        setupListeners();
    }

    // --- 1.1 Ánh xạ View (Thực hiện findViewById) ---
    private void mapViews(View view) {
        // HEADER
        btnBack = view.findViewById(R.id.btn_back);

        // 1. Ảnh đại diện
        imgAvatar = view.findViewById(R.id.img_avatar);

        // 2. Thông tin cá nhân (CẦN ID cho TextInputLayout trong XML)
        // LƯU Ý QUAN TRỌNG: Các TextInputLayout trong XML chưa có ID. Bạn cần thêm các ID sau vào XML:
        inputFullName = view.findViewById(R.id.input_full_name);
        inputBirthDate = view.findViewById(R.id.input_birth_date);
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

        // FOOTER
        btnCancel = view.findViewById(R.id.btn_cancel); // ID mới được thêm
        btnAddNew = view.findViewById(R.id.btn_add_new); // ID đã có
    }


    // --- 2. Xử lý Listener và Logic Giao diện ---

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
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Tải lên ảnh đại diện (Mock)
        if (imgAvatar != null) imgAvatar.setOnClickListener(v -> showToast("Chức năng chọn ảnh HDV", false));

        // Tải lên Chứng chỉ/File (Mock)
        if (layoutUploadFile != null) layoutUploadFile.setOnClickListener(v -> showToast("Chức năng chọn File Chứng chỉ", false));
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
        // Dùng if (inputField != null) để kiểm tra tránh NullPointerException nếu thiếu ID trong XML

        // Đặt lại lỗi trước khi kiểm tra
        if (inputFullName != null && inputFullName.getEditText() != null) inputFullName.setError(null);
        if (inputBirthDate != null && inputBirthDate.getEditText() != null) inputBirthDate.setError(null);
        if (inputPhoneNumber != null && inputPhoneNumber.getEditText() != null) inputPhoneNumber.setError(null);

        // Kiểm tra các trường bắt buộc
        if (inputFullName == null || inputFullName.getEditText() == null || inputFullName.getEditText().getText().toString().trim().isEmpty()) {
            if (inputFullName != null) inputFullName.setError("Không được để trống");
            else Log.e("Validation", "inputFullName is null. Missing ID in XML?");
            return false;
        }
        if (inputBirthDate == null || inputBirthDate.getEditText() == null || inputBirthDate.getEditText().getText().toString().trim().isEmpty()) {
            if (inputBirthDate != null) inputBirthDate.setError("Không được để trống");
            return false;
        }
        if (inputPhoneNumber == null || inputPhoneNumber.getEditText() == null || inputPhoneNumber.getEditText().getText().toString().trim().isEmpty()) {
            if (inputPhoneNumber != null) inputPhoneNumber.setError("Không được để trống");
            return false;
        }

        // Thêm kiểm tra cho Giới tính, Ngôn ngữ, v.v.
        return true;
    }

    private Map<String, Object> getFormData() {
        // Lấy dữ liệu từ các trường TextInputLayout và TextInputEditText
        String fullName = inputFullName != null && inputFullName.getEditText() != null ? inputFullName.getEditText().getText().toString() : "";
        String birthDate = inputBirthDate != null && inputBirthDate.getEditText() != null ? inputBirthDate.getEditText().getText().toString() : "";
        String phoneNumber = inputPhoneNumber != null && inputPhoneNumber.getEditText() != null ? inputPhoneNumber.getEditText().getText().toString() : "";
        String email = inputEmail != null && inputEmail.getEditText() != null ? inputEmail.getEditText().getText().toString() : "";
        String address = inputAddress != null && inputAddress.getEditText() != null ? inputAddress.getEditText().getText().toString() : "";

        // Giới tính
        String gender = dropdownGender != null ? dropdownGender.getText().toString() : "";

        // Ngôn ngữ (Giả định, trong thực tế cần lấy từ ChipGroup)
        // TODO: Cần thêm logic để đọc các chip được chọn từ chipGroupLanguages
        List<String> languages = Arrays.asList("Tiếng Việt", "Tiếng Anh");

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", fullName);
        data.put("guideCode", "HDV-8392"); // Mã cứng tạm thời
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

    // --- 4. Logic Lưu trữ Firebase Firestore ---

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

        db.collection("tour_guides")
                .add(guideData)
                .addOnSuccessListener(documentReference -> {
                    showToast("Thêm HDV thành công! ID: " + documentReference.getId(), false);
                    // Reset form sau khi lưu
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

    // --- 5. Hàm Utility & Dọn dẹp ---

    private void resetForm() {
        // Dùng if (inputField != null) để tránh NullPointerException
        if (inputFullName != null && inputFullName.getEditText() != null) inputFullName.getEditText().setText("");
        if (inputBirthDate != null && inputBirthDate.getEditText() != null) inputBirthDate.getEditText().setText("");
        if (inputPhoneNumber != null && inputPhoneNumber.getEditText() != null) inputPhoneNumber.getEditText().setText("");
        if (inputEmail != null && inputEmail.getEditText() != null) inputEmail.getEditText().setText("");
        if (inputAddress != null && inputAddress.getEditText() != null) inputAddress.getEditText().setText("");
        if (dropdownGender != null) dropdownGender.setText("");

        // Reset Stepper
        experienceYears = 2; // Đặt lại về giá trị ban đầu (2 năm)
        if (textExperienceValue != null) textExperienceValue.setText(String.valueOf(experienceYears));
    }

    private void showToast(String message, boolean isError) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        if (isError) {
            Log.e("AddHDV", message);
        } else {
            Log.d("AddHDV", message);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Không cần dọn dẹp binding
    }
}