package com.example.quanlytourdl;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChiTietKhachHangFragment extends Fragment {

    // --- KHAI BÁO VIEW ---
    private ImageView btnBack, btnCopyCode, imgAvatar;
    private TextView tvCode, tvNationality;

    // Các trường cho phép chỉnh sửa (EditText)
    private EditText edtName, edtDob, edtGender, edtCitizenId, edtPhone, edtEmail, edtAddress;

    private MaterialButton btnEditProfile;
    private LinearLayout btnActionCall, btnActionMessage, btnActionEmail;

    // --- BIẾN LOGIC ---
    private String fullCustomerId; // ID gốc dùng để query Firebase
    private boolean isEditing = false; // Trạng thái: false = Xem, true = Sửa

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo tên layout đúng với file XML bạn đã sửa
        View view = inflater.inflate(R.layout.fragment_chi_tiet_khach_hang, container, false);

        initViews(view);
        loadData();
        setupEvents();

        return view;
    }

    private void initViews(View view) {
        // Nút điều hướng & Header
        btnBack = view.findViewById(R.id.btnBack);

        // Avatar & Info cơ bản
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvCode = view.findViewById(R.id.tvCode);
        btnCopyCode = view.findViewById(R.id.btnCopyCode);

        // Các trường nhập liệu (Ánh xạ đúng với ID trong XML)
        edtName = view.findViewById(R.id.edtName);
        edtDob = view.findViewById(R.id.edtDob);
        edtGender = view.findViewById(R.id.edtGender);
        edtCitizenId = view.findViewById(R.id.edtCitizenId);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtAddress = view.findViewById(R.id.edtAddress);

        // Trường này chỉ hiển thị, không sửa
        tvNationality = view.findViewById(R.id.tvNationality);

        // Các nút hành động
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnActionCall = view.findViewById(R.id.btnActionCall);
        btnActionMessage = view.findViewById(R.id.btnActionMessage);
        btnActionEmail = view.findViewById(R.id.btnActionEmail);
    }

    private void loadData() {
        Bundle args = getArguments();
        if (args != null) {
            String rawId = args.getString("id");

            // --- BƯỚC 1: LÀM SẠCH ID TUYỆT ĐỐI ---
            // replaceAll("\\s+", "") sẽ xóa mọi dấu cách, dấu xuống dòng (\n), tab...
            if (rawId != null) {
                fullCustomerId = rawId.replaceAll("\\s+", "");
            } else {
                fullCustomerId = "";
            }

            // --- BƯỚC 2: HIỂN THỊ MÃ KH (Style Ticket) ---
            if (!fullCustomerId.isEmpty()) {
                String displayId = fullCustomerId;
                // Nếu dài quá 8 ký tự thì cắt bớt cho đẹp (VD: #L1ALJQ...)
                if (fullCustomerId.length() > 8) {
                    displayId = fullCustomerId.substring(0, 7) + "...";
                }
                tvCode.setText("#" + displayId.toUpperCase());
            } else {
                tvCode.setText("#UNKNOWN");
            }

            // --- BƯỚC 3: ĐỔ DỮ LIỆU VÀO VIEW ---
            edtName.setText(args.getString("name", ""));
            edtDob.setText(args.getString("dob", ""));
            edtGender.setText(args.getString("gender", ""));
            edtCitizenId.setText(args.getString("cccd", ""));
            edtPhone.setText(args.getString("phone", ""));
            edtEmail.setText(args.getString("email", ""));
            edtAddress.setText(args.getString("address", ""));

            // Xử lý Avatar theo giới tính
            String gender = args.getString("gender", "");
            if (gender != null && gender.trim().equalsIgnoreCase("Nam")) {
                imgAvatar.setImageResource(R.drawable.ic_avatar_male);
            } else if (gender != null && gender.trim().equalsIgnoreCase("Nữ")) {
                imgAvatar.setImageResource(R.drawable.ic_avatar_female);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

    private void setupEvents() {
        // Nút Back
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // Nút Copy Mã KH đầy đủ
        btnCopyCode.setOnClickListener(v -> {
            if (fullCustomerId != null && !fullCustomerId.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ID Khách hàng", fullCustomerId);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Đã sao chép mã: " + fullCustomerId, Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Chỉnh sửa / Lưu thay đổi
        btnEditProfile.setOnClickListener(v -> {
            if (!isEditing) {
                // ĐANG XEM -> Chuyển sang SỬA
                toggleEditMode(true);
            } else {
                // ĐANG SỬA -> Thực hiện LƯU
                saveDataToFirebase();
            }
        });

        // Nút Gọi nhanh
        btnActionCall.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().replaceAll("\\s+", "");
            if (!phone.isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Lỗi cuộc gọi", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Hàm bật/tắt chế độ chỉnh sửa (Ẩn hiện khung nhập liệu)
     */
    private void toggleEditMode(boolean enable) {
        isEditing = enable;

        // Danh sách các trường cần mở khóa
        EditText[] fields = {edtName, edtDob, edtGender, edtCitizenId, edtPhone, edtEmail, edtAddress};

        for (EditText field : fields) {
            if (field == null) continue;

            field.setEnabled(enable); // Mở khóa hoặc khóa

            if (enable) {
                // CHẾ ĐỘ SỬA: Thêm nền xanh nhạt, padding
                field.setBackgroundColor(Color.parseColor("#E3F2FD"));
                field.setPadding(16, 16, 16, 16);
            } else {
                // CHẾ ĐỘ XEM: Xóa nền
                field.setBackground(null);
                field.setPadding(0, 0, 0, 0);
            }
        }

        // Cập nhật giao diện nút bấm
        if (enable) {
            btnEditProfile.setText("Lưu thay đổi");
            // Lưu ý: Đảm bảo bạn có icon ic_save hoặc ic_check, nếu không có thể dùng tạm ic_edit
            btnEditProfile.setIconResource(R.drawable.ic_edit);
            btnEditProfile.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_700)); // Màu xanh lá

            // Focus vào tên
            edtName.requestFocus();
        } else {
            btnEditProfile.setText("Chỉnh sửa hồ sơ");
            btnEditProfile.setIconResource(R.drawable.ic_edit);
            btnEditProfile.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue_700)); // Màu xanh dương
        }
    }

    /**
     * Hàm lưu dữ liệu lên Firebase (Đã sửa lỗi NOT_FOUND)
     */
    private void saveDataToFirebase() {
        if (fullCustomerId == null || fullCustomerId.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Làm sạch ID
        String cleanId = fullCustomerId.replaceAll("\\s+", "");

        // --- SỬA QUAN TRỌNG: ĐỒNG BỘ TÊN FIELD VỚI MODEL ---
        // Kiểm tra kỹ file KhachHang.java của bạn xem biến tên là gì.
        // Thông thường Model đặt là 'ten', 'sdt' thì trên Firebase cũng phải là 'ten', 'sdt'.
        // Nếu bạn dùng 'tenKhachHang' ở đây mà Model lại get("ten") thì nó sẽ không khớp.

        Map<String, Object> updates = new HashMap<>();
        updates.put("ten", edtName.getText().toString().trim());       // Sửa "tenKhachHang" -> "ten" (hoặc tên field chính xác trên Firebase)
        updates.put("sdt", edtPhone.getText().toString().trim());
        updates.put("email", edtEmail.getText().toString().trim());
        updates.put("diaChi", edtAddress.getText().toString().trim());
        updates.put("ngaySinh", edtDob.getText().toString().trim());
        updates.put("gioiTinh", edtGender.getText().toString().trim());
        updates.put("cccd", edtCitizenId.getText().toString().trim());
        // updates.put("quocTich", ...); // Nếu có sửa quốc tịch

        btnEditProfile.setText("Đang lưu...");
        btnEditProfile.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // --- SỬA QUAN TRỌNG: CHỈ DÙNG 1 TÊN COLLECTION DUY NHẤT ---
        // Bên danh sách dùng "khachhang" (thường) thì ở đây BẮT BUỘC phải dùng "khachhang".
        // Bỏ logic try/catch lung tung để tránh lưu nhầm nơi.

        db.collection("khachhang").document(cleanId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                    // Tắt chế độ sửa
                    toggleEditMode(false);
                    btnEditProfile.setEnabled(true);

                    // Cập nhật lại giao diện UI hiện tại luôn để người dùng thấy ngay
                    // (Dù List bên ngoài tự cập nhật, nhưng màn hình này cũng cần hiển thị cái mới)
                    // Các EditText đã hiển thị cái mới rồi nên không cần set lại text.
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateError", "Lỗi update: " + e.getMessage());
                    Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnEditProfile.setText("Lưu thay đổi");
                    btnEditProfile.setEnabled(true);
                });
    }

    // Hàm phụ xử lý khi thành công
    private void handleUpdateSuccess() {
        Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        toggleEditMode(false);
        btnEditProfile.setEnabled(true);
    }
}