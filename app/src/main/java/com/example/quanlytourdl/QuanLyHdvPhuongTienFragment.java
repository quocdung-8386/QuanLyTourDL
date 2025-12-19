package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.QuanLyHdvPhuongTienAdapter;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.example.quanlytourdl.model.Guide;
import com.example.quanlytourdl.model.Vehicle;

public class QuanLyHdvPhuongTienFragment extends Fragment
        implements QuanLyHdvPhuongTienAdapter.OnItemActionListener {

    private static final String TAG = "QLHDVPT_Fragment";

    private static final String TYPE_HDV = "Hướng dẫn viên";
    private static final String TYPE_PT = "Phương tiện";

    private static final String COLLECTION_HDV = "tour_guides";
    private static final String COLLECTION_PT = "phuongtien";

    private static final String STATUS_ALL = "Tất cả";
    private static final String CHIP_SAN_SANG = "Sẵn sàng";
    private static final String CHIP_TAM_NGHI = "Tạm nghỉ";

    private FirebaseFirestore db;
    private ListenerRegistration currentListener = null;

    private TabLayout tabLayout;
    private FloatingActionButton btnAddItem;
    private ChipGroup chipGroupStatus;
    private RecyclerView recyclerView;
    private TextInputEditText etSearch;
    private TextView tvGoodReview, tvReviewCount, tvUpcomingExpiry, tvUpcomingExpiryUnit;

    private QuanLyHdvPhuongTienAdapter adapter;
    private int chipTatCaId;
    private String currentType = TYPE_HDV;
    private String currentStatusFilter = STATUS_ALL;
    private List<Object> fullList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quan_ly_hdv_phuong_tien, container, false);
        mapViews(view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new QuanLyHdvPhuongTienAdapter(currentType.equals(TYPE_HDV), this);
        recyclerView.setAdapter(adapter);

        if (chipTatCaId != 0) chipGroupStatus.check(chipTatCaId);

        setupListeners(view);
        loadList(currentType);
        return view;
    }

    private void mapViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout_hdv_pt);
        btnAddItem = view.findViewById(R.id.btn_add_item);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        recyclerView = view.findViewById(R.id.recycler_hdv_phuong_tien);
        tvGoodReview = view.findViewById(R.id.text_good_review);
        tvReviewCount = view.findViewById(R.id.text_review_count);
        tvUpcomingExpiry = view.findViewById(R.id.text_upcoming_expiry);
        tvUpcomingExpiryUnit = view.findViewById(R.id.text_upcoming_expiry_unit);

        TextInputLayout inputLayout = view.findViewById(R.id.input_search);
        if (inputLayout != null) etSearch = (TextInputEditText) inputLayout.getEditText();

        View chipTatCa = view.findViewById(R.id.chip_tat_ca);
        if (chipTatCa != null) chipTatCaId = chipTatCa.getId();
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.btn_back_quan_ly).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentType = tab.getText().toString();
                adapter = new QuanLyHdvPhuongTienAdapter(currentType.equals(TYPE_HDV), QuanLyHdvPhuongTienFragment.this);
                recyclerView.setAdapter(adapter);

                if (chipTatCaId != 0) chipGroupStatus.check(chipTatCaId);
                currentStatusFilter = STATUS_ALL;
                if (etSearch != null) etSearch.setText("");

                loadList(currentType);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                currentStatusFilter = getStatusFromChipId(checkedIds.get(0));
                filterDataList(etSearch != null ? etSearch.getText().toString().trim() : "");
            } else if (chipTatCaId != 0) {
                chipGroupStatus.check(chipTatCaId);
            }
        });

        btnAddItem.setOnClickListener(v -> handleAddItemClick());

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterDataList(s.toString().trim());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadList(String type) {
        if (currentListener != null) currentListener.remove();

        String collectionName = type.equals(TYPE_HDV) ? COLLECTION_HDV : COLLECTION_PT;
        String orderByField = type.equals(TYPE_HDV) ? "fullName" : "bienSoXe";

        currentListener = db.collection(collectionName)
                .orderBy(orderByField, Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    fullList.clear();
                    int readyCount = 0; // Đếm số lượng Sẵn sàng
                    int offCount = 0;   // Đếm số lượng Tạm nghỉ

                    for (QueryDocumentSnapshot doc : value) {
                        if (type.equals(TYPE_HDV)) {
                            Guide guide = doc.toObject(Guide.class);
                            guide.setId(doc.getId());
                            fullList.add(guide);
                            // HDV: isApproved == Sẵn sàng
                            if (guide.isApproved()) readyCount++;
                            else offCount++;
                        } else {
                            Vehicle vehicle = doc.toObject(Vehicle.class);
                            vehicle.setId(doc.getId());
                            fullList.add(vehicle);
                            // Xe: "Hoạt động tốt" == Sẵn sàng
                            if ("Hoạt động tốt".equalsIgnoreCase(vehicle.getTinhTrangBaoDuong())) readyCount++;
                            else offCount++;
                        }
                    }

                    // Cập nhật thẻ Summary: readyCount dùng để tính %, offCount hiển thị số lượng tạm nghỉ
                    updateSummaryCards(type, fullList.size(), offCount, readyCount);
                    filterDataList(etSearch != null ? etSearch.getText().toString().trim() : "");
                });
    }

    private void filterDataList(String query) {
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault());

        List<Object> filtered = fullList.stream()
                .filter(item -> {
                    // 1. Lọc theo Chip Status
                    if (!currentStatusFilter.equals(STATUS_ALL)) {
                        if (item instanceof Guide) {
                            Guide g = (Guide) item;
                            boolean isReady = g.isApproved();
                            if (currentStatusFilter.equals(CHIP_SAN_SANG) && !isReady) return false;
                            if (currentStatusFilter.equals(CHIP_TAM_NGHI) && isReady) return false;
                        } else {
                            Vehicle v = (Vehicle) item;
                            String status = v.getTinhTrangBaoDuong();
                            boolean isReady = "Hoạt động tốt".equalsIgnoreCase(status);
                            if (currentStatusFilter.equals(CHIP_SAN_SANG) && !isReady) return false;
                            if (currentStatusFilter.equals(CHIP_TAM_NGHI) && isReady) return false;
                        }
                    }

                    // 2. Lọc theo Search Query
                    if (lowerCaseQuery.isEmpty()) return true;
                    if (item instanceof Guide) {
                        Guide g = (Guide) item;
                        return (g.getFullName() != null && g.getFullName().toLowerCase().contains(lowerCaseQuery)) ||
                                (g.getGuideCode() != null && g.getGuideCode().toLowerCase().contains(lowerCaseQuery));
                    } else {
                        Vehicle v = (Vehicle) item;
                        return (v.getBienSoXe() != null && v.getBienSoXe().toLowerCase().contains(lowerCaseQuery)) ||
                                (v.getHangXe() != null && v.getHangXe().toLowerCase().contains(lowerCaseQuery));
                    }
                })
                .collect(Collectors.toList());

        adapter.updateData(filtered);
    }

    private String getStatusFromChipId(int chipId) {
        if (chipId == R.id.chip_san_sang) return CHIP_SAN_SANG;
        if (chipId == R.id.chip_tam_nghi) return CHIP_TAM_NGHI;
        return STATUS_ALL;
    }

    private void updateSummaryCards(String type, int total, int offStatus, int ready) {
        if (total == 0) {
            tvGoodReview.setText("0%");
            tvReviewCount.setText("0 mục");
        } else {
            // Hiển thị % Sẵn sàng phục vụ
            int percent = (ready * 100) / total;
            tvGoodReview.setText(percent + "%");
            tvReviewCount.setText(total + (type.equals(TYPE_HDV) ? " HDV" : " xe"));
        }

        // Cập nhật số lượng và nhãn cho mục "Tạm nghỉ"
        tvUpcomingExpiry.setText(String.valueOf(offStatus));
        tvUpcomingExpiryUnit.setText(type.equals(TYPE_HDV) ? "tạm nghỉ" : "cần bảo trì");
    }

    private void handleAddItemClick() {
        Fragment target = currentType.equals(TYPE_HDV) ? new AddHdvFragment() : new AddPhuongTienFragment();
        openFragment(target, "Thêm mới " + currentType);
    }

    private void openFragment(Fragment fragment, String log) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.main_content_frame, fragment)
                .addToBackStack(null)
                .commit();
        Log.d(TAG, log);
    }

    @Override public void onEditItem(Object item) {
        String id = (item instanceof Guide) ? ((Guide) item).getId() : ((Vehicle) item).getId();
        Fragment f = (item instanceof Guide) ? SuaHdvFragment.newInstance(id) : SuaPhuongTienFragment.newInstance(id);
        openFragment(f, "Edit item ID: " + id);
    }

    @Override public void onDeleteItem(Object item) {
        String id = (item instanceof Guide) ? ((Guide) item).getId() : ((Vehicle) item).getId();
        String name = (item instanceof Guide) ? ((Guide) item).getFullName() : ((Vehicle) item).getBienSoXe();
        String coll = (item instanceof Guide) ? COLLECTION_HDV : COLLECTION_PT;

        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa " + name + "?")
                .setPositiveButton("Xóa", (d, w) -> {
                    db.collection(coll).document(id).delete().addOnSuccessListener(a ->
                            Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null).show();
    }

    @Override public void onViewDetails(Object item) {
        Fragment f = DetailFragment.newInstance((Serializable) item);
        openFragment(f, "View details");
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (currentListener != null) currentListener.remove();
    }
}