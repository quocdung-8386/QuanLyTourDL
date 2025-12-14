package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class DanhSachHoanTienFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout xml vào Fragment
        View view = inflater.inflate(R.layout.fragment_danh_sach_hoan_tien, container, false);

        // Ánh xạ view
        LinearLayout btnXemChinhSach = view.findViewById(R.id.btnXemChinhSach);

        // Sự kiện click chuyển Fragment
        btnXemChinhSach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment chinhSachFragment = new ChinhSachFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                
                // Thay thế fragment hiện tại bằng fragment mới
                transaction.replace(R.id.fragment_container, chinhSachFragment);
                
                // Thêm vào back stack để khi ấn nút Back của điện thoại sẽ quay lại màn hình này
                transaction.addToBackStack(null);
                
                transaction.commit();
            }
        });

        return view;
    }
}