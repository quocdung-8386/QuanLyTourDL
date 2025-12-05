package com.example.quanlytourdl.firebase;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FBHelper {

    private static final String TAG = "FBHelper";
    private FirebaseAuth mAuth;
    private Context context;

    // Interface để gửi kết quả về Activity/Fragment
    public interface AuthListener {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    // Constructor để khởi tạo FirebaseAuth và lấy Context
    public FBHelper(Context context) {
        this.context = context;
        // Lấy một instance duy nhất của FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
    }

    // Phương thức kiểm tra xem người dùng đã đăng nhập hay chưa
    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }

    // Phương thức Đăng nhập
    public void signIn(String email, String password, final AuthListener listener) {
        if (email.isEmpty() || password.isEmpty()) {
            listener.onFailure("Vui lòng nhập đầy đủ Email và Mật khẩu.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            Log.d(TAG, "signIn:success");
                            listener.onSuccess("Đăng nhập thành công!");
                        } else {
                            // Đăng nhập thất bại
                            Log.w(TAG, "signIn:failure", task.getException());
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định.";
                            listener.onFailure("Đăng nhập thất bại: " + errorMessage);
                        }
                    }
                });
    }

    // Phương thức Đăng ký (Bạn sẽ cần nó cho RegisterActivity)
    public void register(String email, String password, final AuthListener listener) {
        if (email.isEmpty() || password.isEmpty()) {
            listener.onFailure("Vui lòng nhập đầy đủ Email và Mật khẩu.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng ký thành công
                            Log.d(TAG, "register:success");
                            // TODO: Thêm logic lưu thông tin người dùng vào Cloud Firestore tại đây
                            listener.onSuccess("Đăng ký thành công!");
                        } else {
                            // Đăng ký thất bại
                            Log.w(TAG, "register:failure", task.getException());
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định.";
                            listener.onFailure("Đăng ký thất bại: " + errorMessage);
                        }
                    }
                });
    }

    // Phương thức Đăng xuất
    public void signOut() {
        mAuth.signOut();
        Toast.makeText(context, "Đã đăng xuất.", Toast.LENGTH_SHORT).show();
    }
}