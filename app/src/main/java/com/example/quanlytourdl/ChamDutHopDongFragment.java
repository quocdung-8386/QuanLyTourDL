package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

// Giả định bạn có một đối tượng HopDong (Hợp đồng) được truyền vào,
// hoặc chỉ truyền ID nhà cung cấp
public class ChamDutHopDongFragment extends Fragment {

    private static final String TAG = "ChamDutHopDongFragment";
    private FirebaseFirestore db;
    private String supplierId; // ID Nhà cung cấp
    private String contractId; // ID Hợp đồng (quan trọng để cập nhật trạng thái)

    private TextView tvNhaCungCap;
    private Spinner spLyDoChamDut;
    private EditText etNgayChamDut, etGhiChu;
    private Button btnXacNhan, btnHuy;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // Lấy dữ liệu ID Hợp đồng hoặc ID Nhà cung cấp từ Bundle
        if (getArguments() != null) {
            supplierId = getArguments().getString("supplier_id");
            contractId = getArguments().getString("contract_id"); // Giả định ID hợp đồng được truyền
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Tên layout giả định: fragment_cham_dut_hop_dong
        int layoutId = getResources().getIdentifier("fragment_cham_dut_hop_dong", "layout", requireContext().getPackageName());
        if (layoutId == 0) {
            Log.e(TAG, "Không tìm thấy layout ID 'fragment_cham_dut_hop_dong'.");
            return null;
        }

        View view = inflater.inflate(layoutId, container, false);

        // Ánh xạ UI
        tvNhaCungCap = view.findViewById(R.id.tv_nha_cung_cap);
        spLyDoChamDut = view.findViewById(R.id.sp_ly_do_cham_dut);
        etNgayChamDut = view.findViewById(R.id.et_ngay_cham_dut);
        etGhiChu = view.findViewById(R.id.et_ghi_chu);
        btnXacNhan = view.findViewById(R.id.btn_xac_nhan_cham_dut);
        btnHuy = view.findViewById(R.id.btn_huy);

        // Xử lý nút quay lại
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Thiết lập dữ liệu ban đầu
        setupInitialData();

        // Thiết lập sự kiện click
        btnXacNhan.setOnClickListener(v -> confirmTermination());
        btnHuy.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    /**
     * Thiết lập các dữ liệu ban đầu cho Fragment (tên NCC, danh sách lý do, ngày hiện tại)
     */
    private void setupInitialData() {
        // 1. Điền tên Nhà Cung Cấp (Giả định tải tên dựa trên supplierId)
        if (supplierId != null) {
            loadSupplierName(supplierId);
        } else {
            tvNhaCungCap.setText("Không rõ Nhà cung cấp");
        }

        // 2. Thiết lập Spinner Lý do chấm dứt
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.termination_reasons, // Giả định bạn có một array resource tên là termination_reasons
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLyDoChamDut.setAdapter(adapter);

        // 3. Thiết lập Ngày chấm dứt hiệu lực (Mặc định là ngày hiện tại)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etNgayChamDut.setText(sdf.format(new Date()));

        // TODO: Thêm logic mở DatePicker khi click vào etNgayChamDut
    }

    /**
     * Tải tên nhà cung cấp dựa trên ID (Chỉ để hiển thị)
     */
    private void loadSupplierName(String id) {
        db.collection("NhaCungCap").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String tenNCC = documentSnapshot.getString("tenNhaCungCap");
                        if (tenNCC != null) {
                            tvNhaCungCap.setText(tenNCC);
                        } else {
                            tvNhaCungCap.setText("Công ty Du lịch [ID: " + id + "]");
                        }
                    } else {
                        tvNhaCungCap.setText("Nhà cung cấp không tồn tại");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải tên NCC: " + e.getMessage());
                    tvNhaCungCap.setText("Lỗi tải tên NCC");
                });
    }

    /**
     * Xử lý xác nhận chấm dứt hợp đồng và cập nhật Firestore.
     */
    private void confirmTermination() {
        if (contractId == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID Hợp đồng để chấm dứt.", Toast.LENGTH_LONG).show();
            return;
        }

        String lyDo = spLyDoChamDut.getSelectedItem().toString();
        String ngayChamDut = etNgayChamDut.getText().toString().trim();
        String ghiChu = etGhiChu.getText().toString().trim();

        if (lyDo.equals("Chọn một lý do")) {
            Toast.makeText(getContext(), "Vui lòng chọn Lý do chấm dứt.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ngayChamDut.isEmpty()) {
            Toast.makeText(getContext(), "Ngày chấm dứt không được để trống.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Chuẩn bị dữ liệu cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("trangThai", "Chấm dứt"); // Cập nhật trạng thái hợp đồng
        updates.put("lyDoChamDut", lyDo);
        updates.put("ngayChamDut", ngayChamDut);
        updates.put("ghiChuChamDut", ghiChu);
        updates.put("ngayCapNhat", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        // 2. Thực hiện cập nhật trên collection "HopDong"
        db.collection("HopDong").document(contractId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Chấm dứt hợp đồng thành công! Đã cập nhật trạng thái.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Hợp đồng " + contractId + " đã được chấm dứt.");
                    // Quay lại màn hình trước
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi chấm dứt hợp đồng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Lỗi cập nhật trạng thái hợp đồng", e);
                });
    }
}