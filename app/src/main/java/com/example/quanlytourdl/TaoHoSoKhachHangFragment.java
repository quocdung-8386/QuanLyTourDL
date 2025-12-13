package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TaoHoSoKhachHangFragment extends Fragment {

    // Khai báo đầy đủ các trường
    private EditText edtFullName, edtPhone, edtEmail, edtDob, edtGender, edtAddress, edtCitizenId, edtNote;
    private ImageView btnBack;
    private MaterialButton btnCreateProfile;

    private FirebaseFirestore db;

    public TaoHoSoKhachHangFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tao_ho_so_khach_hang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        initViews(view);

        // Sự kiện Back
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Sự kiện chọn ngày sinh
        edtDob.setOnClickListener(v -> showDatePicker());

        // Sự kiện Lưu Hồ Sơ
        btnCreateProfile.setOnClickListener(v -> saveCustomerToFirestore());
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnCreateProfile = view.findViewById(R.id.btnCreateProfile);

        edtFullName = view.findViewById(R.id.edtFullName);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtDob = view.findViewById(R.id.edtDob);

        // Các trường mới thêm để khớp với file XML
        edtGender = view.findViewById(R.id.edtGender);
        edtAddress = view.findViewById(R.id.edtAddress);
        edtCitizenId = view.findViewById(R.id.edtCitizenId);
        edtNote = view.findViewById(R.id.edtNote);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year1, month1, dayOfMonth) -> {
                    // Format lại cho đẹp: dd/MM/yyyy
                    String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
                    edtDob.setText(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void saveCustomerToFirestore() {
        String name = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        String gender = edtGender.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String cccd = edtCitizenId.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        // 1. Validate dữ liệu cơ bản
        if (name.isEmpty()) {
            edtFullName.setError("Vui lòng nhập họ tên");
            return;
        }
        if (phone.isEmpty()) {
            edtPhone.setError("Vui lòng nhập SĐT");
            return;
        }

        // Disable nút để tránh bấm nhiều lần
        btnCreateProfile.setEnabled(false);
        btnCreateProfile.setText("Đang lưu...");

        // 2. Tạo Map dữ liệu để đẩy lên Firestore
        // Các key này ("ten", "sdt", "email"...) phải khớp với key bạn dùng khi GET ở màn hình Chi Tiết
        Map<String, Object> customer = new HashMap<>();
        customer.put("ten", name);
        customer.put("sdt", phone);
        customer.put("email", email);
        customer.put("ngaySinh", dob);
        customer.put("gioiTinh", gender);
        customer.put("diaChi", address);
        customer.put("cccd", cccd);
        customer.put("ghiChu", note);
        customer.put("quocTich", "Việt Nam"); // Mặc định hoặc thêm trường nhập liệu
        customer.put("ngayTao", System.currentTimeMillis());

        // 3. Lưu lên Collection "khachhang"
        db.collection("khachhang")
                .add(customer)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Tạo hồ sơ thành công!", Toast.LENGTH_SHORT).show();

                    // Quay lại màn hình trước
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnCreateProfile.setEnabled(true);
                    btnCreateProfile.setText("Lưu Hồ sơ Khách hàng");
                });
    }
}