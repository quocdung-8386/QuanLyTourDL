package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.HoaDonAdapter;
import com.example.quanlytourdl.model.HoaDon;

import java.util.ArrayList;
import java.util.List;

public class DanhSachHoaDonFragment extends Fragment {

    private RecyclerView rvHoaDon;
    private HoaDonAdapter adapter;
    private List<HoaDon> listHoaDon;
    private ImageView btnBack, btnAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danh_sach_hoa_don, container, false);

        initViews(view);
        setupData();
        setupEvents();

        return view;
    }

    private void initViews(View view) {
        rvHoaDon = view.findViewById(R.id.rvHoaDon);
        btnBack = view.findViewById(R.id.btnBack);
        btnAdd = view.findViewById(R.id.btnAdd);

        // Cài đặt RecyclerView
        rvHoaDon.setLayoutManager(new LinearLayoutManager(getContext()));
        listHoaDon = new ArrayList<>();
        adapter = new HoaDonAdapter(getContext(), listHoaDon);
        rvHoaDon.setAdapter(adapter);
    }

    private void setupData() {
        // Dữ liệu giả lập (giống trong hình)
        // 1: Đã thanh toán, 2: Chờ thanh toán, 3: Quá hạn
        listHoaDon.add(new HoaDon("#INV-2023001", "24/10/2023", "Nguyen Van A", 5200000, 1));
        listHoaDon.add(new HoaDon("#INV-2023002", "23/10/2023", "Công ty TNHH ABC", 12500000, 2));
        listHoaDon.add(new HoaDon("#INV-2023003", "20/10/2023", "Le Thi B", 850000, 3));
        listHoaDon.add(new HoaDon("#INV-2023004", "18/10/2023", "Tran Van C", 3100000, 1));
        
        adapter.notifyDataSetChanged();
    }

    private void setupEvents() {
        // Nút Back: Quay lại màn hình Quản lý đơn hàng (hoặc màn hình trước đó)
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Nút Add: Mở màn hình tạo hóa đơn mới (nếu có)
        btnAdd.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tạo hóa đơn mới", Toast.LENGTH_SHORT).show();
        });
    }
}