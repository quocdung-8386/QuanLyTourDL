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

public class LuongThuongPhatFragment extends Fragment {

    private RecyclerView rvLuongThuongPhat;
    private LuongThuongPhatAdapter adapter;
    private List<LuongThuongPhat> list;
    private Spinner spinnerPhongBan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_luong_thuong_phat, container, false);

        rvLuongThuongPhat = view.findViewById(R.id.rv_luong_thuong_phat);
        spinnerPhongBan = view.findViewById(R.id.spinner_phong_ban);

        // Setup Spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.phong_ban_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhongBan.setAdapter(spinnerAdapter);

        rvLuongThuongPhat.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample Data
        list = new ArrayList<>();
        list.add(new LuongThuongPhat("Thưởng", "Nông Quốc Dũng", "Thưởng nóng hoàn thành xuất sắc KPI tháng 11/2025. Đề xuất bởi: Nqdung", "Chờ phê duyệt", true));
        list.add(new LuongThuongPhat("Thưởng", "Vũ Quang", "Thưởng nhân viên xuất sắc quý 4/2025. Đề xuất bởi: Ban Giám đốc", "Đã phê duyệt", false));
        list.add(new LuongThuongPhat("Phạt", "Long Nông", "Phạt do làm mất tài sản công ty. Lý do từ chối: Chưa đủ bằng chứng.", "Đã từ chối", false));

        adapter = new LuongThuongPhatAdapter(getContext(), list);
        rvLuongThuongPhat.setAdapter(adapter);

        return view;
    }
}