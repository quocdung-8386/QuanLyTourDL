package com.example.quanlytourdl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class TaoNhaCungCapFragment extends Fragment {

    private static final String TAG = "TaoNhaCungCapFragment";

    // THAY ĐỔI: Sử dụng FirebaseFirestore
    private FirebaseFirestore db;
    private CollectionReference nhaCungCapRef;
    private FirebaseAuth mAuth;

    private EditText etTenNhaCungCap, etDiaChi, etSoDienThoai, etEmail, etNguoiLienHe;
    private Spinner spLoaiDichVu;
    private Button btnTaoMoi, btnHuy;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tao_nha_cung_cap, container, false);

        // Khởi tạo Firestore và Auth
        db = FirebaseFirestore.getInstance();
        nhaCungCapRef = db.collection("NhaCungCap"); // Tham chiếu đến collection 'NhaCungCap'
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
        btnHuy.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void setupSpinner() {
        // Danh sách dịch vụ (ví dụ)
        String[] loaiDichVuArray = new String[]{"Khách sạn", "Vận chuyển", "Ăn uống", "Tham quan"};
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

        // Kiểm tra dữ liệu đầu vào đơn giản
        if (ten.isEmpty() || diaChi.isEmpty() || sdt.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ các trường bắt buộc.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy UID của người dùng đang đăng nhập (hoặc sử dụng một ID mặc định nếu chưa đăng nhập)
        // Vì Rules đang mở (if true), nên việc không có UID cũng không bị lỗi Permission Denied.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String maNguoiDungTao = (currentUser != null) ? currentUser.getUid() : "anonymous_public";

        // Tạo đối tượng NhaCungCap
        NhaCungCap newSupplier = new NhaCungCap(ten, diaChi, sdt, email, nguoiLH, loaiDV, maNguoiDungTao);

        // Gọi hàm lưu vào Firestore
        saveNewSupplierToFirestore(newSupplier);
    }

    private void saveNewSupplierToFirestore(NhaCungCap newSupplier) {

        // THAY ĐỔI: Sử dụng add() để Firestore tự động tạo ID và document
        nhaCungCapRef.add(newSupplier)
                .addOnSuccessListener(documentReference -> {
                    // Lấy ID tự động tạo của document vừa được thêm
                    String supplierId = documentReference.getId();
                    newSupplier.setMaNhaCungCap(supplierId); // Cập nhật lại model nếu cần

                    Toast.makeText(getContext(), "Tạo nhà cung cấp THÀNH CÔNG! ID: " + supplierId, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Đã thêm Nhà Cung Cấp với ID: " + supplierId);

                    // Sau khi thêm thành công, có thể quay lại fragment trước đó
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    // Dù rules đang mở, vẫn bắt lỗi để đề phòng các lỗi khác như lỗi mạng
                    Toast.makeText(getContext(), "LỖI LƯU DỮ LIỆU: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Lỗi lưu Nhà cung cấp vào Firestore: ", e);
                });
    }
}