package com.example.quanlytourdl;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays; // Cần thiết cho List.of()
import java.util.List;

/**
 * Fragment hiển thị danh sách các Tour cần phân công và đã phân công.
 */
public class TourAssignmentListFragment extends Fragment
        implements TourAssignmentAdapter.AssignmentListener {

    private static final String TAG = "TourAssignmentListFrag";

    // UI Components
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private TabLayout tabLayout;
    private ImageButton btnBack;

    // Data & Firebase
    private FirebaseFirestore db;
    private TourAssignmentAdapter adapter;
    private final List<Tour> allTourList = new ArrayList<>(); // Danh sách gốc
    private final List<Tour> currentDisplayedList = new ArrayList<>(); // Danh sách đang hiển thị

    // ⭐ LƯU Ý: Đảm bảo đường dẫn này đúng. Nếu bạn lưu trực tiếp trong collection 'Tours',
    // hãy thay đổi thành "Tours".
    private static final String TOURS_COLLECTION_PATH = "Tours";

    // ⭐ Hằng số Trạng thái Tour: Đảm bảo khớp với DB
    private static final String STATUS_AWAITING_ASSIGNMENT = "DANG_CHO_PHAN_CONG";
    private static final String STATUS_ASSIGNED = "DA_GAN_NHAN_VIEN";


    public static TourAssignmentListFragment newInstance() {
        return new TourAssignmentListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng R.layout.fragment_tour_assignment_list
        View view = inflater.inflate(R.layout.fragment_tour_assignment_list, container, false);

        mapViews(view);
        setupToolbar();
        setupRecyclerView();
        setupTabLayout();

        loadToursForAssignment();

        return view;
    }

    // --- 1. Ánh xạ View (Mapping) ---
    private void mapViews(View view) {
        btnBack = view.findViewById(R.id.btn_back_assignment_list);
        tabLayout = view.findViewById(R.id.tab_layout_assignment);
        recyclerView = view.findViewById(R.id.recycler_tours_for_assignment);
        progressBar = view.findViewById(R.id.progress_loading_assignment);
        emptyStateTextView = view.findViewById(R.id.text_empty_state_assignment);
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        // Logic cho btn_notification_assignment (nếu có) có thể được thêm vào đây
    }

    private void setupRecyclerView() {
        adapter = new TourAssignmentAdapter(getContext(), currentDisplayedList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    // --- 2. Xử lý Tab ---
    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterToursByTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Đảm bảo tab đầu tiên được chọn và lọc dữ liệu ban đầu
        if (tabLayout.getTabCount() > 0) {
            // Lấy lại tab đầu tiên, đảm bảo logic filter chạy đúng sau khi load data
            TabLayout.Tab firstTab = tabLayout.getTabAt(0);
            if (firstTab != null) {
                tabLayout.selectTab(firstTab);
                // KHÔNG cần gọi filterToursByTab(0) ở đây vì nó sẽ được gọi trong listener
                // khi loadToursForAssignment hoàn thành.
            }
        }
    }

    /**
     * Lọc danh sách tour đã tải về (allTourList) dựa trên tab được chọn.
     * @param position 0 cho Chờ Phân công, 1 cho Đã Phân công (Giả định Tab 0, 1)
     */
    private void filterToursByTab(int position) {
        currentDisplayedList.clear();

        // Xác định trạng thái mục tiêu
        String targetStatus;
        if (position == 0) {
            targetStatus = STATUS_AWAITING_ASSIGNMENT;
            emptyStateTextView.setText("Không có chuyến tour nào đang chờ phân công.");
        } else {
            targetStatus = STATUS_ASSIGNED;
            emptyStateTextView.setText("Không có chuyến tour nào đã được gán.");
        }

        // Lọc danh sách
        for (Tour tour : allTourList) {
            if (tour.getStatus() != null && tour.getStatus().equals(targetStatus)) {
                currentDisplayedList.add(tour);
            }
        }

        // Cập nhật giao diện
        adapter.updateList(currentDisplayedList);
        updateEmptyState();
    }

    // --- 3. Tải Dữ liệu ---

    private void loadToursForAssignment() {
        showLoading(true);

        // TẢI TẤT CẢ các tour có trạng thái CHỜ GÁN HOẶC ĐÃ GÁN
        // Sử dụng Arrays.asList cho tính tương thích
        List<String> statusesToLoad = Arrays.asList(STATUS_AWAITING_ASSIGNMENT, STATUS_ASSIGNED);

        Query query = db.collection(TOURS_COLLECTION_PATH)
                .whereIn("status", statusesToLoad)
                .orderBy("ngayKhoiHanh", Query.Direction.ASCENDING);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allTourList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Tour tour = document.toObject(Tour.class);
                            tour.setMaTour(document.getId());

                            // Đảm bảo lấy các trường phân công (chống lỗi null pointer khi lấy dữ liệu)
                            tour.setAssignedGuideName(document.getString("assignedGuideName"));
                            tour.setAssignedVehicleLicensePlate(document.getString("assignedVehicleLicensePlate"));

                            // Có thể thêm assignedGuideId và assignedVehicleId nếu cần

                            allTourList.add(tour);
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi mapping Tour: " + document.getId(), e);
                        }
                    }

                    // Sau khi tải, lọc lại danh sách hiển thị theo tab hiện tại
                    // Đảm bảo tabLayout.getSelectedTabPosition() không trả về -1
                    int currentTab = tabLayout.getSelectedTabPosition();
                    if (currentTab != -1) {
                        filterToursByTab(currentTab);
                    } else {
                        // Trường hợp không có tab nào được chọn (hiếm)
                        filterToursByTab(0);
                    }

                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải danh sách Tour", e);
                    Toast.makeText(getContext(), "Không thể tải danh sách Tour.", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    // --- 4. Trạng thái Hiển thị ---

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (!isLoading) {
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        boolean isEmpty = currentDisplayedList.isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyStateTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        // Nội dung Empty State đã được cập nhật trong filterToursByTab
    }

    // --- 5. Xử lý sự kiện click (AssignmentListener) ---
    @Override
    public void onAssignTourClick(Tour tour) {
        if (tour.getMaTour() == null) {
            Toast.makeText(getContext(), "Không tìm thấy ID Tour.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mở Bottom Sheet Phân công Tài nguyên
        // Lưu ý: Cần đảm bảo AssignResourceBottomSheetFragment tồn tại
        // và bạn đã xử lý việc tải lại dữ liệu trong BottomSheet khi phân công xong.
        AssignResourceBottomSheetFragment bottomSheet = AssignResourceBottomSheetFragment.newInstance(tour.getMaTour());
        bottomSheet.show(getParentFragmentManager(), "AssignmentBottomSheet");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi Fragment được hiển thị trở lại
        // Điều này đảm bảo khi người dùng đóng Bottom Sheet Phân công, danh sách sẽ được cập nhật.
        loadToursForAssignment();
    }
}