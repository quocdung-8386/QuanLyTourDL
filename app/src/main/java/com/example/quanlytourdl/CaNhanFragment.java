package com.example.quanlytourdl;

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
import com.google.android.material.switchmaterial.SwitchMaterial;

public class CaNhanFragment extends Fragment {

    // Khai báo các thành phần UI
    private EditText editFullName, editPhone, editEmail;
    private MaterialButton btnSaveChanges;
    private ImageView iconEditProfile;
    private View cardChangePassword, cardTwoFactorAuth;
    private SwitchMaterial switchTwoFactorAuth;

    public CaNhanFragment() {
        // Constructor rỗng bắt buộc
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Liên kết Fragment với Layout XML (fragment_ca_nhan.xml)
        View view = inflater.inflate(R.layout.fragment_ca_nhan, container, false);

        // 1. Ánh xạ các thành phần UI
        editFullName = view.findViewById(R.id.edit_full_name);
        editPhone = view.findViewById(R.id.edit_phone);
        editEmail = view.findViewById(R.id.edit_email);
        btnSaveChanges = view.findViewById(R.id.btn_save_changes);
        iconEditProfile = view.findViewById(R.id.icon_edit_profile);
        cardChangePassword = view.findViewById(R.id.card_change_password);
        cardTwoFactorAuth = view.findViewById(R.id.card_2fa);
        switchTwoFactorAuth = view.findViewById(R.id.switch_2fa);


        // 2. Xử lý sự kiện

        // Nút chỉnh sửa ảnh đại diện
        iconEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở thư viện/camera để chọn ảnh", Toast.LENGTH_SHORT).show();
            // THƯỜNG: Mở Intent để chọn ảnh
        });

        // Nút Lưu thay đổi
        btnSaveChanges.setOnClickListener(v -> {
            saveProfileChanges();
        });

        // Thẻ Thay đổi Mật khẩu
        cardChangePassword.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuyển sang màn hình Thay đổi Mật khẩu", Toast.LENGTH_SHORT).show();
            // THƯỜNG: Chuyển Fragment hoặc Activity mới
        });

        // Thẻ Xác thực 2 yếu tố
        cardTwoFactorAuth.setOnClickListener(v -> {
            // Đảo trạng thái của Switch khi click vào toàn bộ thẻ
            switchTwoFactorAuth.setChecked(!switchTwoFactorAuth.isChecked());
            // Xử lý logic bảo mật
            Toast.makeText(getContext(), "Xác thực 2 yếu tố: " + (switchTwoFactorAuth.isChecked() ? "Đã Bật" : "Đã Tắt"), Toast.LENGTH_SHORT).show();
        });

        // Sự kiện riêng cho Switch (nếu cần)
        switchTwoFactorAuth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(), "Cập nhật trạng thái 2FA", Toast.LENGTH_SHORT).show();
        });

        // Load dữ liệu ban đầu
        //
        //
        // loadUserProfile();

        return view;
    }

    // Hàm giả lập việc lưu thay đổi hồ sơ
    private void saveProfileChanges() {
        String fullName = editFullName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String email = editEmail.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Gửi dữ liệu đã cập nhật lên server/database

        Toast.makeText(getContext(), "Đã lưu thay đổi hồ sơ thành công!", Toast.LENGTH_SHORT).show();
    }
}
