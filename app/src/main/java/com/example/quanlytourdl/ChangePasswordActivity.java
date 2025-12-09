package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextInputEditText etCurrentPass, etNewPass, etConfirmPass;
    private MaterialButton btnSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // 1. Ánh xạ View
        btnBack = findViewById(R.id.btn_back);
        etCurrentPass = findViewById(R.id.et_current_password);
        etNewPass = findViewById(R.id.et_new_password);
        etConfirmPass = findViewById(R.id.et_confirm_password);
        btnSavePassword = findViewById(R.id.btn_save_password);

        // 2. Xử lý sự kiện nút Back
        btnBack.setOnClickListener(v -> finish());

        // 3. Xử lý nút Lưu mật khẩu
        btnSavePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String currentPass = etCurrentPass.getText().toString();
        String newPass = etNewPass.getText().toString();
        String confirmPass = etConfirmPass.getText().toString();

        if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 8) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Gọi API đổi mật khẩu
        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}