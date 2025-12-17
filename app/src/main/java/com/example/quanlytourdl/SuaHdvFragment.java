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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SuaHdvFragment extends Fragment {

    private static final String TAG = "SuaHdvFragment";
    private static final String COLLECTION_HDV = "tour_guides";
    private static final String ARG_GUIDE_ID = "guide_id"; // ⭐ Hằng số cho Bundle Key

    // UI Components
    private ImageButton btnCloseDialog;
    private TextInputEditText etFullName, etGuideCode, etEmail, etSdt, etExperienceYears;
    private AutoCompleteTextView actvTrangThai;
    private TextView tvGuideIdStatic, tvRating;
    private ChipGroup chipGroupLanguages;
    private MaterialButton btnResetHdv, btnSaveHdv;
    private Chip chipAddLanguage;

    // Data
    private FirebaseFirestore db;
    private String guideId;
    private Guide currentGuide; // Lưu trữ đối tượng HDV hiện tại

    // Trạng thái mẫu (Ví dụ)
    private static final String[] TRANG_THAI_HDV = {"Available", "On Tour", "On Leave", "Training"};

    // Ngôn ngữ mẫu (Ví dụ)
    private static final String[] AVAILABLE_LANGUAGES = {"Tiếng Anh", "Tiếng Pháp", "Tiếng Trung", "Tiếng Đức", "Tiếng Nhật"};


    public SuaHdvFragment() {
        // Required empty public constructor
    }

    /**
     * ⭐ Phương thức static newInstance để tạo Fragment và truyền ID.
     * Phương thức này giúp DetailFragment gọi Fragment một cách an toàn và nhất quán.
     */
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

        // Nhận ID từ Bundle
        if (getArguments() != null) {
            guideId = getArguments().getString(ARG_GUIDE_ID); // ⭐ Dùng hằng số ARG_GUIDE_ID
            Log.d(TAG, "Đang chỉnh sửa HDV với ID: " + guideId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sua_hdv_dialog, container, false);
        mapViews(view);

        // Thiết lập Adapter cho Dropdown Trạng thái
        setupTrangThaiDropdown();

        setupListeners();

        if (guideId != null) {
            loadGuideData(guideId);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID HDV.", Toast.LENGTH_LONG).show();
            // Đóng Fragment nếu không có ID
            closeFragment();
        }

        return view;
    }

    private void mapViews(View view) {
        btnCloseDialog = view.findViewById(R.id.btn_close_dialog);

        // Thông tin cơ bản
        tvGuideIdStatic = view.findViewById(R.id.tv_guide_id_static);
        etFullName = view.findViewById(R.id.et_full_name);
        etGuideCode = view.findViewById(R.id.et_guide_code);

        // Liên hệ & Trạng thái
        etEmail = view.findViewById(R.id.et_email);
        etSdt = view.findViewById(R.id.et_sdt);
        actvTrangThai = view.findViewById(R.id.actv_trang_thai); // Dropdown Trạng thái

        // Chuyên môn
        etExperienceYears = view.findViewById(R.id.et_experience_years);
        chipGroupLanguages = view.findViewById(R.id.chip_group_languages);
        tvRating = view.findViewById(R.id.tv_rating);
        chipAddLanguage = view.findViewById(R.id.chip_add_language);

        // Nút hành động
        btnResetHdv = view.findViewById(R.id.btn_reset_hdv);
        btnSaveHdv = view.findViewById(R.id.btn_save_hdv);
    }

    private void setupTrangThaiDropdown() {
        ArrayAdapter<String> adapterTrangThai = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                TRANG_THAI_HDV
        );
        actvTrangThai.setAdapter(adapterTrangThai);
    }

    private void setupListeners() {
        // Nút đóng Fragment
        btnCloseDialog.setOnClickListener(v -> closeFragment());

        // Nút Reset
        btnResetHdv.setOnClickListener(v -> {
            if (currentGuide != null) {
                displayGuideData(currentGuide); // Load lại dữ liệu gốc
                Toast.makeText(getContext(), "Đã đặt lại dữ liệu gốc.", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Lưu
        btnSaveHdv.setOnClickListener(v -> validateAndSaveData());

        // Xử lý Chip thêm Ngôn ngữ
        chipAddLanguage.setOnClickListener(v -> showLanguageSelectionDialog());
    }

    /**
     * Tải dữ liệu HDV hiện tại từ Firestore.
     */
    private void loadGuideData(String id) {
        db.collection(COLLECTION_HDV).document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentGuide = documentSnapshot.toObject(Guide.class);
                        if (currentGuide != null) {
                            currentGuide.setId(documentSnapshot.getId());
                            displayGuideData(currentGuide);
                        } else {
                            Toast.makeText(getContext(), "Lỗi chuyển đổi dữ liệu HDV.", Toast.LENGTH_LONG).show();
                            closeFragment();
                        }
                    } else {
                        // ⭐ Nếu COLLECTION_HDV bị sai tên (ví dụ: 'tourguides' thay vì 'tour_guides')
                        // thì lỗi "Không tìm thấy HDV này." sẽ xảy ra ở đây.
                        Toast.makeText(getContext(), "Không tìm thấy HDV này.", Toast.LENGTH_LONG).show();
                        closeFragment();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải dữ liệu HDV", e);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    closeFragment();
                });
    }

    /**
     * Hiển thị dữ liệu lên UI.
     */
    private void displayGuideData(Guide guide) {
        tvGuideIdStatic.setText(String.format("ID: %s (Không thể chỉnh sửa)", guide.getId()));
        etFullName.setText(guide.getFullName());
        etGuideCode.setText(guide.getGuideCode());
        etEmail.setText(guide.getEmail());
        etSdt.setText(guide.getSdt());

        // Trạng thái và Kinh nghiệm
        actvTrangThai.setText(guide.getTrangThai(), false); // false để không trigger dropdown khi set
        // Xử lý giá trị null/0 cho experienceYears
        String experience = (guide.getExperienceYears() > 0) ? String.valueOf(guide.getExperienceYears()) : "";
        etExperienceYears.setText(experience);

        // Đánh giá (Chỉ đọc)
        tvRating.setText(String.format("%.1f", guide.getRating()));

        // Ngôn ngữ
        updateLanguageChips(guide.getLanguages());
    }

    /**
     * Cập nhật ChipGroup hiển thị ngôn ngữ.
     */
    private void updateLanguageChips(List<String> languages) {
        chipGroupLanguages.removeAllViews();
        if (languages != null) {
            for (String lang : languages) {
                addLanguageChip(lang);
            }
        }
        // Luôn thêm nút 'Thêm ngôn ngữ' sau cùng
        chipGroupLanguages.addView(chipAddLanguage);
    }

    /**
     * Thêm một chip ngôn ngữ vào ChipGroup
     */
    private void addLanguageChip(String lang) {
        Chip chip = new Chip(requireContext());
        chip.setText(lang);
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);
        chip.setOnCloseIconClickListener(v -> {
            // Xóa chip khi nhấn biểu tượng đóng
            chipGroupLanguages.removeView(chip);
        });
        // Chèn chip trước nút 'Thêm ngôn ngữ' (Nếu đã tồn tại)
        // Dùng Index - 1 để luôn đặt chip mới vào vị trí cuối cùng trước nút thêm
        int addButtonIndex = chipGroupLanguages.indexOfChild(chipAddLanguage);
        if (addButtonIndex != -1) {
            chipGroupLanguages.addView(chip, addButtonIndex);
        } else {
            chipGroupLanguages.addView(chip);
        }
    }

    /**
     * Hiển thị hộp thoại chọn ngôn ngữ.
     */
    private void showLanguageSelectionDialog() {
        // Lấy danh sách ngôn ngữ hiện tại trong ChipGroup (trừ nút thêm)
        List<String> currentSelected = new ArrayList<>();
        for (int i = 0; i < chipGroupLanguages.getChildCount(); i++) {
            View child = chipGroupLanguages.getChildAt(i);
            if (child instanceof Chip && child.getId() != R.id.chip_add_language) {
                currentSelected.add(((Chip) child).getText().toString());
            }
        }

        // Tạo boolean array cho trạng thái check của các ngôn ngữ mẫu
        boolean[] checkedItems = new boolean[AVAILABLE_LANGUAGES.length];
        for (int i = 0; i < AVAILABLE_LANGUAGES.length; i++) {
            checkedItems[i] = currentSelected.contains(AVAILABLE_LANGUAGES[i]);
        }

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Chọn Ngôn ngữ")
                .setMultiChoiceItems(AVAILABLE_LANGUAGES, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    List<String> newLanguages = new ArrayList<>();
                    for (int i = 0; i < AVAILABLE_LANGUAGES.length; i++) {
                        if (checkedItems[i]) {
                            newLanguages.add(AVAILABLE_LANGUAGES[i]);
                        }
                    }
                    updateLanguageChips(newLanguages);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Xác thực dữ liệu và gọi hàm lưu.
     */
    private void validateAndSaveData() {
        String fullName = Objects.requireNonNull(etFullName.getText()).toString().trim();
        String guideCode = Objects.requireNonNull(etGuideCode.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String sdt = Objects.requireNonNull(etSdt.getText()).toString().trim();
        String trangThai = Objects.requireNonNull(actvTrangThai.getText()).toString().trim();
        String experienceStr = Objects.requireNonNull(etExperienceYears.getText()).toString().trim();

        // 1. Xác thực cơ bản
        if (fullName.isEmpty() || guideCode.isEmpty() || trangThai.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền Tên, Mã HDV và Trạng thái.", Toast.LENGTH_SHORT).show();
            return;
        }

        int experienceYears = 0;
        if (!experienceStr.isEmpty()) {
            try {
                experienceYears = Integer.parseInt(experienceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số năm kinh nghiệm không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Lấy danh sách ngôn ngữ hiện tại
        List<String> languagesToSave = new ArrayList<>();
        for (int i = 0; i < chipGroupLanguages.getChildCount(); i++) {
            View child = chipGroupLanguages.getChildAt(i);
            if (child instanceof Chip && child.getId() != R.id.chip_add_language) {
                languagesToSave.add(((Chip) child).getText().toString());
            }
        }

        // Tạo Map chứa các trường cần cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("guideCode", guideCode);
        updates.put("email", email.isEmpty() ? null : email); // Lưu null nếu rỗng
        updates.put("sdt", sdt.isEmpty() ? null : sdt);       // Lưu null nếu rỗng
        updates.put("trangThai", trangThai);
        updates.put("experienceYears", experienceYears);
        updates.put("languages", languagesToSave);
        // Giữ nguyên rating, không cho phép sửa
        // updates.put("rating", currentGuide.getRating());

        // Gọi hàm cập nhật
        saveGuideData(updates);
    }

    /**
     * Cập nhật dữ liệu lên Firestore.
     */
    private void saveGuideData(Map<String, Object> updates) {
        if (guideId == null) {
            Toast.makeText(getContext(), "Lỗi hệ thống: Không có ID để cập nhật.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection(COLLECTION_HDV).document(guideId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật HDV thành công!", Toast.LENGTH_SHORT).show();
                    closeFragment();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi cập nhật HDV", e);
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