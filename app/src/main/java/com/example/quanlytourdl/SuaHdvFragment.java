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

import com.example.quanlytourdl.model.Guide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SuaHdvFragment extends Fragment {

    private static final String TAG = "SuaHdvFragment";
    private static final String COLLECTION_HDV = "tour_guides";
    private static final String ARG_GUIDE_ID = "guide_id";

    // UI Components
    private ImageButton btnCloseDialog;
    private TextInputEditText etFullName, etGuideCode, etEmail, etSdt, etExperienceYears, etAddress, etBirthDate;
    private AutoCompleteTextView actvTrangThai, actvGender;
    private TextView tvGuideIdStatic, tvRating;
    private ChipGroup chipGroupLanguages;
    private MaterialButton btnResetHdv, btnSaveHdv;
    private Chip chipAddLanguage;

    // Data
    private FirebaseFirestore db;
    private String guideId;
    private Guide currentGuide;

    // Logic Trạng thái khớp với Boolean isApproved
    private static final String ST_APPROVED = "Sẵn sàng";
    private static final String ST_PENDING = "Chờ phê duyệt";
    private static final String[] TRANG_THAI_DISPLAY = {ST_APPROVED, ST_PENDING};
    private static final String[] GENDER_OPTIONS = {"Nam", "Nữ", "Khác"};
    private static final String[] AVAILABLE_LANGUAGES = {"Tiếng Việt", "Tiếng Anh", "Tiếng Pháp", "Tiếng Trung", "Tiếng Nhật", "Tiếng Hàn"};

    public SuaHdvFragment() {}

    public static SuaHdvFragment newInstance(String guideId) {
        SuaHdvFragment fragment = new SuaHdvFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GUIDE_ID, guideId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            guideId = getArguments().getString(ARG_GUIDE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sua_hdv_dialog, container, false);
        mapViews(view);
        setupDropdowns();
        setupListeners();

        if (guideId != null) {
            loadGuideData(guideId);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID HDV.", Toast.LENGTH_LONG).show();
            closeFragment();
        }
        return view;
    }

    private void mapViews(View view) {
        btnCloseDialog = view.findViewById(R.id.btn_close_dialog);
        tvGuideIdStatic = view.findViewById(R.id.tv_guide_id_static);
        etFullName = view.findViewById(R.id.et_full_name);
        etGuideCode = view.findViewById(R.id.et_guide_code);
        etEmail = view.findViewById(R.id.et_email);
        etSdt = view.findViewById(R.id.et_sdt);
        etAddress = view.findViewById(R.id.et_address);
        etBirthDate = view.findViewById(R.id.et_birth_date);
        actvTrangThai = view.findViewById(R.id.actv_trang_thai);
        actvGender = view.findViewById(R.id.actv_gender);
        etExperienceYears = view.findViewById(R.id.et_experience_years);
        chipGroupLanguages = view.findViewById(R.id.chip_group_languages);
        tvRating = view.findViewById(R.id.tv_rating);
        chipAddLanguage = view.findViewById(R.id.chip_add_language);
        btnResetHdv = view.findViewById(R.id.btn_reset_hdv);
        btnSaveHdv = view.findViewById(R.id.btn_save_hdv);
    }

    private void setupDropdowns() {
        ArrayAdapter<String> adapterTrangThai = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, TRANG_THAI_DISPLAY);
        actvTrangThai.setAdapter(adapterTrangThai);

        ArrayAdapter<String> adapterGender = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, GENDER_OPTIONS);
        actvGender.setAdapter(adapterGender);
    }

    private void setupListeners() {
        btnCloseDialog.setOnClickListener(v -> closeFragment());
        btnResetHdv.setOnClickListener(v -> {
            if (currentGuide != null) displayGuideData(currentGuide);
        });
        btnSaveHdv.setOnClickListener(v -> validateAndSaveData());
        chipAddLanguage.setOnClickListener(v -> showLanguageSelectionDialog());
    }

    private void loadGuideData(String id) {
        db.collection(COLLECTION_HDV).document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentGuide = doc.toObject(Guide.class);
                        if (currentGuide != null) {
                            currentGuide.setId(doc.getId());
                            displayGuideData(currentGuide);
                        }
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy HDV.", Toast.LENGTH_SHORT).show();
                        closeFragment();
                    }
                })
                .addOnFailureListener(e -> closeFragment());
    }

    private void displayGuideData(Guide guide) {
        tvGuideIdStatic.setText("Mã hệ thống: " + guide.getId());
        etFullName.setText(guide.getFullName());
        etGuideCode.setText(guide.getGuideCode());
        etEmail.setText(guide.getEmail());
        etSdt.setText(guide.getPhoneNumber()); // Đổi từ getSdt sang getPhoneNumber
        etAddress.setText(guide.getAddress());
        etBirthDate.setText(guide.getBirthDate());

        // Hiển thị giới tính
        actvGender.setText(guide.getGender(), false);

        // Chuyển Boolean isApproved sang text hiển thị
        String statusText = (guide.isApproved()) ? ST_APPROVED : ST_PENDING;
        actvTrangThai.setText(statusText, false);

        etExperienceYears.setText(String.valueOf(guide.getExperienceYears()));
        tvRating.setText(String.format("%.1f Sao", guide.getRating()));

        updateLanguageChips(guide.getLanguages());
    }

    private void updateLanguageChips(List<String> languages) {
        chipGroupLanguages.removeAllViews();
        if (languages != null) {
            for (String lang : languages) addLanguageChip(lang);
        }
        chipGroupLanguages.addView(chipAddLanguage);
    }

    private void addLanguageChip(String lang) {
        Chip chip = new Chip(requireContext());
        chip.setText(lang);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroupLanguages.removeView(chip));
        int index = chipGroupLanguages.indexOfChild(chipAddLanguage);
        chipGroupLanguages.addView(chip, Math.max(index, 0));
    }

    private void showLanguageSelectionDialog() {
        List<String> currentSelected = new ArrayList<>();
        for (int i = 0; i < chipGroupLanguages.getChildCount(); i++) {
            View child = chipGroupLanguages.getChildAt(i);
            if (child instanceof Chip && child != chipAddLanguage) {
                currentSelected.add(((Chip) child).getText().toString());
            }
        }

        boolean[] checkedItems = new boolean[AVAILABLE_LANGUAGES.length];
        for (int i = 0; i < AVAILABLE_LANGUAGES.length; i++) {
            checkedItems[i] = currentSelected.contains(AVAILABLE_LANGUAGES[i]);
        }

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Chọn ngôn ngữ chuyên môn")
                .setMultiChoiceItems(AVAILABLE_LANGUAGES, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    List<String> newLangs = new ArrayList<>();
                    for (int i = 0; i < AVAILABLE_LANGUAGES.length; i++) {
                        if (checkedItems[i]) newLangs.add(AVAILABLE_LANGUAGES[i]);
                    }
                    updateLanguageChips(newLangs);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void validateAndSaveData() {
        String fullName = etFullName.getText().toString().trim();
        String guideCode = etGuideCode.getText().toString().trim();
        String phone = etSdt.getText().toString().trim();
        String statusText = actvTrangThai.getText().toString();

        if (fullName.isEmpty() || guideCode.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ các trường bắt buộc.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thu thập danh sách ngôn ngữ
        List<String> languages = new ArrayList<>();
        for (int i = 0; i < chipGroupLanguages.getChildCount(); i++) {
            View child = chipGroupLanguages.getChildAt(i);
            if (child instanceof Chip && child != chipAddLanguage) {
                languages.add(((Chip) child).getText().toString());
            }
        }

        // Tạo Map cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("guideCode", guideCode);
        updates.put("phoneNumber", phone);
        updates.put("email", etEmail.getText().toString().trim());
        updates.put("address", etAddress.getText().toString().trim());
        updates.put("birthDate", etBirthDate.getText().toString().trim());
        updates.put("gender", actvGender.getText().toString());
        updates.put("experienceYears", Integer.parseInt(etExperienceYears.getText().toString().isEmpty() ? "0" : etExperienceYears.getText().toString()));
        updates.put("languages", languages);

        // Chuyển text hiển thị ngược lại thành Boolean isApproved
        updates.put("isApproved", statusText.equals(ST_APPROVED));

        saveGuideData(updates);
    }

    private void saveGuideData(Map<String, Object> updates) {
        btnSaveHdv.setEnabled(false);
        db.collection(COLLECTION_HDV).document(guideId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    closeFragment();
                })
                .addOnFailureListener(e -> btnSaveHdv.setEnabled(true));
    }

    private void closeFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}