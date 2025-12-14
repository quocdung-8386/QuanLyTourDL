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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.ChiTietAdapter;
import com.example.quanlytourdl.model.ChiTietItem;
import com.example.quanlytourdl.model.HoaDon;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDonFragment extends Fragment {

    private HoaDon hoaDonData;
    private FirebaseFirestore db;

    private ImageView btnBack;
    private TextView tvInvoiceCode, tvCreatedDate, tvStatusDetail;
    private TextView tvCustomerName, tvCustomerDetail;
    private TextView tvBigTotal;

    private RecyclerView rvChiTietDonHang;
    private ChiTietAdapter chiTietAdapter;
    private List<ChiTietItem> listChiTiet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_hoa_don, container, false);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            hoaDonData = (HoaDon) getArguments().getSerializable("hoa_don_data");
        }

        initViews(view);

        // 1. Hiển thị thông tin cơ bản có sẵn trong đơn hàng
        displayBasicInfo();

        // 2. Tải thêm thông tin khách hàng (SĐT, Địa chỉ) từ bảng khachhang
        loadRealtimeCustomerInfo();

        // 3. Tải danh sách tour/vé
        loadOrderDetails();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        tvInvoiceCode = view.findViewById(R.id.tvInvoiceCode);
        tvCreatedDate = view.findViewById(R.id.tvCreatedDate);
        tvStatusDetail = view.findViewById(R.id.tvStatusDetail);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvCustomerDetail = view.findViewById(R.id.tvCustomerDetail);
        tvBigTotal = view.findViewById(R.id.tvBigTotal);

        rvChiTietDonHang = view.findViewById(R.id.rvChiTietDonHang);
        rvChiTietDonHang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChiTietDonHang.setNestedScrollingEnabled(false);

        listChiTiet = new ArrayList<>();
        chiTietAdapter = new ChiTietAdapter(listChiTiet);
        rvChiTietDonHang.setAdapter(chiTietAdapter);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });
    }

    private void displayBasicInfo() {
        if (hoaDonData != null) {
            String rawId = hoaDonData.getMaHoaDon();
            String shortId = "#" + (rawId.length() > 8 ? rawId.substring(0, 8).toUpperCase() : rawId);
            tvInvoiceCode.setText(shortId);

            tvCreatedDate.setText(hoaDonData.getNgayTao());
            tvCustomerName.setText(hoaDonData.getTenKhachHang());

            // Tạm thời hiện "Đang tải..." trong lúc chờ lấy dữ liệu từ bảng Khách hàng
            tvCustomerDetail.setText("Đang tải thông tin liên hệ...");

            DecimalFormat formatter = new DecimalFormat("#,###");
            tvBigTotal.setText(formatter.format(hoaDonData.getTongTien()) + "đ");

            updateStatusView(hoaDonData.getTrangThai());
        }
    }

    // --- HÀM MỚI: TÌM KHÁCH HÀNG BÊN BẢNG "khachhang" ---
    private void loadRealtimeCustomerInfo() {
        if (hoaDonData == null || hoaDonData.getTenKhachHang() == null) return;

        // Tìm trong collection "khachhang" xem ai có tên trùng với tên trong hóa đơn
        db.collection("khachhang")
                .whereEqualTo("ten", hoaDonData.getTenKhachHang())
                .limit(1) // Chỉ lấy 1 người đầu tiên tìm thấy
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Tìm thấy khách hàng -> Lấy SĐT và Địa chỉ
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        String sdt = doc.getString("sdt");
                        String diaChi = doc.getString("diaChi");

                        // Xử lý null
                        String sdtHienThi = (sdt != null && !sdt.isEmpty()) ? sdt : "Không có SĐT";
                        String diaChiHienThi = (diaChi != null && !diaChi.isEmpty()) ? diaChi : "Chưa cập nhật địa chỉ";

                        // Cập nhật giao diện
                        tvCustomerDetail.setText(sdtHienThi + " • " + diaChiHienThi);
                    } else {
                        // Không tìm thấy khách hàng này trong hệ thống
                        tvCustomerDetail.setText("Khách vãng lai • Chưa có hồ sơ");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CustomerInfo", "Lỗi lấy thông tin khách: " + e.getMessage());
                    tvCustomerDetail.setText("Lỗi tải thông tin");
                });
    }

    private void updateStatusView(int status) {
        switch (status) {
            case 1:
                tvStatusDetail.setText("ĐÃ THANH TOÁN");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_paid_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_paid);
                break;
            case 2:
                tvStatusDetail.setText("ĐANG CHỜ");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_pending_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case 3:
                tvStatusDetail.setText("ĐÃ HỦY");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_overdue_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_overdue);
                break;
        }
    }

    private void loadOrderDetails() {
        if (hoaDonData == null) return;

        db.collection("ChiTietDonHang")
                .whereEqualTo("maDonHang", hoaDonData.getMaHoaDon()) // ID dài
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listChiTiet.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                ChiTietItem item = document.toObject(ChiTietItem.class);
                                listChiTiet.add(item);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        chiTietAdapter.notifyDataSetChanged();
                    }
                });
    }
}