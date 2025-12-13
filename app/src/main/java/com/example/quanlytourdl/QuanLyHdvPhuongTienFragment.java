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


    private static final String TAG = "QLHDVPT_Fragment";

    private static final String TYPE_HDV = "Hướng dẫn viên";
    private static final String TYPE_PT = "Phương tiện";

    private static final String COLLECTION_HDV = "tour_guides";
    private static final String COLLECTION_PT = "phuongtien";

    private static final String FIELD_STATUS_HDV = "trangThai";
    private static final String FIELD_STATUS_PT = "tinhTrangBaoDuong";

    private static final String STATUS_ALL = "Tất cả";
    private static final String STATUS_PT_HOAT_DONG_TOT = "Hoạt động tốt";
    private static final String STATUS_PT_DANG_BAO_DUONG = "Đang bảo dưỡng";
    private static final String STATUS_PT_CAN_SUA_CHUA_LON = "Cần sửa chữa lớn";
    private FirebaseFirestore db;
    private ListenerRegistration currentListener = null;

    private TabLayout tabLayout;
    private FloatingActionButton btnAddItem;
    private ChipGroup chipGroupStatus;
    private RecyclerView recyclerView;

    private QuanLyHdvPhuongTienAdapter adapter;
    private int chipTatCaId;

    public QuanLyHdvPhuongTienFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quan_ly_hdv_phuong_tien, container, false);
        ImageButton btnBack = view.findViewById(R.id.btn_back_quan_ly);
        ImageButton btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer_ql);
        tabLayout = view.findViewById(R.id.tab_layout_hdv_pt);
        btnAddItem = view.findViewById(R.id.btn_add_item);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        recyclerView = view.findViewById(R.id.recycler_hdv_phuong_tien);

        View chipTatCa = view.findViewById(R.id.chip_tat_ca);
        if (chipTatCa != null) {
            chipTatCaId = chipTatCa.getId();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new QuanLyHdvPhuongTienAdapter(true);
        recyclerView.setAdapter(adapter);
        if (chipTatCaId != 0) {
            chipGroupStatus.check(chipTatCaId);
        }
        loadList(TYPE_HDV, STATUS_ALL);
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        btnMenuDrawer.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở Navigation Drawer hoặc Overflow Menu", Toast.LENGTH_SHORT).show();
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String type = tab.getText().toString();

                boolean isGuideList = type.equals(TYPE_HDV);
                adapter = new QuanLyHdvPhuongTienAdapter(isGuideList);
                recyclerView.setAdapter(adapter);

                if (chipTatCaId != 0) {
                    chipGroupStatus.check(chipTatCaId);
                }
                loadList(type, STATUS_ALL);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);

                String type = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
                String statusChipName = getStatusFromChipId(checkedId);

                loadList(type, statusChipName);
            } else {
                if (chipTatCaId != 0) {
                    chipGroupStatus.check(chipTatCaId);
                }
            }
        });

        btnAddItem.setOnClickListener(v -> handleAddItemClick());

        return view;
    }

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
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content_frame, targetFragment)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else if (targetFragment == null) {
            Toast.makeText(getContext(), "Lỗi: Fragment thêm mới chưa được định nghĩa hoặc loại không xác định.", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadList(String type, String statusChipName) {
        if (currentListener != null) {
            currentListener.remove();
            Log.d(TAG, "Đã hủy listener cũ.");
        }

        String collectionName;
        String statusField;
        String filterValue = statusChipName;

        if (type.equals(TYPE_HDV)) {
            collectionName = COLLECTION_HDV;
            statusField = FIELD_STATUS_HDV;
        } else if (type.equals(TYPE_PT)) {
            collectionName = COLLECTION_PT;
            statusField = FIELD_STATUS_PT;
            filterValue = mapStatusChipToVehicleStatus(statusChipName);
        } else {
            Toast.makeText(getContext(), "Loại dữ liệu không hợp lệ.", Toast.LENGTH_SHORT).show();
            adapter.updateData(new ArrayList<>());
            return;
        }

        Query query = db.collection(collectionName);

        if (!statusChipName.equals(STATUS_ALL)) {
            query = query.whereEqualTo(statusField, filterValue);
        }

        Log.d(TAG, "Đang tải dữ liệu: " + type + " - Lọc: " + statusField + "=" + filterValue + " từ collection: " + collectionName);
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
                int successCount = 0;
                int errorCount = 0;

                for (QueryDocumentSnapshot doc : value) {
                    try {
                        if (type.equals(TYPE_HDV)) {
                            Guide guide = doc.toObject(Guide.class);
                            guide.setId(doc.getId()); // Lưu ID Document
                            items.add(guide);
                            successCount++;
                        } else if (type.equals(TYPE_PT)) {
                            Vehicle vehicle = doc.toObject(Vehicle.class);
                            vehicle.setId(doc.getId()); // Lưu ID Document
                            items.add(vehicle);
                            successCount++;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi chuyển đổi dữ liệu cho doc ID: " + doc.getId() + " trong collection " + collectionName + ". Kiểm tra Model Class.", e);
                        errorCount++;
                    }
                }

                adapter.updateData(items); // Cập nhật dữ liệu vào Adapter

                if (successCount > 0) {
                    Log.d(TAG, "Đã tải thành công " + successCount + " mục cho " + type + ". (Tổng documents: " + value.size() + ", Lỗi conversion: " + errorCount + ")");
                } else if (value.size() > 0 && errorCount == value.size()) {
                    Log.e(TAG, "LỖI CHUYỂN ĐỔI: Tất cả " + value.size() + " documents trong collection '" + collectionName + "' đều lỗi. Kiểm tra class Model (" + (type.equals(TYPE_HDV) ? "Guide" : "Vehicle") + ").");
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu. Kiểm tra định dạng dữ liệu trong Firestore.", Toast.LENGTH_LONG).show();
                } else {
                    // Trường hợp value.isEmpty() hoặc không có data hợp lệ
                    adapter.updateData(new ArrayList<>());
                    Log.d(TAG, "Không có dữ liệu nào (hoặc không có dữ liệu hợp lệ) cho " + type + ".");
                }
            } else {
                adapter.updateData(new ArrayList<>()); // Xóa dữ liệu nếu value là null (rất hiếm)
                Toast.makeText(getContext(), "Không tìm thấy dữ liệu " + type + " nào.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Giá trị Snapshot là null. Không có dữ liệu cho " + type + ".");
            }
        });
    }
    private String mapStatusChipToVehicleStatus(String statusChipName) {
        switch (statusChipName) {
            case "Sẵn sàng":
                return STATUS_PT_HOAT_DONG_TOT;
            case "Đang đi tour":
                return STATUS_PT_DANG_BAO_DUONG;
            case "Tạm nghỉ":
                return STATUS_PT_CAN_SUA_CHUA_LON;
            case STATUS_ALL:
            default:
                return STATUS_ALL;
        }
    }

    private String getStatusFromChipId(int chipId) {
        if (chipId == R.id.chip_san_sang) return "Sẵn sàng";
        if (chipId == R.id.chip_dang_di_tour) return "Đang đi tour";
        if (chipId == R.id.chip_tam_nghi) return "Tạm nghỉ";


        return STATUS_ALL;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentListener != null) {
            currentListener.remove();
            currentListener = null;
            Log.d(TAG, "Đã hủy listener khi Fragment bị hủy.");
        }
    }
}