package com.example.quanlytourdl;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.example.quanlytourdl.firebase.FBHelper;
import com.example.quanlytourdl.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    // Khai báo các biến giao diện
    private TextInputEditText editTextName;
    private TextInputEditText editTextPhone; // MỚI: Biến cho trường Số điện thoại
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;

    private Button buttonRegister;
    private TextView textSignIn;
    private ImageView iconBack;

    // Khai báo Helper
    private FBHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo FBHelper
        fbHelper = new FBHelper(this);

        // Ánh xạ View (Kết nối code Java với XML)
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone); // MỚI: Ánh xạ ID từ XML
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        buttonRegister = findViewById(R.id.buttonRegister);
        textSignIn = findViewById(R.id.textSignIn);
        iconBack = findViewById(R.id.iconBack);

        // Sự kiện click nút Đăng ký
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        // Sự kiện click nút Đăng nhập (Chuyển về màn hình login)
        textSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Sự kiện nút Back
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performRegistration() {
        // 1. Lấy dữ liệu từ các ô nhập liệu
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim(); // MỚI: Lấy SĐT
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // 2. Validate (Kiểm tra dữ liệu)
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin (cả số điện thoại).", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Gọi hàm Đăng ký (Auth) từ FBHelper
        fbHelper.register(email, password, new FBHelper.AuthListener() {
            @Override
            public void onSuccess(String message) {
                // Đăng ký Auth thành công -> Tiến hành lưu thông tin chi tiết vào Firestore

                // Lấy UID vừa được tạo
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Tạo đối tượng UserModel (bao gồm cả phone vừa nhập)
                // "Khách hàng" là vai trò mặc định
                UserModel newUser = new UserModel(uid, name, email, phone, "Nhân viên");

                // Gọi hàm lưu vào Firestore
                fbHelper.addUserToFirestore(newUser, new FBHelper.AuthListener() {
                    @Override
                    public void onSuccess(String msg) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                        // Đóng màn hình đăng ký, quay về đăng nhập
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        Toast.makeText(RegisterActivity.this, "Lỗi lưu dữ liệu: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}