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
import java.util.Arrays;
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

    // Firebase
    private FirebaseFirestore db;
    private TourAssignmentAdapter adapter;
    private final List<Tour> allTourList = new ArrayList<>();
    private final List<Tour> currentDisplayedList = new ArrayList<>();

    // Các hằng số khớp với dữ liệu Firestore thực tế của bạn
    private static final String TOURS_COLLECTION_PATH = "Tours";
    private static final String STATUS_OPEN = "DANG_MO_BAN";
    private static final String STATUS_AWAITING = "DANG_CHO_PHAN_CONG";
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
        View view = inflater.inflate(R.layout.fragment_tour_assignment_list, container, false);
        mapViews(view);
        setupToolbar();
        setupRecyclerView();
        setupTabLayout();
        return view;
    }

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
    }

    private void setupRecyclerView() {
        adapter = new TourAssignmentAdapter(getContext(), currentDisplayedList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterToursByTab(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterToursByTab(int position) {
        currentDisplayedList.clear();
        Log.d(TAG, "Filtering for tab position: " + position + ". Total items in allTourList: " + allTourList.size());

        if (position == 0) {
            emptyStateTextView.setText("Không có chuyến tour nào đang chờ phân công.");
            for (Tour tour : allTourList) {
                String s = tour.getStatus();
                if (STATUS_OPEN.equals(s) || STATUS_AWAITING.equals(s)) {
                    currentDisplayedList.add(tour);
                }
            }
        } else {
            emptyStateTextView.setText("Không có chuyến tour nào đã được gán.");
            for (Tour tour : allTourList) {
                if (STATUS_ASSIGNED.equals(tour.getStatus())) {
                    currentDisplayedList.add(tour);
                }
            }
        }

        Log.d(TAG, "Items after filter: " + currentDisplayedList.size());
        adapter.updateList(currentDisplayedList);
        updateEmptyState();
    }

    private void loadToursForAssignment() {
        if (!isAdded()) return;
        showLoading(true);

        List<String> statusesToLoad = Arrays.asList(STATUS_OPEN, STATUS_AWAITING, STATUS_ASSIGNED);

        // Sử dụng Index đã tạo (status + ngayKhoiHanh)
        db.collection(TOURS_COLLECTION_PATH)
                .whereIn("status", statusesToLoad)
                .orderBy("ngayKhoiHanh", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allTourList.clear();
                    Log.d(TAG, "Firebase success. Found docs: " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Tour tour = document.toObject(Tour.class);
                            tour.setMaTour(document.getId());
                            allTourList.add(tour);
                        } catch (Exception e) {
                            Log.e(TAG, "Mapping error for ID: " + document.getId(), e);
                        }
                    }

                    if (isAdded()) {
                        int currentTab = tabLayout.getSelectedTabPosition();
                        filterToursByTab(currentTab != -1 ? currentTab : 0);
                        showLoading(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase error: " + e.getMessage());
                    if (isAdded()) {
                        showLoading(false);
                        Toast.makeText(getContext(), "Không thể tải dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyState() {
        boolean isEmpty = currentDisplayedList.isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyStateTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAssignTourClick(Tour tour) {
        if (tour.getMaTour() == null) return;
        AssignResourceBottomSheetFragment bottomSheet = AssignResourceBottomSheetFragment.newInstance(tour.getMaTour());
        bottomSheet.show(getParentFragmentManager(), "AssignmentBottomSheet");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadToursForAssignment();
    }
}