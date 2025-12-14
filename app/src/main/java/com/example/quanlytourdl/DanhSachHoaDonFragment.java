package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.HoaDonAdapter;
import com.example.quanlytourdl.model.HoaDon;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DanhSachHoaDonFragment extends Fragment {

    private RecyclerView rvHoaDon;
    private HoaDonAdapter adapter;
    private List<HoaDon> listHoaDon;
    private ImageView btnBack, btnAdd;
    private FirebaseFirestore db; // 1. Khai báo Firestore

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danh_sach_hoa_don, container, false);

        db = FirebaseFirestore.getInstance(); // 2. Khởi tạo Firestore

        initViews(view);
        loadDataFromFirestore(); // 3. Gọi hàm tải dữ liệu thật
        setupEvents();

        return view;
    }

    private void initViews(View view) {
        rvHoaDon = view.findViewById(R.id.rvHoaDon);
        btnBack = view.findViewById(R.id.btnBack);
        btnAdd = view.findViewById(R.id.btnAdd);

        rvHoaDon.setLayoutManager(new LinearLayoutManager(getContext()));
        listHoaDon = new ArrayList<>();

        adapter = new HoaDonAdapter(getContext(), listHoaDon, new HoaDonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HoaDon hoaDon) {
                openDetailFragment(hoaDon);
            }
        });
        rvHoaDon.setAdapter(adapter);
    }

    // --- HÀM TẢI DỮ LIỆU TỪ FIREBASE ---
    private void loadDataFromFirestore() {
        // Lấy từ collection "DonHang" (hoặc "HoaDon" tùy cách bạn lưu lúc tạo đơn)
        // Ở đây giả sử bạn lưu vào "DonHang" lúc tạo
        db.collection("DonHang")
                .orderBy("ngayTao", Query.Direction.DESCENDING) // Sắp xếp mới nhất lên đầu
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listHoaDon.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Map dữ liệu từ Firestore sang Model HoaDon
                                // Lưu ý: Cần đảm bảo tên trường trên Firestore khớp với tên biến trong Model
                                HoaDon hd = new HoaDon();
                                hd.setMaHoaDon(document.getId());

                                // Lấy các trường an toàn (tránh null)
                                if (document.contains("tenKhachHang"))
                                    hd.setTenKhachHang(document.getString("tenKhachHang"));
                                if (document.contains("tongTien"))
                                    hd.setTongTien(document.getLong("tongTien"));
                                if (document.contains("ngayTao")) {
                                    // Xử lý ngày tháng (Object -> String) nếu cần
                                    // Ở đây tạm lấy ngày dạng chuỗi nếu bạn lưu chuỗi, hoặc convert Date
                                    hd.setNgayTao("---");
                                }
                                if (document.contains("trangThai")) {
                                    // Chuyển đổi trạng thái string "CHO_XU_LY" sang int (1,2,3) để Adapter hiển thị màu
                                    String statusStr = document.getString("trangThai");
                                    hd.setTrangThai(convertStatus(statusStr));
                                }

                                listHoaDon.add(hd);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hàm phụ: Chuyển đổi trạng thái chữ sang số (cho Adapter hiển thị màu)
    private int convertStatus(String statusStr) {
        if (statusStr == null) return 2; // Mặc định chờ
        switch (statusStr) {
            case "DA_THANH_TOAN": return 1;
            case "CHO_XU_LY": return 2;
            case "HUY": return 3;
            default: return 2;
        }
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        btnAdd.setOnClickListener(v -> {
            int containerId = ((ViewGroup) getView().getParent()).getId();
            getParentFragmentManager().beginTransaction()
                    .replace(containerId, new TaoDonHangFragment())
                    .addToBackStack(null)
                    .commit();
        });
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