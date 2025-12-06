package com.example.quanlytourdl;

import android.os.Bundle;
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

import com.example.quanlytourdl.adapter.NhaCungCapAdapter;
import com.example.quanlytourdl.model.NhaCungCap;

// Imports cho các Fragment chuyển hướng
import com.example.quanlytourdl.TaoNhaCungCapFragment;
import com.example.quanlytourdl.SuaNhaCungCapFragment;
import com.example.quanlytourdl.ChamDutHopDongFragment;
import com.example.quanlytourdl.QuanLyHopDongFragment; // Đã thêm import cho Fragment Quản lý Hợp đồng

import java.util.ArrayList;
import java.util.List;

public class KinhDoanhFragment extends Fragment implements NhaCungCapAdapter.OnItemActionListener {

    private static final String TAG = "KinhDoanhFragment";

    private RecyclerView recyclerView;
    private NhaCungCapAdapter adapter;
    private List<NhaCungCap> nhaCungCapList;

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
            Log.e(TAG, "Không tìm thấy layout ID 'fragment_kinhdoanh'.");
            return null;
        }

        View view = inflater.inflate(layoutId, container, false);

        // 1. Xử lý Menu Dấu 3 Gạch
        ImageButton btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer);
        if (btnMenuDrawer != null) {
            btnMenuDrawer.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Mở Menu chính (Navigation Drawer)", Toast.LENGTH_SHORT).show();
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
        // Xử lý khi bấm Sửa: Mở Fragment Sửa
        openEditSupplierFragment(ncc.getMaNhaCungCap());
    }

    @Override
    public void onViewClick(NhaCungCap ncc) {
        // Xử lý khi bấm Xem chi tiết
        Toast.makeText(requireContext(), "Mở chi tiết nhà cung cấp: " + ncc.getTenNhaCungCap(), Toast.LENGTH_SHORT).show();
        // TODO: Mở Fragment xem chi tiết NCC
    }

    /**
     * Phương thức xử lý khi người dùng nhấn XÓA/HÀNH ĐỘNG.
     * Theo yêu cầu mới: Luôn luôn chuyển hướng sang ChamDutHopDongFragment, không cần kiểm tra Hợp đồng Active.
     */
    @Override
    public void onDeleteClick(NhaCungCap ncc) {
        // Lấy ID hợp đồng active (có thể là null hoặc rỗng)
        String contractIdToTerminate = ncc.getMaHopDong();

        // Gán là chuỗi rỗng nếu null để đảm bảo Bundle không bị lỗi và Fragment đích có thể xử lý logic kiểm tra
        if (contractIdToTerminate == null) {
            contractIdToTerminate = "";
        }

        // Chuyển hướng ngay lập tức đến màn hình Chấm dứt Hợp đồng
        openTerminateContractFragment(ncc.getMaNhaCungCap(), contractIdToTerminate);

        Toast.makeText(requireContext(), "Đang chuyển đến màn hình Chấm dứt Hợp đồng/Xóa Nhà Cung Cấp...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTerminateContract(NhaCungCap ncc) {
        // Phương thức này có thể được sử dụng trong Adapter, nhưng hiện tại không có logic
    }

    // --- HÀM HỖ TRỢ CHUYỂN FRAGMENT ---

    /**
     * Mở Fragment Quản lý Hợp Đồng.
     */
    private void openQuanLyHopDongFragment() {
        if (getParentFragmentManager() != null) {
            QuanLyHopDongFragment contractFragment = new QuanLyHopDongFragment();

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // Đảm bảo R.id.main_content_frame là ID Frame layout chứa Fragment chính
            transaction.replace(R.id.main_content_frame, contractFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();

            Log.d(TAG, "Chuyển sang màn hình Quản lý Hợp đồng NCC");
        }
    }

    /**
     * Mở Fragment Sửa NhaCungCap và truyền ID của nhà cung cấp cần sửa.
     */
    private void openEditSupplierFragment(String supplierId) {
        if (getParentFragmentManager() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("supplier_id", supplierId);

            SuaNhaCungCapFragment editFragment = new SuaNhaCungCapFragment();
            editFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content_frame, editFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();

            Log.d(TAG, "Chuyển sang màn hình Sửa Nhà Cung Cấp: " + supplierId);
        }
    }

    /**
     * Mở Fragment Chấm Dứt Hợp Đồng và truyền ID Nhà cung cấp VÀ ID Hợp đồng.
     */
    private void openTerminateContractFragment(String supplierId, String contractId) {
        if (getParentFragmentManager() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("supplier_id", supplierId);
            bundle.putString("contract_id", contractId);

            ChamDutHopDongFragment terminateFragment = new ChamDutHopDongFragment();
            terminateFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content_frame, terminateFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();

            Log.d(TAG, "Chuyển sang màn hình Chấm dứt Hợp đồng. NCC ID: " + supplierId + ", Contract ID: " + contractId);
        }
    }

    /**
     * Hàm xử lý chuyển sang Fragment Tạo Nhà Cung Cấp Mới.
     */
    private void openCreateSupplierFragment() {
        if (getParentFragmentManager() != null) {
            TaoNhaCungCapFragment createFragment = new TaoNhaCungCapFragment();

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.add(R.id.main_content_frame, createFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();

            Log.d(TAG, "Chuyển sang màn hình Thêm Nhà Cung Cấp");
        }
    }

    /**
     * Cập nhật nội dung cho các Quick Actions (Hành động nhanh) và thiết lập listener.
     */
    private void setupQuickActions(View view) {
        // Giả định các ID và resource tồn tại
        View actionContract = view.findViewById(R.id.action_contract);
        View actionPerformance = view.findViewById(R.id.action_performance);
        View actionFilter = view.findViewById(R.id.action_filter);

        if (actionContract != null) {
            // [Image of icon for document management]
            ((ImageView) actionContract.findViewById(R.id.action_icon)).setImageResource(R.drawable.ic_document);
            ((TextView) actionContract.findViewById(R.id.action_title)).setText("Quản lý hợp đồng");

            // THIẾT LẬP SỰ KIỆN CLICK MỚI ĐỂ MỞ QUẢN LÝ HỢP ĐỒNG
            actionContract.setOnClickListener(v -> openQuanLyHopDongFragment());
        }
        if (actionPerformance != null) {
            // [Image of icon for performance assessment]
            ((ImageView) actionPerformance.findViewById(R.id.action_icon)).setImageResource(R.drawable.ic_assessment);
            ((TextView) actionPerformance.findViewById(R.id.action_title)).setText("Đánh giá hiệu suất");
            actionPerformance.setOnClickListener(v -> Toast.makeText(requireContext(), "Tới Đánh giá hiệu suất", Toast.LENGTH_SHORT).show());
        }
        if (actionFilter != null) {
            // [Image of icon for filtering data]
            ((ImageView) actionFilter.findViewById(R.id.action_icon)).setImageResource(R.drawable.ic_filter);
            ((TextView) actionFilter.findViewById(R.id.action_title)).setText("Bộ lọc");
            actionFilter.setOnClickListener(v -> Toast.makeText(requireContext(), "Mở Bộ lọc", Toast.LENGTH_SHORT).show());
        }
    }
}