package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class TaoDonHangFragment extends Fragment {

    // Khai báo view
    private ImageView btnBack;
    private EditText edtNgayDi, edtNgayVe;
    private RelativeLayout btnChonTour;
    private TextView tvSelectedTour;
    private Button btnSubmit;
    private ImageButton btnAddKhachMoi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tao_don_hang, container, false);
        
        // Ánh xạ
        initViews(view);
        
        // Sự kiện
        setupEvents();
        
        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        edtNgayDi = view.findViewById(R.id.edtNgayDi);
        edtNgayVe = view.findViewById(R.id.edtNgayVe);
        btnChonTour = view.findViewById(R.id.btnChonTour);
        tvSelectedTour = view.findViewById(R.id.tvSelectedTour);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnAddKhachMoi = view.findViewById(R.id.btnAddKhachMoi);
    }

    private void setupEvents() {
        // 1. Quay lại
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // 2. Chọn Ngày Đi
        edtNgayDi.setOnClickListener(v -> showDatePicker(edtNgayDi));

        // 3. Chọn Ngày Về
        edtNgayVe.setOnClickListener(v -> showDatePicker(edtNgayVe));

        // 4. Chọn Tour (Mở dialog hoặc chuyển màn hình chọn tour)
        btnChonTour.setOnClickListener(v -> {
            // Demo thay đổi text khi chọn
            Toast.makeText(getContext(), "Mở danh sách Tour...", Toast.LENGTH_SHORT).show();
            // Ví dụ sau khi chọn xong:
            // tvSelectedTour.setText("Tour Hà Giang 3N2Đ");
        });
        
        // 5. Thêm khách mới
        btnAddKhachMoi.setOnClickListener(v -> {
             Toast.makeText(getContext(), "Mở màn hình thêm khách hàng", Toast.LENGTH_SHORT).show();
        });

        // 6. Tạo đơn hàng
        btnSubmit.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đang xử lý tạo đơn...", Toast.LENGTH_SHORT).show();
            // Xử lý logic lưu vào Firestore ở đây
        });
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(getContext(), 
            (view, year, month, dayOfMonth) -> {
                String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                target.setText(date);
            }, y, m, d);
        dpd.show();
    }
}