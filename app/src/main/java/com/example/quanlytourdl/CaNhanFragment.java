package com.example.quanlytourdl;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quanlytourdl.firebase.FBHelper;
import com.example.quanlytourdl.model.UserModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class CaNhanFragment extends Fragment {

    private EditText editFullName, editPhone, editEmail;
    private TextView textUserName, textUserRole;
    private MaterialButton btnSaveChanges, btnLogout;
    private ImageView iconEditProfile;
    private FBHelper fbHelper;

    public CaNhanFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ca_nhan, container, false);

        fbHelper = new FBHelper(getContext());

        // Ánh xạ
        editFullName = view.findViewById(R.id.edit_full_name);
        editPhone = view.findViewById(R.id.edit_phone);
        editEmail = view.findViewById(R.id.edit_email);
        textUserName = view.findViewById(R.id.text_user_name);
        textUserRole = view.findViewById(R.id.text_user_role);

        btnSaveChanges = view.findViewById(R.id.btn_save_changes);
        btnLogout = view.findViewById(R.id.btn_logout);
        iconEditProfile = view.findViewById(R.id.icon_edit_profile);

        // --- LOAD DỮ LIỆU TỪ FIRESTORE ---
        loadUserProfile();

        // Nút Lưu thay đổi
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());

        // Nút Đăng xuất
        btnLogout.setOnClickListener(v -> {
            fbHelper.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserProfile() {
        fbHelper.getCurrentUserData(new FBHelper.DataListener() {
            @Override
            public void onDataReceived(UserModel user) {
                if (user != null) {
                    // Đổ dữ liệu vào giao diện
                    editFullName.setText(user.getFullName());
                    editEmail.setText(user.getEmail());
                    editPhone.setText(user.getPhone());

                    // Cập nhật Header
                    textUserName.setText(user.getFullName());
                    textUserRole.setText(user.getRole());

                    // Email không cho sửa
                    editEmail.setEnabled(false);
                }
            }

            @Override
            public void onError(String error) {
                // Nếu chưa có dữ liệu hoặc lỗi mạng
                Toast.makeText(getContext(), "Không tải được hồ sơ: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileChanges() {
        String newName = editFullName.getText().toString().trim();
        String newPhone = editPhone.getText().toString().trim();
        String uid = fbHelper.getCurrentUserID();

        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        fbHelper.updateUserInfo(uid, newName, newPhone, new FBHelper.AuthListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                // Cập nhật lại tên hiển thị ở Header ngay lập tức
                textUserName.setText(newName);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}