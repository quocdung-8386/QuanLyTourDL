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
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;

// Import các Fragment cần thiết
import com.example.quanlytourdl.TaoTourDetailFullFragment;
// Giả định TourAssignmentListFragment tồn tại
import com.example.quanlytourdl.TourAssignmentListFragment;

public class TaoTourFragment extends Fragment {

    // Thay thế bằng ID FrameLayout/FragmentContainerView thực tế của bạn
    private static final int CONTAINER_ID = R.id.main_content_frame; // Giả định ID này tồn tại

    private MaterialButton btnStartCreateTour;
    private View cardEditTour, cardAssignGuide;

    private ImageButton btnBack, btnMenuDrawer;

    // --- Placeholder Fragment Names cho các Fragment khác ---
    // Đã thay thế FRAGMENT_ASSIGN_GUIDE bằng tên class mới (nếu dùng reflection)
    private static final String FRAGMENT_TOUR_MANAGER = "com.example.quanlytourdl.QuanLyTourFragment";

    // Tên class mới cho Fragment gán HDV/Phương tiện
    private static final String FRAGMENT_ASSIGNMENT_LIST = "com.example.quanlytourdl.TourAssignmentListFragment";


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


        // Nút BACK: Quay lại Fragment trước đó trong Back Stack
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Sử dụng getParentFragmentManager để quản lý Back Stack
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                    Toast.makeText(getContext(), "Quay lại màn hình trước", Toast.LENGTH_SHORT).show();
                } else {
                    // Trường hợp không còn gì trong back stack, có thể đóng activity
                    getActivity().finish();
                }
            }
        });

        // 1. Nút Bắt đầu tạo Tour -> CHUYỂN ĐẾN TaoTourDetailFullFragment (Fragment đa bước)
        btnStartCreateTour.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuyển đến màn hình nhập chi tiết Tour đa bước", Toast.LENGTH_SHORT).show();
            navigateToFragment(new TaoTourDetailFullFragment()); // Sử dụng constructor mặc định
        });

        // 2. Card Chỉnh sửa Tour -> Chuyển đến Fragment Quản Lý/Danh Sách Tour
        cardEditTour.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở danh sách Tour để chỉnh sửa", Toast.LENGTH_SHORT).show();
            navigateToFragmentByClassName(FRAGMENT_TOUR_MANAGER);
        });

        // 3. Card Gán hướng dẫn viên và phương tiện -> Chuyển đến TourAssignmentListFragment
        cardAssignGuide.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở màn hình Gán hướng dẫn viên và phương tiện cho Tour", Toast.LENGTH_SHORT).show();
            navigateToFragmentByClassName(FRAGMENT_ASSIGNMENT_LIST); // Gọi Fragment mới
        });

        return view;
    }

    /**
     * Hàm tiện ích để chuyển đổi giữa các Fragment bằng đối tượng Fragment.
     * Đây là phương pháp an toàn và được khuyến nghị.
     * @param targetFragment Đối tượng Fragment đích.
     */
    private void navigateToFragment(Fragment targetFragment) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(CONTAINER_ID, targetFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * Hàm tiện ích gốc để chuyển đổi bằng tên lớp (dùng cho các Fragments placeholder).
     * @param fragmentClassName Tên đầy đủ của Fragment đích.
     */
    private void navigateToFragmentByClassName(String fragmentClassName) {
        if (getParentFragmentManager() != null) {
            FragmentManager fm = getParentFragmentManager();
            Fragment targetFragment;

            try {
                // Khởi tạo Fragment đích thông qua reflection
                Class<?> fragmentClass = Class.forName(fragmentClassName);
                // Giả định có constructor không tham số
                targetFragment = (Fragment) fragmentClass.newInstance();
            } catch (ClassNotFoundException e) {
                Toast.makeText(getContext(), "Lỗi: Fragment " + fragmentClassName + " chưa được định nghĩa.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return;
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi khởi tạo Fragment: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return;
            }

            // Thực hiện giao dịch Fragment
            fm.beginTransaction()
                    .replace(CONTAINER_ID, targetFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}