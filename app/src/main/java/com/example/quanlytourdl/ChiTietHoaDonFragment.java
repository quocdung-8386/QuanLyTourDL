package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.quanlytourdl.model.HoaDon;
import java.text.DecimalFormat;

public class ChiTietHoaDonFragment extends Fragment {

    private HoaDon hoaDonData;
    private ImageView btnBack;
    private TextView tvInvoiceCode, tvCustomerName, tvTotal, tvStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_hoa_don, container, false);

        // Nhận dữ liệu
        if (getArguments() != null) {
            hoaDonData = (HoaDon) getArguments().getSerializable("hoa_don_data");
        }

        btnBack = view.findViewById(R.id.btnBack);
        tvInvoiceCode = view.findViewById(R.id.tvInvoiceCode);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvTotal = view.findViewById(R.id.tvBigTotal); // Ví dụ ánh xạ 1 số trường chính

        if (hoaDonData != null) {
            tvInvoiceCode.setText(hoaDonData.getMaHoaDon());
            tvCustomerName.setText(hoaDonData.getTenKhachHang());

            DecimalFormat formatter = new DecimalFormat("#,###");
            tvTotal.setText(formatter.format(hoaDonData.getTongTien()) + "đ");
        }

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        return view;
    }
}