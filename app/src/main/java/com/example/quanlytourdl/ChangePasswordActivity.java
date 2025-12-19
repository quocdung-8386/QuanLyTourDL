package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        // 2. Xử lý sự kiện
        btnBack.setOnClickListener(v -> finish()); // Đóng Activity quay về màn hình trước

        btnSavePassword.setOnClickListener(v -> handleChangePasswordFirebase());
    }

    private void handleChangePasswordFirebase() {
        String currentPass = etCurrentPass.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();

        // Validate dữ liệu
        if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase yêu cầu tối thiểu 6, nhưng UI bạn ghi 8 nên ta check 8 cho chuyên nghiệp
        if (newPass.length() < 8) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPass.equals(newPass)) {
            Toast.makeText(this, "Mật khẩu mới không được trùng với mật khẩu cũ", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- BẮT ĐẦU LOGIC FIREBASE ---
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getEmail() != null) {
            // Khóa nút bấm để tránh spam
            btnSavePassword.setEnabled(false);
            btnSavePassword.setText("Đang xác thực...");

            // BƯỚC 1: Xác thực lại bằng mật khẩu CŨ (Re-authenticate)
            // Đây là bước bắt buộc của Firebase để đảm bảo người dùng chính chủ đang đổi mật khẩu
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Re-auth thành công -> Mật khẩu cũ ĐÚNG -> Tiến hành cập nhật
                        btnSavePassword.setText("Đang cập nhật...");

                        user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                btnSavePassword.setEnabled(true);
                                btnSavePassword.setText("Lưu thay đổi");

                                if (task.isSuccessful()) {
                                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                    finish(); // Đóng màn hình này lại
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    } else {
                        // Re-auth thất bại -> Mật khẩu cũ SAI
                        btnSavePassword.setEnabled(true);
                        btnSavePassword.setText("Lưu thay đổi");
                        Toast.makeText(ChangePasswordActivity.this, "Mật khẩu hiện tại không đúng!", Toast.LENGTH_SHORT).show();
                        etCurrentPass.requestFocus();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
        }
    }
}