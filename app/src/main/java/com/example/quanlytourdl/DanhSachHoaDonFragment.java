package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        rvHoaDon.setLayoutManager(new LinearLayoutManager(getContext()));
        listHoaDon = new ArrayList<>();

        // Khởi tạo Adapter với sự kiện Click
        adapter = new HoaDonAdapter(getContext(), listHoaDon, new HoaDonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HoaDon hoaDon) {
                openDetailFragment(hoaDon);
            }
        });
        rvHoaDon.setAdapter(adapter);
    }

    private void setupData() {
        listHoaDon.add(new HoaDon("#INV-2023001", "24/10/2023", "Nguyen Van A", 5200000, 1, "Tour Đà Nẵng"));
        listHoaDon.add(new HoaDon("#INV-2023002", "23/10/2023", "Công ty ABC", 12500000, 2, "Tour Hạ Long"));
        listHoaDon.add(new HoaDon("#INV-2023003", "20/10/2023", "Le Thi B", 850000, 3, "Vé SunWorld"));
        adapter.notifyDataSetChanged();
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        btnAdd.setOnClickListener(v -> {
            // Mở màn hình tạo đơn mới
            int containerId = ((ViewGroup) getView().getParent()).getId();
            getParentFragmentManager().beginTransaction()
                    .replace(containerId, new TaoDonHangFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void openDetailFragment(HoaDon hoaDon) {
        ChiTietHoaDonFragment detailFragment = new ChiTietHoaDonFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("hoa_don_data", hoaDon);
        detailFragment.setArguments(bundle);

        int containerId = ((ViewGroup) getView().getParent()).getId();
        getParentFragmentManager().beginTransaction()
                .replace(containerId, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}