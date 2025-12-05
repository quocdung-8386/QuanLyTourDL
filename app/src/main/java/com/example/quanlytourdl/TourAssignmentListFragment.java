package com.example.quanlytourdl; // Thay thế bằng package của bạn

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

public class TourAssignmentListFragment extends Fragment {

    private TabLayout tabLayout;
    private RecyclerView recyclerViewTours;
    private ImageButton btnBack, btnNotification;

    public TourAssignmentListFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tour_assignment_list, container, false);

        // 1. Ánh xạ
        tabLayout = view.findViewById(R.id.tab_layout_assignment);
        recyclerViewTours = view.findViewById(R.id.recycler_tours_for_assignment);
        btnBack = view.findViewById(R.id.btn_back_assignment_list);
        btnNotification = view.findViewById(R.id.btn_notification_assignment);

        // 2. Thiết lập RecyclerView (Giả lập)
        recyclerViewTours.setLayoutManager(new LinearLayoutManager(getContext()));
        // TODO: Thay thế bằng Adapter thực sự
        // recyclerViewTours.setAdapter(new TourAssignmentAdapter(getList("Chờ Gán")));

        // 3. Xử lý sự kiện Toolbar
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnNotification.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở thông báo về phân công Tour", Toast.LENGTH_SHORT).show();
        });

        // 4. Xử lý sự kiện Tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabTitle = tab.getText().toString();
                Toast.makeText(getContext(), "Đang tải danh sách Tour: " + tabTitle, Toast.LENGTH_SHORT).show();

                // TODO: Thay đổi dữ liệu cho RecyclerView Adapter dựa trên tabTitle (Chờ Gán / Đã Gán)
                // recyclerViewTours.setAdapter(new TourAssignmentAdapter(getList(tabTitle)));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không làm gì
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không làm gì
            }
        });

        return view;
    }
}