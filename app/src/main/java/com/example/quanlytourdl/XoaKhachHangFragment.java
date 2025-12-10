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

public class XoaKhachHangFragment extends Fragment {

    private ImageView btnBack;
    private Button btnConfirmDelete;
    private TextView tvName;
    private String customerName;

    public XoaKhachHangFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_xoa_khach_hang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack = view.findViewById(R.id.btnBack);
        btnConfirmDelete = view.findViewById(R.id.btnConfirmDelete);
        tvName = view.findViewById(R.id.tvName);

        if (getArguments() != null) {
            customerName = getArguments().getString("name");
            tvName.setText(customerName);
        }

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnConfirmDelete.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putBoolean("confirm_delete", true);

            // Gửi xác nhận về Fragment danh sách
            getParentFragmentManager().setFragmentResult("delete_customer_request", result);
            getParentFragmentManager().popBackStack();
        });
    }
}