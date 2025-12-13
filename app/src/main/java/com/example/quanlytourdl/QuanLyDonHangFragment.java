package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class QuanLyDonHangFragment extends Fragment {

    // 1. Khai báo các thành phần giao diện
    private CardView btnThongBao;       // Nút chuông thông báo
    private CardView cvTaoDonMoi;       // Thẻ xanh: Tạo đơn mới
    private CardView cvHoaDon;          // Thẻ trắng: Hóa đơn
    private TextView tvXemTatCa;        // Nút chữ: Xem tất cả
    private CardView btnHoanTien;       // Nút quản trị: Hoàn tiền/Hủy
    private CardView btnDoiChieu;       // Nút quản trị: Đối chiếu

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Liên kết với file giao diện XML
        View view = inflater.inflate(R.layout.fragment_quan_ly_don_hang, container, false);

        // Gọi hàm ánh xạ và cài đặt sự kiện
        khoiTaoView(view);
        caiDatSuKien();

        return view;
    }

    // Hàm tìm và gán view từ XML vào biến Java
    private void khoiTaoView(View view) {
        btnThongBao  = view.findViewById(R.id.btnThongBao);
        cvTaoDonMoi  = view.findViewById(R.id.cvTaoDonMoi);
        cvHoaDon     = view.findViewById(R.id.cvHoaDon);
        tvXemTatCa   = view.findViewById(R.id.tvXemTatCa);

        // Các nút trong phần quản trị
        btnHoanTien  = view.findViewById(R.id.btnHoanTien);
        btnDoiChieu  = view.findViewById(R.id.btnDoiChieu);
    }

    // Hàm bắt sự kiện khi người dùng nhấn vào nút
    private void caiDatSuKien() {

        // Sự kiện: Nhấn nút Thông báo
        btnThongBao.setOnClickListener(v -> {
            hienThiThongBao("Bạn vừa nhấn vào Thông báo!");
        });

        // Sự kiện: Nhấn thẻ Tạo đơn hàng mới
        cvTaoDonMoi.setOnClickListener(v -> {
            // Lấy ID của View cha đang chứa Fragment hiện tại (KinhDoanhFragment)
            // Cách này giúp tránh lỗi "No view found"
            int currentContainerId = ((ViewGroup) getView().getParent()).getId();

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(currentContainerId, new TaoDonHangFragment())
                        .addToBackStack(null) // Để ấn nút Back sẽ quay lại được
                        .commit();
            }
        });

        // Sự kiện: Nhấn thẻ Hóa đơn
        cvHoaDon.setOnClickListener(v -> {
            // Lấy ID container hiện tại
            int currentContainerId = ((ViewGroup) getView().getParent()).getId();

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(currentContainerId, new DanhSachHoaDonFragment())
                        .addToBackStack(null) // Cho phép quay lại bằng nút Back
                        .commit();
            }
        });
        // Sự kiện: Nhấn Xem tất cả đơn hàng
        tvXemTatCa.setOnClickListener(v -> {
            hienThiThongBao("Xem tất cả đơn hàng gần đây");
        });

        // Sự kiện Admin: Hoàn tiền / Hủy bỏ
        btnHoanTien.setOnClickListener(v -> {
            hienThiThongBao("Truy cập công cụ Hoàn tiền / Hủy bỏ");
        });

        // Sự kiện Admin: Đối chiếu ngân hàng
        btnDoiChieu.setOnClickListener(v -> {
            hienThiThongBao("Truy cập công cụ Đối chiếu");
        });
    }

    // Hàm phụ trợ để hiện thông báo nhanh (Toast)
    private void hienThiThongBao(String noiDung) {
        Toast.makeText(getContext(), noiDung, Toast.LENGTH_SHORT).show();
    }
}