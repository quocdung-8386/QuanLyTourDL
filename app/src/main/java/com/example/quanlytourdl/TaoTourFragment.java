package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class TaoTourFragment extends Fragment {

    private MaterialButton btnStartCreateTour;
    private View cardEditTour, cardAssignGuide;

    private ImageButton btnBack, btnMenuDrawer;

    public TaoTourFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tao_tour, container, false);

        btnStartCreateTour = view.findViewById(R.id.btn_start_create_tour);
        cardEditTour = view.findViewById(R.id.card_edit_tour);
        cardAssignGuide = view.findViewById(R.id.card_assign_guide);

        btnBack = view.findViewById(R.id.btn_back_tour);
        btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer_tour);



        btnBack.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Quay lại màn hình trước", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // 🍔 Nút MENU 3 GẠCH
        btnMenuDrawer.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở Navigation Drawer hoặc Menu Tùy chọn", Toast.LENGTH_SHORT).show();
            // Xử lý logic mở Navigation Drawer hoặc hiển thị Overflow Menu
        });


        // Nút Bắt đầu tạo Tour
        btnStartCreateTour.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Bắt đầu quá trình tạo Tour mới theo các bước", Toast.LENGTH_SHORT).show();
            // THƯỜNG: Chuyển sang Fragment/Activity đầu tiên trong quy trình tạo Tour (ví dụ: màn hình nhập thông tin cơ bản)
        });

        // Card Chỉnh sửa Tour
        cardEditTour.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở danh sách Tour để chỉnh sửa", Toast.LENGTH_SHORT).show();
        });

        // Card Gán hướng dẫn viên
        cardAssignGuide.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở màn hình Gán hướng dẫn viên cho Tour", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}