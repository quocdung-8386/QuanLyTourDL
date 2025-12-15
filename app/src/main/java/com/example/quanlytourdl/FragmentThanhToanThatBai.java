package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragmentThanhToanThatBai extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thanh_toan_that_bai, container, false);

        TextView tvMaDon = view.findViewById(R.id.tvMaDonHangFail);
        TextView tvThoiGian = view.findViewById(R.id.tvThoiGianFail);
        TextView tvTongTien = view.findViewById(R.id.tvTongTienFail);
        Button btnThuLai = view.findViewById(R.id.btnThuLai);
        Button btnPhuongThucKhac = view.findViewById(R.id.btnPhuongThucKhac);
        ImageView btnClose = view.findViewById(R.id.btnCloseFail);

        if (getArguments() != null) {
            double tongTien = getArguments().getDouble("tongTien", 0);
            String maHoaDon = getArguments().getString("maHoaDon", "Unknown");

            // Format tiền
            Locale vn = new Locale("vi", "VN");
            tvTongTien.setText(NumberFormat.getCurrencyInstance(vn).format(tongTien));

            // Format mã đơn
            String displayId = maHoaDon.length() > 8 ? maHoaDon.substring(0, 8).toUpperCase() : maHoaDon;
            tvMaDon.setText("#" + displayId);

            // Thời gian hiện tại
            String timeStamp = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(new Date());
            tvThoiGian.setText(timeStamp);
        }

        // --- Xử lý sự kiện ---

        // 1. Thử lại / Chọn phương thức khác -> Quay lại màn hình Thanh Toán
        View.OnClickListener retryAction = v -> getParentFragmentManager().popBackStack();
        btnThuLai.setOnClickListener(retryAction);
        btnPhuongThucKhac.setOnClickListener(retryAction);

        // 2. Đóng -> Về màn hình chính (Huỷ hết luồng thanh toán)
        btnClose.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });

        return view;
    }
}