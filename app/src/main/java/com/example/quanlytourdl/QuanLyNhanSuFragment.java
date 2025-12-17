package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class QuanLyNhanSuFragment extends Fragment {

    private RecyclerView rvNhanSu;
    private NhanSuAdapter nhanSuAdapter;
    private List<NhanVien> mListNhanVien;
    private FloatingActionButton fabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quan_ly_nhan_su, container, false);

        rvNhanSu = view.findViewById(R.id.rv_nhan_su);
        fabAdd = view.findViewById(R.id.fab_add);

        mListNhanVien = new ArrayList<>();
        // Add sample data
        mListNhanVien.add(new NhanVien("", "Nông Quốc Dũng", "NV001", "Kinh doanh"));
        mListNhanVien.add(new NhanVien("", "Vũ Hồng Quang", "NV002", "CSKH"));
        mListNhanVien.add(new NhanVien("", "Trương Văn Long", "NV003", null));


        nhanSuAdapter = new NhanSuAdapter(mListNhanVien);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvNhanSu.setLayoutManager(linearLayoutManager);
        rvNhanSu.setAdapter(nhanSuAdapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.main_content_frame, new TaoTaiKhoanNhanVienFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
}