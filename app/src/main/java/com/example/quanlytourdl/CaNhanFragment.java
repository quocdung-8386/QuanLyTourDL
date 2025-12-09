package com.example.quanlytourdl;

import android.content.Intent;
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
    private MaterialButton btnLogout; // 1. Khai báo thêm nút Đăng xuất
    private ImageView iconEditProfile;
    private View cardChangePassword, cardTwoFactorAuth;
    private SwitchMaterial switchTwoFactorAuth;

    public CaNhanFragment() {
        // Constructor rỗng bắt buộc
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ca_nhan, container, false);

        // 2. Ánh xạ các thành phần UI
        editFullName = view.findViewById(R.id.edit_full_name);
        editPhone = view.findViewById(R.id.edit_phone);
        editEmail = view.findViewById(R.id.edit_email);
        btnSaveChanges = view.findViewById(R.id.btn_save_changes);
        iconEditProfile = view.findViewById(R.id.icon_edit_profile);
        cardChangePassword = view.findViewById(R.id.card_change_password);
        cardTwoFactorAuth = view.findViewById(R.id.card_2fa);
        switchTwoFactorAuth = view.findViewById(R.id.switch_2fa);

        // Ánh xạ nút Đăng xuất
        btnLogout = view.findViewById(R.id.btn_logout);

        // 3. Xử lý sự kiện

        // Sự kiện Đổi ảnh đại diện
        iconEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở thư viện/camera để chọn ảnh", Toast.LENGTH_SHORT).show();
        });

        // Sự kiện Lưu thông tin
        btnSaveChanges.setOnClickListener(v -> {
            saveProfileChanges();
        });

        // Chuyển sang màn hình Đổi Mật Khẩu
        cardChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Chuyển sang màn hình Xác thực 2 yếu tố
        cardTwoFactorAuth.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TwoFactorAuthActivity.class);
            startActivity(intent);
        });

        switchTwoFactorAuth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Xử lý nhanh trạng thái bật/tắt 2FA tại chỗ
        });

        // --- SỰ KIỆN ĐĂNG XUẤT (CODE MỚI) ---
        btnLogout.setOnClickListener(v -> {
            // Thông báo
            Toast.makeText(getContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

            // Chuyển về màn hình Đăng nhập (LoginActivity)
            Intent intent = new Intent(getActivity(), LoginActivity.class);

            // Xóa toàn bộ lịch sử Activity cũ (để user không bấm Back quay lại được)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        });

        return view;
    }

    private void saveProfileChanges() {
        String fullName = editFullName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String email = editEmail.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO: Gửi API lưu thông tin
        Toast.makeText(getContext(), "Đã lưu thay đổi hồ sơ thành công!", Toast.LENGTH_SHORT).show();
    }
}