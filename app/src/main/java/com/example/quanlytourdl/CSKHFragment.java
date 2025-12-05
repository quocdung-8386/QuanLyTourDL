package com.example.quanlytourdl; // THAY THẾ bằng package của bạn

import android.annotation.SuppressLint;
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

import com.example.quanlytourdl.adapter.SupportTicketAdapter;
import com.example.quanlytourdl.model.SupportTicket;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class CSKHFragment extends Fragment {

    private ImageButton btnMenuDrawer;
    private TabLayout tabLayout;
    private MaterialButton btnMoPhieuHoTro;
    private RecyclerView recyclerView;
    private SupportTicketAdapter adapter; // Khai báo Adapter

    public CSKHFragment() {
        // Constructor rỗng bắt buộc
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Liên kết Fragment với Layout XML (fragment_cskh.xml)
        View view = inflater.inflate(R.layout.fragment_cskh, container, false);

        // 1. Ánh xạ các thành phần UI
        btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer_cskh);
        tabLayout = view.findViewById(R.id.tabLayout_cskh);
        btnMoPhieuHoTro = view.findViewById(R.id.btn_mo_phieu_ho_tro);
        recyclerView = view.findViewById(R.id.recycler_support_tickets);

        // 2. Thiết lập các Listener
        btnMenuDrawer.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở Navigation Drawer/Menu Chính", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.card_sentiment_analysis).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuyển sang màn hình Báo cáo Cảm xúc KH", Toast.LENGTH_SHORT).show();
        });

        btnMoPhieuHoTro.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở form Tạo Phiếu Hỗ trợ mới", Toast.LENGTH_SHORT).show();
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(getContext(), "Đang tải dữ liệu cho tab: " + tab.getText(), Toast.LENGTH_SHORT).show();
                // Tải dữ liệu tương ứng với tab (Ví dụ: switch case tab.getPosition())
                // loadDataForTab(tab.getPosition());
            }
            // ... (các hàm onTabUnselected và onTabReselected)
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 3. Thiết lập RecyclerView (Tải dữ liệu mẫu)
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        // Tạo dữ liệu mẫu
        List<SupportTicket> ticketList = new ArrayList<>();
        ticketList.add(new SupportTicket("1205", "Vấn đề thanh toán online bị lỗi", "Nguyễn Văn A - Tour Đà Lạt", "Đã giải quyết", "2 giờ trước"));
        ticketList.add(new SupportTicket("1204", "Yêu cầu thay đổi ngày đi tour", "Trần Thị B - Tour Phú Quốc", "Đang xử lý", "5 phút trước"));
        ticketList.add(new SupportTicket("1203", "Báo cáo lỗi ứng dụng mobile", "Lê Văn C - Khách hàng mới", "Mới", "1 ngày trước"));

        // Thiết lập Adapter
        adapter = new SupportTicketAdapter(ticketList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
}