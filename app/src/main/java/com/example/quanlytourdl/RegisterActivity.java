package com.example.quanlytourdl;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanlytourdl.R;
import com.google.android.material.textfield.TextInputEditText;
import com.example.quanlytourdl.firebase.FBHelper; // Import FBHelper
import com.example.quanlytourdl.firebase.FBHelper.AuthListener; // Import Interface

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textSignIn;
    private ImageView iconBack;

    // Khai báo FBHelper
    private FBHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo FBHelper
        fbHelper = new FBHelper(this);

        // Ánh xạ các View
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textSignIn = findViewById(R.id.textSignIn);
        iconBack = findViewById(R.id.iconBack);

        // Xử lý sự kiện cho nút Đăng ký
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        // Xử lý sự kiện cho text Đăng nhập (Chuyển về LoginActivity)
        textSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển về màn hình Đăng nhập
                finish();
                // Hoặc: startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        // Xử lý sự kiện cho nút Back
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // TODO: Xử lý sự kiện cho nút Đăng ký với Google và Điều khoản dịch vụ/Chính sách bảo mật
    }

    private void performRegistration() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // 1. Kiểm tra các trường trống
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra độ dài mật khẩu tối thiểu
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Kiểm tra mật khẩu khớp nhau
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Gọi hàm đăng ký thông qua FBHelper
        fbHelper.register(email, password, new AuthListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();

                // TODO: Tại đây, bạn cũng nên lưu tên (name) và email vào Cloud Firestore (User collection)
                // vì Firebase Authentication chỉ lưu email và password.

                // Chuyển sang màn hình chính (hoặc trở về màn hình đăng nhập)
                // startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                // finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}