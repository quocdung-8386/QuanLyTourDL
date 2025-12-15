package com.example.quanlytourdl;

import android.app.DatePickerDialog;
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

import com.example.quanlytourdl.model.HopDong;
import com.example.quanlytourdl.model.NhaCungCap;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SuaHopDongFragment extends Fragment {

    private static final String TAG = "SuaHopDongFragment";
    private static final String COLLECTION_HOPDONG = "HopDong";
    private static final String COLLECTION_NCC = "NhaCungCap";

    // ⭐ SỬA LỖI 1: Đổi key Argument sang "contract_id" để đồng bộ với Fragment gọi.
    private static final String ARG_DOCUMENT_ID = "contract_id";

    // UI Components
    private ImageButton btnCloseDialog;
    private TextView tvTitle, tvContractCodeStatic, tvNgayCapNhat;
    private AutoCompleteTextView actvNhaCungCap, actvTrangThai;
    private TextInputEditText etNgayKyKet, etNgayHetHan, etNoiDung, etDieuKhoan, etLyDoChamDut;
    private TextInputLayout tilLyDoChamDut;
    private MaterialButton btnResetHopDong, btnSaveHopDong;

    // Data
    private FirebaseFirestore db;
    private String documentId;
    private HopDong currentHopDong;
    private List<NhaCungCap> nhaCungCapList = new ArrayList<>(); // Danh sách NCC để hiển thị dropdown

    // Trạng thái hợp đồng mẫu
    private static final String[] TRANG_THAI_HD = {"Có hiệu lực", "Đã hết hạn", "Đã chấm dứt", "Chờ ký"};

    public SuaHopDongFragment() {
        // Required empty public constructor
    }

    /**
     * Phương thức static newInstance để tạo Fragment và truyền Document ID.
     */
    public static SuaHopDongFragment newInstance(String documentId) {
        SuaHopDongFragment fragment = new SuaHopDongFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DOCUMENT_ID, documentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            // ⭐ SỬA LỖI 1: Lấy ID bằng key đã đồng bộ
            documentId = getArguments().getString(ARG_DOCUMENT_ID);
            Log.d(TAG, "Đang chỉnh sửa Hợp Đồng với Document ID: " + documentId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sua_hop_dong_dialog, container, false);
        mapViews(view);
        setupListeners();
        setupDropdowns();

        // Kiểm tra Document ID nhận được trước khi tải dữ liệu
        if (documentId != null && !documentId.isEmpty()) {
            loadAllNhaCungCap(); // Tải NCC trước, sau đó tải dữ liệu HĐ
        } else {
            // Đây là nơi lỗi "ID Hợp Đồng: null" trước đó xảy ra
            Log.e(TAG, "Lỗi: Document ID Hợp Đồng bị null hoặc rỗng.");
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID Hợp Đồng.", Toast.LENGTH_LONG).show();
            closeFragment();
        }

        return view;
    }

    private void mapViews(View view) {
        btnCloseDialog = view.findViewById(R.id.btn_close_dialog);
        tvTitle = view.findViewById(R.id.tv_title);
        tvContractCodeStatic = view.findViewById(R.id.tv_contract_code_static);
        tvNgayCapNhat = view.findViewById(R.id.tv_ngay_cap_nhat);

        // Input Fields
        actvNhaCungCap = view.findViewById(R.id.actv_nha_cung_cap);
        actvTrangThai = view.findViewById(R.id.actv_trang_thai);
        etNgayKyKet = view.findViewById(R.id.et_ngay_ky_ket);
        etNgayHetHan = view.findViewById(R.id.et_ngay_het_han);
        etNoiDung = view.findViewById(R.id.et_noi_dung);
        etDieuKhoan = view.findViewById(R.id.et_dieu_khoan);
        etLyDoChamDut = view.findViewById(R.id.et_ly_do_cham_dut);

        // Layouts
        tilLyDoChamDut = view.findViewById(R.id.til_ly_do_cham_dut);

        // Buttons
        btnResetHopDong = view.findViewById(R.id.btn_reset_hop_dong);
        btnSaveHopDong = view.findViewById(R.id.btn_save_hop_dong);
    }

    private void setupDropdowns() {
        // Adapter cho Trạng Thái Hợp Đồng
        ArrayAdapter<String> adapterTrangThai = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                TRANG_THAI_HD
        );
        actvTrangThai.setAdapter(adapterTrangThai);

        // Sự kiện thay đổi trạng thái
        actvTrangThai.setOnItemClickListener((parent, v, position, id) -> {
            String selectedStatus = TRANG_THAI_HD[position];
            // Hiển thị/Ẩn trường Lý do Chấm dứt
            if (selectedStatus.equals("Đã chấm dứt")) {
                tilLyDoChamDut.setVisibility(View.VISIBLE);
            } else {
                tilLyDoChamDut.setVisibility(View.GONE);
                etLyDoChamDut.setText(""); // Xóa nội dung nếu không còn trạng thái chấm dứt
            }
        });
    }

    private void setupListeners() {
        btnCloseDialog.setOnClickListener(v -> closeFragment());
        btnSaveHopDong.setOnClickListener(v -> validateAndSaveData());
        btnResetHopDong.setOnClickListener(v -> {
            if (currentHopDong != null) {
                displayHopDongData(currentHopDong);
                Toast.makeText(getContext(), "Đã đặt lại dữ liệu gốc.", Toast.LENGTH_SHORT).show();
            }
        });

        // Date Picker Listeners
        etNgayKyKet.setOnClickListener(v -> showDatePickerDialog(etNgayKyKet));
        etNgayHetHan.setOnClickListener(v -> showDatePickerDialog(etNgayHetHan));
    }

    /**
     * Hiển thị DatePickerDialog và cập nhật trường EditText.
     */
    private void showDatePickerDialog(final TextInputEditText targetEditText) {
        final Calendar c = Calendar.getInstance();
        if (!targetEditText.getText().toString().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                c.setTime(Objects.requireNonNull(sdf.parse(targetEditText.getText().toString())));
            } catch (Exception e) {
                // Sử dụng ngày hiện tại nếu parse lỗi
            }
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format yyyy-MM-dd
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    targetEditText.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Tải danh sách tất cả Nhà Cung Cấp để điền vào AutoCompleteTextView.
     */
    private void loadAllNhaCungCap() {
        db.collection(COLLECTION_NCC).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    nhaCungCapList.clear();
                    List<String> nccNames = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            NhaCungCap ncc = document.toObject(NhaCungCap.class);
                            ncc.setMaNhaCungCap(document.getId());
                            nhaCungCapList.add(ncc);
                            nccNames.add(ncc.getTenNhaCungCap());
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi chuyển đổi dữ liệu NCC", e);
                        }
                    }

                    // Thiết lập Adapter cho NCC
                    ArrayAdapter<String> adapterNcc = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            nccNames
                    );
                    actvNhaCungCap.setAdapter(adapterNcc);

                    // Sau khi tải NCC xong, tải dữ liệu Hợp đồng
                    loadHopDongData(documentId);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải danh sách NCC", e);
                    Toast.makeText(getContext(), "Lỗi tải NCC: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    closeFragment();
                });
    }

    /**
     * Tải dữ liệu Hợp Đồng hiện tại từ Firestore.
     */
    private void loadHopDongData(String docId) {
        db.collection(COLLECTION_HOPDONG).document(docId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentHopDong = documentSnapshot.toObject(HopDong.class);
                        if (currentHopDong != null) {
                            currentHopDong.setDocumentId(documentSnapshot.getId());
                            displayHopDongData(currentHopDong);
                        } else {
                            Toast.makeText(getContext(), "Lỗi chuyển đổi dữ liệu Hợp Đồng.", Toast.LENGTH_LONG).show();
                            closeFragment();
                        }
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy Hợp Đồng này.", Toast.LENGTH_LONG).show();
                        closeFragment();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải dữ liệu Hợp Đồng", e);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu HĐ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    closeFragment();
                });
    }

    /**
     * Hiển thị dữ liệu lên UI.
     */
    private void displayHopDongData(HopDong hd) {
        tvContractCodeStatic.setText(String.format("Mã HD: %s (ID Document: %s)", hd.getMaHopDong(), hd.getDocumentId()));

        // Ánh xạ các trường
        // Chú ý: Trường NhaCungCap trong model có thể khác tên trường tenNhaCungCap trong Firestore.
        // Giả định model HopDong có getter/setter NhaCungCap
        actvNhaCungCap.setText(hd.getNhaCungCap(), false);
        etNgayKyKet.setText(hd.getNgayKyKet());
        etNgayHetHan.setText(hd.getNgayHetHan());
        actvTrangThai.setText(hd.getTrangThai(), false);
        etNoiDung.setText(hd.getNoiDung());
        etDieuKhoan.setText(hd.getDieuKhoanThanhToan());
        tvNgayCapNhat.setText(String.format("Cập nhật cuối: %s", hd.getNgayCapNhat() != null ? hd.getNgayCapNhat() : "Chưa có"));

        // Xử lý Lý do Chấm dứt (ẩn/hiện)
        if (hd.getTrangThai() != null && hd.getTrangThai().equals("Đã chấm dứt")) {
            tilLyDoChamDut.setVisibility(View.VISIBLE);
            etLyDoChamDut.setText(hd.getLyDoChamDut());
        } else {
            tilLyDoChamDut.setVisibility(View.GONE);
            etLyDoChamDut.setText("");
        }
    }

    /**
     * Xác thực dữ liệu và gọi hàm lưu.
     */
    private void validateAndSaveData() {
        if (currentHopDong == null) {
            Toast.makeText(getContext(), "Lỗi: Không có dữ liệu hợp đồng gốc.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy dữ liệu từ UI
        String nhaCungCapName = Objects.requireNonNull(actvNhaCungCap.getText()).toString().trim();
        String ngayKyKet = Objects.requireNonNull(etNgayKyKet.getText()).toString().trim();
        String ngayHetHan = Objects.requireNonNull(etNgayHetHan.getText()).toString().trim();
        String trangThai = Objects.requireNonNull(actvTrangThai.getText()).toString().trim();
        String noiDung = Objects.requireNonNull(etNoiDung.getText()).toString().trim();
        String dieuKhoan = Objects.requireNonNull(etDieuKhoan.getText()).toString().trim();
        String lyDoChamDut = Objects.requireNonNull(etLyDoChamDut.getText()).toString().trim();

        // 1. Xác thực cơ bản
        if (nhaCungCapName.isEmpty() || ngayKyKet.isEmpty() || trangThai.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ NCC, Ngày ký và Trạng thái.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Tìm supplierId từ tên NCC
        String supplierId = findSupplierIdByName(nhaCungCapName);
        if (supplierId == null) {
            Toast.makeText(getContext(), "Nhà Cung Cấp không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Xử lý Lý do Chấm dứt
        if (trangThai.equals("Đã chấm dứt") && lyDoChamDut.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập Lý do Chấm dứt Hợp đồng.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Tạo Map chứa các trường cần cập nhật
        Map<String, Object> updates = new HashMap<>();

        // ⭐ SỬA LỖI 2: Đảm bảo tên trường cập nhật khớp với Firestore (như trong ảnh bạn cung cấp)
        updates.put("tenNhaCungCap", nhaCungCapName); // Trong ảnh là tenNhaCungCap
        updates.put("supplierId", supplierId);
        updates.put("ngayKy", ngayKyKet);             // Trong ảnh là ngayKy
        updates.put("ngayHetHan", ngayHetHan);
        updates.put("trangThai", trangThai);
        updates.put("noiDung", noiDung);
        updates.put("dieuKhoanThanhToan", dieuKhoan); // Trong ảnh là dieuKhoanThanhToan

        updates.put("ngayCapNhat", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()));

        // Trường Chấm dứt
        if (trangThai.equals("Đã chấm dứt")) {
            updates.put("lyDoChamDut", lyDoChamDut);
        } else {
            updates.put("lyDoChamDut", null); // Đảm bảo xóa trường nếu không phải trạng thái chấm dứt
        }

        // Gọi hàm cập nhật
        saveHopDongData(updates);
    }

    /**
     * Tìm kiếm ID của Nhà Cung Cấp dựa trên tên đã chọn.
     */
    private String findSupplierIdByName(String name) {
        for (NhaCungCap ncc : nhaCungCapList) {
            if (ncc.getTenNhaCungCap().equals(name)) {
                return ncc.getMaNhaCungCap();
            }
        }
        return null;
    }

    /**
     * Cập nhật dữ liệu lên Firestore.
     */
    private void saveHopDongData(Map<String, Object> updates) {
        if (documentId == null) {
            Toast.makeText(getContext(), "Lỗi hệ thống: Không có ID để cập nhật.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection(COLLECTION_HOPDONG).document(documentId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật Hợp Đồng thành công!", Toast.LENGTH_SHORT).show();
                    closeFragment();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi cập nhật Hợp Đồng", e);
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