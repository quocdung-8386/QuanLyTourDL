package com.example.quanlytourdl;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanlytourdl.R;
import com.google.android.material.textfield.TextInputEditText;
import com.example.quanlytourdl.firebase.FBHelper;
import com.example.quanlytourdl.firebase.FBHelper.AuthListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;
    private TextView textRegister;
    private TextView textForgotPassword;
    private CheckBox checkBoxRememberMe;

    // Khai báo FBHelper
    private FBHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo bạn đã đặt tên file layout là activity_login.xml
        setContentView(R.layout.activity_login);

        // Khởi tạo FBHelper
        fbHelper = new FBHelper(this);

        // Ánh xạ các View
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textRegister = findViewById(R.id.textRegister);
        textForgotPassword = findViewById(R.id.textForgotPassword);
        checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe);

        // Kiểm tra trạng thái đăng nhập tự động
//        if (fbHelper.isUserLoggedIn()) {
//            // TODO: Tự động chuyển đến màn hình chính nếu đã đăng nhập
//            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//            finish();
//            // Hiện tại chỉ hiển thị Toast
//            Toast.makeText(this, "Bạn đã đăng nhập trước đó.", Toast.LENGTH_SHORT).show();
//        }

        // Xử lý sự kiện cho nút Đăng nhập
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        // Xử lý sự kiện cho text Đăng ký
        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Chuyển sang màn hình Đăng ký (RegisterActivity)
                Toast.makeText(LoginActivity.this, "Chuyển sang màn hình Đăng ký", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // Xử lý sự kiện cho Quên mật khẩu
        textForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Xử lý logic Quên mật khẩu (gọi phương thức resetPassword trong FBHelper)
                Toast.makeText(LoginActivity.this, "Yêu cầu đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin() {
        // Lấy dữ liệu từ ô nhập
        String emailOrPhone = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        boolean rememberMe = checkBoxRememberMe.isChecked();

        if (emailOrPhone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ email/SĐT và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        fbHelper.signIn(emailOrPhone, password, new AuthListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                // TODO: Xử lý logic ghi nhớ (sử dụng SharedPreferences) nếu rememberMe = true

                // Chuyển sang màn hình chính
              startActivity(new Intent(LoginActivity.this, MainActivity.class));
              finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}