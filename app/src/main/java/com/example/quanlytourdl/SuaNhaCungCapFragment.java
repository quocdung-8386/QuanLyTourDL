package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quanlytourdl.model.NhaCungCap;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class SuaNhaCungCapFragment extends Fragment {

    private static final String TAG = "SuaNhaCungCapFragment";
    private static final String ARG_SUPPLIER_ID = "supplier_id";

    // Khai báo các View
    private EditText edtSupplierName, edtAddress, edtPhone, edtEmail, edtContactPerson;
    private Spinner spinnerServiceType;
    private Button btnCancel, btnSaveChanges; // btn_create_new trong XML đóng vai trò nút Lưu
    private ImageView iconClose;

    // Khai báo Firebase & Data
    private FirebaseFirestore db;
    private String currentSupplierId;
    private final List<String> serviceTypes = Arrays.asList("Khách sạn", "Vận chuyển", "Ăn uống", "Tham quan", "Khác");

    public SuaNhaCungCapFragment() {
        // Constructor rỗng bắt buộc
    }

    // Phương thức static để tạo Fragment và truyền ID
    public static SuaNhaCungCapFragment newInstance(String supplierId) {
        SuaNhaCungCapFragment fragment = new SuaNhaCungCapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUPPLIER_ID, supplierId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSupplierId = getArguments().getString(ARG_SUPPLIER_ID);
        }
        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout fragment_sua_nha_cung_cap.xml (hoặc fragment_tao_nha_cung_cap nếu bạn tái sử dụng)
        View view = inflater.inflate(R.layout.fragment_sua_nha_cung_cap, container, false);

        // 1. Ánh xạ View (Khớp ID với file XML bạn đã cung cấp)
        edtSupplierName = view.findViewById(R.id.edt_supplier_name);
        edtAddress = view.findViewById(R.id.edt_address);
        edtPhone = view.findViewById(R.id.edt_phone);
        edtEmail = view.findViewById(R.id.edt_email);
        edtContactPerson = view.findViewById(R.id.edt_contact_person);
        spinnerServiceType = view.findViewById(R.id.spinner_service_type);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSaveChanges = view.findViewById(R.id.btn_create_new); // Nút này tên ID là create_new nhưng text là "Lưu thay đổi"
        iconClose = view.findViewById(R.id.icon_close);

        // 2. Thiết lập Spinner
        setupSpinner();

        // 3. Tải dữ liệu nếu có ID
        if (currentSupplierId != null && !currentSupplierId.isEmpty()) {
            loadSupplierData(currentSupplierId);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID nhà cung cấp", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        }

        // 4. Xử lý sự kiện
        setupEvents();

        return view;
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                serviceTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServiceType.setAdapter(adapter);
    }

    private void setupEvents() {
        // Nút Đóng và Hủy -> Quay lại
        View.OnClickListener closeAction = v -> getParentFragmentManager().popBackStack();
        iconClose.setOnClickListener(closeAction);
        btnCancel.setOnClickListener(closeAction);

        // Nút Lưu thay đổi
        btnSaveChanges.setOnClickListener(v -> {
            if (validateInputs()) {
                updateSupplierToFirestore();
            }
        });
    }

    private void loadSupplierData(String id) {
        db.collection("NhaCungCap").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        NhaCungCap ncc = documentSnapshot.toObject(NhaCungCap.class);
                        if (ncc != null) {
                            // Điền dữ liệu vào form
                            edtSupplierName.setText(ncc.getTenNhaCungCap());
                            edtAddress.setText(ncc.getDiaChi());
                            edtPhone.setText(ncc.getSoDienThoai());
                            edtEmail.setText(ncc.getEmail());
                            edtContactPerson.setText(ncc.getNguoiLienHe());

                            // Chọn giá trị cho Spinner
                            int spinnerPosition = serviceTypes.indexOf(ncc.getLoaiDichVu());
                            if (spinnerPosition >= 0) {
                                spinnerServiceType.setSelection(spinnerPosition);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Nhà cung cấp không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading supplier", e);
                });
    }

    private void updateSupplierToFirestore() {
        String name = edtSupplierName.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String contact = edtContactPerson.getText().toString().trim();
        String type = spinnerServiceType.getSelectedItem().toString();

        // Cập nhật đối tượng (Giữ nguyên maNguoiDungTao cũ nếu cần, hoặc load lại từ doc)
        // Ở đây ta dùng update() của Firestore để chỉ cập nhật các trường thay đổi

        db.collection("NhaCungCap").document(currentSupplierId)
                .update(
                        "tenNhaCungCap", name,
                        "diaChi", address,
                        "soDienThoai", phone,
                        "email", email,
                        "nguoiLienHe", contact,
                        "loaiDichVu", type
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Quay lại danh sách
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating supplier", e);
                });
    }

    private boolean validateInputs() {
        if (edtSupplierName.getText().toString().trim().isEmpty()) {
            edtSupplierName.setError("Tên không được để trống");
            return false;
        }
        if (edtPhone.getText().toString().trim().isEmpty()) {
            edtPhone.setError("SĐT không được để trống");
            return false;
        }
        return true;
    }
}