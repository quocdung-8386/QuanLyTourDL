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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

// IMPORT FRAGMENT THÊM MỚI (Đảm bảo các file này tồn tại trong package)
import com.example.quanlytourdl.AddPhuongTienFragment;
import com.example.quanlytourdl.AddHdvFragment; // ĐÃ THÊM IMPORT

public class QuanLyHdvPhuongTienFragment extends Fragment {

    // Khai báo các View cần tương tác
    private ImageButton btnBack;
    private ImageButton btnMenuDrawer;
    private TabLayout tabLayout;
    private FloatingActionButton btnAddItem;
    private ChipGroup chipGroupStatus;
    private RecyclerView recyclerView;

    // Lưu ID nút 'Tất cả' để dùng làm mặc định và reset
    private int chipTatCaId;

    public QuanLyHdvPhuongTienFragment() {
        // Constructor rỗng bắt buộc
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quan_ly_hdv_phuong_tien, container, false);

        // 1. Ánh xạ các thành phần từ XML
        btnBack = view.findViewById(R.id.btn_back_quan_ly);
        btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer_ql);
        tabLayout = view.findViewById(R.id.tab_layout_hdv_pt);
        btnAddItem = view.findViewById(R.id.btn_add_item);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        recyclerView = view.findViewById(R.id.recycler_hdv_phuong_tien);

        // Lấy ID chip 'Tất cả' để sử dụng sau này
        chipTatCaId = view.findViewById(R.id.chip_tat_ca).getId();

        // 2. Thiết lập trạng thái ban đầu và dữ liệu mặc định
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Chọn chip "Tất cả" mặc định
        chipGroupStatus.check(chipTatCaId);
        // Tải danh sách mặc định (Hướng dẫn viên - Tất cả)
        loadList("Hướng dẫn viên", "Tất cả");

        // 3. Xử lý sự kiện Toolbar
        btnBack.setOnClickListener(v -> {
            // Xử lý quay lại (ví dụ: pop the fragment or finish activity)
            if (getActivity() != null) {
                // Quay lại Fragment trước đó trên Back Stack
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        btnMenuDrawer.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở Navigation Drawer hoặc Overflow Menu", Toast.LENGTH_SHORT).show();
        });

        // 4. Xử lý sự kiện Tab (Hướng dẫn viên / Phương tiện)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String type = tab.getText().toString();
                // ➡️ Khi chuyển tab, reset bộ lọc trạng thái về "Tất cả"
                chipGroupStatus.check(chipTatCaId);
                loadList(type, "Tất cả");
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 5. Xử lý sự kiện ChipGroup (Lọc trạng thái)
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Chỉ xử lý khi có ít nhất một Chip được chọn (vì singleSelection=true, chỉ có 1 ID trong checkedIds)
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);

                // Lấy loại đang được chọn (HDV hoặc PT)
                String type = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
                // Lấy trạng thái từ ID chip được chọn
                String status = getStatusFromChipId(checkedId);

                // 🚀 GỌI HÀM LỌC DỮ LIỆU
                loadList(type, status);
            }
        });

        // 6. Xử lý nút Thêm (+) ĐÃ CẬP NHẬT CONTAINER ID
        btnAddItem.setOnClickListener(v -> {
            String type = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
            Fragment targetFragment = null;

            if (type.equals("Hướng dẫn viên")) {
                // TẠO INSTANCE CỦA AddHdvFragment
                targetFragment = new AddHdvFragment();
                Toast.makeText(getContext(), "Chuyển sang màn hình Thêm Hướng dẫn viên", Toast.LENGTH_SHORT).show();
            } else if (type.equals("Phương tiện")) {
                // TẠO INSTANCE CỦA AddPhuongTienFragment
                targetFragment = new AddPhuongTienFragment();
                Toast.makeText(getContext(), "Chuyển sang màn hình Thêm Phương tiện", Toast.LENGTH_SHORT).show();
            }

            // Thực hiện giao dịch Fragment
            if (targetFragment != null && requireActivity() != null) {

                // SỬ DỤNG FragmentManager CỦA ACTIVITY ĐỂ THAY THẾ CONTAINER CHÍNH
                // FIX: Sử dụng R.id.main_content_frame để khớp với activity_main.xml
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content_frame, targetFragment)
                        .addToBackStack(null) // Cho phép nhấn nút Back để quay lại Fragment này
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN) // Thêm hiệu ứng chuyển động
                        .commit();
            } else if (targetFragment == null) {
                Toast.makeText(getContext(), "Lỗi: Không tìm thấy Fragment thêm mới hoặc loại không xác định.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * Hàm giả lập việc tải và lọc dữ liệu cho RecyclerView
     * @param type Loại đối tượng: "Hướng dẫn viên" hoặc "Phương tiện"
     * @param status Trạng thái: "Tất cả", "Sẵn sàng", "Đang đi tour", "Tạm nghỉ"
     */
    private void loadList(String type, String status) {
        // Trong ứng dụng thực tế, bạn sẽ gọi ViewModel/Repository ở đây để fetch data

        Toast.makeText(getContext(), "Đang tải: " + type + " - Trạng thái lọc: " + status, Toast.LENGTH_SHORT).show();

        // Ví dụ:
        // List<Object> filteredData = dataRepository.getFilteredItems(type, status);
        // recyclerView.setAdapter(new MyAdapter(filteredData));
    }

    /**
     * Ánh xạ ID của Chip thành chuỗi Trạng thái
     */
    private String getStatusFromChipId(int chipId) {
        if (chipId == R.id.chip_san_sang) return "Sẵn sàng";
        if (chipId == R.id.chip_dang_di_tour) return "Đang đi tour";
        if (chipId == R.id.chip_tam_nghi) return "Tạm nghỉ";
        // Mặc định là chip_tat_ca
        return "Tất cả";
    }
}