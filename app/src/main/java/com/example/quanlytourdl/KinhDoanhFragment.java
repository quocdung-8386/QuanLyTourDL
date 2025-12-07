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

// Giả định class này đã tồn tại và có constructor mặc định
import com.example.quanlytourdl.ChoPheDuyetTourFragment;

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

    // Hằng số cho ID Menu tùy chọn mới
    private static final int MENU_ID_SUPPLIER = 101;
    private static final int MENU_ID_TOUR_MANAGEMENT = 102;
    private static final int MENU_ID_TOUR_APPROVAL = 103;
    private static final int MENU_ID_CUSTOMER_LIST = 104;
    private static final int MENU_ID_CUSTOMER_REPORT = 105;
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
            btnMenuDrawer.setOnClickListener(this::showDrawerMenu); // Gọi phương thức hiển thị menu
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

        // Thêm các mục menu tùy chọn sử dụng Hằng số ID
        popup.getMenu().add(1, MENU_ID_SUPPLIER, 1, "Nhà Cung Cấp");
        popup.getMenu().add(1, MENU_ID_TOUR_MANAGEMENT, 2, "Quản Lý Tour");
        popup.getMenu().add(1, MENU_ID_TOUR_APPROVAL, 3, "Phê duyệt/Duyệt bán Tour");
        popup.getMenu().add(1, MENU_ID_CUSTOMER_LIST, 4, "Danh Sách Khách Hàng");
        popup.getMenu().add(1, MENU_ID_CUSTOMER_REPORT, 5, "Báo cáo Danh sách KH");
        popup.getMenu().add(1, MENU_ID_PAYMENT_RECORD, 6, "Ghi Nhận Thanh Toán");

        // Thiết lập sự kiện click cho các mục menu
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case MENU_ID_SUPPLIER:
                        // Đã ở màn hình này, chỉ cần đóng menu hoặc refresh
                        Toast.makeText(getContext(), "Đang ở màn hình Quản lý Nhà Cung Cấp", Toast.LENGTH_SHORT).show();
                        return true;
                    case MENU_ID_TOUR_MANAGEMENT:
                        openQuanLyTourFragment();
                        return true;
                    case MENU_ID_TOUR_APPROVAL:
                        // Đã cập nhật để gọi ChoPheDuyetTourFragment thực tế
                        openChoPheDuyetTourFragment();
                        return true;
                    case MENU_ID_CUSTOMER_LIST:
                        openDanhSachKhachHangFragment();
                        return true;
                    case MENU_ID_CUSTOMER_REPORT:
                        openBaoCaoKhachHangFragment();
                        return true;
                    case MENU_ID_PAYMENT_RECORD:
                        openGhiNhanThanhToanFragment();
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
            int frameId = getResources().getIdentifier("main_content_frame", "id", requireContext().getPackageName());

            if (frameId != 0) {
                transaction.replace(frameId, targetFragment);
                transaction.addToBackStack(null);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
                Log.d(TAG, logMessage);
            } else {
                // Thử với ID R.id.main_content_frame nếu không tìm thấy ID động
                try {
                    transaction.replace(R.id.main_content_frame, targetFragment);
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                    Log.d(TAG, logMessage + " (Sử dụng R.id.main_content_frame)");
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi chuyển Fragment, không tìm thấy ID main_content_frame trong cả hai cách.", e);
                }
            }
        }
    }

    /**
     * Mở Fragment Quản Lý Tour.
     */
    private void openQuanLyTourFragment() {
        performFragmentTransaction(new QuanLyTourFragment(), "Chuyển sang màn hình Quản Lý Tour.");
    }

    /**
     * Mở Fragment Cho Phê Duyệt Tour (ĐÃ TRIỂN KHAI).
     * Giả định class ChoPheDuyetTourFragment tồn tại và có constructor mặc định.
     */
    private void openChoPheDuyetTourFragment() {
        performFragmentTransaction(new ChoPheDuyetTourFragment(), "Chuyển sang màn hình Phê duyệt/Duyệt bán Tour.");
    }

    /**
     * Mở Fragment Danh Sách Khách Hàng (Placeholder).
     */
    private void openDanhSachKhachHangFragment() {
        performFragmentTransaction(new PlaceholderFragment("Danh Sách Khách Hàng"), "Chuyển sang màn hình Danh Sách Khách Hàng.");
    }

    /**
     * Mở Fragment Báo cáo Danh sách KH (Placeholder).
     */
    private void openBaoCaoKhachHangFragment() {
        performFragmentTransaction(new PlaceholderFragment("Báo cáo Danh sách KH"), "Chuyển sang màn hình Báo cáo Danh sách KH.");
    }

    /**
     * Mở Fragment Ghi Nhận Thanh Toán (Placeholder).
     */
    private void openGhiNhanThanhToanFragment() {
        performFragmentTransaction(new PlaceholderFragment("Ghi Nhận Thanh Toán"), "Chuyển sang màn hình Ghi Nhận Thanh Toán.");
    }


    // --- CLASS GIẢ ĐỊNH CHO MỤC ĐÍCH NAVIGATE (CHỈ DÙNG CHO CÁC MÀN HÌNH CHƯA TRIỂN KHAI) ---
    /**
     * Class Fragment giả định để hiển thị tên màn hình được chuyển đến.
     */
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
    // -----------------------------------------------------------------


    // --- HÀM BỔ SUNG: CÀI ĐẶT RECYCLERVIEW ---
    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_providers);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            nhaCungCapList = new ArrayList<>();
            // TRUYỀN 'this' (FRAGMENT) LÀM LISTENER CHO ADAPTER
            adapter = new NhaCungCapAdapter(requireContext(), nhaCungCapList, this);
            recyclerView.setAdapter(adapter);
        } else {
            Log.e(TAG, "Không tìm thấy RecyclerView với ID: recycler_providers");
        }
    }

    // --- HÀM BỔ SUNG: TẢI DỮ LIỆU TỪ FIRESTORE (REAL-TIME) ---
    private void loadNhaCungCapData() {
        listenerRegistration = nhaCungCapRef.addSnapshotListener(new com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable com.google.firebase.firestore.QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Lỗi lắng nghe Firestore: ", e);
                    Toast.makeText(requireContext(), "Lỗi tải dữ liệu real-time: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (snapshots != null) {
                    nhaCungCapList.clear();
                    for (QueryDocumentSnapshot document : snapshots) {
                        try {
                            // Chuyển đổi Document thành đối tượng NhaCungCap
                            NhaCungCap ncc = document.toObject(NhaCungCap.class);
                            ncc.setMaNhaCungCap(document.getId());
                            nhaCungCapList.add(ncc);
                        } catch (Exception ex) {
                            Log.e(TAG, "Lỗi chuyển đổi dữ liệu document ID " + document.getId() + ": " + ex.getMessage());
                        }
                    }
                    adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                    Log.d(TAG, "Đã tải thành công " + nhaCungCapList.size() + " Nhà Cung Cấp từ Firestore.");
                }
            }
        });
    }

    // Xóa listener khi Fragment bị hủy để tránh rò rỉ bộ nhớ
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
        Toast.makeText(requireContext(), "Mở chi tiết nhà cung cấp: " + ncc.getTenNhaCungCap(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(NhaCungCap ncc) {
        showDeleteConfirmationDialog(ncc);
    }

    @Override
    public void onTerminateContract(NhaCungCap ncc) {
        // Không sử dụng trong luồng này
        Toast.makeText(requireContext(), "Chức năng chấm dứt hợp đồng chưa được triển khai.", Toast.LENGTH_SHORT).show();
    }

    // --- HÀM HỖ TRỢ XÓA (CASCADING DELETE/XÓA CỨNG) ---

    private void showDeleteConfirmationDialog(NhaCungCap ncc) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa Nhà Cung Cấp " + ncc.getTenNhaCungCap() + "?\nCẢ các Hợp Đồng liên quan CŨNG sẽ bị XÓA VĨNH VIỄN khỏi cơ sở dữ liệu.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteNhaCungCap(ncc);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteNhaCungCap(NhaCungCap ncc) {
        final String supplierId = ncc.getMaNhaCungCap();
        final WriteBatch batch = db.batch();

        hopDongRef.whereEqualTo("supplierId", supplierId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }

                    batch.delete(nhaCungCapRef.document(supplierId));

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Đã xóa NCC và XÓA HĐ liên quan: " + supplierId);
                                Toast.makeText(requireContext(), "Đã xóa vĩnh viễn Nhà Cung Cấp và tất cả Hợp Đồng liên quan: " + ncc.getTenNhaCungCap(), Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Lỗi khi Commit Batch Xóa NCC " + supplierId, e);
                                Toast.makeText(requireContext(), "Lỗi xóa NCC (Batch): " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Lỗi khi truy vấn Hợp Đồng để xóa NCC " + supplierId, e);
                    Toast.makeText(requireContext(), "Lỗi truy vấn Hợp Đồng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // --- HÀM HỖ TRỢ CHUYỂN FRAGMENT (NCC và Hợp Đồng) ---

    private void openQuanLyHopDongFragment() {
        if (getParentFragmentManager() != null) {
            QuanLyHopDongFragment contractFragment = new QuanLyHopDongFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            int frameId = getResources().getIdentifier("main_content_frame", "id", requireContext().getPackageName());
            if (frameId != 0) {
                transaction.replace(frameId, contractFragment);
                transaction.addToBackStack(null);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
                Log.d(TAG, "Chuyển sang màn hình Quản lý Hợp đồng NCC");
            } else {
                try {
                    transaction.replace(R.id.main_content_frame, contractFragment);
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi chuyển Fragment, không tìm thấy ID main_content_frame.", e);
                }
            }
        }
    }

    private void openEditSupplierFragment(String supplierId) {
        if (getParentFragmentManager() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("supplier_id", supplierId);
            SuaNhaCungCapFragment editFragment = new SuaNhaCungCapFragment();
            editFragment.setArguments(bundle);
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            int frameId = getResources().getIdentifier("main_content_frame", "id", requireContext().getPackageName());
            if (frameId != 0) {
                transaction.replace(frameId, editFragment);
                transaction.addToBackStack(null);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
                Log.d(TAG, "Chuyển sang màn hình Sửa Nhà Cung Cấp: " + supplierId);
            } else {
                try {
                    transaction.replace(R.id.main_content_frame, editFragment);
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi chuyển Fragment, không tìm thấy ID main_content_frame.", e);
                }
            }
        }
    }

    private void openCreateSupplierFragment() {
        if (getParentFragmentManager() != null) {
            TaoNhaCungCapFragment createFragment = new TaoNhaCungCapFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            int frameId = getResources().getIdentifier("main_content_frame", "id", requireContext().getPackageName());
            if (frameId != 0) {
                transaction.add(frameId, createFragment);
                transaction.addToBackStack(null);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
                Log.d(TAG, "Chuyển sang màn hình Thêm Nhà Cung Cấp");
            } else {
                try {
                    transaction.add(R.id.main_content_frame, createFragment);
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi chuyển Fragment, không tìm thấy ID main_content_frame.", e);
                }
            }
        }
    }

    /**
     * Cập nhật nội dung cho các Quick Actions (Hành động nhanh) và thiết lập listener.
     */
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