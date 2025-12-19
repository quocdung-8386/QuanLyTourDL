package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.CongNoAdapter;
import com.example.quanlytourdl.model.CongNoModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CongNoNccFragment extends Fragment {

    private RecyclerView rvCongNo;
    private TextView tvTongPhaiTra, tvQuaHan;
    private ImageButton btnBack, btnToolbarFilter, btnCalendar;
    private EditText edtSearch;
    private FloatingActionButton fabAddCongNo;

    private CongNoAdapter adapter;
    private List<CongNoModel> congNoList;
    private List<CongNoModel> filteredList;
    private FirebaseFirestore db;

    private String currentServiceType = "Tất cả";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cong_no_ncc, container, false);

        initViews(view);

        db = FirebaseFirestore.getInstance();
        congNoList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Khởi tạo Adapter với listener mở Dialog phê duyệt
        adapter = new CongNoAdapter(filteredList, getContext(), model -> {
            if (model.getId() != null) {
                PheDuyetCongNoDialogFragment dialog = PheDuyetCongNoDialogFragment.newInstance(model.getId());
                dialog.show(getChildFragmentManager(), "PheDuyetCongNo");
            } else {
                Toast.makeText(getContext(), "Không tìm thấy ID công nợ!", Toast.LENGTH_SHORT).show();
            }
        });

        rvCongNo.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCongNo.setAdapter(adapter);

        setupListeners();
        loadDataCongNo();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnToolbarFilter = view.findViewById(R.id.btnToolbarFilter);
        tvTongPhaiTra = view.findViewById(R.id.tvTongPhaiTra);
        tvQuaHan = view.findViewById(R.id.tvQuaHan);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnCalendar = view.findViewById(R.id.btnCalendar);
        rvCongNo = view.findViewById(R.id.rvCongNo);
        fabAddCongNo = view.findViewById(R.id.fabAddCongNo);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        btnToolbarFilter.setOnClickListener(this::showFilterPopupMenu);

        fabAddCongNo.setOnClickListener(v -> {
            TaoCongNoDialogFragment dialog = new TaoCongNoDialogFragment();
            dialog.show(getChildFragmentManager(), "TaoCongNo");
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showFilterPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenu().add("Tất cả");
        popup.getMenu().add("Thuê xe");
        popup.getMenu().add("Khách sạn");
        popup.getMenu().add("Nhà hàng");
        popup.getMenu().add("Vé tham quan");

        popup.setOnMenuItemClickListener(item -> {
            currentServiceType = item.getTitle().toString();
            applyFilterAndSearch();
            return true;
        });
        popup.show();
    }

    private void applyFilterAndSearch() {
        String query = edtSearch.getText().toString().toLowerCase().trim();
        filteredList.clear();

        for (CongNoModel item : congNoList) {
            boolean matchesService = currentServiceType.equals("Tất cả") ||
                    (item.getLoaiDichVu() != null && item.getLoaiDichVu().equalsIgnoreCase(currentServiceType));

            boolean matchesQuery = (item.getTenNcc() != null && item.getTenNcc().toLowerCase().contains(query)) ||
                    (item.getMaHopDong() != null && item.getMaHopDong().toLowerCase().contains(query));

            if (matchesService && matchesQuery) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadDataCongNo() {
        // Sử dụng addSnapshotListener để cập nhật realtime khi dữ liệu trên Firebase thay đổi
        db.collection("CongNo").addSnapshotListener((value, error) -> {
            if (error != null) return;

            if (value != null) {
                congNoList.clear();
                double totalPhaiTra = 0;
                double totalQuaHan = 0;

                for (DocumentSnapshot doc : value.getDocuments()) {
                    CongNoModel model = doc.toObject(CongNoModel.class);
                    if (model != null) {
                        model.setId(doc.getId());
                        congNoList.add(model);

                        // LOGIC SỬA ĐỔI TẠI ĐÂY:
                        // Chỉ cộng vào "Tổng phải trả" nếu trạng thái KHÔNG PHẢI là "Đã phê duyệt"
                        // (Bao gồm các mục "Chờ phê duyệt" và "Quá hạn")
                        String status = model.getTrangThai() != null ? model.getTrangThai() : "";

                        if (!status.equalsIgnoreCase("Đã phê duyệt") && !status.equalsIgnoreCase("Bị từ chối")) {
                            totalPhaiTra += model.getSoTien();
                        }

                        // Tính riêng cho mục Quá hạn
                        if (status.equalsIgnoreCase("Quá hạn")) {
                            totalQuaHan += model.getSoTien();
                        }
                    }
                }
                updateSummaryUI(totalPhaiTra, totalQuaHan);
                applyFilterAndSearch();
            }
        });
    }

    private void updateSummaryUI(double total, double overdue) {
        // Định dạng hiển thị tiền Việt Nam: 1.000.000 đ
        DecimalFormat df = new DecimalFormat("#,###");
        if (tvTongPhaiTra != null) {
            tvTongPhaiTra.setText(df.format(total) + " đ");
        }
        if (tvQuaHan != null) {
            tvQuaHan.setText(df.format(overdue) + " đ");
        }
    }
}