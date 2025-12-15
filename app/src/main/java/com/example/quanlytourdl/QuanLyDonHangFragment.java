package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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

public class QuanLyDonHangFragment extends Fragment {

    // 1. Khai báo các View
    private ImageView btnBack;
    private CardView btnThongBao, cvTaoDonMoi, cvHoaDon;
    private TextView tvXemTatCa;
    private LinearLayout btnHoanTien, btnDoiChieu;

    // 2. Khai báo RecyclerView & Adapter
    private RecyclerView rvDonHangGanDay;
    private HoaDonAdapter hoaDonAdapter;
    private List<HoaDon> mListHoaDon;

    // 3. Khai báo Firestore
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quan_ly_don_hang, container, false);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        khoiTaoView(view);       // Ánh xạ view và cài đặt RecyclerView
        layDuLieuGanDay();       // Tải dữ liệu từ Firebase
        caiDatSuKien();          // Cài đặt sự kiện click cho các nút

        return view;
    }

    private void khoiTaoView(View view) {
        // Ánh xạ View từ XML
        btnBack = view.findViewById(R.id.btnBack);
        btnThongBao  = view.findViewById(R.id.btnThongBao);
        cvTaoDonMoi  = view.findViewById(R.id.cvTaoDonMoi);
        cvHoaDon     = view.findViewById(R.id.cvHoaDon);
        tvXemTatCa   = view.findViewById(R.id.tvXemTatCa);
        btnHoanTien  = view.findViewById(R.id.btnHoanTien);
        btnDoiChieu  = view.findViewById(R.id.btnDoiChieu);
        rvDonHangGanDay = view.findViewById(R.id.rvDonHangGanDay);

        // Cài đặt RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvDonHangGanDay.setLayoutManager(layoutManager);

        // Khởi tạo List
        mListHoaDon = new ArrayList<>();

        // Khởi tạo Adapter và xử lý sự kiện Click vào từng dòng đơn hàng
        hoaDonAdapter = new HoaDonAdapter(getContext(), mListHoaDon, new HoaDonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HoaDon hoaDon) {
                // --- LOGIC CHUYỂN SANG MÀN HÌNH CHI TIẾT ---
                ChiTietHoaDonFragment detailFragment = new ChiTietHoaDonFragment();

                // Đóng gói dữ liệu HoaDon vào Bundle
                Bundle bundle = new Bundle();
                bundle.putSerializable("hoa_don_data", hoaDon);
                detailFragment.setArguments(bundle);

                // Chuyển Fragment
                if (getParentFragmentManager() != null) {
                    int containerId = ((ViewGroup) getView().getParent()).getId();
                    getParentFragmentManager().beginTransaction()
                            .replace(containerId, detailFragment)
                            .addToBackStack(null) // Để user ấn Back quay lại được
                            .commit();
                }
            }
        });

        rvDonHangGanDay.setAdapter(hoaDonAdapter);
    }

    private void layDuLieuGanDay() {
        // Lấy 5 đơn hàng mới nhất từ Firestore
        db.collection("DonHang")
                .orderBy("ngayTao", Query.Direction.DESCENDING) // Sắp xếp giảm dần theo ngày
                .limit(5) // Giới hạn 5 đơn
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListHoaDon.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                HoaDon hd = new HoaDon();
                                hd.setMaHoaDon(document.getId());

                                // Map dữ liệu text/số
                                if (document.contains("tenKhachHang"))
                                    hd.setTenKhachHang(document.getString("tenKhachHang"));

                                if (document.contains("tongTien"))
                                    hd.setTongTien(document.getDouble("tongTien"));

                                if (document.contains("sdt")) {
                                    hd.setSdt(document.getString("sdt"));
                                } else {
                                    hd.setSdt(""); // Mặc định rỗng nếu không có
                                }

                                if (document.contains("diaChi")) {
                                    hd.setDiaChi(document.getString("diaChi"));
                                } else {
                                    hd.setDiaChi(""); // Mặc định rỗng nếu không có
                                }

                                if (document.contains("tenTour"))
                                    hd.setTenTour(document.getString("tenTour"));

                                // --- XỬ LÝ NGÀY THÁNG (TIMESTAMP -> STRING) ---
                                if (document.contains("ngayTao")) {
                                    Timestamp timestamp = document.getTimestamp("ngayTao");
                                    if (timestamp != null) {
                                        Date date = timestamp.toDate();
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        hd.setNgayTao(sdf.format(date));
                                    } else {
                                        hd.setNgayTao("---");
                                    }
                                } else {
                                    hd.setNgayTao("---");
                                }
                                // ----------------------------------------------

                                // Xử lý trạng thái (String -> int)
                                if (document.contains("trangThai")) {
                                    String statusStr = document.getString("trangThai");
                                    hd.setTrangThai(convertStatus(statusStr));
                                }

                                mListHoaDon.add(hd);
                            } catch (Exception e) {
                                Log.e("FirestoreError", "Lỗi parse data: " + e.getMessage());
                            }
                        }
                        hoaDonAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int convertStatus(String statusStr) {
        if (statusStr == null) return 2;
        switch (statusStr) {
            case "DA_THANH_TOAN": return 1;
            case "CHO_XU_LY": return 2;
            case "HUY": return 3;
            default: return 2;
        }
    }

    private void caiDatSuKien() {
        // 1. Nút Thông báo
        btnThongBao.setOnClickListener(v -> hienThiThongBao("Bạn vừa nhấn vào Thông báo!"));

        // 2. Nút Tạo đơn mới
        cvTaoDonMoi.setOnClickListener(v -> chuyenFragment(new TaoDonHangFragment()));

        // 3. Nút Quản lý hóa đơn
        cvHoaDon.setOnClickListener(v -> chuyenFragment(new DanhSachHoaDonFragment()));

        // 4. Nút Xem tất cả
        tvXemTatCa.setOnClickListener(v -> chuyenFragment(new DanhSachHoaDonFragment()));

        // 5. Nút Hoàn tiền (Admin)
        btnHoanTien.setOnClickListener(v -> chuyenFragment(new DanhSachHoanTienFragment()));

        // 6. Nút Đối chiếu (Admin)
        btnDoiChieu.setOnClickListener(v -> chuyenFragment(new DataReconciliationFragment()));
        // 7. Nút quay lại màn hình chính
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem có thể quay lại được không
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    // Nếu đây là Fragment đầu tiên, có thể đóng Activity hoặc không làm gì
                    // getActivity().onBackPressed(); // Bỏ comment dòng này nếu muốn thoát app/activity
                }
            }
        });
    }

    // Hàm phụ để chuyển Fragment cho gọn code
    private void chuyenFragment(Fragment fragment) {
        if (getParentFragmentManager() != null) {
            int containerId = ((ViewGroup) getView().getParent()).getId();
            getParentFragmentManager().beginTransaction()
                    .replace(containerId, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void hienThiThongBao(String noiDung) {
        Toast.makeText(getContext(), noiDung, Toast.LENGTH_SHORT).show();
    }
}