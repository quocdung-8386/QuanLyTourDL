package com.example.quanlytourdl;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class ChiTietKhachHangFragment extends Fragment {

    // Khai báo View
    private ImageView btnBack;
    private TextView tvName, tvCode, tvDob, tvGender, tvCitizenId, tvPhone, tvEmail, tvAddress, tvNationality;
    private MaterialButton btnEditProfile;
    private LinearLayout btnActionCall, btnActionMessage, btnActionEmail;
    private TextView btnViewAllHistory;

    private String customerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Lưu ý: Đảm bảo tên layout đúng với file XML của bạn
        View view = inflater.inflate(R.layout.fragment_chi_tiet_khach_hang, container, false);

        // 1. ÁNH XẠ VIEW
        initViews(view);

        // 2. NHẬN DỮ LIỆU TỪ BUNDLE
        // SỬA LỖI: Truyền biến 'view' vào hàm này
        loadDataFromBundle(view);

        // 3. XỬ LÝ SỰ KIỆN
        setupEvents();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);

        // Thông tin Header
        tvName = view.findViewById(R.id.tvName);

        // Thông tin cá nhân
        tvCode = view.findViewById(R.id.tvCode);
        tvDob = view.findViewById(R.id.tvDob);
        tvGender = view.findViewById(R.id.tvGender);
        tvCitizenId = view.findViewById(R.id.tvCitizenId);
        tvNationality = view.findViewById(R.id.tvNationality);

        // Thông tin liên hệ
        tvPhone = view.findViewById(R.id.tvPhone);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAddress = view.findViewById(R.id.tvAddress);

        // Nút chức năng
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnViewAllHistory = view.findViewById(R.id.btnViewAllHistory);

        // Các nút hành động nhanh
        btnActionCall = view.findViewById(R.id.btnActionCall);
        btnActionMessage = view.findViewById(R.id.btnActionMessage);
        btnActionEmail = view.findViewById(R.id.btnActionEmail);
    }

    // SỬA LỖI: Thêm tham số (View view) vào hàm
    private void loadDataFromBundle(View view) {
        Bundle args = getArguments();
        if (args != null) {
            customerId = args.getString("id");

            // Set Text an toàn
            tvCode.setText(customerId != null ? customerId : "---");
            tvName.setText(args.getString("name", "Tên khách hàng"));
            tvPhone.setText(args.getString("phone", "---"));
            tvEmail.setText(args.getString("email", "---"));
            tvDob.setText(args.getString("dob", "---"));
            tvAddress.setText(args.getString("address", "---"));

            // Set các trường bổ sung
            tvCitizenId.setText(args.getString("cccd", "---"));
            tvNationality.setText("Việt Nam");

            String gender = args.getString("gender");
            // Cập nhật text hiển thị giới tính
            if (tvGender != null) tvGender.setText(gender != null ? gender : "---");

            // --- LOGIC HIỂN THỊ ẢNH ---
            // SỬA LỖI: Dùng view.findViewById thay vì getView().findViewById
            ImageView imgAvatar = view.findViewById(R.id.imgAvatar);

            if (gender != null) {
                if (gender.trim().equalsIgnoreCase("Nam")) {
                    imgAvatar.setImageResource(R.drawable.ic_avatar_male);
                } else if (gender.trim().equalsIgnoreCase("Nữ")) {
                    imgAvatar.setImageResource(R.drawable.ic_avatar_female);
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_avatar_default); // Nên dùng icon default thay vì launcher
                }
            } else {
                imgAvatar.setImageResource(R.drawable.ic_avatar_default);
            }
        }
    }

    private void setupEvents() {
        // Nút Back
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Nút Chỉnh sửa
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Nút Xem lịch sử
        btnViewAllHistory.setOnClickListener(v -> {
            LichSuDatTourFragment historyFragment = new LichSuDatTourFragment();
            Bundle bundle = new Bundle();
            bundle.putString("customer_id", customerId);
            historyFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_content_frame, historyFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Nút Gọi nhanh
        if (btnActionCall != null) {
            btnActionCall.setOnClickListener(v -> {
                String phoneNumber = tvPhone.getText().toString();
                if (!phoneNumber.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                }
            });
        }
    }
}