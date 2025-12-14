package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiTietHoaDonFragment extends Fragment {

    private HoaDon hoaDonData;
    private FirebaseFirestore db;

    private ImageView btnBack;
    // Khai báo đầy đủ các TextView hiển thị thông tin
    private TextView tvInvoiceCode, tvCreatedDate, tvDueDate, tvPaymentMethod, tvStatusDetail;
    private TextView tvCustomerName, tvCustomerDetail;
    private TextView tvBigTotal;

    // Nút chức năng (nếu cần xử lý sự kiện click sau này)
    private ImageButton btnPrint, btnPdf;
    private View btnSendInvoice;

    private RecyclerView rvChiTietDonHang;
    private ChiTietAdapter chiTietAdapter;
    private List<ChiTietItem> listChiTiet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_hoa_don, container, false);

        db = FirebaseFirestore.getInstance();

        // Nhận dữ liệu từ màn hình trước truyền qua
        if (getArguments() != null) {
            hoaDonData = (HoaDon) getArguments().getSerializable("hoa_don_data");
        }

        initViews(view);

        // 1. Hiển thị thông tin cơ bản (Mã, Ngày, Hạn, Tổng tiền...)
        displayBasicInfo();

        // 2. Tải thông tin chi tiết khách hàng (SĐT, Địa chỉ) từ Firebase
        loadRealtimeCustomerInfo();

        // 3. Tải danh sách tour/vé chi tiết từ Firebase
        loadOrderDetails();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);

        // Nhóm thông tin hóa đơn
        tvInvoiceCode = view.findViewById(R.id.tvInvoiceCode);
        tvCreatedDate = view.findViewById(R.id.tvCreatedDate);
        tvDueDate = view.findViewById(R.id.tvDueDate);           // Mới
        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod); // Mới

        tvStatusDetail = view.findViewById(R.id.tvStatusDetail);
        tvBigTotal = view.findViewById(R.id.tvBigTotal);

        // Nhóm thông tin khách hàng
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvCustomerDetail = view.findViewById(R.id.tvCustomerDetail);

        // Nhóm nút bấm
        btnPrint = view.findViewById(R.id.btnPrint);
        btnPdf = view.findViewById(R.id.btnPdf);
        btnSendInvoice = view.findViewById(R.id.btnSendInvoice);

        // RecyclerView danh sách chi tiết
        rvChiTietDonHang = view.findViewById(R.id.rvChiTietDonHang);
        rvChiTietDonHang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChiTietDonHang.setNestedScrollingEnabled(false); // Để scroll mượt trong NestedScrollView

        listChiTiet = new ArrayList<>();
        chiTietAdapter = new ChiTietAdapter(listChiTiet);
        rvChiTietDonHang.setAdapter(chiTietAdapter);

        // Sự kiện quay lại
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });
    }

    private void displayBasicInfo() {
        if (hoaDonData != null) {
            // --- XỬ LÝ MÃ HÓA ĐƠN ---
            String rawId = hoaDonData.getMaHoaDon();
            // Cắt chuỗi lấy 8 ký tự đầu và in hoa để làm mã ngắn gọn (ví dụ: #A1B2C3D4)
            String shortId = "#" + (rawId.length() > 8 ? rawId.substring(0, 8).toUpperCase() : rawId);
            tvInvoiceCode.setText(shortId);


            // --- XỬ LÝ NGÀY GIỜ (REAL-TIME) ---
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = new Date(); // Lấy thời gian thực tại hiện tại

            // 1. Gán ngày lập là ngày hôm nay
            tvCreatedDate.setText(sdf.format(today));

            // 2. Tự động tính hạn thanh toán (Ví dụ: +3 ngày kể từ hôm nay)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_YEAR, 3); // Cộng thêm 3 ngày
            tvDueDate.setText(sdf.format(calendar.getTime()));


            // --- CÁC THÔNG TIN KHÁC ---
            tvPaymentMethod.setText("Chuyển khoản"); // Có thể thay đổi tùy logic của bạn

            tvCustomerName.setText(hoaDonData.getTenKhachHang());
            tvCustomerDetail.setText("Đang tải thông tin liên hệ..."); // Placeholder trước khi load xong

            DecimalFormat formatter = new DecimalFormat("#,###");
            tvBigTotal.setText(formatter.format(hoaDonData.getTongTien()) + "đ");

            updateStatusView(hoaDonData.getTrangThai());
        }
    }

    private void loadRealtimeCustomerInfo() {
        if (hoaDonData == null || hoaDonData.getTenKhachHang() == null) return;

        // Tìm trong collection "khachhang" dựa vào tên
        db.collection("khachhang")
                .whereEqualTo("ten", hoaDonData.getTenKhachHang())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        String sdt = doc.getString("sdt");
                        String diaChi = doc.getString("diaChi");

                        // Kiểm tra null để hiển thị đẹp hơn
                        String sdtHienThi = (sdt != null && !sdt.isEmpty()) ? sdt : "Không có SĐT";
                        String diaChiHienThi = (diaChi != null && !diaChi.isEmpty()) ? diaChi : "Địa chỉ chưa cập nhật";

                        tvCustomerDetail.setText(sdtHienThi + " • " + diaChiHienThi);
                    } else {
                        tvCustomerDetail.setText("Khách vãng lai • Chưa có hồ sơ");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CustomerInfo", "Lỗi lấy thông tin khách: " + e.getMessage());
                    tvCustomerDetail.setText("Lỗi tải thông tin");
                });
    }

    private void updateStatusView(int status) {
        // Cập nhật màu sắc và text trạng thái dựa trên mã status (1, 2, 3...)
        switch (status) {
            case 1: // Đã thanh toán
                tvStatusDetail.setText("ĐÃ THANH TOÁN");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_paid_text));
                // Lưu ý: Đảm bảo bạn có định nghĩa màu trong colors.xml hoặc dùng Color.parseColor("#10B981")
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_paid_pill); // Đảm bảo drawable này tồn tại
                break;
            case 2: // Đang chờ
                tvStatusDetail.setText("ĐANG CHỜ");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_pending_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case 3: // Đã hủy / Quá hạn
                tvStatusDetail.setText("ĐÃ HỦY");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_overdue_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_overdue);
                break;
            default:
                tvStatusDetail.setText("TRẠNG THÁI KHÁC");
                break;
        }
    }

    private void loadOrderDetails() {
        if (hoaDonData == null) return;

        // Lấy danh sách chi tiết tour/vé dựa trên mã đơn hàng gốc
        db.collection("ChiTietDonHang")
                .whereEqualTo("maDonHang", hoaDonData.getMaHoaDon())
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
                    } else {
                        Log.e("OrderDetails", "Error getting details", task.getException());
                    }
                });
    }
}