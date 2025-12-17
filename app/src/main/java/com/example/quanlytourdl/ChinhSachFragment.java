package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChinhSachFragment extends Fragment {

    // Khai báo các biến View
    private ImageView btnQuayLai, btnThemChinhSach;
    private EditText edtTimKiem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout mới cập nhật
        View view = inflater.inflate(R.layout.fragment_chinh_sach, container, false);

        initViews(view);
        setupEvents();

        return view;
    }

    private void initViews(View view) {
        // Ánh xạ đúng với ID trong file XML mới
        btnQuayLai = view.findViewById(R.id.btnQuayLai);
        btnThemChinhSach = view.findViewById(R.id.btnThemChinhSach);
        edtTimKiem = view.findViewById(R.id.edtTimKiem);
    }

    private void setupEvents() {
        // 1. Sự kiện nút Quay lại
        btnQuayLai.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // 2. Sự kiện nút Thêm Chính Sách (+)
        btnThemChinhSach.setOnClickListener(v -> {
            // Hiện tại hiển thị thông báo, sau này bạn có thể thay bằng lệnh chuyển màn hình
            Toast.makeText(getContext(), "Chức năng thêm chính sách đang phát triển", Toast.LENGTH_SHORT).show();

            // Ví dụ code chuyển màn hình sau này:
            // replaceFragment(new ThemChinhSachFragment());
        });

        // 3. Xử lý ô tìm kiếm (Tùy chọn: Nếu muốn bắt sự kiện khi gõ phím)
        /*
        edtTimKiem.addTextChangedListener(new TextWatcher() {
            // ... (Xử lý lọc danh sách tại đây nếu danh sách là động)
        });
        */
    }

    // Hàm phụ để chuyển Fragment (nếu cần dùng sau này)
    /*
    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }
    */
}