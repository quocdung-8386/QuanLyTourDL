package com.example.quanlytourdl;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
// IMPORT BATCH WRITE
import com.google.firebase.firestore.WriteBatch;

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

// --- [THÊM MỚI] Import DanhSachKhachHangFragment ---
import com.example.quanlytourdl.DanhSachKhachHangFragment;

import java.util.ArrayList;
import java.util.List;


public class KinhDoanhFragment extends Fragment implements NhaCungCapAdapter.OnItemActionListener {

    private static final String TAG = "KinhDoanhFragment";

    private RecyclerView recyclerView;
    private NhaCungCapAdapter adapter;
    private List<NhaCungCap> nhaCungCapList;

    private FirebaseFirestore db;
    private CollectionReference nhaCungCapRef;
    private CollectionReference hopDongRef;
    private ListenerRegistration listenerRegistration;

    // Hằng số cho ID Menu tùy chọn
    private static final int MENU_ID_SUPPLIER = 101;
    private static final int MENU_ID_TOUR_MANAGEMENT = 102;
    private static final int MENU_ID_TOUR_APPROVAL = 103;
    private static final int MENU_ID_CUSTOMER_LIST = 104;
    private static final int MENU_ID_GUIDE_VEHICLE_MANAGEMENT = 105;
    private static final int MENU_ID_PAYMENT_RECORD = 106;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // KHỞI TẠO FIRESTORE
        db = FirebaseFirestore.getInstance();
        nhaCungCapRef = db.collection("NhaCungCap");
        hopDongRef = db.collection("HopDong");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Giả sử layout ID là fragment_kinhdoanh
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

        // 2. Xử lý FAB để chuyển sang Fragment tạo Nhà cung cấp mới
        View fabAddProvider = view.findViewById(R.id.fab_add_provider);
        if (fabAddProvider != null) {
            fabAddProvider.setOnClickListener(v -> {
                openCreateSupplierFragment();
            });
        }

        // 3. Cập nhật nội dung Quick Actions
        setupQuickActions(view);

        // 4. Khởi tạo RecyclerView cho Danh sách Nhà cung cấp
        setupRecyclerView(view);

        // 5. Tải dữ liệu từ Firestore
        loadNhaCungCapData();

        return view;
    }

    /**
     * Hiển thị một PopupMenu tùy chọn và xử lý chuyển Fragment khi click.
     */
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
                        // --- GỌI HÀM MỞ DANH SÁCH KHÁCH HÀNG ---
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


    // --- HÀM HỖ TRỢ CHUYỂN FRAGMENT TỪ MENU DRAWER ---

    private void performFragmentTransaction(Fragment targetFragment, String logMessage) {
        if (getParentFragmentManager() != null) {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // Cố gắng lấy ID động, nếu không được thì dùng ID cứng R.id.main_content_frame
            int frameId = getResources().getIdentifier("main_content_frame", "id", requireContext().getPackageName());

            if (frameId != 0) {
                transaction.replace(frameId, targetFragment);
            } else {
                // Fallback nếu không tìm thấy ID động, sử dụng R.id nếu đã import
                // Giả sử trong file XML layout chính của Activity có FrameLayout id là main_content_frame
                // Nếu báo đỏ ở đây, hãy thay bằng R.id.fragment_container hoặc ID đúng của bạn
                transaction.replace(R.id.fragment_container, targetFragment);
            }

            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
            Log.d(TAG, logMessage);
        }
    }

    private void openQuanLyTourFragment() {
        performFragmentTransaction(new QuanLyTourFragment(), "Chuyển sang màn hình Quản Lý Tour.");
    }

    private void openChoPheDuyetTourFragment() {
        performFragmentTransaction(new ChoPheDuyetTourFragment(), "Chuyển sang màn hình Phê duyệt/Duyệt bán Tour.");
    }

    /**
     * Mở Fragment Danh Sách Khách Hàng.
     * Đã cập nhật để gọi Fragment thực tế thay vì Placeholder.
     */
    private void openDanhSachKhachHangFragment() {
        // --- [CẬP NHẬT] Khởi tạo Fragment thật ---
        performFragmentTransaction(new DanhSachKhachHangFragment(), "Chuyển sang màn hình Danh Sách Khách Hàng.");
    }

    private void openQuanLyHdvPhuongTienFragment() {
        performFragmentTransaction(new QuanLyHdvPhuongTienFragment(), "Chuyển sang màn hình Quản lý HDV & Phương tiện.");
    }
    private void openQuanLyDonHangFragment() {
        performFragmentTransaction(new QuanLyDonHangFragment(), "Chuyển sang màn hình Quản lý Đơn Hàng.");
    }

    // --- CLASS GIẢ ĐỊNH CHO MỤC ĐÍCH NAVIGATE (CHỈ DÙNG CHO CÁC MÀN HÌNH CHƯA TRIỂN KHAI) ---
    public static class PlaceholderFragment extends Fragment {
        private String title;

        public PlaceholderFragment(String title) {
            this.title = title;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            TextView textView = new TextView(getContext());
            textView.setText("Màn hình: " + title + " (Chưa triển khai)");
            textView.setTextSize(24);
            textView.setGravity(android.view.Gravity.CENTER);
            return textView;
        }
    }

    // ... (Các phần code Setup RecyclerView, Load Data, Delete logic giữ nguyên như cũ) ...

    // --- HÀM BỔ SUNG: CÀI ĐẶT RECYCLERVIEW ---
    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_providers);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            nhaCungCapList = new ArrayList<>();
            adapter = new NhaCungCapAdapter(requireContext(), nhaCungCapList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    // --- HÀM BỔ SUNG: TẢI DỮ LIỆU TỪ FIRESTORE (REAL-TIME) ---
    private void loadNhaCungCapData() {
        listenerRegistration = nhaCungCapRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Lỗi lắng nghe Firestore: ", e);
                return;
            }

            if (snapshots != null) {
                nhaCungCapList.clear();
                for (QueryDocumentSnapshot document : snapshots) {
                    try {
                        NhaCungCap ncc = document.toObject(NhaCungCap.class);
                        ncc.setMaNhaCungCap(document.getId());
                        nhaCungCapList.add(ncc);
                    } catch (Exception ex) {
                        Log.e(TAG, "Lỗi chuyển đổi dữ liệu document ID " + document.getId());
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    // --- TRIỂN KHAI PHƯƠNG THỨC CỦA INTERFACE NhaCungCapAdapter.OnItemActionListener ---
    @Override
    public void onEditClick(NhaCungCap ncc) {
        openEditSupplierFragment(ncc.getMaNhaCungCap());
    }

    @Override
    public void onViewClick(NhaCungCap ncc) {
        Toast.makeText(requireContext(), "Chi tiết: " + ncc.getTenNhaCungCap(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(NhaCungCap ncc) {
        showDeleteConfirmationDialog(ncc);
    }

    @Override
    public void onTerminateContract(NhaCungCap ncc) {
        Toast.makeText(requireContext(), "Chưa triển khai chấm dứt HĐ.", Toast.LENGTH_SHORT).show();
    }

    // --- HÀM HỖ TRỢ XÓA ---
    private void showDeleteConfirmationDialog(NhaCungCap ncc) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn muốn xóa Nhà Cung Cấp " + ncc.getTenNhaCungCap() + "?\nTất cả Hợp Đồng liên quan sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteNhaCungCap(ncc))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteNhaCungCap(NhaCungCap ncc) {
        final String supplierId = ncc.getMaNhaCungCap();
        final WriteBatch batch = db.batch();

        hopDongRef.whereEqualTo("supplierId", supplierId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }
                    batch.delete(nhaCungCapRef.document(supplierId));
                    batch.commit()
                            .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Đã xóa thành công.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    // --- HÀM CHUYỂN FRAGMENT PHỤ ---
    private void openQuanLyHopDongFragment() {
        performFragmentTransaction(new QuanLyHopDongFragment(), "Mở Quản lý Hợp đồng");
    }

    private void openEditSupplierFragment(String supplierId) {
        SuaNhaCungCapFragment editFragment = new SuaNhaCungCapFragment();
        Bundle bundle = new Bundle();
        bundle.putString("supplier_id", supplierId);
        editFragment.setArguments(bundle);
        performFragmentTransaction(editFragment, "Mở Sửa NCC: " + supplierId);
    }

    private void openCreateSupplierFragment() {
        performFragmentTransaction(new TaoNhaCungCapFragment(), "Mở Tạo NCC");
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

        if (actionPerformance != null) {
            if (icAssessmentId != 0) {
                ImageView icon = actionPerformance.findViewById(R.id.action_icon);
                if (icon != null) icon.setImageResource(icAssessmentId);
            }
            TextView title = actionPerformance.findViewById(R.id.action_title);
            if (title != null) title.setText("Đánh giá hiệu suất");
            actionPerformance.setOnClickListener(v -> Toast.makeText(requireContext(), "Tới Đánh giá hiệu suất", Toast.LENGTH_SHORT).show());
        }

        if (actionFilter != null) {
            if (icFilterId != 0) {
                ImageView icon = actionFilter.findViewById(R.id.action_icon);
                if (icon != null) icon.setImageResource(icFilterId);
            }
            TextView title = actionFilter.findViewById(R.id.action_title);
            if (title != null) title.setText("Bộ lọc");
            actionFilter.setOnClickListener(v -> Toast.makeText(requireContext(), "Mở Bộ lọc", Toast.LENGTH_SHORT).show());
        }
    }
}