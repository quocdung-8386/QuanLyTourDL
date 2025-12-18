package com.example.quanlytourdl;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Imports Firebase Firestore
import com.example.quanlytourdl.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.Query;

import com.example.quanlytourdl.adapter.NhaCungCapAdapter;
import com.example.quanlytourdl.model.NhaCungCap;

// Imports cho các Fragment chuyển hướng
import com.example.quanlytourdl.TaoNhaCungCapFragment;
import com.example.quanlytourdl.SuaNhaCungCapFragment;
import com.example.quanlytourdl.QuanLyTourFragment;
import com.example.quanlytourdl.QuanLyHopDongFragment;
import com.example.quanlytourdl.ChoPheDuyetTourFragment;
import com.example.quanlytourdl.QuanLyHdvPhuongTienFragment;
import com.example.quanlytourdl.DanhSachKhachHangFragment;
import com.example.quanlytourdl.QuanLyDonHangFragment;
import com.example.quanlytourdl.DetailFragment;
import com.example.quanlytourdl.DanhGiaNhaCungCapFragment; // ⭐ ĐÃ THÊM IMPORT
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class KinhDoanhFragment extends Fragment implements NhaCungCapAdapter.OnItemActionListener {

    private static final String TAG = "KinhDoanhFragment";

    private List<NhaCungCap> fullNhaCungCapList = new ArrayList<>();
    private EditText etSearchProvider;
    private TextView tvProviderCount;

    private RecyclerView recyclerView;
    private NhaCungCapAdapter adapter;
    private List<NhaCungCap> nhaCungCapList; // Danh sách hiển thị (đã lọc)

    private FirebaseFirestore db;
    private CollectionReference nhaCungCapRef;
    private CollectionReference hopDongRef;
    private ListenerRegistration listenerRegistration;

    // ⭐ THÊM BIẾN TRẠNG THÁI LỌC LOẠI DỊCH VỤ
    private String currentLoaiDichVuFilter = "Tất cả"; // Giá trị mặc định

    // Hằng số cho ID Menu tùy chọn (Giữ nguyên)
    private static final int MENU_ID_SUPPLIER = 101;
    private static final int MENU_ID_TOUR_MANAGEMENT = 102;
    private static final int MENU_ID_TOUR_APPROVAL = 103;
    private static final int MENU_ID_CUSTOMER_LIST = 104;
    private static final int MENU_ID_GUIDE_VEHICLE_MANAGEMENT = 105;
    private static final int MENU_ID_PAYMENT_RECORD = 106;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        nhaCungCapRef = db.collection("NhaCungCap");
        hopDongRef = db.collection("HopDong");
        nhaCungCapList = new ArrayList<>(); // Khởi tạo list hiển thị
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = getResources().getIdentifier("fragment_kinhdoanh", "layout", requireContext().getPackageName());
        if (layoutId == 0) {
            Log.e(TAG, "Không tìm thấy layout ID 'fragment_kinhdoanh'.");
            return null;
        }

        View view = inflater.inflate(layoutId, container, false);

        // 1. Xử lý Menu Dấu 3 Gạch
        ImageButton btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer);
        if (btnMenuDrawer != null) {
            btnMenuDrawer.setOnClickListener(this::showDrawerMenu);
        }

        // 2. Xử lý FAB
        View fabAddProvider = view.findViewById(R.id.fab_add_provider);
        if (fabAddProvider != null) {
            fabAddProvider.setOnClickListener(v -> {
                openCreateSupplierFragment();
            });
        }

        // 3. Cập nhật nội dung Quick Actions
        setupQuickActions(view);

        // 4. Ánh xạ các thành phần cho Tìm kiếm/Lọc
        etSearchProvider = view.findViewById(R.id.edit_search_provider);
        tvProviderCount = view.findViewById(R.id.text_provider_count);

        // 5. Khởi tạo RecyclerView
        setupRecyclerView(view);

        // 6. Tải dữ liệu từ Firestore và Thiết lập Tìm kiếm
        setupSearchFunctionality();

        return view;
    }

    /**
     * Cập nhật số lượng kết quả được hiển thị (từ danh sách đã lọc).
     */
    private void updateProviderCountText() {
        if (tvProviderCount != null) {
            tvProviderCount.setText(String.format("%d kết quả", nhaCungCapList.size()));
        }
    }

    // --- LOGIC TÌM KIẾM VÀ LỌC MỚI (CLIENT-SIDE) ---

    private void setupSearchFunctionality() {
        // Tải toàn bộ dữ liệu lần đầu tiên (một listener duy nhất)
        loadNhaCungCapData();

        if (etSearchProvider != null) {
            etSearchProvider.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Gọi hàm lọc Client-side mỗi khi chuỗi tìm kiếm thay đổi
                    String currentSearchQuery = s.toString().trim();
                    filterNhaCungCap(currentSearchQuery); // ⭐ CHỈ GỌI MỘT HÀM LỌC CHUNG
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    /**
     * Tải TOÀN BỘ dữ liệu Nhà Cung Cấp từ Firestore (Real-time).
     */
    private void loadNhaCungCapData() {
        // Hủy đăng ký Listener cũ nếu có
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        // Luôn tải tất cả, sắp xếp theo tên
        Query query = nhaCungCapRef.orderBy("tenNhaCungCap", Query.Direction.ASCENDING);

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Lỗi lắng nghe Firestore: ", e);
                return;
            }

            if (snapshots != null) {
                fullNhaCungCapList.clear(); // Xóa danh sách gốc
                for (QueryDocumentSnapshot document : snapshots) {
                    try {
                        NhaCungCap ncc = document.toObject(NhaCungCap.class);
                        ncc.setMaNhaCungCap(document.getId());
                        fullNhaCungCapList.add(ncc); // Thêm vào danh sách gốc
                    } catch (Exception ex) {
                        Log.e(TAG, "Lỗi chuyển đổi dữ liệu document ID " + document.getId(), ex);
                    }
                }

                // Sau khi tải xong, gọi hàm lọc Client-side
                String currentSearchQuery = etSearchProvider != null ? etSearchProvider.getText().toString().trim() : "";
                filterNhaCungCap(currentSearchQuery);

                Log.d(TAG, "Đã tải toàn bộ " + fullNhaCungCapList.size() + " NCC.");
            }
        });
    }

    /**
     * ⭐ CẬP NHẬT: Lọc danh sách Nhà Cung Cấp theo cả Tên/Mã và Loại Dịch Vụ.
     */
    private void filterNhaCungCap(String searchQuery) {
        final String lowerCaseQuery = searchQuery.toLowerCase(Locale.getDefault());
        final String currentFilter = currentLoaiDichVuFilter;
        List<NhaCungCap> filteredResults;

        if (lowerCaseQuery.isEmpty() && currentFilter.equals("Tất cả")) {
            // Không có lọc nào được áp dụng
            filteredResults = fullNhaCungCapList;
        } else {
            filteredResults = fullNhaCungCapList.stream()
                    .filter(ncc -> {
                        boolean matchesLoaiDichVu;

                        // 1. Lọc theo Loại Dịch Vụ
                        if (currentFilter.equals("Tất cả")) {
                            matchesLoaiDichVu = true;
                        } else {
                            // ⭐ GIẢ ĐỊNH: Lớp NhaCungCap có phương thức getLoaiDichVu()
                            // Kiểm tra xem loaiDichVu có chứa từ khóa lọc (Case-insensitive)
                            matchesLoaiDichVu = ncc.getLoaiDichVu() != null &&
                                    ncc.getLoaiDichVu().toLowerCase(Locale.getDefault()).contains(currentFilter.toLowerCase(Locale.getDefault()));
                        }

                        // 2. Lọc theo Chuỗi Tìm kiếm (Tên/Mã)
                        boolean matchesSearchQuery;
                        if (lowerCaseQuery.isEmpty()) {
                            matchesSearchQuery = true;
                        } else {
                            // Lọc theo TenNhaCungCap hoặc MaNhaCungCap
                            matchesSearchQuery = (ncc.getTenNhaCungCap() != null &&
                                    ncc.getTenNhaCungCap().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                                    (ncc.getMaNhaCungCap() != null &&
                                            ncc.getMaNhaCungCap().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery));
                        }

                        // Kết hợp cả hai điều kiện lọc
                        return matchesLoaiDichVu && matchesSearchQuery;
                    })
                    .collect(Collectors.toList());
        }

        // Cập nhật danh sách hiển thị (nhaCungCapList)
        nhaCungCapList.clear();
        nhaCungCapList.addAll(filteredResults);
        adapter.notifyDataSetChanged();

        // Cập nhật số lượng
        updateProviderCountText();
    }


    // --- HÀM HỖ TRỢ CHUYỂN FRAGMENT ---

    private void performFragmentTransaction(Fragment targetFragment, String logMessage) {
        if (isAdded() && getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    // Luôn sử dụng ID chính xác đã khai báo trong activity_main.xml
                    .replace(R.id.main_content_frame, targetFragment)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
            Log.d(TAG, logMessage);
        }
    }

    private void openQuanLyTourFragment() {
        performFragmentTransaction(new QuanLyTourFragment(), "Chuyển sang màn hình Quản Lý Tour.");
    }

    private void openChoPheDuyetTourFragment() {
        performFragmentTransaction(new ChoPheDuyetTourFragment(), "Chuyển sang màn hình Phê duyệt/Duyệt bán Tour.");
    }

    private void openDanhSachKhachHangFragment() {
        performFragmentTransaction(new DanhSachKhachHangFragment(), "Chuyển sang màn hình Danh Sách Khách Hàng.");
    }

    private void openQuanLyHdvPhuongTienFragment() {
        performFragmentTransaction(new QuanLyHdvPhuongTienFragment(), "Chuyển sang màn hình Quản lý HDV & Phương tiện.");
    }
    private void openQuanLyDonHangFragment() {
        performFragmentTransaction(new QuanLyDonHangFragment(), "Chuyển sang màn hình Quản lý Đơn Hàng.");
    }

    private void openQuanLyHopDongFragment() {
        performFragmentTransaction(new QuanLyHopDongFragment(), "Mở Quản lý Hợp đồng");
    }

    /**
     * Cập nhật: Sử dụng newInstance() của SuaNhaCungCapFragment
     */
    private void openEditSupplierFragment(String supplierId) {
        // Giả định SuaNhaCungCapFragment có newInstance(String id)
        Fragment editFragment = SuaNhaCungCapFragment.newInstance(supplierId);
        performFragmentTransaction(editFragment, "Mở Sửa NCC: " + supplierId);
    }

    private void openCreateSupplierFragment() {
        performFragmentTransaction(new TaoNhaCungCapFragment(), "Mở Tạo NCC");
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_providers);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            // Adapter sử dụng nhaCungCapList (danh sách đã lọc)
            adapter = new NhaCungCapAdapter(requireContext(), nhaCungCapList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    // --- TRIỂN KHAI INTERFACE NhaCungCapAdapter.OnItemActionListener ---

    @Override
    public void onEditClick(NhaCungCap ncc) {
        openEditSupplierFragment(ncc.getMaNhaCungCap());
    }

    /**
     * ⭐ CẬP NHẬT: Tri triển khai onViewClick để mở DetailFragment, sử dụng phương thức
     * newInstance(NhaCungCap) giả định đã được thêm vào DetailFragment.
     */
    @Override
    public void onViewClick(NhaCungCap ncc) {
        // Thay thế DetailFragment.newInstance(null, null, ncc);
        Fragment detailFragment = DetailFragment.newInstance(ncc);
        performFragmentTransaction(detailFragment, "Mở Chi tiết NCC: " + ncc.getTenNhaCungCap());
    }

    @Override
    public void onDeleteClick(NhaCungCap ncc) {
        showDeleteConfirmationDialog(ncc);
    }

    @Override
    public void onTerminateContract(NhaCungCap ncc) {
        Toast.makeText(requireContext(), "Chưa triển khai chấm dứt HĐ.", Toast.LENGTH_SHORT).show();
    }

    // --- LOGIC XÓA VÀ MENU (Đã cải thiện) ---

    private void showDrawerMenu(View anchorView) {
        if (getContext() == null) return;

        PopupMenu popup = new PopupMenu(getContext(), anchorView);

        // Thêm các mục menu tùy chọn
        popup.getMenu().add(1, MENU_ID_SUPPLIER, 1, "Nhà Cung Cấp");
        popup.getMenu().add(1, MENU_ID_TOUR_MANAGEMENT, 2, "Quản Lý Tour");
        popup.getMenu().add(1, MENU_ID_TOUR_APPROVAL, 3, "Phê duyệt/Duyệt bán Tour");
        popup.getMenu().add(1, MENU_ID_CUSTOMER_LIST, 4, "Danh Sách Khách Hàng");
        popup.getMenu().add(1, MENU_ID_GUIDE_VEHICLE_MANAGEMENT, 5, "Quản Lý HDV & Phương tiện");
        popup.getMenu().add(1, MENU_ID_PAYMENT_RECORD, 6, "Quản Lý Đơn Hàng");

        // Thiết lập sự kiện click cho các mục menu
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case MENU_ID_SUPPLIER:
                        Toast.makeText(getContext(), "Đang ở màn hình Quản lý Nhà Cung Cấp", Toast.LENGTH_SHORT).show();
                        return true;
                    case MENU_ID_TOUR_MANAGEMENT:
                        openQuanLyTourFragment();
                        return true;
                    case MENU_ID_TOUR_APPROVAL:
                        openChoPheDuyetTourFragment();
                        return true;
                    case MENU_ID_CUSTOMER_LIST:
                        openDanhSachKhachHangFragment();
                        return true;
                    case MENU_ID_GUIDE_VEHICLE_MANAGEMENT:
                        openQuanLyHdvPhuongTienFragment();
                        return true;
                    case MENU_ID_PAYMENT_RECORD:
                        openQuanLyDonHangFragment();
                        return true;
                    default:
                        return false;
                }
            }
        });

        popup.show();
    }

    private void showDeleteConfirmationDialog(NhaCungCap ncc) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn muốn xóa Nhà Cung Cấp " + ncc.getTenNhaCungCap() + "?\nTất cả Hợp Đồng liên quan sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteNhaCungCap(ncc))
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Xóa Nhà Cung Cấp và các Hợp Đồng liên quan bằng Batch, có xử lý lỗi.
     */
    private void deleteNhaCungCap(NhaCungCap ncc) {
        final String supplierId = ncc.getMaNhaCungCap();
        if (supplierId == null) {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy ID Nhà Cung Cấp.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Lấy tất cả Hợp đồng liên quan
        hopDongRef.whereEqualTo("supplierId", supplierId).get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException(); // Chuyển lỗi nếu query thất bại
                    }

                    WriteBatch batch = db.batch();
                    // 2. Thêm lệnh xóa các Hợp đồng vào Batch
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        batch.delete(document.getReference());
                        Log.d(TAG, "Thêm lệnh xóa Hợp đồng ID: " + document.getId());
                    }
                    // 3. Thêm lệnh xóa Nhà Cung Cấp vào Batch
                    batch.delete(nhaCungCapRef.document(supplierId));
                    Log.d(TAG, "Thêm lệnh xóa NCC ID: " + supplierId);

                    // 4. Commit Batch
                    return batch.commit();
                })
                .addOnSuccessListener(aVoid -> {
                    // Thành công cả Query và Commit Batch
                    Toast.makeText(requireContext(), "Đã xóa Nhà Cung Cấp và Hợp Đồng liên quan thành công.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Thất bại Query hoặc Commit Batch
                    Log.e(TAG, "Lỗi xóa NCC và Hợp đồng: ", e);
                    Toast.makeText(requireContext(), "Lỗi xóa Nhà Cung Cấp: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setupQuickActions(View view) {
        View actionContract = view.findViewById(R.id.action_contract);
        View actionPerformance = view.findViewById(R.id.action_performance);
        View actionFilter = view.findViewById(R.id.action_filter);

        int icDocumentId = getResources().getIdentifier("ic_document", "drawable", requireContext().getPackageName());
        int icAssessmentId = getResources().getIdentifier("ic_assessment", "drawable", requireContext().getPackageName());
        int icFilterId = getResources().getIdentifier("ic_filter", "drawable", requireContext().getPackageName());

        if (actionContract != null) {
            if (icDocumentId != 0) {
                ImageView icon = actionContract.findViewById(R.id.action_icon);
                if (icon != null) icon.setImageResource(icDocumentId);
            }
            TextView title = actionContract.findViewById(R.id.action_title);
            if (title != null) title.setText("Quản lý hợp đồng");
            actionContract.setOnClickListener(v -> openQuanLyHopDongFragment());
        }

        // ⭐ ĐÃ CẬP NHẬT: Xử lý click Đánh giá hiệu suất
        if (actionPerformance != null) {
            if (icAssessmentId != 0) {
                ImageView icon = actionPerformance.findViewById(R.id.action_icon);
                if (icon != null) icon.setImageResource(icAssessmentId);
            }
            TextView title = actionPerformance.findViewById(R.id.action_title);
            if (title != null) title.setText("Đánh giá hiệu suất");
            actionPerformance.setOnClickListener(v -> showSupplierSelectionForEvaluation());
        }

        if (actionFilter != null) {
            if (icFilterId != 0) {
                ImageView icon = actionFilter.findViewById(R.id.action_icon);
                if (icon != null) icon.setImageResource(icFilterId);
            }
            TextView title = actionFilter.findViewById(R.id.action_title);
            if (title != null) title.setText("Bộ lọc");
            // ⭐ CẬP NHẬT: Mở dialog chọn lọc Loại Dịch Vụ
            actionFilter.setOnClickListener(v -> showServiceTypeFilterDialog());
        }
    }

    /**
     * ⭐ HÀM MỚI: Hiển thị Dialog để chọn Nhà cung cấp cần đánh giá
     */
    private void showSupplierSelectionForEvaluation() {
        if (fullNhaCungCapList == null || fullNhaCungCapList.isEmpty()) {
            Toast.makeText(requireContext(), "Danh sách nhà cung cấp trống.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] supplierNames = new String[fullNhaCungCapList.size()];
        for (int i = 0; i < fullNhaCungCapList.size(); i++) {
            supplierNames[i] = fullNhaCungCapList.get(i).getTenNhaCungCap();
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Chọn nhà cung cấp để đánh giá")
                .setItems(supplierNames, (dialog, which) -> {
                    String selectedId = fullNhaCungCapList.get(which).getMaNhaCungCap();
                    openEvaluationFragment(selectedId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * ⭐ HÀM MỚI: Chuyển sang Fragment Đánh giá
     */
    private void openEvaluationFragment(String nccId) {
        Fragment evalFragment = DanhGiaNhaCungCapFragment.newInstance(nccId);
        performFragmentTransaction(evalFragment, "Mở màn hình đánh giá cho NCC: " + nccId);
    }

    // ⭐ HÀM MỚI: HIỂN THỊ DIALOG LỌC THEO LOẠI DỊCH VỤ
    private void showServiceTypeFilterDialog() {
        if (getContext() == null) return;

        // Danh sách các tùy chọn lọc
        final String[] serviceTypes = new String[]{"Tất cả", "Khách sạn", "Vận tải", "Ăn uống", "Vé tham quan", "Khác"};
        int checkedItem = -1; // Vị trí của loại dịch vụ đang được chọn

        // Tìm vị trí của loại dịch vụ đang được chọn
        for (int i = 0; i < serviceTypes.length; i++) {
            if (serviceTypes[i].equals(currentLoaiDichVuFilter)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Chọn Loại Dịch Vụ")
                .setSingleChoiceItems(serviceTypes, checkedItem, (dialog, which) -> {
                    // Cập nhật trạng thái lọc
                    String selectedFilter = serviceTypes[which];
                    if (!currentLoaiDichVuFilter.equals(selectedFilter)) {
                        currentLoaiDichVuFilter = selectedFilter;

                        // Áp dụng lại bộ lọc ngay lập tức
                        String currentSearchQuery = etSearchProvider != null ? etSearchProvider.getText().toString().trim() : "";
                        filterNhaCungCap(currentSearchQuery);

                        Toast.makeText(requireContext(), "Đã chọn lọc: " + currentLoaiDichVuFilter, Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}