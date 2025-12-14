package com.example.quanlytourdl;

import android.graphics.Color;
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
import com.example.quanlytourdl.model.Tour; // Cần model Tour để lấy thông tin chi tiết
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AssignResourceBottomSheetFragment extends BottomSheetDialogFragment
        implements ResourceAdapter.OnResourceSelectedListener {

    private static final String TAG = "AssignResourceBS";
    private static final String ARG_TOUR_ID = "tour_id";

    private String tourId;
    private FirebaseFirestore db;

    // --- UI Components ---
    private ImageButton btnCloseSheet;
    private TabLayout tabLayout;
    private RecyclerView recyclerResourceList;
    private RecyclerView recyclerConflictingList;
    private TextView tvTourSummary, tvSelectedResource;
    private Button btnSaveAndContinue, btnCancelAssignment;

    // --- Data & State ---
    private ResourceAdapter adapterSuggested;
    private ResourceAdapter adapterConflicting;
    private final List<Object> suggestedList = new ArrayList<>();
    private final List<Object> conflictingList = new ArrayList<>();

    private Object selectedResource; // Tài nguyên đang được chọn
    private String currentAssignmentType = "GUIDE"; // "GUIDE" hoặc "VEHICLE"
    private Tour currentTourDetails; // Chi tiết Tour đang gán

    // --- Firestore Path ---
    private static final String APP_ID = "QLTDL_AppId_Placeholder";
    private static final String TOURS_COLLECTION_PATH = String.format("artifacts/%s/public/data/tours", APP_ID);
    private static final String GUIDES_COLLECTION_PATH = "guides"; // Giả định path cho HDV
    private static final String VEHICLES_COLLECTION_PATH = "vehicles"; // Giả định path cho Xe


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
        if (getArguments() != null) {
            tourId = getArguments().getString(ARG_TOUR_ID);
        }
        db = FirebaseFirestore.getInstance();
        // Cấu hình BottomSheet thành full width
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogThemFull);
    }

    // Đảm bảo Bottom Sheet có chiều cao phù hợp (nếu cần)
    // Cần phải set height trong onResume hoặc onStart nếu sử dụng style mặc định

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

        btnCloseSheet.setOnClickListener(v -> dismiss());
        btnCancelAssignment.setOnClickListener(v -> dismiss());
        btnSaveAndContinue.setOnClickListener(v -> handleSaveAndContinue());

        setupTabs();
        loadTourDetails(tourId); // Bắt đầu load thông tin Tour
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
        if (getContext() != null) {
            adapterSuggested = new ResourceAdapter(getContext(), suggestedList, this);
            recyclerResourceList.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerResourceList.setAdapter(adapterSuggested);

            adapterConflicting = new ResourceAdapter(getContext(), conflictingList, this);
            recyclerConflictingList.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerConflictingList.setAdapter(adapterConflicting);
        }
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    currentAssignmentType = "GUIDE";
                } else {
                    currentAssignmentType = "VEHICLE";
                }
                // Reset lựa chọn và hiển thị
                selectedResource = null;
                tvSelectedResource.setText("Đã chọn: Chưa chọn");
                btnSaveAndContinue.setText(currentAssignmentType.equals("GUIDE") ? "Lưu & Tiếp tục (Xe)" : "Lưu & Hoàn tất");

                loadResources(currentAssignmentType);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });

        // Tab đầu tiên sẽ được chọn và kích hoạt loadResources trong onViewCreated
    }

    /**
     * Tải thông tin chi tiết của Tour hiện tại.
     */
    private void loadTourDetails(String id) {
        db.collection(TOURS_COLLECTION_PATH).document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentTourDetails = documentSnapshot.toObject(Tour.class);
                        currentTourDetails.setMaTour(documentSnapshot.getId()); // Đảm bảo ID được set

                        // Cập nhật UI thông tin Tour
                        String summary = String.format(Locale.getDefault(),
                                "%s (#%s)\n%s - %s",
                                currentTourDetails.getTenTour(),
                                currentTourDetails.getMaTour(),
                                currentTourDetails.getNgayKhoiHanh(), // Giả định có field này
                                currentTourDetails.getNgayKetThuc() // Giả định có field này
                        );
                        tvTourSummary.setText(summary);

                        // Kích hoạt tab đầu tiên để load HDV
                        tabLayout.getTabAt(0).select();
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin Tour.", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải thông tin Tour", e);
                    Toast.makeText(getContext(), "Lỗi kết nối khi tải Tour.", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }

    /**
     * Tải danh sách HDV hoặc Phương tiện.
     */
    private void loadResources(String type) {
        String collectionPath = type.equals("GUIDE") ? GUIDES_COLLECTION_PATH : VEHICLES_COLLECTION_PATH;

        // Reset list và hiển thị loading/empty state (Tùy chọn)
        suggestedList.clear();
        conflictingList.clear();
        adapterSuggested.notifyDataSetChanged();
        adapterConflicting.notifyDataSetChanged();

        db.collection(collectionPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // **GIẢ ĐỊNH LOGIC LỌC LỊCH TRÌNH** (Thực tế phức tạp hơn)
                            boolean isConflict = Math.random() < 0.3; // 30% vướng lịch ngẫu nhiên

                            if (type.equals("GUIDE")) {
                                Guide guide = document.toObject(Guide.class);
                                guide.setId(document.getId());
                                guide.setAvailable(!isConflict);
                                guide.setRating(3.5 + Math.random() * 1.5); // Giả định rating ngẫu nhiên
                                guide.setLanguages(Arrays.asList("Tiếng Anh", "Tiếng Việt")); // Giả định ngôn ngữ
                                guide.setExperienceYears((int) (Math.random() * 10) + 1);

                                if (!isConflict) {
                                    suggestedList.add(guide);
                                } else {
                                    conflictingList.add(guide);
                                }
                            } else {
                                Vehicle vehicle = document.toObject(Vehicle.class);
                                vehicle.setId(document.getId());
                                vehicle.setAvailable(!isConflict);
                                vehicle.setDriverName(vehicle.getDriverName() != null ? vehicle.getDriverName() : "Tài xế Mặc Định");

                                if (!isConflict) {
                                    suggestedList.add(vehicle);
                                } else {
                                    conflictingList.add(vehicle);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi mapping resource: " + document.getId(), e);
                        }
                    }

                    adapterSuggested.updateList(suggestedList);
                    adapterConflicting.updateList(conflictingList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải tài nguyên " + type, e);
                    Toast.makeText(getContext(), "Lỗi tải danh sách.", Toast.LENGTH_SHORT).show();
                });
    }

    // --- Xử lý sự kiện từ Adapter (Chọn Item) ---
    @Override
    public void onResourceSelected(Object resource) {
        selectedResource = resource;
        String selectedName;

        if (resource instanceof Guide) {
            selectedName = ((Guide) resource).getFullName();
        } else if (resource instanceof Vehicle) {
            selectedName = ((Vehicle) resource).getBienSoXe();
        } else {
            selectedName = "Không xác định";
        }

        tvSelectedResource.setText(String.format("Đã chọn: %s", selectedName));
    }

    // --- Xử lý nút Lưu & Tiếp tục/Hoàn tất ---
    private void handleSaveAndContinue() {
        if (selectedResource == null) {
            Toast.makeText(getContext(), "Vui lòng chọn một tài nguyên để gán.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Chuẩn bị dữ liệu cập nhật
        String resourceId;
        String resourceName;
        String updateKeyId;
        String updateKeyName;

        if (currentAssignmentType.equals("GUIDE")) {
            Guide guide = (Guide) selectedResource;
            resourceId = guide.getId();
            resourceName = guide.getFullName();
            updateKeyId = "assignedGuideId";
            updateKeyName = "assignedGuideName";
        } else { // VEHICLE
            Vehicle vehicle = (Vehicle) selectedResource;
            resourceId = vehicle.getId();
            resourceName = vehicle.getBienSoXe();
            updateKeyId = "assignedVehicleId";
            updateKeyName = "assignedVehicleLicensePlate";
        }

        // 2. Gọi hàm lưu
        saveAssignmentToFirestore(updateKeyId, updateKeyName, resourceId, resourceName);
    }

    /**
     * Cập nhật thông tin phân công lên Firestore.
     */
    private void saveAssignmentToFirestore(String keyId, String keyName, String resourceId, String resourceName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(keyId, resourceId);
        updates.put(keyName, resourceName);

        // Nếu là bước cuối (Gán Xe), cập nhật trạng thái Tour
        boolean isFinalStep = currentAssignmentType.equals("VEHICLE");
        if (isFinalStep) {
            updates.put("status", "DA_GAN_NHAN_VIEN");
        }

        DocumentReference tourRef = db.collection(TOURS_COLLECTION_PATH).document(tourId);

        tourRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Gán " + currentAssignmentType + " thành công.", Toast.LENGTH_SHORT).show();

                    if (isFinalStep) {
                        dismiss();
                        // ⭐ OPTIONAL: Gợi ý refresh danh sách Tour (Nếu TourAssignmentListFragment implement interface)
                    } else {
                        // Chuyển sang tab Phương tiện
                        tabLayout.getTabAt(1).select();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi cập nhật gán tài nguyên", e);
                    Toast.makeText(getContext(), "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}