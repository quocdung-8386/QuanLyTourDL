package com.example.quanlytourdl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.TourAssignmentAdapter;
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fragment hiển thị danh sách Tour cần Phân công, được lọc theo trạng thái (Chờ Gán/Đã Gán)
 * và xử lý sự kiện phân công.
 */
public class TourAssignmentListFragment extends Fragment implements TourAssignmentAdapter.AssignmentListener {

    private static final String TAG = "TourAssignListFragment";

    // --- UI Components ---
    private TabLayout tabLayout;
    private RecyclerView recyclerViewTours;
    private ImageButton btnBack, btnNotification;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;

    // --- Firebase & Data Variables ---
    private FirebaseFirestore db;
    private ListenerRegistration tourListener;
    private TourAssignmentAdapter adapter;
    private final List<Tour> toursList = new ArrayList<>();

    // ⭐ Đặt biến cho Collection Path để dễ quản lý hơn
    private static final String APP_ID = "QLTDL_AppId_Placeholder";
    private static final String TOURS_COLLECTION_PATH = String.format("artifacts/%s/public/data/tours", APP_ID);


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Giả định layout ID fragment_tour_assignment_list
        View view = inflater.inflate(R.layout.fragment_tour_assignment_list, container, false);

        // 1. Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // 2. Ánh xạ các thành phần UI
        tabLayout = view.findViewById(R.id.tab_layout_assignment);
        recyclerViewTours = view.findViewById(R.id.recycler_tours_for_assignment);
        btnBack = view.findViewById(R.id.btn_back_assignment_list);
        btnNotification = view.findViewById(R.id.btn_notification_assignment);

        progressBar = view.findViewById(R.id.progress_loading_assignment);
        emptyStateTextView = view.findViewById(R.id.text_empty_state_assignment);

        // 3. Thiết lập RecyclerView
        recyclerViewTours.setLayoutManager(new LinearLayoutManager(getContext()));

        // 4. Khởi tạo Adapter
        Context context = getContext();
        if (context != null) {
            adapter = new TourAssignmentAdapter(context, toursList, this);
            recyclerViewTours.setAdapter(adapter);
        }

        // 5. Xử lý sự kiện Toolbar
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Quay lại Fragment trước đó hoặc Activity (tùy thuộc vào cách bạn sử dụng)
                // Ưu tiên pop Fragment nếu nó được nhúng
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    getActivity().onBackPressed();
                }
            }
        });

        btnNotification.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở thông báo về phân công Tour", Toast.LENGTH_SHORT).show();
        });


        // 6. Xử lý sự kiện Tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabTitle = Objects.requireNonNull(tab.getText()).toString();
                String statusForQuery = mapTabTitleToStatus(tabTitle);
                loadToursFromFirestore(statusForQuery, tabTitle);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Tải lại khi nhấp lại
                String tabTitle = Objects.requireNonNull(tab.getText()).toString();
                String statusForQuery = mapTabTitleToStatus(tabTitle);
                loadToursFromFirestore(statusForQuery, tabTitle);
            }
        });

        // 7. Tải dữ liệu ban đầu
        // ⭐ Tối ưu hóa: Chọn tab đầu tiên và kích hoạt sự kiện tải dữ liệu
        if (tabLayout.getTabCount() > 0) {
            TabLayout.Tab initialSelectedTab = tabLayout.getTabAt(0);
            if (initialSelectedTab != null) {
                // Chọn tab (đảm bảo tab được chọn và sự kiện onTabSelected được kích hoạt)
                initialSelectedTab.select();
                // Nếu sự kiện select() không kích hoạt onTabSelected, ta cần gọi thủ công:
                // String initialTitle = initialSelectedTab.getText() != null ? initialSelectedTab.getText().toString() : "Chờ Gán";
                // String initialStatus = mapTabTitleToStatus(initialTitle);
                // loadToursFromFirestore(initialStatus, initialTitle);
            }
        }

        return view;
    }

    // -------------------------------------------------------------------
    //                       IMPLEMENTATION CỦA AssignmentListener
    // -------------------------------------------------------------------

    @Override
    public void onAssignTourClick(Tour tour) {
        Log.d(TAG, "Yêu cầu xử lý phân công cho Tour: " + tour.getMaTour() + " - " + tour.getTenTour());
        Toast.makeText(getContext(), "Mở màn hình Gán Nhân Viên cho tour: " + tour.getTenTour(), Toast.LENGTH_LONG).show();
        // ⭐ Bổ sung: Chuyển Fragment tại đây để mở màn hình phân công chi tiết (ví dụ: TourAssignmentDetailFragment)
    }

    private String mapTabTitleToStatus(String tabTitle) {
        if (tabTitle.equalsIgnoreCase("Chờ Gán")) {
            return "DANG_CHO_PHAN_CONG";
        }
        else if (tabTitle.equalsIgnoreCase("Đã Gán")) {
            return "DA_GAN_NHAN_VIEN";
        }
        // Trường hợp mặc định cho tab đầu tiên hoặc lỗi
        return "DANG_CHO_PHAN_CONG";
    }

    /**
     * Thiết lập Listener để lắng nghe dữ liệu Tour từ Firestore dựa trên trạng thái (status).
     */
    private void loadToursFromFirestore(String status, String tabTitle) {
        // 1. Hiển thị Loading State
        showLoading();

        // 2. Gỡ bỏ listener cũ
        if (tourListener != null) {
            tourListener.remove();
        }

        CollectionReference toursRef = db.collection(TOURS_COLLECTION_PATH);
        // Lọc theo trường "status"
        Query query = toursRef.whereEqualTo("status", status);

        // 3. Thiết lập lắng nghe theo thời gian thực (real-time listener)
        tourListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Lỗi lắng nghe dữ liệu Tour cho trạng thái: " + status, e);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                showEmptyState("Lỗi kết nối hoặc truy vấn dữ liệu.");
                return;
            }

            if (snapshots != null) {
                toursList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    try {
                        Tour tour = doc.toObject(Tour.class);
                        tour.setMaTour(doc.getId()); // Lưu ID tài liệu Firestore làm MaTour
                        toursList.add(tour);
                    } catch (Exception ex) {
                        Log.e(TAG, "Lỗi khi chuyển đổi Document sang đối tượng Tour: " + doc.getId(), ex);
                    }
                }

                // 4. Cập nhật giao diện dựa trên kết quả
                if (toursList.isEmpty()) {
                    showEmptyState("Hiện không có Tour nào ở trạng thái \"" + tabTitle + "\".");
                } else {
                    if (adapter != null) {
                        adapter.updateList(toursList);
                    }
                    showContent();
                }

                Log.d(TAG, "Đã tải thành công " + toursList.size() + " Tour có trạng thái: " + status);
            }
        });
    }


    // --- Quản lý Trạng thái Hiển thị ---

    private void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerViewTours != null) recyclerViewTours.setVisibility(View.GONE);
        if (emptyStateTextView != null) emptyStateTextView.setVisibility(View.GONE);
    }

    private void showContent() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (emptyStateTextView != null) emptyStateTextView.setVisibility(View.GONE);
        if (recyclerViewTours != null) recyclerViewTours.setVisibility(View.VISIBLE);
    }

    private void showEmptyState(String message) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (recyclerViewTours != null) recyclerViewTours.setVisibility(View.GONE);
        if (emptyStateTextView != null) {
            emptyStateTextView.setText(message);
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Luôn gỡ bỏ listener khi View bị hủy để tránh rò rỉ bộ nhớ
        if (tourListener != null) {
            tourListener.remove();
            Log.d(TAG, "Tour Listener đã được hủy.");
        }
    }
}