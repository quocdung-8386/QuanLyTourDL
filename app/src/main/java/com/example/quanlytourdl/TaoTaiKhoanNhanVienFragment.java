package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class TaoTaiKhoanNhanVienFragment extends Fragment {

    // Khai báo biến giao diện
    private EditText etName, etEmail, etPassword;
    private TextView tvTitle, tvPasswordLabel, tvCheckEmail;
    private Spinner spinnerDepartment, spinnerRole;
    private Button btnCreate, btnCancel;
    private ImageView ivClose;

    // Biến dữ liệu
    private FirebaseFirestore db;
    private NhanVien mNhanVienUpdate;
    private boolean isEmailChecked = false; // Biến kiểm tra xem đã check email chưa

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tao_tai_khoan_nhan_vien, container, false);

        db = FirebaseFirestore.getInstance();

        // 1. ÁNH XẠ VIEW
        initViews(view);
        setupSpinners();

        // 2. NHẬN DỮ LIỆU SỬA (Nếu có)
        if (getArguments() != null) {
            mNhanVienUpdate = (NhanVien) getArguments().getSerializable("nhanvien_data");
        }

        // 3. CHẾ ĐỘ CHỈNH SỬA
        if (mNhanVienUpdate != null) {
            fillDataForEdit();
        }

        // 4. GẮN SỰ KIỆN CLICK
        ivClose.setOnClickListener(v -> goBack());
        btnCancel.setOnClickListener(v -> goBack());

        // Nút Kiểm tra email (Thêm vào dựa trên ảnh màn hình)
        tvCheckEmail.setOnClickListener(v -> validateEmailBeforeCreate());

        // Nút Tạo/Cập nhật
        btnCreate.setOnClickListener(v -> handleSaveUser());

        return view;
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        tvTitle = view.findViewById(R.id.tv_title);
        tvPasswordLabel = view.findViewById(R.id.tv_password_label);
        tvCheckEmail = view.findViewById(R.id.tv_check_email); // TextView "Kiểm tra" dưới ô Email

        spinnerDepartment = view.findViewById(R.id.spinner_department);
        spinnerRole = view.findViewById(R.id.spinner_role);

        btnCreate = view.findViewById(R.id.btn_create_account);
        btnCancel = view.findViewById(R.id.btn_cancel);
        ivClose = view.findViewById(R.id.iv_close);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> deptAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.phong_ban_array, android.R.layout.simple_spinner_item);
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(deptAdapter);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.vai_tro_array, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
    }

    private void fillDataForEdit() {
        if (tvTitle != null) tvTitle.setText("Chỉnh sửa Nhân viên");
        if (btnCreate != null) btnCreate.setText("Cập nhật");
        if (tvCheckEmail != null) tvCheckEmail.setVisibility(View.GONE); // Đang sửa thì không cần check email

        if (etPassword != null) etPassword.setVisibility(View.GONE);
        if (tvPasswordLabel != null) tvPasswordLabel.setVisibility(View.GONE);

        etName.setText(mNhanVienUpdate.getFullName());
        etEmail.setText(mNhanVienUpdate.getEmail());
        etEmail.setEnabled(false); // Không cho sửa email gốc
        etEmail.setAlpha(0.6f);

        setSpinnerValue(spinnerDepartment, mNhanVienUpdate.getDepartment());
        setSpinnerValue(spinnerRole, mNhanVienUpdate.getRole());
        isEmailChecked = true; // Sửa dữ liệu cũ thì mặc định email đã hợp lệ
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // Logic kiểm tra email đã tồn tại hay chưa
    private void validateEmailBeforeCreate() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Vui lòng nhập Email để kiểm tra", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            isEmailChecked = true;
                            Toast.makeText(getContext(), "Email hợp lệ!", Toast.LENGTH_SHORT).show();
                            tvCheckEmail.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        } else {
                            isEmailChecked = false;
                            Toast.makeText(getContext(), "Email này đã được sử dụng!", Toast.LENGTH_SHORT).show();
                            tvCheckEmail.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                    }
                });
    }

    private void handleSaveUser() {
        String name = etName.getText().toString().trim();
        String dept = spinnerDepartment.getSelectedItem().toString();
        String role = spinnerRole.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || dept.equals("Chọn phòng ban") || role.equals("Chọn vai trò")) {
            Toast.makeText(getContext(), "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailChecked && mNhanVienUpdate == null) {
            Toast.makeText(getContext(), "Vui lòng 'Kiểm tra' email trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCreate.setEnabled(false);
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("fullName", name);
        userMap.put("department", dept);
        userMap.put("role", role);

        if (mNhanVienUpdate == null) {
            // TẠO MỚI
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Mật khẩu không được trống", Toast.LENGTH_SHORT).show();
                btnCreate.setEnabled(true);
                return;
            }

            userMap.put("email", email);
            userMap.put("createdAt", System.currentTimeMillis());

            db.collection("Users").get().addOnCompleteListener(task -> {
                int codeValue = 1001;
                if (task.isSuccessful() && task.getResult() != null) {
                    codeValue = 1001 + task.getResult().size();
                }

                final int finalNextCode = codeValue;
                userMap.put("employeeCode", String.valueOf(finalNextCode));

                db.collection("Users").add(userMap)
                        .addOnSuccessListener(doc -> {
                            Toast.makeText(getContext(), "Đã tạo nhân viên: " + finalNextCode, Toast.LENGTH_SHORT).show();
                            goBack();
                        })
                        .addOnFailureListener(e -> btnCreate.setEnabled(true));
            });
        } else {
            // CẬP NHẬT
            db.collection("Users").document(mNhanVienUpdate.getDocumentId())
                    .update(userMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        goBack();
                    })
                    .addOnFailureListener(e -> btnCreate.setEnabled(true));
        }
    }

    private void goBack() {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack();
        }
    }
}