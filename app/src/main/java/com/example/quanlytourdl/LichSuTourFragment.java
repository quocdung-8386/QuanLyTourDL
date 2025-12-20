package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.LichSuTourAdapter;
import com.example.quanlytourdl.model.DonDatTour;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class LichSuTourFragment extends Fragment {

    private RecyclerView rvAllHistory;
    private List<DonDatTour> fullList, displayList; // Đổi từ Map sang Model DonDatTour
    private LichSuTourAdapter adapter;
    private String customerId;
    private LinearLayout layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lich_su_tour, container, false);

        if (getArguments() != null) {
            customerId = getArguments().getString("customerId");
        }

        initViews(view);
        fetchData();
        return view;
    }

    private void initViews(View view) {
        // Nút quay lại
        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        rvAllHistory = view.findViewById(R.id.rvAllHistory);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        rvAllHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        fullList = new ArrayList<>();
        displayList = new ArrayList<>();

        // Thiết lập tìm kiếm
        EditText edtSearch = view.findViewById(R.id.edtSearchHistory);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchData() {
        if (customerId == null || customerId.isEmpty()) return;

        FirebaseFirestore.getInstance().collection("dattour")
                .whereEqualTo("maKhachHang", customerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Ánh xạ Document sang Object DonDatTour
                        DonDatTour item = doc.toObject(DonDatTour.class);
                        if (item != null) {
                            item.setId(doc.getId()); // Nếu bạn cần ID để sửa/xóa
                            fullList.add(item);
                        }
                    }
                    displayList.clear();
                    displayList.addAll(fullList);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseError", "Lỗi tải lịch sử: " + e.getMessage());
                    Toast.makeText(getContext(), "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Hàm lọc danh sách dựa trên tên Tour
     */
    private void filter(String text) {
        displayList.clear();
        if (text.isEmpty()) {
            displayList.addAll(fullList);
        } else {
            for (DonDatTour item : fullList) {
                // Kiểm tra tên tour (giả sử getter là getTenTourSnapshot)
                if (item.getTenTourSnapshot().toLowerCase().contains(text.toLowerCase())) {
                    displayList.add(item);
                }
            }
        }
        updateUI();
    }

    /**
     * Cập nhật giao diện khi dữ liệu thay đổi
     */
    private void updateUI() {
        if (displayList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvAllHistory.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvAllHistory.setVisibility(View.VISIBLE);
        }

        // Khởi tạo adapter với dữ liệu mới
        adapter = new LichSuTourAdapter(displayList, item -> {
            // Xử lý khi click vào 1 item: Mở chi tiết Tour hoặc đơn đặt
            Toast.makeText(getContext(), "Mở: " + item.getTenTourSnapshot(), Toast.LENGTH_SHORT).show();
            // Điều hướng tại đây:
            // openDetailFragment(item);
        });
        rvAllHistory.setAdapter(adapter);
    }
}