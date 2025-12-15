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

        if (getArguments() != null) {
            hoaDonData = (HoaDon) getArguments().getSerializable("hoa_don_data");
        }

        initViews(view);
        displayBasicInfo();
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
        btnThanhToan = view.findViewById(R.id.btnThanhToan);

        rvChiTietDonHang = view.findViewById(R.id.rvChiTietDonHang);
        rvChiTietDonHang.setLayoutManager(new LinearLayoutManager(getContext()));
        listChiTiet = new ArrayList<>();
        chiTietAdapter = new ChiTietAdapter(listChiTiet);
        rvChiTietDonHang.setAdapter(chiTietAdapter);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // SỰ KIỆN: Chuyển sang màn hình thanh toán
        btnThanhToan.setOnClickListener(v -> chuyenSangManHinhThanhToan());
    }

    private void chuyenSangManHinhThanhToan() {
        if (hoaDonData == null) return;

        // Kiểm tra nếu đã thanh toán thì không cho bấm
        if (hoaDonData.getTrangThai() == 1) {
            Toast.makeText(getContext(), "Đơn hàng này đã được thanh toán!", Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentThanhToan fragmentThanhToan = new FragmentThanhToan();
        Bundle bundle = new Bundle();

        // Truyền dữ liệu cần thiết sang Fragment Thanh Toán
        bundle.putString("maHoaDonHienTai", hoaDonData.getMaHoaDon()); // Quan trọng: Mã đơn để update
        bundle.putString("tenTour", hoaDonData.getTenTour());
        bundle.putDouble("giaTour", hoaDonData.getTongTien()); // Giá tour lúc này là tổng tiền hóa đơn
        bundle.putString("ngayKhoiHanh", tvCreatedDate.getText().toString());

        // Đánh dấu để FragmentThanhToan biết đây là thanh toán hóa đơn cũ
        bundle.putBoolean("isThanhToanHoaDonCu", true);

        fragmentThanhToan.setArguments(bundle);

        View containerView = (View) getView().getParent();
        int containerId = containerView.getId(); // Lấy ID thực tế của nó

        getParentFragmentManager().beginTransaction()
                .replace(containerId, fragmentThanhToan) // Dùng ID vừa lấy được
                .addToBackStack(null)
                .commit();
    }

    private void displayBasicInfo() {
        if (hoaDonData != null) {
            String rawId = hoaDonData.getMaHoaDon();
            String shortId = "#" + (rawId.length() > 8 ? rawId.substring(0, 8).toUpperCase() : rawId);
            tvInvoiceCode.setText(shortId);
            tvCustomerName.setText(hoaDonData.getTenKhachHang());

            // Format tiền
            DecimalFormat formatter = new DecimalFormat("#,###");
            String moneyString = formatter.format(hoaDonData.getTongTien()) + "đ";
            tvBigTotal.setText(moneyString);

            // Cập nhật trạng thái hiển thị
            updateStatusView(hoaDonData.getTrangThai());

            // Nếu chưa thanh toán thì nút hiển thị số tiền cần trả
            if (hoaDonData.getTrangThai() != 1) {
                btnThanhToan.setText("Thanh toán • " + moneyString);
            }
        }
    }

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
                        String ngayDi = documentSnapshot.getString("ngayKhoiHanh"); // Hoặc "ngayDi" tùy db bạn
                        String maTour = documentSnapshot.getString("maTour");
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
                            hoaDonData.setTongTien(tongTien);
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