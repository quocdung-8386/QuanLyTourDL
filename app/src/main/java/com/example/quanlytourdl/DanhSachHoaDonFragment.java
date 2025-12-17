package com.example.quanlytourdl;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.HoaDonAdapter;
import com.example.quanlytourdl.model.HoaDon;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DanhSachHoaDonFragment extends Fragment {

    private RecyclerView rvHoaDon;
    private HoaDonAdapter adapter;
    private List<HoaDon> listHoaDonOriginal;
    private List<HoaDon> listHoaDonDisplay;

    private ImageView btnBack, btnAdd;
    private EditText edtSearch;
    private ImageButton btnFilter;
    private AppCompatButton btnFilterAll, btnFilterPaid, btnFilterUnpaid;

    private int currentStatusMode = 0;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danh_sach_hoa_don, container, false);
        db = FirebaseFirestore.getInstance();
        initViews(view);

        rvHoaDon.setLayoutManager(new LinearLayoutManager(getContext()));
        listHoaDonOriginal = new ArrayList<>();
        listHoaDonDisplay = new ArrayList<>();

        adapter = new HoaDonAdapter(getContext(), listHoaDonDisplay, new HoaDonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HoaDon hoaDon) {
                openDetailFragment(hoaDon);
            }
        });
        rvHoaDon.setAdapter(adapter);

        // Lưu ý: Đã bỏ loadDataFromFirestore() ở đây và chuyển sang onResume
        setupEvents();

        return view;
    }

    // --- SỬA 1: Thêm onResume để tự động cập nhật lại danh sách khi quay về ---
    @Override
    public void onResume() {
        super.onResume();
        loadDataFromFirestore();
    }

    private void initViews(View view) {
        rvHoaDon = view.findViewById(R.id.rvHoaDon);
        btnBack = view.findViewById(R.id.btnBack);
        btnAdd = view.findViewById(R.id.btnAdd);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterPaid = view.findViewById(R.id.btnFilterPaid);
        btnFilterUnpaid = view.findViewById(R.id.btnFilterUnpaid);
    }

    private void loadDataFromFirestore() {
        db.collection("DonHang")
                .orderBy("ngayTao", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listHoaDonOriginal.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                HoaDon hd = new HoaDon();
                                hd.setMaHoaDon(document.getId());

                                if (document.contains("tenKhachHang"))
                                    hd.setTenKhachHang(document.getString("tenKhachHang"));

                                if (document.contains("tongTien"))
                                    hd.setTongTien(document.getDouble("tongTien"));

                                if (document.contains("ngayTao")) {
                                    // Xử lý cả trường hợp String và Timestamp cho ngày tạo
                                    Object rawDate = document.get("ngayTao");
                                    if (rawDate instanceof Timestamp) {
                                        Date date = ((Timestamp) rawDate).toDate();
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        hd.setNgayTao(sdf.format(date));
                                    } else if (rawDate instanceof String) {
                                        hd.setNgayTao((String) rawDate);
                                    } else {
                                        hd.setNgayTao("---");
                                    }
                                }

                                // --- SỬA 2: Logic đọc trạng thái (QUAN TRỌNG) ---
                                // Xử lý để đọc được cả số 1 và chữ "DA_THANH_TOAN"
                                if (document.contains("trangThai")) {
                                    Object rawStatus = document.get("trangThai");
                                    int statusInt = 2; // Mặc định chờ

                                    if (rawStatus instanceof Number) {
                                        // Nếu là số (1, 2, 3) do FragmentThanhToan lưu
                                        statusInt = ((Number) rawStatus).intValue();
                                    } else if (rawStatus instanceof String) {
                                        // Nếu là chữ (dữ liệu cũ)
                                        statusInt = convertStatusStringToInt((String) rawStatus);
                                    }
                                    hd.setTrangThai(statusInt);
                                } else {
                                    hd.setTrangThai(2); // Không có trường này thì mặc định chờ
                                }

                                listHoaDonOriginal.add(hd);
                            } catch (Exception e) {
                                Log.e("DEBUG_APP", "Lỗi parse data: " + e.getMessage());
                            }
                        }
                        filterAndDisplayData(edtSearch.getText().toString());
                    } else {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int convertStatusStringToInt(String status) {
        if ("DA_THANH_TOAN".equals(status)) return 1;
        if ("CHO_XU_LY".equals(status)) return 2;
        if ("HUY".equals(status)) return 3;
        return 2;
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> { if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack(); });

        btnAdd.setOnClickListener(v -> {
            int containerId = ((ViewGroup) getView().getParent()).getId();
            getParentFragmentManager().beginTransaction()
                    .replace(containerId, new TaoDonHangFragment())
                    .addToBackStack(null)
                    .commit();
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplayData(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnFilterAll.setOnClickListener(v -> updateFilterMode(0, btnFilterAll));
        btnFilterPaid.setOnClickListener(v -> updateFilterMode(1, btnFilterPaid));
        btnFilterUnpaid.setOnClickListener(v -> updateFilterMode(2, btnFilterUnpaid));
    }

    private void updateFilterMode(int mode, AppCompatButton selectedBtn) {
        this.currentStatusMode = mode;
        resetButtonStyle(btnFilterAll);
        resetButtonStyle(btnFilterPaid);
        resetButtonStyle(btnFilterUnpaid);

        selectedBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0EA5E9")));
        selectedBtn.setTextColor(Color.WHITE);
        filterAndDisplayData(edtSearch.getText().toString());
    }

    private void resetButtonStyle(AppCompatButton btn) {
        btn.setBackgroundTintList(null);
        btn.setBackgroundResource(R.drawable.bg_search_input);
        btn.setTextColor(Color.parseColor("#4B5563"));
    }

    private void filterAndDisplayData(String keyword) {
        listHoaDonDisplay.clear();
        String key = keyword.toLowerCase().trim();

        for (HoaDon item : listHoaDonOriginal) {
            boolean matchKey = key.isEmpty() ||
                    (item.getMaHoaDon().toLowerCase().contains(key)) ||
                    (item.getTenKhachHang().toLowerCase().contains(key));

            boolean matchStatus = false;
            // Mode 1: Đã thanh toán (status == 1)
            // Mode 2: Chưa thanh toán (status != 1)
            if (currentStatusMode == 0) matchStatus = true;
            else if (currentStatusMode == 1) matchStatus = (item.getTrangThai() == 1);
            else if (currentStatusMode == 2) matchStatus = (item.getTrangThai() != 1);

            if (matchKey && matchStatus) {
                listHoaDonDisplay.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void openDetailFragment(HoaDon hoaDon) {
        ChiTietHoaDonFragment detailFragment = new ChiTietHoaDonFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("hoa_don_data", hoaDon);
        detailFragment.setArguments(bundle);

        int containerId = ((ViewGroup) getView().getParent()).getId();
        getParentFragmentManager().beginTransaction()
                .replace(containerId, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}