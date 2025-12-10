package com.example.quanlytourdl;

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
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChiTietKhachHangFragment extends Fragment {

    // Khai báo View
    private ImageView btnBack;
    private EditText edtName, edtPhone, edtEmail, edtDob;
    private TextView tvCode;
    private MaterialButton btnUpdate;

    // Khai báo nút Xem lịch sử (Là View hoặc LinearLayout tùy XML)
    private View btnViewHistory;

    private String customerId; // ID dùng để truy vấn và update

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_khach_hang, container, false);

        // 1. ÁNH XẠ VIEW
        btnBack = view.findViewById(R.id.btnBack);
        tvCode = view.findViewById(R.id.tvCodeDetail);

        edtName = view.findViewById(R.id.edtNameDetail);
        edtPhone = view.findViewById(R.id.edtPhoneDetail);
        edtEmail = view.findViewById(R.id.edtEmailDetail);
        edtDob = view.findViewById(R.id.edtDobDetail);

        btnUpdate = view.findViewById(R.id.btnUpdate);

        // Ánh xạ nút Lịch sử (ID này phải có trong XML fragment_chi_tiet_khach_hang)
        btnViewHistory = view.findViewById(R.id.btnViewHistory);

        // 2. NHẬN DỮ LIỆU TỪ DANH SÁCH
        Bundle args = getArguments();
        if (args != null) {
            customerId = args.getString("id");
            String name = args.getString("name");
            String phone = args.getString("phone");
            String dob = args.getString("dob");
            String email = args.getString("email");

            // Hiển thị lên giao diện
            if (customerId != null) {
                tvCode.setText("Mã KH: " + customerId);
            }
            edtName.setText(name);
            edtPhone.setText(phone);
            if (dob != null) edtDob.setText(dob);
            if (email != null) edtEmail.setText(email);
        }

        // 3. SỰ KIỆN BACK
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // 4. SỰ KIỆN CẬP NHẬT
        btnUpdate.setOnClickListener(v -> updateCustomerToFirestore());

        // 5. SỰ KIỆN XEM LỊCH SỬ (Mới thêm)
        if (btnViewHistory != null) {
            btnViewHistory.setOnClickListener(v -> {
                // Tạo Fragment Lịch sử
                LichSuDatTourFragment historyFragment = new LichSuDatTourFragment();

                // Đóng gói ID khách hàng gửi sang
                Bundle bundle = new Bundle();
                bundle.putString("customer_id", customerId);
                historyFragment.setArguments(bundle);

                // Chuyển màn hình
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.main_content_frame, historyFragment); // ID FrameLayout trong MainActivity
                transaction.addToBackStack(null);
                transaction.commit();
            });
        }

        return view;
    }

    private void updateCustomerToFirestore() {
        String newName = edtName.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();
        String newDob = edtDob.getText().toString().trim();
        String newEmail = edtEmail.getText().toString().trim();

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(getContext(), "Tên và SĐT là bắt buộc!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("ten", newName);
        updates.put("sdt", newPhone);
        updates.put("ngaySinh", newDob);

        // Đã mở khóa dòng này để lưu Email
        updates.put("email", newEmail);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("khachhang").document(customerId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}