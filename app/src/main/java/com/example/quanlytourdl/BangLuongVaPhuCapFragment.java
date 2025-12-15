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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BangLuongVaPhuCapFragment extends Fragment {

    private RecyclerView rvBangLuong;
    private BangLuongAdapter bangLuongAdapter;
    private List<BangLuong> mListBangLuong;
    private Spinner spinnerKyLuong;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bang_luong_va_phu_cap, container, false);

        rvBangLuong = view.findViewById(R.id.rv_bang_luong);
        spinnerKyLuong = view.findViewById(R.id.spinner_ky_luong);

        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.ky_luong_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKyLuong.setAdapter(adapter);


        mListBangLuong = new ArrayList<>();
        // Add sample data
        mListBangLuong.add(new BangLuong("Bảng lương tháng 07 năm 2024", true, "15.000.000 VNĐ", "2.500.000 VNĐ", "+ 1.000.000 VNĐ", "- 500.000 VNĐ", "18.000.000 VNĐ"));
        mListBangLuong.add(new BangLuong("Bảng lương tháng 06 năm 2024", true, "15.000.000 VNĐ", "2.500.000 VNĐ", "+ 1.200.000 VNĐ", "0 VNĐ", "18.700.000 VNĐ"));

        bangLuongAdapter = new BangLuongAdapter(mListBangLuong);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvBangLuong.setLayoutManager(linearLayoutManager);
        rvBangLuong.setAdapter(bangLuongAdapter);

        return view;
    }
}