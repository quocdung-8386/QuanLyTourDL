package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class TaoHoSoKhachHangFragment extends Fragment {

    // 1. Khai báo thêm edtEmail
    private EditText edtDob, edtFullName, edtPhone, edtEmail;
    private ImageView btnBack;
    private Button btnCreateProfile;

    public TaoHoSoKhachHangFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tao_ho_so_khach_hang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        edtDob.setOnClickListener(v -> showDatePicker());

        btnCreateProfile.setOnClickListener(v -> {
            String name = edtFullName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String dob = edtDob.getText().toString().trim();

            // 2. Lấy dữ liệu Email
            String email = edtEmail.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên khách hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle result = new Bundle();
            result.putString("new_name", name);
            result.putString("new_phone", phone);
            result.putString("new_dob", dob);

            // 3. Đóng gói Email gửi đi
            result.putString("new_email", email);

            getParentFragmentManager().setFragmentResult("add_customer_request", result);
            getParentFragmentManager().popBackStack();
        });
    }

    private void initViews(View view) {
        edtDob = view.findViewById(R.id.edtDob);
        edtFullName = view.findViewById(R.id.edtFullName);
        edtPhone = view.findViewById(R.id.edtPhone);

        // 4. Ánh xạ (Đảm bảo trong XML có EditText id là edtEmail)
        edtEmail = view.findViewById(R.id.edtEmail);

        btnBack = view.findViewById(R.id.btnBack);
        btnCreateProfile = view.findViewById(R.id.btnCreateProfile);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    edtDob.setText(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }
}