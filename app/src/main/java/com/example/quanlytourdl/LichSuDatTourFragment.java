package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.LichSuTourAdapter;
import com.example.quanlytourdl.model.DonDatTour;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LichSuDatTourFragment extends Fragment {

    private RecyclerView rvHistory;
    private LichSuTourAdapter adapter;
    private List<DonDatTour> listBooking;
    private ImageView btnBack;
    private TextView tvEmptyState; // Text hiển thị khi không có dữ liệu

    private String maKhachHang; // ID khách hàng cần xem
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lich_su_dat_tour, container, false);

        // 1. Ánh xạ View
        rvHistory = view.findViewById(R.id.rvTourHistory);
        btnBack = view.findViewById(R.id.btnBackHistory);
        // Bạn nên thêm 1 TextView id tvEmpty trong XML để hiện khi list rỗng
        // tvEmptyState = view.findViewById(R.id.tvEmpty); 

        // 2. Nhận ID Khách hàng từ Fragment trước truyền sang
        if (getArguments() != null) {
            maKhachHang = getArguments().getString("customer_id");
        }

        // 3. Setup RecyclerView
        listBooking = new ArrayList<>();
        adapter = new LichSuTourAdapter(listBooking);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(adapter);

        // 4. Khởi tạo Firestore và lấy dữ liệu
        db = FirebaseFirestore.getInstance();
        if (maKhachHang != null) {
            getBookingHistoryFromFirestore(maKhachHang);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy Mã KH", Toast.LENGTH_SHORT).show();
        }

        // 5. Nút Back
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });
        
        // --- (TÙY CHỌN) TEST DATA: Uncomment dòng dưới để tạo dữ liệu giả nếu DB rỗng ---
        // taoDuLieuGiaDeTest(maKhachHang);

        return view;
    }

    private void getBookingHistoryFromFirestore(String customerId) {
        // Query vào collection "dondattour"
        // Lọc theo trường "maKhachHang" bằng với ID đang xem
        db.collection("dondattour")
                .whereEqualTo("maKhachHang", customerId)
                // .orderBy("ngayDat", Query.Direction.DESCENDING) // Cần tạo Index trong Firestore mới dùng được sort
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Lỗi lấy lịch sử", error);
                        return;
                    }

                    if (value != null) {
                        listBooking.clear();
                        // Convert Documents thành Objects
                        List<DonDatTour> data = value.toObjects(DonDatTour.class);
                        listBooking.addAll(data);
                        
                        adapter.notifyDataSetChanged();
                        
                        // Hiển thị thông báo nếu không có đơn nào
                        if (listBooking.isEmpty()) {
                            Toast.makeText(getContext(), "Khách hàng này chưa đặt tour nào.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Hàm phụ trợ: Tạo nhanh dữ liệu mẫu để test giao diện
    private void taoDuLieuGiaDeTest(String cusId) {
        DonDatTour don1 = new DonDatTour(cusId, "tour1", "Khám phá Châu Âu", "T230815EU", "15/08/2023 - 25/08/2023", "2 người lớn", "Hoàn thành", new Date());
        DonDatTour don2 = new DonDatTour(cusId, "tour2", "Di sản Miền Trung", "T241220VN", "20/12/2024 - 25/12/2024", "4 người lớn", "Sắp diễn ra", new Date());
        
        db.collection("dondattour").add(don1);
        db.collection("dondattour").add(don2);
    }
}