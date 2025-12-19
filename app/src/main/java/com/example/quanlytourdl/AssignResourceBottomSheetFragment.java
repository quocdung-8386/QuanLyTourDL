package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.ResourceAdapter;
import com.example.quanlytourdl.model.Guide;
import com.example.quanlytourdl.model.Vehicle;
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AssignResourceBottomSheetFragment extends BottomSheetDialogFragment
        implements ResourceAdapter.OnResourceSelectedListener {

    private static final String TAG = "AssignResourceBS";
    private static final String ARG_TOUR_ID = "tour_id";

    private static final String COLLECTION_TOURS = "Tours";
    private static final String COLLECTION_GUIDES = "tour_guides";
    private static final String COLLECTION_VEHICLES = "phuongtien";

    private String tourId;
    private FirebaseFirestore db;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    private ImageButton btnCloseSheet;
    private TabLayout tabLayout;
    private RecyclerView recyclerResourceList, recyclerConflictingList;
    private TextView tvTourSummary, tvSelectedResource;
    private Button btnSaveAndContinue, btnCancelAssignment;

    private ResourceAdapter adapterSuggested, adapterConflicting;

    // Sử dụng List mới trong loadResources để tránh lỗi tham chiếu khi Adapter đang xử lý
    private List<Object> suggestedList = new ArrayList<>();
    private List<Object> conflictingList = new ArrayList<>();

    private Object selectedResource;
    private String currentAssignmentType = "GUIDE";

    public static AssignResourceBottomSheetFragment newInstance(String tourId) {
        AssignResourceBottomSheetFragment fragment = new AssignResourceBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOUR_ID, tourId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) tourId = getArguments().getString(ARG_TOUR_ID);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assign_resource_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapViews(view);
        setupAdapters();
        setupListeners();
        loadTourDetails();
    }

    private void mapViews(View view) {
        btnCloseSheet = view.findViewById(R.id.btn_close_sheet);
        tabLayout = view.findViewById(R.id.tab_layout_assignment_type);
        recyclerResourceList = view.findViewById(R.id.recycler_resource_list);
        recyclerConflictingList = view.findViewById(R.id.recycler_conflicting_list);
        tvTourSummary = view.findViewById(R.id.tv_tour_summary);
        tvSelectedResource = view.findViewById(R.id.tv_selected_resource);
        btnSaveAndContinue = view.findViewById(R.id.btn_save_and_continue);
        btnCancelAssignment = view.findViewById(R.id.btn_cancel_assignment);
    }

    private void setupAdapters() {
        // Khởi tạo adapter với list rỗng ban đầu
        adapterSuggested = new ResourceAdapter(getContext(), new ArrayList<>(), this);
        recyclerResourceList.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerResourceList.setAdapter(adapterSuggested);

        adapterConflicting = new ResourceAdapter(getContext(), new ArrayList<>(), this);
        recyclerConflictingList.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerConflictingList.setAdapter(adapterConflicting);
    }

    private void setupListeners() {
        btnCloseSheet.setOnClickListener(v -> dismiss());
        btnCancelAssignment.setOnClickListener(v -> dismiss());
        btnSaveAndContinue.setOnClickListener(v -> handleSaveAndContinue());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentAssignmentType = (tab.getPosition() == 0) ? "GUIDE" : "VEHICLE";
                resetSelection();
                loadResources(currentAssignmentType);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void resetSelection() {
        selectedResource = null;
        tvSelectedResource.setText("Đã chọn: Chưa chọn");
        btnSaveAndContinue.setText(currentAssignmentType.equals("GUIDE") ? "Lưu & Tiếp tục (Xe)" : "Lưu & Hoàn tất");
    }

    private void loadTourDetails() {
        if (tourId == null) return;
        db.collection(COLLECTION_TOURS).document(tourId).get()
                .addOnSuccessListener(doc -> {
                    Tour tour = doc.toObject(Tour.class);
                    if (tour != null) {
                        String dateStr = tour.getNgayKhoiHanh() != null ? dateFormatter.format(tour.getNgayKhoiHanh()) : "Chưa xác định";
                        tvTourSummary.setText(String.format("%s (%s)\nNgày: %s",
                                tour.getTenTour(), tour.getMaTour(), dateStr));
                        loadResources("GUIDE"); // Mặc định load HDV trước
                    }
                });
    }

    private void loadResources(String type) {
        String path = type.equals("GUIDE") ? COLLECTION_GUIDES : COLLECTION_VEHICLES;

        db.collection(path).get().addOnSuccessListener(querySnapshot -> {
            List<Object> tempSuggested = new ArrayList<>();
            List<Object> tempConflicting = new ArrayList<>();

            for (QueryDocumentSnapshot doc : querySnapshot) {
                if (type.equals("GUIDE")) {
                    Guide guide = doc.toObject(Guide.class);
                    if (guide != null) {
                        guide.setId(doc.getId());
                        if (guide.isApproved()) {
                            tempSuggested.add(guide);
                        } else {
                            tempConflicting.add(guide);
                        }
                    }
                } else {
                    Vehicle vehicle = doc.toObject(Vehicle.class);
                    if (vehicle != null) {
                        vehicle.setId(doc.getId());
                        if ("Hoạt động tốt".equalsIgnoreCase(vehicle.getTinhTrangBaoDuong())) {
                            tempSuggested.add(vehicle);
                        } else {
                            tempConflicting.add(vehicle);
                        }
                    }
                }
            }

            // Cập nhật vào list chính và notify adapter
            suggestedList = tempSuggested;
            conflictingList = tempConflicting;

            adapterSuggested.updateList(suggestedList);
            adapterConflicting.updateList(conflictingList);

            Log.d(TAG, "Loaded " + type + ": " + suggestedList.size() + " items");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading resources", e);
            Toast.makeText(getContext(), "Không thể tải danh sách dữ liệu", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResourceSelected(Object resource) {
        selectedResource = resource;
        String displayName = "";
        if (resource instanceof Guide) {
            displayName = ((Guide) resource).getFullName();
        } else if (resource instanceof Vehicle) {
            displayName = ((Vehicle) resource).getBienSoXe();
        }
        tvSelectedResource.setText(String.format("Đã chọn: %s", displayName));
    }

    private void handleSaveAndContinue() {
        if (selectedResource == null) {
            Toast.makeText(getContext(), "Vui lòng chọn một mục!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        if (currentAssignmentType.equals("GUIDE")) {
            Guide g = (Guide) selectedResource;
            updates.put("assignedGuideId", g.getId());
            updates.put("assignedGuideName", g.getFullName());
        } else {
            Vehicle v = (Vehicle) selectedResource;
            updates.put("assignedVehicleId", v.getId());
            updates.put("assignedVehicleLicensePlate", v.getBienSoXe());
            // Chỉ khi gán xe xong mới coi là hoàn tất bước gán nhân sự/phương tiện
            updates.put("status", "DA_GAN_NHAN_VIEN");
        }

        db.collection(COLLECTION_TOURS).document(tourId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (currentAssignmentType.equals("GUIDE")) {
                        Toast.makeText(getContext(), "Đã lưu HDV. Vui lòng chọn Xe.", Toast.LENGTH_SHORT).show();
                        // Chuyển sang tab Xe tự động
                        TabLayout.Tab vehicleTab = tabLayout.getTabAt(1);
                        if (vehicleTab != null) vehicleTab.select();
                    } else {
                        Toast.makeText(getContext(), "Hoàn tất phân công!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Update failed", e);
                    Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}