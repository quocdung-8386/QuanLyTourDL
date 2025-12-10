package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChiTietKhachHangFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_khach_hang, container, false);

        // 1. Ánh xạ View
        TextView tvName = view.findViewById(R.id.tvNameDetail);
        TextView tvPhone = view.findViewById(R.id.tvPhoneDetail);
        ImageView imgAvatar = view.findViewById(R.id.imgAvatarDetail);
        ImageView btnBack = view.findViewById(R.id.btnBack);
        View btnUpdate = view.findViewById(R.id.btnUpdate);

        // 2. Nhận dữ liệu từ Fragment Danh Sách gửi sang
        Bundle args = getArguments();
        if (args != null) {
            String name = args.getString("name");
            String phone = args.getString("phone");
            int avatarResId = args.getInt("avatar");

            tvName.setText(name);
            tvPhone.setText(phone);
            imgAvatar.setImageResource(avatarResId);
        }

        // 3. Xử lý nút Back (Quay lại danh sách)
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // 4. Xử lý nút Cập nhật
        btnUpdate.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}