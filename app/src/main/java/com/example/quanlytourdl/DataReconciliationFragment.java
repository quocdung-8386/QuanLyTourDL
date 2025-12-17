package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class DataReconciliationFragment extends Fragment {

    private TextView tvDateFrom, tvDateTo;
    private Button btnStartReconcile;

    public DataReconciliationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout đã tạo ở trên
        return inflater.inflate(R.layout.fragment_doi_chieu_du_lieu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        tvDateFrom = view.findViewById(R.id.tvDateFrom);
        tvDateTo = view.findViewById(R.id.tvDateTo);
        btnStartReconcile = view.findViewById(R.id.btnStartReconcile);
        View btnBack = view.findViewById(R.id.btnBack);

        // Xử lý sự kiện nút Back
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Xử lý chọn ngày bắt đầu
        tvDateFrom.setOnClickListener(v -> showDatePicker(tvDateFrom));

        // Xử lý chọn ngày kết thúc
        tvDateTo.setOnClickListener(v -> showDatePicker(tvDateTo));

        // Xử lý nút Bắt đầu đối chiếu
        btnStartReconcile.setOnClickListener(v -> {
            // Logic xử lý khi bấm nút (ví dụ: gọi API, tính toán...)
            Toast.makeText(getContext(), "Đang bắt đầu đối chiếu dữ liệu...", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDatePicker(TextView targetTextView) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Format ngày: DD/MM/YYYY
                    String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    targetTextView.setText(date);
                },
                year, month, day);
        datePickerDialog.show();
    }
}