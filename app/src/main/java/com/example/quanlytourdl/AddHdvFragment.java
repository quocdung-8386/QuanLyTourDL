package com.example.quanlytourdl;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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

import com.example.quanlytourdl.model.Guide;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class AddHdvFragment extends Fragment {

    private static final String TAG = "AddHdvFragment";

    // UI Components
    private ImageButton btnBack;
    private ShapeableImageView imgAvatar;
    private FloatingActionButton btnUploadAvatar;
    private TextInputLayout inputFullName, inputGuideCode, inputBirthDate, inputPhoneNumber, inputEmail, inputAddress, inputGender;
    private TextInputEditText edittextFullName, edittextGuideCode, edittextBirthDate, edittextPhoneNumber, edittextEmail, edittextAddress, inputAddLanguage;
    private AutoCompleteTextView dropdownGender;
    private ChipGroup chipGroupLanguages;
    private MaterialButton btnDecrementExp, btnIncrementExp, btnAddNew, btnCancel;
    private TextView textExperienceValue, uploadedFileNameTextView;
    private LinearLayout layoutUploadFile;

    private FirebaseFirestore db;
    private int experienceYears = 0;
    private String localDocumentUriString = null;

    private final ActivityResultLauncher<String[]> documentPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            this::handleDocumentPickerResult
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_hdv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mapViews(view);
        setupInitialUI();
        setupListeners();
    }

    private void mapViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        imgAvatar = view.findViewById(R.id.img_avatar);
        btnUploadAvatar = view.findViewById(R.id.btn_upload_avatar);
        inputFullName = view.findViewById(R.id.input_full_name);
        edittextFullName = view.findViewById(R.id.edittext_full_name);
        inputGuideCode = view.findViewById(R.id.input_guide_code);
        edittextGuideCode = view.findViewById(R.id.edittext_guide_code);
        inputBirthDate = view.findViewById(R.id.input_birth_date);
        edittextBirthDate = view.findViewById(R.id.edittext_birth_date);
        inputGender = view.findViewById(R.id.input_gender);
        dropdownGender = view.findViewById(R.id.dropdown_gender);
        inputPhoneNumber = view.findViewById(R.id.input_phone_number);
        edittextPhoneNumber = view.findViewById(R.id.edittext_phone_number);
        inputEmail = view.findViewById(R.id.input_email);
        edittextEmail = view.findViewById(R.id.edittext_email);
        inputAddress = view.findViewById(R.id.input_address);
        edittextAddress = view.findViewById(R.id.edittext_address);
        chipGroupLanguages = view.findViewById(R.id.chip_group_languages);
        inputAddLanguage = view.findViewById(R.id.input_add_language);
        btnDecrementExp = view.findViewById(R.id.btn_decrement_exp);
        btnIncrementExp = view.findViewById(R.id.btn_increment_exp);
        textExperienceValue = view.findViewById(R.id.text_experience_value);
        layoutUploadFile = view.findViewById(R.id.layout_upload_file);
        uploadedFileNameTextView = view.findViewById(R.id.tv_uploaded_file_name);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnAddNew = view.findViewById(R.id.btn_add_new);
    }

    private void setupInitialUI() {
        experienceYears = 0;
        textExperienceValue.setText(String.valueOf(experienceYears));

        String[] genders = new String[]{"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, genders);
        dropdownGender.setAdapter(adapter);

        edittextGuideCode.setText(generateUniqueGuideCode());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        btnDecrementExp.setOnClickListener(v -> updateExperience(-1));
        btnIncrementExp.setOnClickListener(v -> updateExperience(1));
        btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());
        btnAddNew.setOnClickListener(v -> saveTourGuide());
        layoutUploadFile.setOnClickListener(v -> pickDocumentFile());

        inputAddLanguage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                addLanguageChip(inputAddLanguage.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void addLanguageChip(String language) {
        if (language.isEmpty()) return;
        Chip chip = new Chip(requireContext());
        chip.setText(language);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroupLanguages.removeView(chip));
        chipGroupLanguages.addView(chip);
        inputAddLanguage.setText("");
    }

    private void updateExperience(int delta) {
        experienceYears = Math.max(0, experienceYears + delta);
        textExperienceValue.setText(String.valueOf(experienceYears));
    }

    private void saveTourGuide() {
        if (!validateInput()) return;

        btnAddNew.setEnabled(false);
        btnAddNew.setText("Đang lưu...");

        // Chuẩn bị dữ liệu Map để khớp chính xác với Firebase
        Map<String, Object> guideData = new HashMap<>();
        guideData.put("fullName", edittextFullName.getText().toString().trim());
        guideData.put("guideCode", edittextGuideCode.getText().toString().trim());
        guideData.put("phoneNumber", edittextPhoneNumber.getText().toString().trim());
        guideData.put("email", edittextEmail.getText().toString().trim());
        guideData.put("address", edittextAddress.getText().toString().trim());
        guideData.put("gender", dropdownGender.getText().toString());
        guideData.put("birthDate", edittextBirthDate.getText().toString().trim());
        guideData.put("experienceYears", experienceYears);
        guideData.put("isApproved", false); // Mặc định chưa duyệt để hiện trong list trùng/chờ
        guideData.put("createdAt", Timestamp.now());
        guideData.put("duongDanGiayPhep", localDocumentUriString != null ? localDocumentUriString : "");

        List<String> languages = new ArrayList<>();
        for (int i = 0; i < chipGroupLanguages.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLanguages.getChildAt(i);
            languages.add(chip.getText().toString());
        }
        guideData.put("languages", languages);

        db.collection("tour_guides")
                .add(guideData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Thêm HDV thành công!", Toast.LENGTH_SHORT).show();
                    if (isAdded()) requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    btnAddNew.setEnabled(true);
                    btnAddNew.setText("Thêm mới");
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInput() {
        boolean valid = true;
        if (edittextFullName.getText().toString().trim().isEmpty()) {
            inputFullName.setError("Bắt buộc");
            valid = false;
        } else inputFullName.setError(null);

        if (edittextPhoneNumber.getText().toString().trim().length() < 10) {
            inputPhoneNumber.setError("SĐT không hợp lệ");
            valid = false;
        } else inputPhoneNumber.setError(null);

        if (!isValidEmail(edittextEmail.getText().toString().trim())) {
            inputEmail.setError("Email không hợp lệ");
            valid = false;
        } else inputEmail.setError(null);

        if (localDocumentUriString == null) {
            Toast.makeText(getContext(), "Vui lòng đính kèm giấy phép HDV", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private boolean isValidEmail(String email) {
        return Pattern.compile("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$").matcher(email).matches();
    }

    private void pickDocumentFile() {
        documentPickerLauncher.launch(new String[] {"application/pdf", "image/*"});
    }

    private void handleDocumentPickerResult(Uri uri) {
        if (uri != null) {
            localDocumentUriString = uri.toString();
            uploadedFileNameTextView.setText("Đã chọn: " + uri.getLastPathSegment());
        }
    }

    private String generateUniqueGuideCode() {
        return "HDV-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }
}