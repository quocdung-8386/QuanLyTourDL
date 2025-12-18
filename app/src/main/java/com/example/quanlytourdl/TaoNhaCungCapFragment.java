package com.example.quanlytourdl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.Timestamp; // Cần import Timestamp

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

        db = FirebaseFirestore.getInstance();
        nhaCungCapRef = db.collection("NhaCungCap");
        mAuth = FirebaseAuth.getInstance();

        etTenNhaCungCap = view.findViewById(R.id.edt_supplier_name);
        etDiaChi = view.findViewById(R.id.edt_address);
        etSoDienThoai = view.findViewById(R.id.edt_phone);
        etEmail = view.findViewById(R.id.edt_email);
        etNguoiLienHe = view.findViewById(R.id.edt_contact_person);
        spLoaiDichVu = view.findViewById(R.id.spinner_service_type);
        btnTaoMoi = view.findViewById(R.id.btn_create_new);
        btnHuy = view.findViewById(R.id.btn_cancel);

        setupSpinner();

        btnTaoMoi.setOnClickListener(v -> taoNhaCungCap());
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

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Email không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnTaoMoi.setEnabled(false);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String maNguoiDungTao = (currentUser != null) ? currentUser.getUid() : "anonymous_public";

        // ⭐ CẬP NHẬT: Truyền đúng 13 tham số cho Constructor mới của model NhaCungCap
        NhaCungCap newSupplier = new NhaCungCap(
                ten,
                diaChi,
                sdt,
                email,
                nguoiLH,
                loaiDV,
                null,
                maNguoiDungTao,
                "Chưa có hợp đồng",
                null,
                0.0f,
                "Chưa có đánh giá",
                null
        );

        saveNewSupplierWithAutoId(newSupplier);
    }

    private void saveNewSupplierWithAutoId(NhaCungCap newSupplier) {
        final DocumentReference counterRef = db.collection("counters").document("supplier_id");

        db.runTransaction((Transaction.Function<String>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef);

            long currentId = 0L;
            if (snapshot.exists()) {
                Long val = snapshot.getLong("current_id");
                if (val != null) currentId = val;
            }

            long nextId = currentId + 1;
            String formattedId = String.format(Locale.US, "NCC-%04d", nextId);

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("current_id", nextId);
            transaction.set(counterRef, updateData);

            newSupplier.setMaNhaCungCap(formattedId);
            DocumentReference newSupplierRef = nhaCungCapRef.document(formattedId);
            transaction.set(newSupplierRef, newSupplier);

            return formattedId;

        }).addOnSuccessListener(generatedId -> {
            Toast.makeText(getContext(), "Tạo thành công! ID: " + generatedId, Toast.LENGTH_LONG).show();
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        }).addOnFailureListener(e -> {
            btnTaoMoi.setEnabled(true);
            Toast.makeText(getContext(), "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Transaction failed: ", e);
        });
    }
}