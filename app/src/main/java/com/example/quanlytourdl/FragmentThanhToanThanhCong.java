package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.quanlytourdl.model.HoaDon;
import java.text.NumberFormat;
import java.util.Locale;

public class FragmentThanhToanThanhCong extends Fragment {

    private TextView tvTenTour, tvTongTien, tvMaGiaoDich, tvThoiGian, tvPhuongThuc;
    private Button btnVeTrangChu;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thanh_toan_thanh_cong, container, false);

        tvTenTour = view.findViewById(R.id.tvTenTourSuccess);
        tvTongTien = view.findViewById(R.id.tvTongTienSuccess);
        tvMaGiaoDich = view.findViewById(R.id.tvMaGiaoDich);
        tvThoiGian = view.findViewById(R.id.tvThoiGianSuccess);
        tvPhuongThuc = view.findViewById(R.id.tvPhuongThucSuccess);
        btnVeTrangChu = view.findViewById(R.id.btnVeTrangChu);

        if (getArguments() != null) {
            // Lấy object HoaDon ra
            HoaDon hoaDon = (HoaDon) getArguments().getSerializable("object_hoadon");
            String phuongThuc = getArguments().getString("phuongThuc");

            if (hoaDon != null) {
                tvTenTour.setText(hoaDon.getTenTour());
                
                // Format tiền
                Locale vn = new Locale("vi", "VN");
                tvTongTien.setText(NumberFormat.getCurrencyInstance(vn).format(hoaDon.getTongTien()));
                
                // Format mã đơn hàng (lấy 8 ký tự đầu cho gọn)
                String shortID = hoaDon.getMaHoaDon();
                if(shortID != null && shortID.length() > 8) shortID = shortID.substring(0, 8).toUpperCase();
                tvMaGiaoDich.setText("TRX-" + shortID);
                
                tvThoiGian.setText(hoaDon.getNgayTao());
                tvPhuongThuc.setText(phuongThuc);
            }
        }

        btnVeTrangChu.setOnClickListener(v -> {
            // Quay về màn hình chính, xoá backstack để không back lại màn hình thanh toán
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });

        return view;
    }
}