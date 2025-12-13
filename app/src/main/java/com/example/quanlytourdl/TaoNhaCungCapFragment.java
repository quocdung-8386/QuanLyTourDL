package com.example.quanlytourdl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.quanlytourdl.model.NhaCungCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaoNhaCungCapFragment extends Fragment {

    private static final String TAG = "TaoNhaCungCapFragment";

    private FirebaseFirestore db;
    private CollectionReference nhaCungCapRef;
    private FirebaseAuth mAuth;

    private EditText etTenNhaCungCap, etDiaChi, etSoDienThoai, etEmail, etNguoiLienHe;
    private Spinner spLoaiDichVu;
    private Button btnTaoMoi, btnHuy;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int layoutId = getResources().getIdentifier("fragment_tao_nha_cung_cap", "layout", requireContext().getPackageName());
        if (layoutId == 0) {
            Log.e(TAG, "Không tìm thấy layout ID 'fragment_tao_nha_cung_cap'.");
            return new View(requireContext());
        }

        View view = inflater.inflate(layoutId, container, false);

        // Khởi tạo Firestore và Auth
        db = FirebaseFirestore.getInstance();
        nhaCungCapRef = db.collection("NhaCungCap");
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các thành phần UI
        etTenNhaCungCap = view.findViewById(R.id.edt_supplier_name);
        etDiaChi = view.findViewById(R.id.edt_address);
        etSoDienThoai = view.findViewById(R.id.edt_phone);
        etEmail = view.findViewById(R.id.edt_email);
        etNguoiLienHe = view.findViewById(R.id.edt_contact_person);
        spLoaiDichVu = view.findViewById(R.id.spinner_service_type);
        btnTaoMoi = view.findViewById(R.id.btn_create_new);
        btnHuy = view.findViewById(R.id.btn_cancel);

        // Thiết lập Spinner
        setupSpinner();

        // Xử lý sự kiện click
        btnTaoMoi.setOnClickListener(v -> taoNhaCungCap());

        // Logic nút Hủy: Quay lại Fragment trước đó
        btnHuy.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void setupSpinner() {
        String[] loaiDichVuArray = new String[]{"Khách sạn", "Vận chuyển", "Ăn uống", "Tham quan", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, loaiDichVuArray);
        spLoaiDichVu.setAdapter(adapter);
    }

    private void taoNhaCungCap() {
        String ten = etTenNhaCungCap.getText().toString().trim();
        String diaChi = etDiaChi.getText().toString().trim();
        String sdt = etSoDienThoai.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String nguoiLH = etNguoiLienHe.getText().toString().trim();
        String loaiDV = spLoaiDichVu.getSelectedItem().toString();

        if (ten.isEmpty() || diaChi.isEmpty() || sdt.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ các trường bắt buộc.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String maNguoiDungTao = (currentUser != null) ? currentUser.getUid() : "anonymous_public";

        NhaCungCap newSupplier = new NhaCungCap(
                ten,
                diaChi,
                sdt,
                email,
                nguoiLH,
                loaiDV,
                null, // maHopDongActive
                maNguoiDungTao,
                null, // trangThaiHopDong
                null  // maHopDongGanNhat
        );

        saveNewSupplierToFirestore(newSupplier);
    }

    private void saveNewSupplierToFirestore(NhaCungCap newSupplier) {
        if (db == null) return;

        // Tham chiếu đến document bộ đếm ID
        final DocumentReference counterRef = db.collection("counters").document("supplier_id");

        // Logic Firebase Transaction để tạo ID NCC-XXXX
        db.runTransaction((Transaction.Function<Void>) transaction -> {

                    Long currentId;
                    try {
                        // 1. Đọc giá trị hiện tại của bộ đếm
                        currentId = transaction.get(counterRef).getLong("current_id");
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi đọc bộ đếm ID: ", e);
                        // Ném ngoại lệ để hủy transaction nếu không đọc được
                        try {
                            throw new Exception("Lỗi cấu hình Firestore, không thể đọc bộ đếm ID nhà cung cấp.");
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    if (currentId == null) {
                        currentId = 0L;
                    }

                    // 2. Tăng giá trị bộ đếm lên 1
                    long nextId = currentId + 1;

                    // 3. Định dạng ID mới
                    String supplierId = String.format(Locale.US, "NCC-%04d", nextId);

                    // 4. Cập nhật lại bộ đếm
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("current_id", nextId);
                    // ⭐ ĐÃ SỬA LỖI: Sử dụng transaction.set() thay vì transaction.update()
                    // để tạo document nếu nó chưa tồn tại (khắc phục lỗi 'Can't update a document that doesn't exist.').
                    transaction.set(counterRef, updateData);

                    // 5. Cập nhật ID vào đối tượng và lưu document mới
                    newSupplier.setMaNhaCungCap(supplierId);

                    DocumentReference newSupplierRef = nhaCungCapRef.document(supplierId);
                    transaction.set(newSupplierRef, newSupplier);

                    return null;

                }).addOnSuccessListener(aVoid -> {
                    // Xử lý thành công
                    Toast.makeText(getContext(), "Tạo nhà cung cấp THÀNH CÔNG! ID: " + newSupplier.getMaNhaCungCap(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Đã thêm Nhà Cung Cấp với ID: " + newSupplier.getMaNhaCungCap());

                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi Transaction
                    Toast.makeText(getContext(), "LỖI LƯU DỮ LIỆU: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Lỗi Transaction khi lưu Nhà cung cấp: ", e);
                });
    }
}