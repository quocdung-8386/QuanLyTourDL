package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

// IMPORT FRAGMENT THÊM MỚI (Giả định các class này tồn tại)
import com.example.quanlytourdl.AddPhuongTienFragment;
import com.example.quanlytourdl.AddHdvFragment;
// IMPORT MODEL CLASS (Giả định các class này tồn tạo)
import com.example.quanlytourdl.model.Guide;
import com.example.quanlytourdl.model.Vehicle;
import com.example.quanlytourdl.model.NhaCungCap; // Import bổ sung cho tham số DetailFragment
import com.example.quanlytourdl.model.HopDong; // Import bổ sung cho tham số DetailFragment
// ⭐ IMPORT FRAGMENT SỬA (Giả định có newInstance(String id))
import com.example.quanlytourdl.SuaHdvFragment;
import com.example.quanlytourdl.SuaPhuongTienFragment;
// ⭐ IMPORT FRAGMENT XEM CHI TIẾT (Mới)
import com.example.quanlytourdl.DetailFragment;


public class QuanLyHdvPhuongTienFragment extends Fragment
        // TRIỂN KHAI INTERFACE ĐỂ LẮNG NGHE SỰ KIỆN TỪ ADAPTER
        implements QuanLyHdvPhuongTienAdapter.OnItemActionListener {


    private static final String TAG = "QLHDVPT_Fragment";

    private static final String TYPE_HDV = "Hướng dẫn viên";
    private static final String TYPE_PT = "Phương tiện";

    private static final String COLLECTION_HDV = "tour_guides";
    private static final String COLLECTION_PT = "phuongtien";

    private static final String STATUS_ALL = "Tất cả";
    private static final String STATUS_PT_HOAT_DONG_TOT = "Hoạt động tốt";
    private static final String STATUS_PT_DANG_BAO_DUONG = "Đang bảo dưỡng";
    private static final String STATUS_PT_CAN_SUA_CHUA_LON = "Cần sửa chữa lớn";

    private FirebaseFirestore db;
    private ListenerRegistration currentListener = null;

    // --- UI Components ---
    private TabLayout tabLayout;
    private FloatingActionButton btnAddItem;
    private ChipGroup chipGroupStatus;
    private RecyclerView recyclerView;
    private TextInputEditText etSearch; // Đã là TextInputEditText
    private TextView tvGoodReview;
    private TextView tvReviewCount;
    private TextView tvUpcomingExpiry;
    private TextView tvUpcomingExpiryUnit;


    // --- Data Management ---
    private QuanLyHdvPhuongTienAdapter adapter;
    private int chipTatCaId;
    private String currentType = TYPE_HDV;
    private String currentStatusFilter = STATUS_ALL;
    private List<Object> fullList = new ArrayList<>();
    private List<Object> filteredList = new ArrayList<>();

    public QuanLyHdvPhuongTienFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    // ⭐ THÊM: Sử dụng onStart() để đảm bảo listener được thiết lập lại
    @Override
    public void onStart() {
        super.onStart();
        // Tải lại danh sách nếu chưa có listener (ví dụ: quay lại từ màn hình Sửa/Thêm)
        if (currentListener == null) {
            loadList(currentType);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gán layout
        View view = inflater.inflate(R.layout.fragment_quan_ly_hdv_phuong_tien, container, false);

        // Ánh xạ UI (Phương thức mới)
        mapViews(view);

        // Thiết lập Adapter và RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo Adapter với 'this' là listener cho các hành động (Edit/Delete/ViewDetails)
        adapter = new QuanLyHdvPhuongTienAdapter(currentType.equals(TYPE_HDV), this);
        recyclerView.setAdapter(adapter);

        // Thiết lập trạng thái ban đầu
        if (chipTatCaId != 0) {
            chipGroupStatus.check(chipTatCaId);
        }

        // Lắng nghe Tab, Chip và Tìm kiếm (Phương thức mới)
        setupListeners(view);

        // Tải dữ liệu ban đầu
        // loadList(currentType); // Đã chuyển sang onStart()

        return view;
    }

    /**
     * Ánh xạ các UI components.
     */
    private void mapViews(View view) {
        // Control chính
        tabLayout = view.findViewById(R.id.tab_layout_hdv_pt);
        btnAddItem = view.findViewById(R.id.btn_add_item);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        recyclerView = view.findViewById(R.id.recycler_hdv_phuong_tien);

        // Thẻ Tổng quan
        tvGoodReview = view.findViewById(R.id.text_good_review);
        tvReviewCount = view.findViewById(R.id.text_review_count);
        tvUpcomingExpiry = view.findViewById(R.id.text_upcoming_expiry);
        tvUpcomingExpiryUnit = view.findViewById(R.id.text_upcoming_expiry_unit);

        // Tìm kiếm (Ánh xạ và ÉP KIỂU an toàn hơn)
        TextInputLayout inputLayout = view.findViewById(R.id.input_search);
        if (inputLayout != null && inputLayout.getEditText() instanceof TextInputEditText) {
            etSearch = (TextInputEditText) inputLayout.getEditText();
        } else if (inputLayout != null && inputLayout.getEditText() != null) {
            // Trường hợp không phải TextInputEditText mà là EditText thường
            etSearch = (TextInputEditText) inputLayout.getEditText();
        } else {
            Log.e(TAG, "Lỗi: Không thể tìm thấy TextInputEditText cho tìm kiếm.");
        }


        View chipTatCa = view.findViewById(R.id.chip_tat_ca);
        if (chipTatCa != null) {
            chipTatCaId = chipTatCa.getId();
        }

        // Khởi tạo dự phòng (Không cần thiết nếu bạn chắc chắn layout tồn tại)
    }

    /**
     * Thiết lập các Listener cho UI.
     */
    private void setupListeners(View view) {
        // Nút Quay lại
        view.findViewById(R.id.btn_back_quan_ly).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        // Tab Layout Listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String newType = tab.getText().toString();
                currentType = newType;

                boolean isGuideList = newType.equals(TYPE_HDV);
                // Cập nhật adapter với listener 'this'
                adapter = new QuanLyHdvPhuongTienAdapter(isGuideList, QuanLyHdvPhuongTienFragment.this);
                recyclerView.setAdapter(adapter);

                // Reset Chip và Search khi chuyển Tab
                if (chipTatCaId != 0) {
                    chipGroupStatus.check(chipTatCaId);
                    currentStatusFilter = STATUS_ALL;
                }
                if (etSearch != null) etSearch.setText("");

                // Tải lại dữ liệu cho loại mới (HDV hoặc PT)
                loadList(newType);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Chip Group Listener
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                String statusChipName = getStatusFromChipId(checkedId);
                currentStatusFilter = statusChipName;

                // Áp dụng lọc Trạng thái và Tìm kiếm
                filterDataList(etSearch != null ? etSearch.getText().toString().trim() : "");
            } else {
                if (chipTatCaId != 0) {
                    chipGroupStatus.check(chipTatCaId);
                }
            }
        });

        // Floating Action Button Listener
        btnAddItem.setOnClickListener(v -> handleAddItemClick());

        // TextInputEditText Listener (Real-time Search)
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String searchQuery = s.toString().trim();
                    filterDataList(searchQuery); // Áp dụng lọc tìm kiếm
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void handleAddItemClick() {
        if (tabLayout.getSelectedTabPosition() == -1) return;

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
            openFragment(targetFragment, "Thêm mới " + type);
        } else if (targetFragment == null) {
            Toast.makeText(getContext(), "Lỗi: Fragment thêm mới chưa được định nghĩa hoặc loại không xác định.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Tải danh sách từ Firestore và lưu vào fullList (không áp dụng lọc trạng thái).
     * @param type Loại dữ liệu (HDV/PT).
     */
    private void loadList(String type) {
        if (currentListener != null) {
            currentListener.remove();
        }

        String collectionName = type.equals(TYPE_HDV) ? COLLECTION_HDV : COLLECTION_PT;
        String orderByField = type.equals(TYPE_HDV) ? "fullName" : "bienSoXe";

        // Tải toàn bộ danh sách
        Query query = db.collection(collectionName)
                .orderBy(orderByField, Query.Direction.ASCENDING);

        Log.d(TAG, "Đang tải dữ liệu gốc (ALL) cho collection: " + collectionName);

        currentListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Lỗi khi lắng nghe dữ liệu:", error);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (value != null) {
                fullList.clear();
                int totalItems = 0;
                int upcomingExpiryCount = 0;
                int goodMetricCount = 0;

                for (QueryDocumentSnapshot doc : value) {
                    try {
                        if (type.equals(TYPE_HDV)) {
                            Guide guide = doc.toObject(Guide.class);
                            guide.setId(doc.getId());
                            fullList.add(guide);
                            if (guide.getRating() >= 4.0) {
                                goodMetricCount++;
                            }
                        } else if (type.equals(TYPE_PT)) {
                            Vehicle vehicle = doc.toObject(Vehicle.class);
                            vehicle.setId(doc.getId());
                            fullList.add(vehicle);
                            if (vehicle.getTinhTrangBaoDuong() != null && vehicle.getTinhTrangBaoDuong().equals(STATUS_PT_HOAT_DONG_TOT)) {
                                goodMetricCount++;
                            }
                        }
                        totalItems++;
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi chuyển đổi dữ liệu cho doc ID: " + doc.getId(), e);
                    }
                }

                // CẬP NHẬT CHỈ SỐ TỔNG QUAN
                updateSummaryCards(type, totalItems, upcomingExpiryCount, goodMetricCount);

                // ÁP DỤNG LỌC VÀ TÌM KIẾM
                String searchQuery = etSearch != null ? etSearch.getText().toString().trim() : "";
                filterDataList(searchQuery);

                Log.d(TAG, "Đã tải thành công " + totalItems + " mục gốc cho " + type + ".");
            }
        });
    }


    /**
     * Xử lý lọc trên client-side theo Trạng thái (Chip) và Chuỗi tìm kiếm (Search).
     */
    private void filterDataList(String query) {
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault());

        // 1. Lọc theo Trạng thái (Chip)
        List<Object> statusFilteredList = fullList.stream()
                .filter(item -> {
                    if (currentStatusFilter.equals(STATUS_ALL)) {
                        return true;
                    }

                    if (item instanceof Guide) {
                        String status = ((Guide) item).getTrangThai();
                        String expectedDbStatus = mapStatusChipToGuideStatus(currentStatusFilter);
                        return expectedDbStatus.equals(status);

                    } else if (item instanceof Vehicle) {
                        String expectedDbStatus = mapStatusChipToVehicleStatus(currentStatusFilter);
                        String status = ((Vehicle) item).getTinhTrangBaoDuong();
                        return expectedDbStatus.equals(status);
                    }
                    return false;
                })
                .collect(Collectors.toList());

        // 2. Lọc tiếp theo Chuỗi tìm kiếm (Search)
        filteredList.clear();

        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(statusFilteredList);
        } else {
            statusFilteredList.stream()
                    .filter(item -> {
                        if (item instanceof Guide) {
                            Guide guide = (Guide) item;
                            // Tìm kiếm theo Tên HDV (fullName) hoặc Mã HDV (guideCode)
                            return (guide.getFullName() != null && guide.getFullName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                                    (guide.getGuideCode() != null && guide.getGuideCode().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery));
                        } else if (item instanceof Vehicle) {
                            Vehicle vehicle = (Vehicle) item;
                            // Tìm kiếm theo Biển số xe (bienSoXe) hoặc Hãng xe (hangXe)
                            return (vehicle.getBienSoXe() != null && vehicle.getBienSoXe().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                                    (vehicle.getHangXe() != null && vehicle.getHangXe().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery));
                        }
                        return false;
                    })
                    .forEach(filteredList::add);
        }

        // 3. Cập nhật RecyclerView
        adapter.updateData(filteredList);
    }

    /**
     * Cập nhật các thẻ Tổng quan.
     */
    private void updateSummaryCards(String type, int totalItems, int upcomingExpiryCount, int goodMetricCount) {
        if (totalItems == 0) {
            tvGoodReview.setText("N/A");
            tvReviewCount.setText("0 mục");
            tvUpcomingExpiry.setText("0");
            tvUpcomingExpiryUnit.setText(type.equals(TYPE_HDV) ? "giấy tờ" : "đăng kiểm");
            return;
        }

        // --- Card 1: Đánh giá tốt / Tình trạng tốt ---
        int goodPercent = (int) ( (double) goodMetricCount / totalItems * 100);
        tvGoodReview.setText(String.format(Locale.getDefault(), "%d%%", goodPercent));

        if (type.equals(TYPE_HDV)) {
            tvReviewCount.setText(String.format(Locale.getDefault(), "%d HDV", totalItems));
            tvUpcomingExpiry.setText("0"); // Placeholder
            tvUpcomingExpiryUnit.setText("giấy tờ");

        } else if (type.equals(TYPE_PT)) {
            tvReviewCount.setText(String.format(Locale.getDefault(), "%d phương tiện", totalItems));
            tvUpcomingExpiry.setText(String.format(Locale.getDefault(), "%d", upcomingExpiryCount)); // Placeholder
            tvUpcomingExpiryUnit.setText("đăng kiểm");
        }
    }


    private String mapStatusChipToVehicleStatus(String statusChipName) {
        switch (statusChipName) {
            case "Sẵn sàng":
                return STATUS_PT_HOAT_DONG_TOT;
            case "Đang đi tour":
                // Giả định 'Đang đi tour' của Chip PT mapping với 'Đang bảo dưỡng' của DB (cần xem lại DB để mapping chuẩn)
                return STATUS_PT_DANG_BAO_DUONG;
            case "Tạm nghỉ":
                // Giả định 'Tạm nghỉ' của Chip PT mapping với 'Cần sửa chữa lớn' của DB (cần xem lại DB để mapping chuẩn)
                return STATUS_PT_CAN_SUA_CHUA_LON;
            case STATUS_ALL:
            default:
                return STATUS_ALL;
        }
    }

    // HÀM MỚI: Mapping trạng thái Chip cho HDV (cần xem lại DB để mapping chuẩn)
    private String mapStatusChipToGuideStatus(String statusChipName) {
        switch (statusChipName) {
            case "Sẵn sàng":
                return "Available"; // Giả định trạng thái DB là 'Available'
            case "Đang đi tour":
                return "On Tour";   // Giả định trạng thái DB là 'On Tour'
            case "Tạm nghỉ":
                return "On Leave";  // Giả định trạng thái DB là 'On Leave'
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

    // HÀM HỖ TRỢ CHUYỂN FRAGMENT
    private void openFragment(Fragment targetFragment, String logMessage) {
        int frameId = requireContext().getResources().getIdentifier("main_content_frame", "id", requireContext().getPackageName());
        if (frameId != 0) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(frameId, targetFragment)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
            Log.d(TAG, logMessage);
        } else {
            Log.e(TAG, "Không tìm thấy ID 'main_content_frame'. Không thể chuyển Fragment.");
        }
    }


    // --- TRIỂN KHAI INTERFACE OnItemActionListener (Edit/Delete/ViewDetails) ---

    @Override
    public void onEditItem(Object item) {
        Fragment targetFragment = null;

        if (item instanceof Guide) {
            Guide guide = (Guide) item;
            // ⭐ SỬ DỤNG PHƯƠNG THỨC newInstance() CÓ SẴN (đã chuẩn hóa ở SuaHdvFragment)
            targetFragment = SuaHdvFragment.newInstance(guide.getId());
            openFragment(targetFragment, "Mở màn hình Sửa HDV: " + guide.getFullName());
        } else if (item instanceof Vehicle) {
            Vehicle vehicle = (Vehicle) item;
            // ⭐ SỬ DỤNG PHƯƠNG THỨC newInstance() CÓ SẴN (Giả định đã chuẩn hóa ở SuaPhuongTienFragment)
            targetFragment = SuaPhuongTienFragment.newInstance(vehicle.getId());
            openFragment(targetFragment, "Mở màn hình Sửa Phương tiện: " + vehicle.getBienSoXe());
        }

        // Không cần kiểm tra targetFragment == null vì nó đã được xử lý trong logic switch/if-else.
        // Tuy nhiên, nếu muốn an toàn tuyệt đối:
        if (targetFragment == null) {
            Toast.makeText(getContext(), "Lỗi: Không thể mở màn hình chỉnh sửa.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteItem(Object item) {
        if (item instanceof Guide) {
            Guide guide = (Guide) item;
            showDeleteConfirmationDialog(guide.getId(), guide.getFullName(), COLLECTION_HDV);
        } else if (item instanceof Vehicle) {
            Vehicle vehicle = (Vehicle) item;
            showDeleteConfirmationDialog(vehicle.getId(), vehicle.getBienSoXe(), COLLECTION_PT);
        }
    }

    /**
     * ⭐ CẬP NHẬT: Xử lý sự kiện xem chi tiết bằng DetailFragment.
     * Sử dụng phương thức newInstance với 4 tham số (Guide, Vehicle, NhaCungCap, HopDong)
     * để đảm bảo tương thích với DetailFragment.
     */
    @Override
    public void onViewDetails(Object item) {
        Fragment targetFragment = null;
        String logMessage = "Mở màn hình Chi tiết: ";

        // Khai báo các đối tượng null để đảm bảo đủ 4 tham số cho newInstance(G, V, N, H)
        final NhaCungCap nullNcc = null;
        final HopDong nullHopDong = null;

        if (item instanceof Guide) {
            Guide guide = (Guide) item;
            // Gọi newInstance(Guide, Vehicle, NhaCungCap, HopDong)
            targetFragment = DetailFragment.newInstance(guide, null, nullNcc, nullHopDong);
            logMessage += "HDV: " + guide.getFullName();

        } else if (item instanceof Vehicle) {
            Vehicle vehicle = (Vehicle) item;
            // Gọi newInstance(Guide, Vehicle, NhaCungCap, HopDong)
            targetFragment = DetailFragment.newInstance(null, vehicle, nullNcc, nullHopDong);
            logMessage += "Phương tiện: " + vehicle.getBienSoXe();
        }

        if (targetFragment != null) {
            openFragment(targetFragment, logMessage);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không thể mở màn hình chi tiết.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị hộp thoại xác nhận xóa.
     */
    private void showDeleteConfirmationDialog(String itemId, String itemName, String collection) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa " + itemName + "?\n(Hành động này không thể hoàn tác)")
                .setPositiveButton("Xóa", (dialog, which) -> deleteItemFromFirestore(itemId, collection))
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Xóa mục khỏi Firestore.
     */
    private void deleteItemFromFirestore(String itemId, String collection) {
        db.collection(collection).document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã xóa thành công.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Document đã bị xóa thành công! Collection: " + collection + ", ID: " + itemId);
                    // Dữ liệu sẽ tự động cập nhật nhờ ListenerRegistration
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Lỗi khi xóa document ID: " + itemId, e);
                });
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