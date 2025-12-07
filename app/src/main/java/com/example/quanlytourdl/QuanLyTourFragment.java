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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.Collections;
import java.util.List;

public class QuanLyTourFragment extends Fragment {

    // ID Container của Activity chính (activity_main.xml)
    private static final int MAIN_CONTAINER_ID = R.id.main_content_frame;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ImageButton btnOptionsMenu;
    private FloatingActionButton fabAddTour;

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
        fabAddTour = view.findViewById(R.id.fab_add_tour);


        // 2. Thiết lập Toolbar và các nút hành động

        // Xử lý nút Back (Navigation Icon)
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            Toast.makeText(getContext(), "Quay lại", Toast.LENGTH_SHORT).show();
        });

        // Xử lý nút Menu 3 gạch (Hamburger Icon)
        btnOptionsMenu.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở Menu Tùy Chọn", Toast.LENGTH_SHORT).show();
            // LƯU Ý: Xem xét tăng kích thước vùng chạm của nút này trong XML (Touch target size too small)
        });

        // 3. Thiết lập TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTourStatus = tab.getText() != null ? tab.getText().toString() : "Đang mở bán";
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

        // 5. Xử lý sự kiện click cho FAB để chuyển sang TaoTourFragment
        fabAddTour.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {

                Fragment taoTourFragment;
                try {
                    // Cố gắng khởi tạo TaoTourFragment
                    taoTourFragment = (Fragment) Class.forName(getContext().getPackageName() + ".TaoTourFragment").newInstance();
                } catch (Exception e) {
                    // Xử lý lỗi nếu TaoTourFragment chưa tồn tại
                    Toast.makeText(getContext(), "Lỗi: Class TaoTourFragment chưa được định nghĩa", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    return;
                }

                // Thực hiện giao dịch Fragment
                getParentFragmentManager().beginTransaction()
                        // *** ĐÃ SỬA: Dùng MAIN_CONTAINER_ID (R.id.main_content_frame) thay cho R.id.fragment_container ***
                        .replace(MAIN_CONTAINER_ID, taoTourFragment)
                        .addToBackStack(null) // Thêm vào Back Stack để có thể quay lại
                        .commit();

                Toast.makeText(getContext(), "Chuyển đến màn hình Tạo Tour Mới", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * Hàm giả lập tải danh sách tour dựa trên trạng thái (status)
     * @param status Trạng thái tour (Đang mở bán, Hết hạn, Nháp)
     */
    private void loadTourList(String status) {
        Toast.makeText(getContext(), "Đang tải danh sách Tour: " + status, Toast.LENGTH_SHORT).show();

        // [Code giả lập Dữ liệu]
        // Cần có các lớp Tour, TourAdapter, và tourRepository để code này chạy thực tế.
        try {
            List<Object> placeholderTours = Collections.emptyList();

            // LƯU Ý: Thay thế code này bằng code thực tế của bạn
            // Ví dụ:
            // TourAdapter adapter = new TourAdapter(placeholderTours);
            // recyclerView.setAdapter(adapter);

        } catch (Exception e) {
            // Xử lý ngoại lệ nếu cần
        }
    }
}