package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
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

import com.example.quanlytourdl.adapter.QuanLyHdvPhuongTienAdapter;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// IMPORT FRAGMENT THÊM MỚI (Giả định các class này tồn tại)
import com.example.quanlytourdl.AddPhuongTienFragment;
import com.example.quanlytourdl.AddHdvFragment;
// IMPORT MODEL CLASS (Giả định các class này tồn tại)
import com.example.quanlytourdl.model.Guide;
import com.example.quanlytourdl.model.Vehicle;

public class QuanLyHdvPhuongTienFragment extends Fragment {

    // MARK: - HẰNG SỐ (CONSTANTS)
    private static final String TAG = "QLHDVPT_Fragment";

    // Loại đối tượng
    private static final String TYPE_HDV = "Hướng dẫn viên";
    private static final String TYPE_PT = "Phương tiện";

    // Tên Collection trong Firestore
    private static final String COLLECTION_HDV = "huongdanvien";
    private static final String COLLECTION_PT = "phuongtien";

    // Tên Trường Trạng thái trong Firestore
    private static final String FIELD_STATUS_HDV = "trangThai";
    private static final String FIELD_STATUS_PT = "tinhTrangBaoDuong"; // Hoặc tên trường khác

    // Giá trị Trạng thái chung
    private static final String STATUS_ALL = "Tất cả";

    // Mapping giá trị trạng thái Phương tiện (PT) với tên chip HDV
    // Giả định: Chip "Sẵn sàng" -> PT "Hoạt động tốt"; Chip "Đang đi tour" -> PT "Đang bảo dưỡng"; Chip "Tạm nghỉ" -> PT "Cần sửa chữa lớn"
    private static final String STATUS_PT_HOAT_DONG_TOT = "Hoạt động tốt";
    private static final String STATUS_PT_DANG_BAO_DUONG = "Đang bảo dưỡng";
    private static final String STATUS_PT_CAN_SUA_CHUA_LON = "Cần sửa chữa lớn";


    // MARK: - BIẾN LỚP
    private FirebaseFirestore db;
    private ListenerRegistration currentListener = null;

    private TabLayout tabLayout;
    private FloatingActionButton btnAddItem;
    private ChipGroup chipGroupStatus;
    private RecyclerView recyclerView;

    private QuanLyHdvPhuongTienAdapter adapter;
    private int chipTatCaId; // ID của chip "Tất cả"

    public QuanLyHdvPhuongTienFragment() {
        // Constructor rỗng bắt buộc
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo Firebase Firestore
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quan_ly_hdv_phuong_tien, container, false);

        // 1. Ánh xạ các thành phần từ XML
        ImageButton btnBack = view.findViewById(R.id.btn_back_quan_ly);
        ImageButton btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer_ql);
        tabLayout = view.findViewById(R.id.tab_layout_hdv_pt);
        btnAddItem = view.findViewById(R.id.btn_add_item);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        recyclerView = view.findViewById(R.id.recycler_hdv_phuong_tien);

        // Lấy ID chip 'Tất cả'
        View chipTatCa = view.findViewById(R.id.chip_tat_ca);
        if (chipTatCa != null) {
            chipTatCaId = chipTatCa.getId();
        }

        // 2. Thiết lập trạng thái ban đầu và dữ liệu mặc định
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Adapter ban đầu là Hướng dẫn viên
        adapter = new QuanLyHdvPhuongTienAdapter(true);
        recyclerView.setAdapter(adapter);

        // Chọn chip "Tất cả" mặc định và tải dữ liệu ban đầu
        if (chipTatCaId != 0) {
            chipGroupStatus.check(chipTatCaId);
        }
        loadList(TYPE_HDV, STATUS_ALL); // Tab mặc định là HDV, Lọc mặc định là Tất cả

        // 3. Xử lý sự kiện Toolbar
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Quay lại màn hình trước
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        btnMenuDrawer.setOnClickListener(v -> {
            // Xử lý sự kiện mở menu drawer
            Toast.makeText(getContext(), "Mở Navigation Drawer hoặc Overflow Menu", Toast.LENGTH_SHORT).show();
        });

        // 4. Xử lý sự kiện Tab (Hướng dẫn viên / Phương tiện)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String type = tab.getText().toString();

                // ➡️ Reset adapter theo loại đối tượng mới
                boolean isGuideList = type.equals(TYPE_HDV);
                adapter = new QuanLyHdvPhuongTienAdapter(isGuideList);
                recyclerView.setAdapter(adapter);

                // Reset bộ lọc trạng thái về "Tất cả" và tải dữ liệu mới
                if (chipTatCaId != 0) {
                    chipGroupStatus.check(chipTatCaId);
                }
                loadList(type, STATUS_ALL);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 5. Xử lý sự kiện ChipGroup (Lọc trạng thái)
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Đảm bảo chỉ có 1 chip được chọn
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);

                String type = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
                String statusChipName = getStatusFromChipId(checkedId);

                // 🚀 GỌI HÀM LỌC DỮ LIỆU MỚI
                loadList(type, statusChipName);
            } else {
                // Trường hợp người dùng có thể bỏ chọn chip (nếu chipGroup cho phép) -> nên luôn chọn lại chip 'Tất cả'
                if (chipTatCaId != 0) {
                    chipGroupStatus.check(chipTatCaId);
                }
            }
        });

        // 6. Xử lý nút Thêm (+)
        btnAddItem.setOnClickListener(v -> handleAddItemClick());

        return view;
    }

    /**
     * Xử lý logic chuyển Fragment khi nhấn nút Thêm (+)
     */
    private void handleAddItemClick() {
        if (tabLayout.getSelectedTabPosition() == -1) return; // Không có tab nào được chọn

        String type = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
        Fragment targetFragment = null;

        if (type.equals(TYPE_HDV)) {
            targetFragment = new AddHdvFragment();
            Toast.makeText(getContext(), "Chuyển sang màn hình Thêm Hướng dẫn viên", Toast.LENGTH_SHORT).show();
        } else if (type.equals(TYPE_PT)) {
            targetFragment = new AddPhuongTienFragment();
            Toast.makeText(getContext(), "Chuyển sang màn hình Thêm Phương tiện", Toast.LENGTH_SHORT).show();
        }

        if (targetFragment != null && requireActivity() != null) {
            // Thay thế Fragment hiện tại bằng Fragment thêm mới
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content_frame, targetFragment)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else if (targetFragment == null) {
            Toast.makeText(getContext(), "Lỗi: Fragment thêm mới chưa được định nghĩa hoặc loại không xác định.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Hàm tải dữ liệu real-time từ Firestore
     * @param type Loại đối tượng: "Hướng dẫn viên" hoặc "Phương tiện"
     * @param statusChipName Tên trạng thái từ chip (ví dụ: "Sẵn sàng", "Tất cả")
     */
    private void loadList(String type, String statusChipName) {
        // Hủy bỏ listener cũ trước khi tạo listener mới để tránh rò rỉ bộ nhớ
        if (currentListener != null) {
            currentListener.remove();
            Log.d(TAG, "Đã hủy listener cũ.");
        }

        // 1. Thiết lập các thông số truy vấn
        String collectionName;
        String statusField;
        String filterValue = statusChipName; // Mặc định: giá trị lọc = tên chip

        if (type.equals(TYPE_HDV)) {
            collectionName = COLLECTION_HDV;
            statusField = FIELD_STATUS_HDV;
        } else if (type.equals(TYPE_PT)) {
            collectionName = COLLECTION_PT;
            statusField = FIELD_STATUS_PT;

            // 2. Ánh xạ tên chip HDV sang giá trị trạng thái PT trong DB
            filterValue = mapStatusChipToVehicleStatus(statusChipName);
        } else {
            Toast.makeText(getContext(), "Loại dữ liệu không hợp lệ.", Toast.LENGTH_SHORT).show();
            adapter.updateData(new ArrayList<>());
            return;
        }

        Query query = db.collection(collectionName);

        // 3. Áp dụng bộ lọc trạng thái nếu không phải là "Tất cả"
        if (!statusChipName.equals(STATUS_ALL)) {
            query = query.whereEqualTo(statusField, filterValue);
        }

        Log.d(TAG, "Đang tải dữ liệu: " + type + " - Lọc: " + statusField + "=" + filterValue);
        Toast.makeText(getContext(), "Đang tải: " + type + " - Trạng thái lọc: " + statusChipName, Toast.LENGTH_SHORT).show();

        // ⚡️ Thiết lập Listener real-time (onSnapshot)
        currentListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Lỗi khi lắng nghe dữ liệu:", error);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (value != null) {
                List<Object> items = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    try {
                        // Chuyển đổi Firestore Document thành Model Class tương ứng
                        if (type.equals(TYPE_HDV)) {
                            Guide guide = doc.toObject(Guide.class);
                            guide.setId(doc.getId()); // Lưu ID Document
                            items.add(guide);
                        } else if (type.equals(TYPE_PT)) {
                            Vehicle vehicle = doc.toObject(Vehicle.class);
                            vehicle.setId(doc.getId()); // Lưu ID Document
                            items.add(vehicle);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi chuyển đổi dữ liệu cho doc: " + doc.getId(), e);
                    }
                }
                adapter.updateData(items); // Cập nhật dữ liệu vào Adapter
                Log.d(TAG, "Đã tải thành công " + items.size() + " mục cho " + type + ". (Filter: " + statusChipName + ")");
            } else {
                adapter.updateData(new ArrayList<>()); // Xóa dữ liệu nếu không có
                Toast.makeText(getContext(), "Không tìm thấy dữ liệu " + type + " nào.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Không có dữ liệu cho " + type + ".");
            }
        });
    }

    /**
     * Ánh xạ tên Chip HDV sang giá trị trạng thái của Phương tiện trong DB.
     * @param statusChipName Tên trạng thái từ chip (ví dụ: "Sẵn sàng")
     * @return Giá trị trạng thái tương ứng trong collection Phương tiện
     */
    private String mapStatusChipToVehicleStatus(String statusChipName) {
        switch (statusChipName) {
            case "Sẵn sàng":
                return STATUS_PT_HOAT_DONG_TOT;
            case "Đang đi tour":
                return STATUS_PT_DANG_BAO_DUONG; // Giả định chip "Đang đi tour" tương ứng với trạng thái bảo dưỡng/bận của PT
            case "Tạm nghỉ":
                return STATUS_PT_CAN_SUA_CHUA_LON; // Giả định chip "Tạm nghỉ" tương ứng với trạng thái nghỉ/hỏng của PT
            case STATUS_ALL:
            default:
                return STATUS_ALL;
        }
    }

    /**
     * Ánh xạ ID của Chip thành chuỗi Trạng thái hiển thị (tên chip)
     */
    private String getStatusFromChipId(int chipId) {
        if (chipId == R.id.chip_san_sang) return "Sẵn sàng";
        if (chipId == R.id.chip_dang_di_tour) return "Đang đi tour";
        if (chipId == R.id.chip_tam_nghi) return "Tạm nghỉ";

        // Mặc định là chip_tat_ca
        return STATUS_ALL;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // ⚠️ QUAN TRỌNG: Hủy bỏ Listener khi Fragment bị hủy để tránh rò rỉ bộ nhớ
        if (currentListener != null) {
            currentListener.remove();
            currentListener = null;
            Log.d(TAG, "Đã hủy listener khi Fragment bị hủy.");
        }
    }
}