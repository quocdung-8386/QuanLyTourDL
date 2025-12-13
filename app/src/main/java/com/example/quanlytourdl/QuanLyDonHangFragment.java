package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout; // <--- Cần Import cái này
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class QuanLyDonHangFragment extends Fragment {

    // 1. Khai báo các thành phần giao diện
    private CardView btnThongBao;
    private CardView cvTaoDonMoi;
    private CardView cvHoaDon;
    private TextView tvXemTatCa;

    // --- SỬA Ở ĐÂY ---
    // Trong XML là <LinearLayout>, nên ở đây phải là LinearLayout
    private LinearLayout btnHoanTien;
    private LinearLayout btnDoiChieu;
    // -----------------

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quan_ly_don_hang, container, false);
        khoiTaoView(view);
        caiDatSuKien();
        return view;
    }

    private void khoiTaoView(View view) {
        btnThongBao  = view.findViewById(R.id.btnThongBao);
        cvTaoDonMoi  = view.findViewById(R.id.cvTaoDonMoi);
        cvHoaDon     = view.findViewById(R.id.cvHoaDon);
        tvXemTatCa   = view.findViewById(R.id.tvXemTatCa);

        // Các nút trong phần quản trị (Kiểu LinearLayout)
        btnHoanTien  = view.findViewById(R.id.btnHoanTien);
        btnDoiChieu  = view.findViewById(R.id.btnDoiChieu);
    }

    private void caiDatSuKien() {
        // Sự kiện: Nhấn nút Thông báo
        btnThongBao.setOnClickListener(v -> hienThiThongBao("Bạn vừa nhấn vào Thông báo!"));

        // Sự kiện: Nhấn thẻ Tạo đơn hàng mới
        cvTaoDonMoi.setOnClickListener(v -> {
            int currentContainerId = ((ViewGroup) getView().getParent()).getId();
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(currentContainerId, new TaoDonHangFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Sự kiện: Nhấn thẻ Hóa đơn
        cvHoaDon.setOnClickListener(v -> {
            int currentContainerId = ((ViewGroup) getView().getParent()).getId();
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(currentContainerId, new DanhSachHoaDonFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Sự kiện: Nhấn Xem tất cả
        tvXemTatCa.setOnClickListener(v -> hienThiThongBao("Xem tất cả đơn hàng gần đây"));

        // --- CÁC NÚT LINEAR LAYOUT VẪN BẮT SỰ KIỆN CLICK BÌNH THƯỜNG ---

        // Sự kiện Admin: Hoàn tiền / Hủy bỏ
        btnHoanTien.setOnClickListener(v -> hienThiThongBao("Truy cập công cụ Hoàn tiền / Hủy bỏ"));

        // Sự kiện Admin: Đối chiếu ngân hàng
        btnDoiChieu.setOnClickListener(v -> hienThiThongBao("Truy cập công cụ Đối chiếu"));
    }

    private void hienThiThongBao(String noiDung) {
        Toast.makeText(getContext(), noiDung, Toast.LENGTH_SHORT).show();
    }
}