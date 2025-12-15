package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.quanlytourdl.model.HoaDon;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FragmentThanhToan extends Fragment {

    private TextView tvTenTour, tvNgayDi, tvSoNguoi, tvTamTinh, tvGiamGia, tvTongTien;
    private Button btnThanhToan;
    private ImageView btnBack;
    private FirebaseFirestore db;

    // Biến dữ liệu
    private String tenTour, ngayKhoiHanh;
    private double giaTour;
    private int soNguoi = 2;
    private double tongTienCuoiCung = 0;
    private String maHoaDonCu = null; // Nếu null => Đặt mới, Không null => Thanh toán nợ

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thanh_toan, container, false);
        db = FirebaseFirestore.getInstance();

        anhXaView(view);
        layDuLieuTuBundle();
        tinhToanHienThiGia();

        btnThanhToan.setOnClickListener(v -> xuLyThanhToanLuuCSDL());

        // Xử lý nút Back
        if(btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if(getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
            });
        }

        return view;
    }

    private void anhXaView(View view) {
        tvTenTour = view.findViewById(R.id.tvTenTourTT);
        tvNgayDi = view.findViewById(R.id.tvNgayKhoiHanhTT);
        tvSoNguoi = view.findViewById(R.id.tvSoNguoiTT);
        tvTamTinh = view.findViewById(R.id.tvTamTinh);
        tvGiamGia = view.findViewById(R.id.tvGiamGia);
        tvTongTien = view.findViewById(R.id.tvTongTienTT);
        btnThanhToan = view.findViewById(R.id.btnXacNhanThanhToan);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void layDuLieuTuBundle() {
        if (getArguments() != null) {
            tenTour = getArguments().getString("tenTour");
            giaTour = getArguments().getDouble("giaTour"); // Nếu là hóa đơn cũ, đây là tổng tiền
            ngayKhoiHanh = getArguments().getString("ngayKhoiHanh");
            soNguoi = getArguments().getInt("soNguoi", 1); // Mặc định 1

            // Lấy mã hóa đơn nếu thanh toán từ màn hình Chi tiết
            maHoaDonCu = getArguments().getString("maHoaDonHienTai");

            tvTenTour.setText(tenTour);
            tvNgayDi.setText("📅 " + ngayKhoiHanh);
            tvSoNguoi.setText("👥 " + soNguoi + " Hành khách");
        }
    }

    private void tinhToanHienThiGia() {
        Locale vn = new Locale("vi", "VN");
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(vn);

        if (maHoaDonCu != null) {
            // TRƯỜNG HỢP: Thanh toán hoá đơn cũ (Giá đã chốt, không tính lại)
            tongTienCuoiCung = giaTour;

            tvTamTinh.setText(currencyVN.format(tongTienCuoiCung));
            tvGiamGia.setText("0 đ"); // Hóa đơn cũ đã trừ khuyến mãi rồi
            tvTongTien.setText(currencyVN.format(tongTienCuoiCung));
        } else {
            // TRƯỜNG HỢP: Đặt Tour mới
            double tamTinh = giaTour * soNguoi;
            double giamGia = 0; // Logic giảm giá tùy bạn
            tongTienCuoiCung = tamTinh - giamGia;

            tvTamTinh.setText(currencyVN.format(tamTinh));
            tvGiamGia.setText("-" + currencyVN.format(giamGia));
            tvTongTien.setText(currencyVN.format(tongTienCuoiCung));
        }

        btnThanhToan.setText("Thanh toán " + currencyVN.format(tongTienCuoiCung));
    }
    // Trong file FragmentThanhToan.java

    private void xuLyThanhToanLuuCSDL() {
        btnThanhToan.setEnabled(false); // Chặn spam click
        btnThanhToan.setText("Đang xử lý...");

        String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        if (maHoaDonCu != null) {
            // --- CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG CŨ ---
            Map<String, Object> updateData = new HashMap<>();

            // QUAN TRỌNG: Lưu số 1 (Long/Int) để khớp với Model của bạn, tránh lỗi parse data
            updateData.put("trangThai", 1);
            updateData.put("ngayThanhToan", currentTime);
            updateData.put("phuongThucThanhToan", "Visa/MasterCard");

            db.collection("DonHang").document(maHoaDonCu)
                    .update(updateData)
                    .addOnSuccessListener(aVoid -> {
                        // Tạo object giả để hiển thị màn thành công
                        HoaDon hoaDonXong = new HoaDon();
                        hoaDonXong.setMaHoaDon(maHoaDonCu);
                        hoaDonXong.setTenTour(tenTour);
                        hoaDonXong.setTongTien(tongTienCuoiCung);
                        hoaDonXong.setNgayTao(currentTime);

                        chuyenManHinhThanhCong(hoaDonXong);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnThanhToan.setEnabled(true);
                        btnThanhToan.setText("Thử lại");
                    });

        } else {
            // --- LOGIC TẠO HOÁ ĐƠN MỚI (Giữ nguyên) ---
            HoaDon hoaDon = new HoaDon();
            hoaDon.setTenTour(tenTour);
            hoaDon.setTongTien(tongTienCuoiCung);
            hoaDon.setNgayTao(currentTime);
            hoaDon.setTrangThai(1);
            hoaDon.setTenKhachHang("Khách mới");

            db.collection("DonHang")
                    .add(hoaDon)
                    .addOnSuccessListener(documentReference -> {
                        String maMoi = documentReference.getId();
                        hoaDon.setMaHoaDon(maMoi);
                        // Update ngược lại ID vào document để dễ tìm kiếm
                        db.collection("DonHang").document(maMoi).update("maHoaDon", maMoi);

                        chuyenManHinhThanhCong(hoaDon);
                    })
                    .addOnFailureListener(e -> {
                        // Gọi màn hình thất bại
                        chuyenManHinhThatBai("NEW-ORDER");
                    });
        }
    }

    // Thêm hàm này vào cuối FragmentThanhToan.java để chuyển màn hình lỗi
    private void chuyenManHinhThatBai(String maDon) {
        FragmentThanhToanThatBai fragmentFail = new FragmentThanhToanThatBai();
        // ... code bundle ...

        if (getView() != null) {
            int containerId = ((View) getView().getParent()).getId(); // Lấy ID động

            getParentFragmentManager().beginTransaction()
                    .replace(containerId, fragmentFail)
                    .addToBackStack(null)
                    .commit();
        }
    }
    private void chuyenManHinhThanhCong(HoaDon hoaDon) {
        FragmentThanhToanThanhCong fragmentSuccess = new FragmentThanhToanThanhCong();
        Bundle bundle = new Bundle();
        bundle.putSerializable("object_hoadon", hoaDon);
        fragmentSuccess.setArguments(bundle);

        // --- SỬA LỖI Ở ĐÂY ---
        // Thay vì dùng R.id.fragment_container, ta lấy ID của view đang chứa Fragment hiện tại
        if (getView() != null && getParentFragmentManager() != null) {
            View containerView = (View) getView().getParent();
            int containerId = containerView.getId(); // Lấy ID thực tế (Dynamic ID)

            getParentFragmentManager().beginTransaction()
                    .replace(containerId, fragmentSuccess) // Dùng ID vừa lấy được
                    // .addToBackStack(null) // Không cần back lại trang nhập thẻ
                    .commit();
        }
    }
}