package com.example.quanlytourdl;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

import com.example.quanlytourdl.adapter.NhaCungCapAdapter;
import com.example.quanlytourdl.model.NhaCungCap;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class KinhDoanhFragment extends Fragment implements NhaCungCapAdapter.OnItemActionListener {

    private static final String TAG = "KinhDoanhFragment";

    private List<NhaCungCap> fullNhaCungCapList = new ArrayList<>();
    private List<NhaCungCap> nhaCungCapList = new ArrayList<>(); // Danh sách hiển thị

    private EditText etSearchProvider;
    private TextView tvProviderCount;
    private RecyclerView recyclerView;
    private NhaCungCapAdapter adapter;

    private FirebaseFirestore db;
    private CollectionReference nhaCungCapRef;
    private CollectionReference hopDongRef;
    private ListenerRegistration listenerRegistration;

    private String currentLoaiDichVuFilter = "Tất cả";

    // Menu IDs
    private static final int MENU_ID_SUPPLIER = 101;
    private static final int MENU_ID_TOUR_MANAGEMENT = 102;
    private static final int MENU_ID_TOUR_APPROVAL = 103;
    private static final int MENU_ID_CUSTOMER_LIST = 104;
    private static final int MENU_ID_GUIDE_VEHICLE_MANAGEMENT = 105;
    private static final int MENU_ID_PAYMENT_RECORD = 106;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        nhaCungCapRef = db.collection("NhaCungCap");
        hopDongRef = db.collection("HopDong");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kinhdoanh, container, false);

        initViews(view);
        setupRecyclerView();
        setupQuickActions(view);
        setupSearchFunctionality();

        return view;
    }

    private void initViews(View view) {
        etSearchProvider = view.findViewById(R.id.edit_search_provider);
        tvProviderCount = view.findViewById(R.id.text_provider_count);
        recyclerView = view.findViewById(R.id.recycler_providers);

        ImageButton btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer);
        if (btnMenuDrawer != null) btnMenuDrawer.setOnClickListener(this::showDrawerMenu);

        FloatingActionButton fabAddProvider = view.findViewById(R.id.fab_add_provider);
        if (fabAddProvider != null) {
            fabAddProvider.setOnClickListener(v -> performFragmentTransaction(new TaoNhaCungCapFragment(), "Mở Tạo NCC"));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NhaCungCapAdapter(requireContext(), nhaCungCapList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupQuickActions(View view) {
        View actionContract = view.findViewById(R.id.action_contract);
        View actionPerformance = view.findViewById(R.id.action_performance);
        View actionDebt = view.findViewById(R.id.action_debt); // Nút Công nợ mới
        View actionFilter = view.findViewById(R.id.action_filter); // Nút lọc cạnh ô tìm kiếm

        // 1. Quản lý hợp đồng
        if (actionContract != null) {
            setupQuickActionItem(actionContract, "Hợp đồng", R.drawable.ic_document);
            actionContract.setOnClickListener(v -> performFragmentTransaction(new QuanLyHopDongFragment(), "Mở Hợp đồng"));
        }

        // 2. Đánh giá hiệu suất
        if (actionPerformance != null) {
            setupQuickActionItem(actionPerformance, "Đánh giá", R.drawable.ic_assessment);
            actionPerformance.setOnClickListener(v -> showSupplierSelectionForEvaluation());
        }

        // 3. Công nợ (MỚI)
        if (actionDebt != null) {
            setupQuickActionItem(actionDebt, "Công nợ", R.drawable.ic_debt); // Đảm bảo bạn có ic_debt
            actionDebt.setOnClickListener(v -> performFragmentTransaction(new CongNoNccFragment(), "Mở Công nợ"));
        }

        // 4. Bộ lọc (Icon cạnh ô Search)
        if (actionFilter != null) {
            actionFilter.setOnClickListener(v -> showServiceTypeFilterDialog());
        }
    }

    private void setupQuickActionItem(View root, String titleStr, int iconRes) {
        TextView title = root.findViewById(R.id.action_title);
        ImageView icon = root.findViewById(R.id.action_icon);
        if (title != null) title.setText(titleStr);
        if (icon != null && iconRes != 0) icon.setImageResource(iconRes);
    }

    private void setupSearchFunctionality() {
        loadNhaCungCapData();
        if (etSearchProvider != null) {
            etSearchProvider.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterNhaCungCap(s.toString().trim());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadNhaCungCapData() {
        if (listenerRegistration != null) listenerRegistration.remove();
        listenerRegistration = nhaCungCapRef.orderBy("tenNhaCungCap", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    if (snapshots != null) {
                        fullNhaCungCapList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            NhaCungCap ncc = doc.toObject(NhaCungCap.class);
                            ncc.setMaNhaCungCap(doc.getId());
                            fullNhaCungCapList.add(ncc);
                        }
                        filterNhaCungCap(etSearchProvider != null ? etSearchProvider.getText().toString() : "");
                    }
                });
    }

    private void filterNhaCungCap(String searchQuery) {
        String query = searchQuery.toLowerCase(Locale.getDefault());
        List<NhaCungCap> filtered = fullNhaCungCapList.stream()
                .filter(ncc -> {
                    boolean matchesType = currentLoaiDichVuFilter.equals("Tất cả") ||
                            (ncc.getLoaiDichVu() != null && ncc.getLoaiDichVu().equalsIgnoreCase(currentLoaiDichVuFilter));

                    boolean matchesSearch = query.isEmpty() ||
                            (ncc.getTenNhaCungCap() != null && ncc.getTenNhaCungCap().toLowerCase().contains(query)) ||
                            (ncc.getMaNhaCungCap() != null && ncc.getMaNhaCungCap().toLowerCase().contains(query));

                    return matchesType && matchesSearch;
                }).collect(Collectors.toList());

        nhaCungCapList.clear();
        nhaCungCapList.addAll(filtered);
        adapter.notifyDataSetChanged();
        if (tvProviderCount != null) tvProviderCount.setText(nhaCungCapList.size() + " kết quả");
    }

    private void showServiceTypeFilterDialog() {
        final String[] serviceTypes = {"Tất cả", "Khách sạn", "Vận tải", "Ăn uống", "Vé tham quan", "Khác"};
        int checkedItem = 0;
        for (int i = 0; i < serviceTypes.length; i++) {
            if (serviceTypes[i].equals(currentLoaiDichVuFilter)) checkedItem = i;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn Loại Dịch Vụ")
                .setSingleChoiceItems(serviceTypes, checkedItem, (dialog, which) -> {
                    currentLoaiDichVuFilter = serviceTypes[which];
                    filterNhaCungCap(etSearchProvider.getText().toString());
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Lọc: " + currentLoaiDichVuFilter, Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void showSupplierSelectionForEvaluation() {
        if (fullNhaCungCapList.isEmpty()) return;
        String[] names = fullNhaCungCapList.stream().map(NhaCungCap::getTenNhaCungCap).toArray(String[]::new);
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn NCC để đánh giá")
                .setItems(names, (dialog, which) -> {
                    String id = fullNhaCungCapList.get(which).getMaNhaCungCap();
                    performFragmentTransaction(DanhGiaNhaCungCapFragment.newInstance(id), "Đánh giá NCC");
                }).show();
    }

    private void performFragmentTransaction(Fragment fragment, String log) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_content_frame, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        Log.d(TAG, log);
    }

    // --- OnItemActionListener Implementation ---
    @Override public void onEditClick(NhaCungCap ncc) { performFragmentTransaction(SuaNhaCungCapFragment.newInstance(ncc.getMaNhaCungCap()), "Sửa NCC"); }
    @Override public void onViewClick(NhaCungCap ncc) { performFragmentTransaction(DetailFragment.newInstance(ncc), "Chi tiết NCC"); }
    @Override public void onDeleteClick(NhaCungCap ncc) { showDeleteConfirmationDialog(ncc); }

    private void showDeleteConfirmationDialog(NhaCungCap ncc) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa " + ncc.getTenNhaCungCap() + " và các hợp đồng liên quan?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    hopDongRef.whereEqualTo("supplierId", ncc.getMaNhaCungCap()).get().addOnSuccessListener(snapshots -> {
                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot doc : snapshots) batch.delete(doc.getReference());
                        batch.delete(nhaCungCapRef.document(ncc.getMaNhaCungCap()));
                        batch.commit().addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã xóa thành công", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Hủy", null).show();
    }

    private void showDrawerMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(1, MENU_ID_SUPPLIER, 1, "Nhà Cung Cấp");
        popup.getMenu().add(1, MENU_ID_TOUR_MANAGEMENT, 2, "Quản Lý Tour");
        popup.getMenu().add(1, MENU_ID_TOUR_APPROVAL, 3, "Phê duyệt Tour");
        popup.getMenu().add(1, MENU_ID_CUSTOMER_LIST, 4, "Khách Hàng");
        popup.getMenu().add(1, MENU_ID_GUIDE_VEHICLE_MANAGEMENT, 5, "HDV & Phương tiện");
        popup.getMenu().add(1, MENU_ID_PAYMENT_RECORD, 6, "Quản Lý Đơn Hàng");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case MENU_ID_TOUR_MANAGEMENT: performFragmentTransaction(new QuanLyTourFragment(), "Tour"); break;
                case MENU_ID_TOUR_APPROVAL: performFragmentTransaction(new ChoPheDuyetTourFragment(), "Duyệt"); break;
                case MENU_ID_CUSTOMER_LIST: performFragmentTransaction(new DanhSachKhachHangFragment(), "Khách"); break;
                case MENU_ID_GUIDE_VEHICLE_MANAGEMENT: performFragmentTransaction(new QuanLyHdvPhuongTienFragment(), "HDV"); break;
                case MENU_ID_PAYMENT_RECORD: performFragmentTransaction(new QuanLyDonHangFragment(), "Đơn hàng"); break;
                default: Toast.makeText(getContext(), "Đang ở màn hình này", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    @Override public void onTerminateContract(NhaCungCap ncc) { Toast.makeText(getContext(), "Chấm dứt HĐ: " + ncc.getTenNhaCungCap(), Toast.LENGTH_SHORT).show(); }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}