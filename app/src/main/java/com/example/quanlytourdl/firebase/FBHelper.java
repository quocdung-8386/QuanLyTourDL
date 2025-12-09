package com.example.quanlytourdl.firebase;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.quanlytourdl.model.UserModel; // Đảm bảo đã import model này
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore; // Import Firestore
import com.google.firebase.firestore.DocumentSnapshot;

public class FBHelper {

    private static final String TAG = "FBHelper";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Khai báo Firestore
    private Context context;

    // 1. Interface để gửi kết quả thao tác (Thành công/Thất bại)
    public interface AuthListener {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    // 2. Interface MỚI: Dùng để trả về dữ liệu User khi tải từ Firestore
    public interface DataListener {
        void onDataReceived(UserModel user);
        void onError(String error);
    }

    // Constructor
    public FBHelper(Context context) {
        this.context = context;
        // Khởi tạo Auth
        mAuth = FirebaseAuth.getInstance();
        // Khởi tạo Firestore (MỚI)
        db = FirebaseFirestore.getInstance();
    }

    // ----------------------------------------------------------------
    // PHẦN 1: CÁC HÀM AUTHENTICATION (Đăng ký, Đăng nhập, Đăng xuất)
    // ----------------------------------------------------------------

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public String getCurrentUserID() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

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
                            Log.d(TAG, "signIn:success");
                            listener.onSuccess("Đăng nhập thành công!");
                        } else {
                            Log.w(TAG, "signIn:failure", task.getException());
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định.";
                            listener.onFailure("Đăng nhập thất bại: " + errorMessage);
                        }
                    }
                });
    }

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
                            Log.d(TAG, "register:success");
                            // Lưu ý: Sau khi register Auth thành công, Activity sẽ gọi tiếp hàm addUserToFirestore bên dưới
                            listener.onSuccess("Tạo tài khoản thành công!");
                        } else {
                            Log.w(TAG, "register:failure", task.getException());
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định.";
                            listener.onFailure("Đăng ký thất bại: " + errorMessage);
                        }
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
        Toast.makeText(context, "Đã đăng xuất.", Toast.LENGTH_SHORT).show();
    }

    // ----------------------------------------------------------------
    // PHẦN 2: CÁC HÀM FIRESTORE (Lưu và Lấy thông tin User) - MỚI
    // ----------------------------------------------------------------

    // Hàm lưu thông tin User vào Firestore (Dùng ngay sau khi Đăng ký thành công)
    public void addUserToFirestore(UserModel user, AuthListener listener) {
        if (user.getUid() == null) return;

        db.collection("users") // Tên collection trong Firestore
                .document(user.getUid()) // Đặt ID document trùng với UID của Auth
                .set(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess("Lưu dữ liệu thành công"))
                .addOnFailureListener(e -> listener.onFailure("Lỗi lưu dữ liệu: " + e.getMessage()));
    }

    // Hàm lấy thông tin User hiện tại để hiển thị lên màn hình Cá nhân
    public void getCurrentUserData(DataListener listener) {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Chuyển document thành object UserModel
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            listener.onDataReceived(user);
                        } else {
                            listener.onError("Không tìm thấy dữ liệu hồ sơ.");
                        }
                    })
                    .addOnFailureListener(e -> listener.onError(e.getMessage()));
        } else {
            listener.onError("Người dùng chưa đăng nhập.");
        }
    }

    // Hàm cập nhật thông tin (Dùng cho nút "Lưu thay đổi" ở màn hình Cá nhân)
    public void updateUserInfo(String uid, String fullName, String phone, AuthListener listener) {
        db.collection("users").document(uid)
                .update("fullName", fullName, "phone", phone)
                .addOnSuccessListener(aVoid -> listener.onSuccess("Cập nhật thành công"))
                .addOnFailureListener(e -> listener.onFailure("Lỗi cập nhật: " + e.getMessage()));
    }
}