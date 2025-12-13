package com.example.quanlytourdl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quanlytourdl.adapter.TourAdapter;
import com.example.quanlytourdl.adapter.ActiveTourAdapter;
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuanLyTourFragment extends Fragment implements TourAdapter.OnTourActionListener, ActiveTourAdapter.OnActiveTourActionListener {

    private static final String TAG = "QuanLyTourFragment";

    // Khai báo ID chính xác của container trong Activity
    private static final int FRAGMENT_CONTAINER_ID = R.id.main_content_frame;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ImageButton btnOptionsMenu;
    private FloatingActionButton fabAddTour;

    private FirebaseFirestore db;
    private TourAdapter approvalRejectAdapter;
    private ActiveTourAdapter activeTourAdapter;
    private String currentTourStatus = "Đang mở bán"; // Trạng thái mặc định

    public QuanLyTourFragment() {
        // Constructor rỗng bắt buộc
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Giả định layout ID fragment_quan_ly_tour
        View view = inflater.inflate(R.layout.fragment_quan_ly_tour, container, false);

        // 1. Ánh xạ các View
        toolbar = view.findViewById(R.id.toolbar_kinh_doanh);
        tabLayout = view.findViewById(R.id.tab_layout_tour_status);
        recyclerView = view.findViewById(R.id.recycler_tour_list);
        btnOptionsMenu = view.findViewById(R.id.btn_options_menu);
        fabAddTour = view.findViewById(R.id.fab_add_tour);

        // 2. XỬ LÝ NÚT BACK TÙY CHỈNH (Navigation Icon)
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // 3. Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // KHỞI TẠO CẢ HAI ADAPTER
        approvalRejectAdapter = new TourAdapter(requireContext(), new ArrayList<>(), this);
        activeTourAdapter = new ActiveTourAdapter(requireContext(), new ArrayList<>(), this);

        // 4. Thiết lập TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTourStatus = tab.getText() != null ? tab.getText().toString() : "Đang mở bán";
                switchAdapter(currentTourStatus);
                loadTourList(currentTourStatus);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Tải dữ liệu mặc định (Lần đầu tiên)
        switchAdapter(currentTourStatus);
        loadTourList(currentTourStatus);

        // ⭐ 5. Xử lý sự kiện click cho FAB (ĐÃ SỬA LỖI ID CONTAINER) ⭐
        fabAddTour.setOnClickListener(v -> {
            Fragment createFragment = new TaoTourFragment();

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        // ĐÃ SỬA: Sử dụng ID chính xác từ layout của Activity
                        .replace(FRAGMENT_CONTAINER_ID, createFragment)
                        .addToBackStack(null)
                        .commit();
                Log.d(TAG, "Đã chuyển sang TaoTourFragment.");
            } else {
                Toast.makeText(getContext(), "Lỗi: Không thể truy cập Fragment Manager.", Toast.LENGTH_SHORT).show();
            }
        });
        // ⭐ KẾT THÚC XỬ LÝ FAB ⭐

        return view;
    }

    /**
     * Chuyển đổi Adapter cho RecyclerView dựa trên trạng thái (tab)
     */
    private void switchAdapter(String status) {
        RecyclerView.Adapter<?> currentAdapter = recyclerView.getAdapter();

        if (status.equalsIgnoreCase("Đang mở bán")) {
            if (currentAdapter != activeTourAdapter) {
                recyclerView.setAdapter(activeTourAdapter);
                Log.d(TAG, "Đã chuyển sang ActiveTourAdapter");
            }
        } else {
            if (currentAdapter != approvalRejectAdapter) {
                recyclerView.setAdapter(approvalRejectAdapter);
                Log.d(TAG, "Đã chuyển sang ApprovalRejectAdapter");
            }
        }
    }

    /**
     * Chuyển đổi trạng thái hiển thị trên Tab thành Trạng thái lưu trong Database.
     */
    private String getDbStatus(String status) {
        if (status.equalsIgnoreCase("Đang mở bán")) {
            return "DANG_MO_BAN";
        } else if (status.equalsIgnoreCase("Hết hạn")) {
            return "HET_HAN";
        } else if (status.equalsIgnoreCase("Nháp")) {
            return "NHAP";
        }
        return "DANG_MO_BAN"; // Trạng thái mặc định nếu không khớp
    }

    /**
     * Hàm tải danh sách tour THỰC TẾ (Gọi Firebase Firestore).
     */
    private void loadTourList(String status) {
        if (getContext() == null || db == null) {
            Log.e(TAG, "Context hoặc Firebase Firestore chưa khả dụng.");
            return;
        }

        final String dbStatus = getDbStatus(status);
        Toast.makeText(getContext(), "Đang tải Tour: " + status + " (" + dbStatus + ")", Toast.LENGTH_SHORT).show();

        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter == null) return;

        // Xóa danh sách hiện tại
        if (adapter instanceof TourAdapter) {
            ((TourAdapter) adapter).updateList(Collections.emptyList());
        } else if (adapter instanceof ActiveTourAdapter) {
            ((ActiveTourAdapter) adapter).updateList(Collections.emptyList());
        }

        db.collection("Tours")
                .whereEqualTo("status", dbStatus)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Tour> tours = new ArrayList<>();
                        Log.d(TAG, "Firebase Query Success! Số lượng documents: " + task.getResult().size());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Tour tour = document.toObject(Tour.class);
                                // Thêm Document ID vào đối tượng Tour
                                tour.setMaTour(document.getId());
                                tours.add(tour);
                            } catch (Exception e) {
                                Log.e(TAG, "LỖI MAPPING/TOOBJECT TOUR. Kiểm tra Tour.java và tên trường Firestore.", e);
                            }
                        }

                        // Cập nhật Adapter đang hoạt động
                        if (dbStatus.equals("DANG_MO_BAN") && recyclerView.getAdapter() instanceof ActiveTourAdapter) {
                            ((ActiveTourAdapter) recyclerView.getAdapter()).updateList(tours);
                        } else if (recyclerView.getAdapter() instanceof TourAdapter) {
                            ((TourAdapter) recyclerView.getAdapter()).updateList(tours);
                        }

                        Log.d(TAG, "Đã cập nhật Adapter với " + tours.size() + " tour.");

                    } else {
                        Log.e(TAG, "LỖI FIREBASE QUERY: Không thể lấy dữ liệu Tours.", task.getException());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // =================================================================
    // CALLBACKS (IMPLEMENTED INTERFACES)
    // =================================================================

    @Override
    public void onUpdateTour(Tour tour) {
        Toast.makeText(getContext(), "Chức năng: Cập nhật Tour " + tour.getMaTour(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApproveReject(String tourId, String tourName, String newStatus, int position) {
        Toast.makeText(getContext(), "Xử lý: Tour " + tourId + " -> " + newStatus, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDetails(Tour tour) {
        Toast.makeText(getContext(), "Xem chi tiết Tour: " + tour.getTenTour(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageLoad(String imageUrl, ImageView targetView) {
        // Triển khai logic tải ảnh
        targetView.setImageResource(R.drawable.tour_placeholder_danang);
    }
}