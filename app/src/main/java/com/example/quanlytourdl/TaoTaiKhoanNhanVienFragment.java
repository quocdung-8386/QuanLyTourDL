package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TaoTaiKhoanNhanVienFragment extends Fragment {

    private Spinner spinnerDepartment;
    private Spinner spinnerRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tao_tai_khoan_nhan_vien, container, false);

        spinnerDepartment = view.findViewById(R.id.spinner_department);
        spinnerRole = view.findViewById(R.id.spinner_role);

        // Setup Department Spinner
        ArrayAdapter<CharSequence> departmentAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.phong_ban_array, android.R.layout.simple_spinner_item);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(departmentAdapter);
        spinnerDepartment.setSelection(departmentAdapter.getPosition("Chọn phòng ban"));


        // Setup Role Spinner
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.vai_tro_array, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        return view;
    }
}