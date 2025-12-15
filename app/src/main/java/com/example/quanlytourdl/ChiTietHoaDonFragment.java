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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiTietHoaDonFragment extends Fragment {

    private HoaDon hoaDonData;
    private FirebaseFirestore db;

    // Views
    private ImageView btnBack;
    private TextView tvInvoiceCode, tvCreatedDate, tvDueDate, tvPaymentMethod, tvStatusDetail;
    private TextView tvCustomerName, tvCustomerDetail, tvBigTotal;
    private MaterialButton btnThanhToan;
    private RecyclerView rvChiTietDonHang;
    private ChiTietAdapter chiTietAdapter;
    private List<ChiTietItem> listChiTiet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_hoa_don, container, false);
        db = FirebaseFirestore.getInstance();

        // Nhận dữ liệu từ màn hình trước
        if (getArguments() != null) {
            hoaDonData = (HoaDon) getArguments().getSerializable("hoa_don_data");
        }

        initViews(view);
        displayBasicInfo(); // Hiển thị thông tin có sẵn từ bundle
        loadOrderDetails(); // Load thêm thông tin chi tiết từ Firebase

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
        btnThanhToan = view.findViewById(R.id.btnThanhToan);

        // Setup RecyclerView
        rvChiTietDonHang = view.findViewById(R.id.rvChiTietDonHang);
        rvChiTietDonHang.setLayoutManager(new LinearLayoutManager(getContext()));
        listChiTiet = new ArrayList<>();
        chiTietAdapter = new ChiTietAdapter(listChiTiet);
        rvChiTietDonHang.setAdapter(chiTietAdapter);

        // Nút Back
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // Nút Thanh Toán
        btnThanhToan.setOnClickListener(v -> chuyenSangManHinhThanhToan());
    }

    private void displayBasicInfo() {
        if (hoaDonData != null) {
            // 1. Mã hóa đơn (Rút gọn cho đẹp)
            String rawId = hoaDonData.getMaHoaDon();
            String shortId = "#" + (rawId.length() > 8 ? rawId.substring(0, 8).toUpperCase() : rawId);
            tvInvoiceCode.setText(shortId);

            // 2. Tên khách hàng
            tvCustomerName.setText(hoaDonData.getTenKhachHang());

            // 3. Hiển thị tổng tiền
            updateUIForTotal(hoaDonData.getTongTien());

            // 4. Cập nhật trạng thái (Màu sắc, chữ)
            updateStatusView(hoaDonData.getTrangThai());
        }
    }

    private void loadOrderDetails() {
        if (hoaDonData == null) return;

        db.collection("DonHang").document(hoaDonData.getMaHoaDon())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listChiTiet.clear();

                        // --- A. LẤY DỮ LIỆU TỪ FIRESTORE ---
                        String tenTour = documentSnapshot.getString("tenTour");
                        Long slNguoiLon = documentSnapshot.getLong("soLuongNguoiLon");
                        Long slTreEm = documentSnapshot.getLong("soLuongTreEm");
                        Double tongTien = documentSnapshot.getDouble("tongTien");
                        String sdt = documentSnapshot.getString("sdtKhachHang");
                        String email = documentSnapshot.getString("emailKhachHang");
                        com.google.firebase.Timestamp timestampNgayDat = documentSnapshot.getTimestamp("ngayDat");

                        // --- B. XỬ LÝ NGÀY THÁNG (LOGIC 7 NGÀY) ---
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date dateCreated;

                        if (timestampNgayDat != null) {
                            dateCreated = timestampNgayDat.toDate();
                        } else {
                            // Nếu data lỗi/trống -> Lấy ngày hiện tại
                            dateCreated = new Date();
                        }

                        // 1. Hiển thị Ngày lập
                        tvCreatedDate.setText(sdf.format(dateCreated));

                        // 2. Tính Hạn thanh toán (Ngày lập + 7 ngày)
                        long sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000;
                        Date dateDue = new Date(dateCreated.getTime() + sevenDaysInMillis);
                        tvDueDate.setText(sdf.format(dateDue));


                        // --- C. HIỂN THỊ THÔNG TIN LIÊN HỆ ---
                        String contactInfo = "";
                        if (sdt != null && !sdt.isEmpty()) contactInfo += sdt;
                        if (email != null && !email.isEmpty()) {
                            if (!contactInfo.isEmpty()) contactInfo += " - ";
                            contactInfo += email;
                        }
                        if (contactInfo.isEmpty()) contactInfo = "Thông tin liên hệ đang cập nhật";
                        tvCustomerDetail.setText(contactInfo);


                        // --- D. HIỂN THỊ CHI TIẾT VÉ (Người lớn / Trẻ em) ---
                        long slNL = (slNguoiLon != null) ? slNguoiLon : 0;
                        long slTE = (slTreEm != null) ? slTreEm : 0;

                        if (slNL > 0) {
                            ChiTietItem itemNL = new ChiTietItem();
                            itemNL.setTenDichVu("Vé người lớn - " + tenTour);
                            itemNL.setSoLuong((int) slNL);
                            itemNL.setGiaTien(0); // Set 0 nếu chỉ muốn hiện tổng tiền ở dưới
                            listChiTiet.add(itemNL);
                        }

                        if (slTE > 0) {
                            ChiTietItem itemTE = new ChiTietItem();
                            itemTE.setTenDichVu("Vé trẻ em - " + tenTour);
                            itemTE.setSoLuong((int) slTE);
                            itemTE.setGiaTien(0);
                            listChiTiet.add(itemTE);
                        }

                        chiTietAdapter.notifyDataSetChanged();

                        // Cập nhật lại tổng tiền nếu trên DB có thay đổi so với lúc truyền qua
                        if (tongTien != null) {
                            updateUIForTotal(tongTien);
                            hoaDonData.setTongTien(tongTien);
                        }

                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy dữ liệu hóa đơn!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ChiTietHoaDon", "Lỗi load chi tiết: " + e.getMessage());
                    Toast.makeText(getContext(), "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStatusView(int status) {
        // Status: 1=Đã thanh toán, 2=Đang chờ, 3=Đã hủy
        switch (status) {
            case 1: // Đã thanh toán
                tvStatusDetail.setText("ĐÃ THANH TOÁN");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_paid_text));
                // Lưu ý: Cần đảm bảo file colors.xml có màu status_paid_text (xanh lá)
                // Hoặc dùng: android.graphics.Color.parseColor("#4CAF50")
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_paid_pill); // Đảm bảo có drawable này

                btnThanhToan.setText("ĐÃ THANH TOÁN");
                btnThanhToan.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                btnThanhToan.setEnabled(false);
                break;

            case 2: // Đang chờ
                tvStatusDetail.setText("ĐANG CHỜ");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_pending_text));
                // Hoặc dùng: android.graphics.Color.parseColor("#F97316") (Màu cam)
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_pending);

                btnThanhToan.setEnabled(true);
                btnThanhToan.setBackgroundColor(android.graphics.Color.parseColor("#F97316")); // Màu cam chủ đạo
                break;

            case 3: // Đã hủy
                tvStatusDetail.setText("ĐÃ HỦY");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_overdue_text));
                // Hoặc dùng: android.graphics.Color.RED
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_overdue);

                btnThanhToan.setText("ĐƠN ĐÃ HỦY");
                btnThanhToan.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                btnThanhToan.setEnabled(false);
                break;

            default:
                tvStatusDetail.setText("KHÔNG XÁC ĐỊNH");
                btnThanhToan.setEnabled(false);
                break;
        }
    }

    private void updateUIForTotal(double totalAmount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String moneyString = formatter.format(totalAmount) + "đ";
        tvBigTotal.setText(moneyString);

        // Chỉ cập nhật text nút bấm nếu đơn đang chờ thanh toán
        if (hoaDonData != null && hoaDonData.getTrangThai() != 1 && hoaDonData.getTrangThai() != 3) {
            btnThanhToan.setText("Thanh toán • " + moneyString);
        }
    }

    private void chuyenSangManHinhThanhToan() {
        if (hoaDonData == null) return;

        // Double check trạng thái
        if (hoaDonData.getTrangThai() == 1) {
            Toast.makeText(getContext(), "Đơn hàng đã được thanh toán!", Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentThanhToan fragmentThanhToan = new FragmentThanhToan();
        Bundle bundle = new Bundle();

        // Truyền dữ liệu sang Fragment Thanh Toán
        bundle.putString("maHoaDonHienTai", hoaDonData.getMaHoaDon());
        bundle.putString("tenTour", hoaDonData.getTenTour());
        bundle.putDouble("giaTour", hoaDonData.getTongTien());

        // Lấy ngày khởi hành từ view (hoặc lấy từ DB nếu có biến riêng)
        // Ở đây tạm dùng ngày lập hoặc chuỗi rỗng nếu chưa load xong
        bundle.putString("ngayKhoiHanh", tvCreatedDate.getText().toString());

        // Cờ đánh dấu: Đây là thanh toán hóa đơn cũ (từ màn hình lịch sử/chi tiết)
        bundle.putBoolean("isThanhToanHoaDonCu", true);

        fragmentThanhToan.setArguments(bundle);

        // Chuyển Fragment
        if (getParentFragmentManager() != null) {
            View containerView = (View) getView().getParent();
            int containerId = containerView.getId();

            getParentFragmentManager().beginTransaction()
                    .replace(containerId, fragmentThanhToan)
                    .addToBackStack(null)
                    .commit();
        }
    }
}