package com.example.quanlytourdl; // THAY THẾ bằng package của bạn

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

public class QuanLyTourFragment extends Fragment {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ImageButton btnOptionsMenu;

    // Biến lưu trạng thái tab đang chọn
    private String currentTourStatus = "Đang mở bán";

    public QuanLyTourFragment() {
        // Constructor rỗng bắt buộc
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quan_ly_tour, container, false);

        // 1. Ánh xạ các View
        toolbar = view.findViewById(R.id.toolbar_kinh_doanh);
        tabLayout = view.findViewById(R.id.tab_layout_tour_status);
        recyclerView = view.findViewById(R.id.recycler_tour_list);
        btnOptionsMenu = view.findViewById(R.id.btn_options_menu);

        // 2. Thiết lập Toolbar và các nút hành động

        // Xử lý nút Back (Navigation Icon)
        toolbar.setNavigationOnClickListener(v -> {
            // Xử lý hành động quay lại, ví dụ: đóng Fragment/Activity
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            Toast.makeText(getContext(), "Quay lại", Toast.LENGTH_SHORT).show();
        });

        // Xử lý nút Menu 3 gạch (Hamburger Icon)
        btnOptionsMenu.setOnClickListener(v -> {
            // Mở Navigation Drawer hoặc hiển thị Dropdown Menu
            Toast.makeText(getContext(), "Mở Menu 3 gạch (Navigation Drawer)", Toast.LENGTH_SHORT).show();
        });

        // 3. Thiết lập TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Lấy tên tab đã chọn
                currentTourStatus = tab.getText() != null ? tab.getText().toString() : "Đang mở bán";
                // Tải dữ liệu mới cho RecyclerView
                loadTourList(currentTourStatus);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 4. Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tải danh sách mặc định (lần đầu tiên mở fragment)
        loadTourList(currentTourStatus);

        return view;
    }

    /**
     * Hàm giả lập tải danh sách tour dựa trên trạng thái (status)
     * @param status Trạng thái tour (Đang mở bán, Hết hạn, Nháp)
     */
    private void loadTourList(String status) {
        // TODO: Viết code thực tế để gọi API/Database và tải dữ liệu

        Toast.makeText(getContext(), "Đang tải danh sách Tour: " + status, Toast.LENGTH_SHORT).show();

        // Ví dụ:
        // List<Tour> tours = tourRepository.getToursByStatus(status);
        // TourAdapter adapter = new TourAdapter(tours);
        // recyclerView.setAdapter(adapter);
    }
}