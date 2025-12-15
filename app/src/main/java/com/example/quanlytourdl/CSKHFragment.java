package com.example.quanlytourdl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu; // Import PopupMenu
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.SupportTicketAdapter;
import com.example.quanlytourdl.model.SupportTicket;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class CSKHFragment extends Fragment {

    private ImageButton btnMenuDrawer;
    private TabLayout tabLayout;
    private MaterialButton btnMoPhieuHoTro;
    private RecyclerView recyclerView;
    private SupportTicketAdapter adapter;

    // Định nghĩa ID cho các item trong Menu để dễ quản lý
    private static final int MENU_ID_CSKH = 1;
    private static final int MENU_ID_QL_NHAN_SU = 2;
    private static final int MENU_ID_BANG_LUONG = 3;
    private static final int MENU_ID_PHAN_QUYEN = 4;
    private static final int MENU_ID_LUONG_THUONG_PHAT = 5;

    public CSKHFragment() {
        // Constructor rỗng bắt buộc
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cskh, container, false);

        // 1. Ánh xạ
        btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer_cskh);
        tabLayout = view.findViewById(R.id.tabLayout_cskh);
        btnMoPhieuHoTro = view.findViewById(R.id.btn_mo_phieu_ho_tro);
        recyclerView = view.findViewById(R.id.recycler_support_tickets);

        // 2. Thiết lập Listener cho Menu (Sử dụng PopupMenu)
        btnMenuDrawer.setOnClickListener(this::showDrawerMenu);

        view.findViewById(R.id.card_sentiment_analysis).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuyển sang màn hình Báo cáo Cảm xúc KH", Toast.LENGTH_SHORT).show();
        });

        btnMoPhieuHoTro.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở form Tạo Phiếu Hỗ trợ mới", Toast.LENGTH_SHORT).show();
        });

        setupTabLayout();
        setupRecyclerView();

        return view;
    }

    // --- XỬ LÝ POPUP MENU ---
    private void showDrawerMenu(View anchorView) {
        if (getContext() == null) return;

        // Khởi tạo PopupMenu gắn vào nút btnMenuDrawer
        PopupMenu popup = new PopupMenu(getContext(), anchorView);

        // Thêm các mục vào menu (groupId, itemId, order, title)
        popup.getMenu().add(1, MENU_ID_CSKH, 1, "CSKH & Hỗ trợ");
        popup.getMenu().add(1, MENU_ID_QL_NHAN_SU, 2, "Quản lý nhân sự");
        popup.getMenu().add(1, MENU_ID_BANG_LUONG, 3, "Bảng lương");
        popup.getMenu().add(1, MENU_ID_PHAN_QUYEN, 4, "Phân quyền truy cập");
        popup.getMenu().add(1, MENU_ID_LUONG_THUONG_PHAT, 5, "Lương thưởng/phạt");

        // Bắt sự kiện click vào từng item
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case MENU_ID_CSKH:
                    Toast.makeText(getContext(), "Bạn đang ở màn hình CSKH", Toast.LENGTH_SHORT).show();
                    return true;

                case MENU_ID_QL_NHAN_SU:
                   openQuanLyNhanSuFragment();
                    return true;

                case MENU_ID_BANG_LUONG:
                   openBangLuongFragment();
                    return true;

                case MENU_ID_PHAN_QUYEN: 
                    openPhanQuyenTruyCapFragment();
                    return true;

                case MENU_ID_LUONG_THUONG_PHAT:
                    openLuongThuongPhatFragment();
                    return true;

                default:
                    return false;
            }
        });

        // Hiển thị menu
        popup.show();
    }
    
    private void openQuanLyNhanSuFragment() {
        performFragmentTransaction(new QuanLyNhanSuFragment(), "Quản lý nhân sự");
    }

    private void openBangLuongFragment() {
        performFragmentTransaction(new BangLuongVaPhuCapFragment(), "Bảng lương & Phụ cấp");
    }

    private void openPhanQuyenTruyCapFragment() {
        performFragmentTransaction(new PhanQuyenTruyCapFragment(), "Phân quyền Truy cập");
    }

    private void openLuongThuongPhatFragment() {
        performFragmentTransaction(new LuongThuongPhatFragment(), "Lương & Thưởng/Phạt");
    }


    // --- HÀM HỖ TRỢ CHUYỂN FRAGMENT ---
    private void performFragmentTransaction(Fragment targetFragment, String screenName) {
        if (getParentFragmentManager() != null) {
            Toast.makeText(getContext(), "Chuyển đến: " + screenName, Toast.LENGTH_SHORT).show();

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            int containerId = getResources().getIdentifier("fragment_container", "id", requireContext().getPackageName());

            if (containerId == 0) {
                Log.e("CSKHFragment", "Could not find fragment container ID 'fragment_container'.");
                return;
            }

            transaction.replace(containerId, targetFragment);
            transaction.addToBackStack(null); // Cho phép quay lại màn hình trước bằng nút Back
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
        }
    }

    // --- CÁC HÀM SETUP UI KHÁC ---
    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Xử lý lọc danh sách theo tab
                Toast.makeText(getContext(), "Lọc: " + tab.getText(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        List<SupportTicket> ticketList = new ArrayList<>();
        ticketList.add(new SupportTicket("1205", "Vấn đề thanh toán online bị lỗi", "Nguyễn Văn A - Tour Đà Lạt", "Đã giải quyết", "2 giờ trước"));
        ticketList.add(new SupportTicket("1204", "Yêu cầu thay đổi ngày đi tour", "Trần Thị B - Tour Phú Quốc", "Đang xử lý", "5 phút trước"));
        ticketList.add(new SupportTicket("1203", "Báo cáo lỗi ứng dụng mobile", "Lê Văn C - Khách hàng mới", "Mới", "1 ngày trước"));

        adapter = new SupportTicketAdapter(ticketList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
}