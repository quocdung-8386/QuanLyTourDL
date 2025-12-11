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
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;

public class ChiTietKhachHangFragment extends Fragment {

    // Khai báo View (Dùng TextView vì chỉ hiển thị)
    private ImageView btnBack;
    private TextView tvName, tvCode, tvDob, tvGender, tvCitizenId, tvPhone, tvEmail, tvAddress, tvNationality;
    private MaterialButton btnEditProfile;
    private LinearLayout btnActionCall, btnActionMessage, btnActionEmail; // Các nút tròn
    private TextView btnViewAllHistory; // Nút xem tất cả lịch sử

    private String customerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo tên layout trùng với file XML của bạn
        View view = inflater.inflate(R.layout.fragment_chi_tiet_khach_hang, container, false);

        // 1. ÁNH XẠ VIEW
        initViews(view);

        // 2. NHẬN DỮ LIỆU TỪ BUNDLE (Danh sách gửi sang)
        loadDataFromBundle();

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

        // Các nút hành động nhanh (Gọi, map...)
        btnActionCall = view.findViewById(R.id.btnActionCall);
        btnActionMessage = view.findViewById(R.id.btnActionMessage);
        btnActionEmail = view.findViewById(R.id.btnActionEmail);
    }

    private void loadDataFromBundle() {
        Bundle args = getArguments();
        if (args != null) {
            customerId = args.getString("id");

            // Set Text an toàn (kiểm tra null)
            tvCode.setText(customerId != null ? customerId : "---");
            tvName.setText(args.getString("name", "Tên khách hàng"));
            tvPhone.setText(args.getString("phone", "---"));
            tvEmail.setText(args.getString("email", "---"));
            tvDob.setText(args.getString("dob", "---"));
            tvAddress.setText(args.getString("address", "---"));

            // Các trường chưa có trong Bundle cũ thì set mặc định hoặc lấy thêm
            tvGender.setText(args.getString("gender", "Nam"));
            tvCitizenId.setText(args.getString("cccd", "---"));
            tvNationality.setText("Việt Nam"); // Ví dụ cứng hoặc lấy từ data
            String gender = args.getString("gender");

            // --- LOGIC HIỂN THỊ ẢNH ---
            ImageView imgAvatar = getView().findViewById(R.id.imgAvatar); // Đảm bảo đã ánh xạ

            if (gender != null) {
                if (gender.trim().equalsIgnoreCase("Nam")) {
                    imgAvatar.setImageResource(R.drawable.ic_avatar_male);
                } else if (gender.trim().equalsIgnoreCase("Nữ")) {
                    imgAvatar.setImageResource(R.drawable.ic_avatar_female);
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_launcher_background);
                }

                // Cập nhật text hiển thị giới tính
                if (tvGender != null) tvGender.setText(gender);
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

        // Nút Chỉnh sửa (Sẽ mở ra Fragment/Activity sửa đổi thông tin)
        btnEditProfile.setOnClickListener(v -> {
            // TODO: Chuyển sang màn hình Chỉnh Sửa (EditFragment)
            Toast.makeText(getContext(), "Chức năng đang phát triển: Chuyển sang màn hình sửa", Toast.LENGTH_SHORT).show();

            /* Code mẫu chuyển trang:
            EditCustomerFragment editFragment = new EditCustomerFragment();
            editFragment.setArguments(getArguments()); // Gửi lại dữ liệu sang để sửa
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_content_frame, editFragment)
                    .addToBackStack(null)
                    .commit();
            */
        });

        // Nút Xem lịch sử
        btnViewAllHistory.setOnClickListener(v -> {
            LichSuDatTourFragment historyFragment = new LichSuDatTourFragment();
            Bundle bundle = new Bundle();
            bundle.putString("customer_id", customerId);
            historyFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_content_frame, historyFragment) // Thay R.id.main_content_frame bằng ID container của bạn
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