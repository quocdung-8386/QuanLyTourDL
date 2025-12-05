package com.example.quanlytourdl;

import android.app.AlertDialog;
import android.os.Bundle; // Import cần thiết cho Bundle
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Imports Firebase Firestore
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentReference;

import com.example.quanlytourdl.adapter.NhaCungCapAdapter;
import com.example.quanlytourdl.model.NhaCungCap;

// THÊM: Imports cho các Fragment chuyển hướng
// Giả sử các Fragment này nằm trong package com.example.quanlytourdl
// Bạn cần đảm bảo các lớp này tồn tại (TaoNhaCungCapFragment và SuaNhaCungCapFragment)
import com.example.quanlytourdl.TaoNhaCungCapFragment;
import com.example.quanlytourdl.SuaNhaCungCapFragment;

import java.util.ArrayList;
import java.util.List;

public class KinhDoanhFragment extends Fragment implements NhaCungCapAdapter.OnItemActionListener {

    private static final String TAG = "KinhDoanhFragment";

    private RecyclerView recyclerView;
    private NhaCungCapAdapter adapter;
    private List<NhaCungCap> nhaCungCapList;

    // Sử dụng Firestore
    private FirebaseFirestore db;
    private CollectionReference nhaCungCapRef;
    private ListenerRegistration listenerRegistration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // KHỞI TẠO FIRESTORE
        db = FirebaseFirestore.getInstance();
        nhaCungCapRef = db.collection("NhaCungCap");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Giả sử layout ID là fragment_kinhdoanh
        int layoutId = getResources().getIdentifier("fragment_kinhdoanh", "layout", requireContext().getPackageName());
        if (layoutId == 0) {
            // Trường hợp không tìm thấy ID, cần kiểm tra lại file XML
            Log.e(TAG, "Không tìm thấy layout ID 'fragment_kinhdoanh'.");
            return null;
        }

        View view = inflater.inflate(layoutId, container, false);

        // 1. Xử lý Menu Dấu 3 Gạch
        ImageButton btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer);
        if (btnMenuDrawer != null) {
            btnMenuDrawer.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Mở Menu chính (Navigation Drawer)", Toast.LENGTH_SHORT).show();
            });
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

    // --- HÀM BỔ SUNG: CÀI ĐẶT RECYCLERVIEW (CẬP NHẬT TRUYỀN LISTENER) ---
    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_providers);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            nhaCungCapList = new ArrayList<>();
            // TRUYỀN 'this' (FRAGMENT) LÀM LISTENER CHO ADAPTER
            adapter = new NhaCungCapAdapter(getContext(), nhaCungCapList, this);
            recyclerView.setAdapter(adapter);
        } else {
            Log.e(TAG, "Không tìm thấy RecyclerView với ID: recycler_providers");
        }
    }

    // --- HÀM BỔ SUNG: TẢI DỮ LIỆU TỪ FIRESTORE (REAL-TIME) ---
    private void loadNhaCungCapData() {
        // Sử dụng addSnapshotListener để lắng nghe thay đổi real-time từ Firestore
        listenerRegistration = nhaCungCapRef.addSnapshotListener(new com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable com.google.firebase.firestore.QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Xử lý lỗi
                    Log.w(TAG, "Lỗi lắng nghe Firestore: ", e);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu real-time: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (snapshots != null) {
                    nhaCungCapList.clear(); // Xóa dữ liệu cũ
                    for (QueryDocumentSnapshot document : snapshots) {
                        try {
                            // Chuyển đổi Document thành đối tượng NhaCungCap
                            NhaCungCap ncc = document.toObject(NhaCungCap.class);
                            // Set ID của document (sẽ hữu ích khi cập nhật/xóa)
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

    // --- TRIỂN KHAI PHƯƠNG THỨC CỦA INTERFACE NhaCungCapAdapter.OnItemActionListener ---

    @Override
    public void onEditClick(NhaCungCap ncc) {
        // Xử lý khi bấm Sửa: Mở Fragment Sửa
        openEditSupplierFragment(ncc.getMaNhaCungCap());
    }

    @Override
    public void onViewClick(NhaCungCap ncc) {
        // Xử lý khi bấm Xem chi tiết
        Toast.makeText(getContext(), "Mở chi tiết nhà cung cấp: " + ncc.getTenNhaCungCap(), Toast.LENGTH_SHORT).show();
        // TODO: Triển khai logic mở Fragment Chi tiết NhaCungCap tại đây
    }

    @Override
    public void onDeleteClick(NhaCungCap ncc) {
        // Xử lý khi bấm Xóa: Hiện Dialog xác nhận
        showDeleteConfirmationDialog(ncc);
    }

    // --- HÀM HỖ TRỢ XỬ LÝ SỰ KIỆN SỬA VÀ XÓA ---

    /**
     * Mở Fragment Sửa NhaCungCap và truyền ID của nhà cung cấp cần sửa.
     */
    private void openEditSupplierFragment(String supplierId) {
        if (getParentFragmentManager() != null) {
            // 1. Tạo Bundle để truyền dữ liệu
            Bundle bundle = new Bundle();
            bundle.putString("supplier_id", supplierId); // Gửi ID qua Bundle

            // 2. Tạo instance của Fragment Sửa (Sử dụng tên class đã cung cấp)
            SuaNhaCungCapFragment editFragment = new SuaNhaCungCapFragment();
            editFragment.setArguments(bundle); // Gán Bundle cho Fragment

            // 3. Thực hiện Transaction
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content_frame, editFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();

            Toast.makeText(getContext(), "Chuyển sang màn hình Sửa Nhà Cung Cấp: " + supplierId, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị Dialog xác nhận và gọi hàm xóa dữ liệu.
     */
    private void showDeleteConfirmationDialog(NhaCungCap ncc) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc chắn muốn xóa nhà cung cấp " + ncc.getTenNhaCungCap() + " (ID: " + ncc.getMaNhaCungCap() + ") không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Thực hiện xóa trên Firebase
                    deleteSupplierFromFirestore(ncc);
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Thực hiện xóa document trên Firestore.
     */
    private void deleteSupplierFromFirestore(NhaCungCap ncc) {
        if (ncc.getMaNhaCungCap() == null || ncc.getMaNhaCungCap().isEmpty()) {
            Toast.makeText(getContext(), "Không thể xóa: Thiếu ID nhà cung cấp.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference docRef = db.collection("NhaCungCap").document(ncc.getMaNhaCungCap());

        docRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã xóa thành công: " + ncc.getTenNhaCungCap(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    // Danh sách sẽ tự cập nhật nhờ ListenerRegistration (real-time listener)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi xóa dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Error deleting document", e);
                });
    }

    // --- CÁC PHƯƠNG THỨC KHÁC ---

    // Xóa listener khi Fragment bị hủy để tránh rò rỉ bộ nhớ
    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    /**
     * Hàm xử lý chuyển sang Fragment Tạo Nhà Cung Cấp Mới.
     */
    private void openCreateSupplierFragment() {
        if (getParentFragmentManager() != null) {
            // THAY ĐỔI: Sử dụng Fragment thực tế cho việc tạo mới
            TaoNhaCungCapFragment createFragment = new TaoNhaCungCapFragment();

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.add(R.id.main_content_frame, createFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();

            Toast.makeText(getContext(), "Chuyển sang màn hình Thêm Nhà Cung Cấp", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupQuickActions(View view) {
// Đảm bảo các ID này tồn tại trong layout fragment_kinhdoanh.xml
        View actionContract = view.findViewById(R.id.action_contract);
        View actionPerformance = view.findViewById(R.id.action_performance);
        View actionFilter = view.findViewById(R.id.action_filter);
        if (actionContract != null) {
            ((ImageView) actionContract.findViewById(R.id.action_icon)).setImageResource(R.drawable.ic_document);
            ((TextView) actionContract.findViewById(R.id.action_title)).setText("Quản lý hợp đồng");
            actionContract.setOnClickListener(v -> Toast.makeText(getContext(), "Tới Quản lý hợp đồng", Toast.LENGTH_SHORT).show());
        }
        if (actionPerformance != null) {
            ((ImageView) actionPerformance.findViewById(R.id.action_icon)).setImageResource(R.drawable.ic_assessment);
            ((TextView) actionPerformance.findViewById(R.id.action_title)).setText("Đánh giá hiệu suất");
            actionPerformance.setOnClickListener(v -> Toast.makeText(getContext(), "Tới Đánh giá hiệu suất", Toast.LENGTH_SHORT).show());
        }
        if (actionFilter != null) {
            ((ImageView) actionFilter.findViewById(R.id.action_icon)).setImageResource(R.drawable.ic_filter);
            ((TextView) actionFilter.findViewById(R.id.action_title)).setText("Bộ lọc");
            actionFilter.setOnClickListener(v -> Toast.makeText(getContext(), "Mở Bộ lọc", Toast.LENGTH_SHORT).show());
        }
    }
}