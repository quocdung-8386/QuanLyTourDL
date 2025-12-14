package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiTietHoaDonFragment extends Fragment {

    private HoaDon hoaDonData;
    private FirebaseFirestore db;

    // Views
    private ImageView btnBack;
    private TextView tvInvoiceCode, tvCreatedDate, tvDueDate, tvPaymentMethod, tvStatusDetail;
    private TextView tvCustomerName, tvCustomerDetail;
    private TextView tvBigTotal;

    // Buttons
    private ImageButton btnPrint, btnPdf;
    private MaterialButton btnSendInvoice; // Nút gửi hóa đơn cũ
    private MaterialButton btnThanhToan;   // Nút thanh toán mới

    // List & Adapter
    private RecyclerView rvChiTietDonHang;
    private ChiTietAdapter chiTietAdapter;
    private List<ChiTietItem> listChiTiet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_hoa_don, container, false);

        db = FirebaseFirestore.getInstance();

        // Nhận dữ liệu từ Fragment trước
        if (getArguments() != null) {
            hoaDonData = (HoaDon) getArguments().getSerializable("hoa_don_data");
        }

        initViews(view);
        displayBasicInfo();
        loadRealtimeCustomerInfo();
        loadOrderDetails();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);

        // Thông tin hóa đơn
        tvInvoiceCode = view.findViewById(R.id.tvInvoiceCode);
        tvCreatedDate = view.findViewById(R.id.tvCreatedDate);
        tvDueDate = view.findViewById(R.id.tvDueDate);
        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        tvStatusDetail = view.findViewById(R.id.tvStatusDetail);
        tvBigTotal = view.findViewById(R.id.tvBigTotal);

        // Thông tin khách hàng
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvCustomerDetail = view.findViewById(R.id.tvCustomerDetail);

        // Nút chức năng
        btnPrint = view.findViewById(R.id.btnPrint);
        btnPdf = view.findViewById(R.id.btnPdf);
        btnSendInvoice = view.findViewById(R.id.btnSendInvoice);
        btnThanhToan = view.findViewById(R.id.btnThanhToan); // Nút thanh toán màu cam

        // RecyclerView
        rvChiTietDonHang = view.findViewById(R.id.rvChiTietDonHang);
        rvChiTietDonHang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChiTietDonHang.setNestedScrollingEnabled(false);

        listChiTiet = new ArrayList<>();
        chiTietAdapter = new ChiTietAdapter(listChiTiet);
        rvChiTietDonHang.setAdapter(chiTietAdapter);

        // --- SỰ KIỆN ---
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // Xử lý nút thanh toán
        btnThanhToan.setOnClickListener(v -> xuLyThanhToan());

        // Các nút khác (Demo)
        btnSendInvoice.setOnClickListener(v -> Toast.makeText(getContext(), "Đang gửi hóa đơn...", Toast.LENGTH_SHORT).show());
        btnPrint.setOnClickListener(v -> Toast.makeText(getContext(), "Chức năng In đang phát triển", Toast.LENGTH_SHORT).show());
        btnPdf.setOnClickListener(v -> Toast.makeText(getContext(), "Xuất PDF thành công", Toast.LENGTH_SHORT).show());
    }

    private void displayBasicInfo() {
        if (hoaDonData != null) {
            // Mã hóa đơn ngắn gọn
            String rawId = hoaDonData.getMaHoaDon();
            String shortId = "#" + (rawId.length() > 8 ? rawId.substring(0, 8).toUpperCase() : rawId);
            tvInvoiceCode.setText(shortId);

            // Ngày tháng
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = new Date();
            tvCreatedDate.setText(sdf.format(today));

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_YEAR, 3);
            tvDueDate.setText(sdf.format(calendar.getTime()));

            tvPaymentMethod.setText("Chuyển khoản");
            tvCustomerName.setText(hoaDonData.getTenKhachHang());
            tvCustomerDetail.setText("Đang tải...");

            updateUIForTotal(hoaDonData.getTongTien());
            updateStatusView(hoaDonData.getTrangThai());
        }
    }

    private void xuLyThanhToan() {
        if (hoaDonData == null) return;

        // Nếu đã thanh toán (status = 1) thì báo lỗi
        if (hoaDonData.getTrangThai() == 1) {
            Toast.makeText(getContext(), "Hóa đơn này đã được thanh toán!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật Firebase
        db.collection("HoaDon").document(hoaDonData.getMaHoaDon())
                .update("trangThai", 1) // 1: Đã thanh toán
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Xác nhận thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    hoaDonData.setTrangThai(1);
                    updateStatusView(1); // Cập nhật giao diện ngay
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRealtimeCustomerInfo() {
        if (hoaDonData == null || hoaDonData.getTenKhachHang() == null) return;

        db.collection("khachhang")
                .whereEqualTo("ten", hoaDonData.getTenKhachHang())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String sdt = doc.getString("sdt");
                        String diaChi = doc.getString("diaChi");
                        String detail = (sdt != null ? sdt : "No phone") + " • " + (diaChi != null ? diaChi : "No address");
                        tvCustomerDetail.setText(detail);
                    } else {
                        tvCustomerDetail.setText("Khách vãng lai");
                    }
                });
    }

    private void loadOrderDetails() {
        if (hoaDonData == null) return;

        db.collection("ChiTietDonHang")
                .whereEqualTo("maDonHang", hoaDonData.getMaHoaDon())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listChiTiet.clear();
                        double calculatedTotal = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                ChiTietItem item = document.toObject(ChiTietItem.class);
                                listChiTiet.add(item);

                                // Chuyển đổi String số lượng sang int để nhân được
                                String strSoLuong = item.getSoLuong();
                                int intSoLuong = 0;
                                try {
                                    if (strSoLuong != null && !strSoLuong.trim().isEmpty()) {
                                        intSoLuong = Integer.parseInt(strSoLuong);
                                    }
                                } catch (NumberFormatException e) {
                                    intSoLuong = 0; // Nếu dữ liệu lỗi thì coi như bằng 0
                                }

                                // TÍNH TỔNG TIỀN: Giá (double) x Số Lượng (int)
                                calculatedTotal += (item.getGiaTien() * intSoLuong);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        chiTietAdapter.notifyDataSetChanged();

                        // Cập nhật lại tổng tiền hiển thị theo danh sách chi tiết thực tế
                        updateUIForTotal(calculatedTotal);

                    } else {
                        Log.e("OrderDetails", "Error getting details", task.getException());
                    }
                });
    }

    private void updateUIForTotal(double totalAmount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String moneyString = formatter.format(totalAmount) + "đ";

        // 1. Cập nhật số to trên cùng
        tvBigTotal.setText(moneyString);

        // 2. Cập nhật chữ trên nút thanh toán (Nếu chưa thanh toán)
        if (hoaDonData.getTrangThai() != 1) {
            btnThanhToan.setText("Thanh toán • " + moneyString);
        }
    }

    private void updateStatusView(int status) {
        switch (status) {
            case 1: // Đã thanh toán
                tvStatusDetail.setText("ĐÃ THANH TOÁN");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_paid_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_paid_pill);

                // Khóa nút thanh toán và đổi màu xám
                btnThanhToan.setText("ĐÃ THANH TOÁN");
                btnThanhToan.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                btnThanhToan.setEnabled(false);
                break;
            case 2: // Đang chờ
                tvStatusDetail.setText("ĐANG CHỜ");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_pending_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_pending);

                // Mở khóa nút
                btnThanhToan.setEnabled(true);
                // Bạn cần định nghĩa màu R.color.orange_primary hoặc dùng Color.parseColor("#F97316")
                btnThanhToan.setBackgroundColor(android.graphics.Color.parseColor("#F97316"));
                break;
            case 3: // Đã hủy
                tvStatusDetail.setText("ĐÃ HỦY");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_overdue_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_overdue);

                btnThanhToan.setText("ĐƠN ĐÃ HỦY");
                btnThanhToan.setEnabled(false);
                break;
            default:
                tvStatusDetail.setText("KHÔNG XÁC ĐỊNH");
                break;
        }
    }
}