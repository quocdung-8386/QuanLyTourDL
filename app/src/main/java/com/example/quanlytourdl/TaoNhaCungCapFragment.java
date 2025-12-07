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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;

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
        // Inflate the layout for this fragment
        // Lấy ID layout thông qua tên chuỗi (giả định)
        int layoutId = getResources().getIdentifier("fragment_tao_nha_cung_cap", "layout", requireContext().getPackageName());
        if (layoutId == 0) {
            Log.e(TAG, "Không tìm thấy layout ID 'fragment_tao_nha_cung_cap'.");
            // Xử lý khi không tìm thấy layout, ví dụ: trả về View trống
            return new View(requireContext());
        }

        View view = inflater.inflate(layoutId, container, false);

        // Khởi tạo Firestore và Auth
        db = FirebaseFirestore.getInstance();
        nhaCungCapRef = db.collection("NhaCungCap"); // Tham chiếu đến collection 'NhaCungCap'
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các thành phần UI
        // Giả định R.id.* tương ứng với layout
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
        btnHuy.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void setupSpinner() {
        // Danh sách dịch vụ (ví dụ)
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

        // Kiểm tra dữ liệu đầu vào đơn giản
        if (ten.isEmpty() || diaChi.isEmpty() || sdt.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ các trường bắt buộc.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy UID của người dùng đang đăng nhập
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String maNguoiDungTao = (currentUser != null) ? currentUser.getUid() : "anonymous_public";

        // --- ĐIỀU CHỈNH ĐỂ KHỚP VỚI CONSTRUCTOR 10 THAM SỐ CỦA NhaCungCap ---
        String maHopDongActive = null; // Ban đầu là null
        String trangThaiHopDong = null; // Trường mới - Ban đầu là null
        String maHopDongGanNhat = null; // Trường mới - Ban đầu là null


        // Tạo đối tượng NhaCungCap với 10 tham số
        NhaCungCap newSupplier = new NhaCungCap(
                ten,
                diaChi,
                sdt,
                email,
                nguoiLH,
                loaiDV,
                maHopDongActive,
                maNguoiDungTao,
                trangThaiHopDong, // Tham số mới
                maHopDongGanNhat  // Tham số mới
        );

        // Gọi hàm lưu vào Firestore
        saveNewSupplierToFirestore(newSupplier);
    }

    private void saveNewSupplierToFirestore(NhaCungCap newSupplier) {

        // THAY ĐỔI: Sử dụng add() để Firestore tự động tạo ID và document
        nhaCungCapRef.add(newSupplier)
                .addOnSuccessListener(documentReference -> {
                    // Lấy ID tự động tạo của document vừa được thêm
                    String supplierId = documentReference.getId();
                    // Chúng ta có thể set ID này lại vào đối tượng, mặc dù Firestore không cần,
                    // nhưng hữu ích nếu chúng ta muốn xử lý đối tượng này tiếp sau khi lưu.
                    newSupplier.setMaNhaCungCap(supplierId);

                    Toast.makeText(getContext(), "Tạo nhà cung cấp THÀNH CÔNG! ID: " + supplierId, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Đã thêm Nhà Cung Cấp với ID: " + supplierId);

                    // Sau khi thêm thành công, có thể quay lại fragment trước đó
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "LỖI LƯU DỮ LIỆU: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Lỗi lưu Nhà cung cấp vào Firestore: ", e);
                });
    }
}