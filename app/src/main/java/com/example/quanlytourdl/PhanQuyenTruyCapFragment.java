package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PhanQuyenTruyCapFragment extends Fragment {

    private RecyclerView rvPermissions;
    private EmployeePermissionAdapter adapter;
    private List<EmployeePermission> employeePermissions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phan_quyen_truy_cap, container, false);

        rvPermissions = view.findViewById(R.id.rv_permissions);
        rvPermissions.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample Data
        employeePermissions = new ArrayList<>();

        List<Permission> permissions1 = new ArrayList<>();
        permissions1.add(new Permission("Quản lý Tour", "Tạo, sửa, xóa tour và lịch trình", true));
        permissions1.add(new Permission("Quản lý Khách hàng", "Truy cập và chỉnh sửa thông tin khách", true));
        permissions1.add(new Permission("Báo cáo & Thống kê", "Xem các báo cáo kinh doanh, CSKH", true));
        employeePermissions.add(new EmployeePermission("Nông Quốc Dũng", "Quản lý", permissions1));

        List<Permission> permissions2 = new ArrayList<>();
        permissions2.add(new Permission("Quản lý Tour", "Tạo, sửa, xóa tour và lịch trình", false));
        permissions2.add(new Permission("Quản lý Khách hàng", "Truy cập và chỉnh sửa thông tin khách", true));
        permissions2.add(new Permission("Báo cáo & Thống kê", "Xem các báo cáo kinh doanh, CSKH", false));
        employeePermissions.add(new EmployeePermission("Trương Văn Long", "Nhân viên", permissions2));

        adapter = new EmployeePermissionAdapter(getContext(), employeePermissions);
        rvPermissions.setAdapter(adapter);

        return view;
    }
}