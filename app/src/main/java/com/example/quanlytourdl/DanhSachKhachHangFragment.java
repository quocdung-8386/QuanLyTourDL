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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.model.KhachHang;
import com.example.quanlytourdl.adapter.KhachHangAdapter;

import java.util.ArrayList;
import java.util.List;

public class DanhSachKhachHangFragment extends Fragment {

    private RecyclerView rvKhachHang;
    private KhachHangAdapter adapter;
    private List<KhachHang> listKhachHang;
    private ImageView btnAdd, btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_dskh, container, false);

        // 1. Ánh xạ View
        rvKhachHang = view.findViewById(R.id.rvKhachHang);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnBack = view.findViewById(R.id.btnBack);

        // 2. Khởi tạo dữ liệu
        khoiTaoDuLieu();

        // 3. Khởi tạo Adapter và xử lý sự kiện Click
        adapter = new KhachHangAdapter(listKhachHang, new KhachHangAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(KhachHang khachHang) {
                // Khi bấm vào một khách hàng, hàm này sẽ chạy
                moManHinhChiTiet(khachHang);
            }
        });

        // 4. Cấu hình RecyclerView
        rvKhachHang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKhachHang.setAdapter(adapter);

        // 5. Các sự kiện nút bấm khác
        btnAdd.setOnClickListener(v ->
                Toast.makeText(getContext(), "Chức năng thêm mới", Toast.LENGTH_SHORT).show()
        );

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    // Hàm chuyển sang màn hình chi tiết
    private void moManHinhChiTiet(KhachHang kh) {
        ChiTietKhachHangFragment fragmentChiTiet = new ChiTietKhachHangFragment();

        // Đóng gói dữ liệu vào Bundle để gửi đi
        Bundle bundle = new Bundle();
        bundle.putString("name", kh.getTen());
        bundle.putString("phone", kh.getSdt());
        bundle.putInt("avatar", kh.getAvatarResId());
        fragmentChiTiet.setArguments(bundle);

        // Thực hiện chuyển Fragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        // LƯU Ý: R.id.fragment_container là ID của FrameLayout trong Activity_Main của bạn
        // Bạn cần đảm bảo trong activity_main.xml có một FrameLayout với id này
        transaction.replace(R.id.fragment_container, fragmentChiTiet);
        transaction.addToBackStack(null); // Cho phép ấn nút Back để quay lại list
        transaction.commit();
    }

    private void khoiTaoDuLieu() {
        listKhachHang = new ArrayList<>();
        listKhachHang.add(new KhachHang("Nguyễn Văn A", "0987 654 321", R.drawable.ic_launcher_background));
        listKhachHang.add(new KhachHang("Trần Thị B", "0123 456 789", R.drawable.ic_launcher_background));
        listKhachHang.add(new KhachHang("Lê Văn C", "0912 345 678", R.drawable.ic_launcher_background));
        listKhachHang.add(new KhachHang("Phạm Thị D", "0905 112 233", R.drawable.ic_launcher_background));
        listKhachHang.add(new KhachHang("Hoàng Văn E", "0888 999 000", R.drawable.ic_launcher_background));
    }
}