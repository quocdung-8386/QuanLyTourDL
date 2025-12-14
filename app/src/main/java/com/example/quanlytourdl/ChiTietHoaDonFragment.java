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
    private MaterialButton btnSendInvoice;
    private MaterialButton btnThanhToan;

    // List & Adapter
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
        displayBasicInfo();
        loadRealtimeCustomerInfo();

        // Gọi hàm load dữ liệu
        loadOrderDetails();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        tvInvoiceCode = view.findViewById(R.id.tvInvoiceCode);
        tvCreatedDate = view.findViewById(R.id.tvCreatedDate);
        tvDueDate = view.findViewById(R.id.tvDueDate);
        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        tvStatusDetail = view.findViewById(R.id.tvStatusDetail);
        tvBigTotal = view.findViewById(R.id.tvBigTotal);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvCustomerDetail = view.findViewById(R.id.tvCustomerDetail);
        btnPrint = view.findViewById(R.id.btnPrint);
        btnPdf = view.findViewById(R.id.btnPdf);
        btnSendInvoice = view.findViewById(R.id.btnSendInvoice);
        btnThanhToan = view.findViewById(R.id.btnThanhToan);

        rvChiTietDonHang = view.findViewById(R.id.rvChiTietDonHang);
        rvChiTietDonHang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChiTietDonHang.setNestedScrollingEnabled(false);

        listChiTiet = new ArrayList<>();
        chiTietAdapter = new ChiTietAdapter(listChiTiet);
        rvChiTietDonHang.setAdapter(chiTietAdapter);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        btnThanhToan.setOnClickListener(v -> xuLyThanhToan());

        // Các nút demo
        btnSendInvoice.setOnClickListener(v -> Toast.makeText(getContext(), "Đang gửi...", Toast.LENGTH_SHORT).show());
        btnPrint.setOnClickListener(v -> Toast.makeText(getContext(), "Chức năng In đang phát triển", Toast.LENGTH_SHORT).show());
        btnPdf.setOnClickListener(v -> Toast.makeText(getContext(), "Xuất PDF thành công", Toast.LENGTH_SHORT).show());
    }

    private void displayBasicInfo() {
        if (hoaDonData != null) {
            String rawId = hoaDonData.getMaHoaDon();
            String shortId = "#" + (rawId.length() > 8 ? rawId.substring(0, 8).toUpperCase() : rawId);
            tvInvoiceCode.setText(shortId);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = new Date(); // Thực tế nên lấy ngày tạo từ hoaDonData nếu có
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
        if (hoaDonData.getTrangThai() == 1) {
            Toast.makeText(getContext(), "Hóa đơn này đã được thanh toán!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("HoaDon").document(hoaDonData.getMaHoaDon())
                .update("trangThai", 1)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    hoaDonData.setTrangThai(1);
                    updateStatusView(1);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                        tvCustomerDetail.setText((sdt != null ? sdt : "No phone") + " • " + (diaChi != null ? diaChi : "No address"));
                    } else {
                        tvCustomerDetail.setText("Khách vãng lai");
                    }
                });
    }

    // --- PHẦN ĐÃ ĐƯỢC SỬA LẠI ĐỂ HIỂN THỊ ĐÚNG DỮ LIỆU ---
    private void loadOrderDetails() {
        if (hoaDonData == null) return;

        // Vì khi Tạo Đơn, ta lưu thông tin trực tiếp vào collection "DonHang"
        // chứ không tạo collection con "ChiTietDonHang", nên phải get() từ "DonHang"
        db.collection("DonHang").document(hoaDonData.getMaHoaDon())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listChiTiet.clear();

                        // Lấy các trường dữ liệu
                        String tenTour = documentSnapshot.getString("tenTour");
                        Long slNguoiLon = documentSnapshot.getLong("soLuongNguoiLon");
                        Long slTreEm = documentSnapshot.getLong("soLuongTreEm");
                        Double tongTien = documentSnapshot.getDouble("tongTien");

                        // Xử lý null pointer
                        long slNL = (slNguoiLon != null) ? slNguoiLon : 0;
                        long slTE = (slTreEm != null) ? slTreEm : 0;

                        // 1. Dòng Người lớn
                        if (slNL > 0) {
                            ChiTietItem itemNL = new ChiTietItem();
                            itemNL.setTenDichVu("Vé người lớn - " + tenTour);
                            itemNL.setSoLuong((int) slNL);
                            // Set giá tiền tạm thời là 0 vì trong DonHang chỉ lưu Tổng Tiền
                            // Nếu muốn hiển thị đơn giá, cần lưu thêm đơn giá lúc tạo đơn
                            itemNL.setGiaTien(0);
                            listChiTiet.add(itemNL);
                        }

                        // 2. Dòng Trẻ em
                        if (slTE > 0) {
                            ChiTietItem itemTE = new ChiTietItem();
                            itemTE.setTenDichVu("Vé trẻ em - " + tenTour);
                            itemTE.setSoLuong((int) slTE);
                            itemTE.setGiaTien(0);
                            listChiTiet.add(itemTE);
                        }

                        chiTietAdapter.notifyDataSetChanged();

                        // Cập nhật lại tổng tiền chính xác từ Firestore
                        if (tongTien != null) {
                            updateUIForTotal(tongTien);
                        }
                    } else {
                        // Trường hợp không tìm thấy đơn (có thể do xóa nhầm trên db)
                        Toast.makeText(getContext(), "Không tìm thấy thông tin chi tiết!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ChiTietHoaDon", "Lỗi load detail: " + e.getMessage());
                });
    }

    private void updateUIForTotal(double totalAmount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String moneyString = formatter.format(totalAmount) + "đ";
        tvBigTotal.setText(moneyString);

        if (hoaDonData.getTrangThai() != 1) {
            btnThanhToan.setText("Thanh toán • " + moneyString);
        }
    }

    private void updateStatusView(int status) {
        switch (status) {
            case 1:
                tvStatusDetail.setText("ĐÃ THANH TOÁN");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_paid_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_paid_pill);
                btnThanhToan.setText("ĐÃ THANH TOÁN");
                btnThanhToan.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                btnThanhToan.setEnabled(false);
                break;
            case 2:
                tvStatusDetail.setText("ĐANG CHỜ");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_pending_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_pending);
                btnThanhToan.setEnabled(true);
                btnThanhToan.setBackgroundColor(android.graphics.Color.parseColor("#F97316"));
                break;
            case 3:
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