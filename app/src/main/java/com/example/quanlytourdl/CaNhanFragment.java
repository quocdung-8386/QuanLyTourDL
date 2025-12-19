package com.example.quanlytourdl;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView; // Import CardView
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.quanlytourdl.firebase.FBHelper;
import com.example.quanlytourdl.model.UserModel;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class CaNhanFragment extends Fragment {

    private EditText editFullName, editPhone, editEmail;
    private TextView textUserName, textUserRole;
    private MaterialButton btnSaveChanges, btnLogout;
    private ImageView iconEditProfile;
    private CircleImageView imageProfile;

    // --- KHAI BÁO BIẾN CHO CÁC MỤC BẢO MẬT MỚI ---
    private CardView cardChangePassword;
    private CardView card2FA;

    private FBHelper fbHelper;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public CaNhanFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ca_nhan, container, false);

        fbHelper = new FBHelper(getContext());

        // 1. ÁNH XẠ VIEW (CŨ + MỚI)
        editFullName = view.findViewById(R.id.edit_full_name);
        editPhone = view.findViewById(R.id.edit_phone);
        editEmail = view.findViewById(R.id.edit_email);
        textUserName = view.findViewById(R.id.text_user_name);
        textUserRole = view.findViewById(R.id.text_user_role);

        imageProfile = view.findViewById(R.id.image_profile);
        iconEditProfile = view.findViewById(R.id.icon_edit_profile);

        btnSaveChanges = view.findViewById(R.id.btn_save_changes);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Ánh xạ 2 CardView Bảo mật mới
        cardChangePassword = view.findViewById(R.id.card_change_password);
        card2FA = view.findViewById(R.id.card_2fa); // ID của card xác thực 2 yếu tố trong XML

        // 2. KHỞI TẠO & LOAD DỮ LIỆU
        registerImagePicker();
        loadUserProfile();

        // 3. CÁC SỰ KIỆN CLICK

        // Sự kiện click vào icon sửa ảnh HOẶC click thẳng vào ảnh đại diện
        iconEditProfile.setOnClickListener(v -> openGallery());
        imageProfile.setOnClickListener(v -> openGallery()); // UX tốt hơn: bấm vào ảnh cũng đổi được

        // Nút Lưu thay đổi thông tin cá nhân
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());

        // Nút Đăng xuất
        btnLogout.setOnClickListener(v -> {
            fbHelper.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // --- XỬ LÝ CHUYỂN MÀN HÌNH BẢO MẬT (MỚI) ---

        // Click vào mục "Thay đổi Mật khẩu" -> Chuyển sang ChangePasswordActivity
        if (cardChangePassword != null) {
            cardChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            });
        }

        // Click vào mục "Xác thực 2 yếu tố" -> Chuyển sang TwoFactorAuthActivity
        // (Lưu ý: Bạn nên bỏ Switch ở màn hình này nếu muốn bấm vào Card để sang màn hình chi tiết)
        if (card2FA != null) {
            card2FA.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), TwoFactorAuthActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }

    // ------------------------------------------------------------------
    // GIỮ NGUYÊN CÁC HÀM LOGIC CŨ BÊN DƯỚI
    // ------------------------------------------------------------------

    private void registerImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Hiển thị ảnh tạm thời
                            imageProfile.setImageURI(selectedImageUri);
                            // Upload ngay lập tức
                            uploadImageToFirebase(selectedImageUri);
                        }
                    }
                }
        );
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri uri) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang cập nhật ảnh đại diện...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        fbHelper.uploadUserAvatar(uri, new FBHelper.AuthListener() {
            @Override
            public void onSuccess(String message) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfile() {
        fbHelper.getCurrentUserData(new FBHelper.DataListener() {
            @Override
            public void onDataReceived(UserModel user) {
                if (user != null) {
                    editFullName.setText(user.getFullName());
                    editEmail.setText(user.getEmail());
                    editPhone.setText(user.getPhone());
                    textUserName.setText(user.getFullName());
                    textUserRole.setText(user.getRole());
                    editEmail.setEnabled(false);

                    // Load ảnh bằng Glide
                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        Glide.with(CaNhanFragment.this)
                                .load(user.getAvatar())
                                .placeholder(R.drawable.profile_placeholder)
                                .error(R.drawable.profile_placeholder)
                                .into(imageProfile);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi tải hồ sơ: " + error, Toast.LENGTH_SHORT).show();
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
                textUserName.setText(newName);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}