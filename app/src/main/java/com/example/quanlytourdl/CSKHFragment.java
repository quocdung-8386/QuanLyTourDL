package com.example.quanlytourdl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
    private SupportTicketAdapter adapter;

    // MENU ID
    private static final int MENU_ID_CSKH = 1;
    private static final int MENU_ID_QL_NHAN_SU = 2;
    private static final int MENU_ID_BANG_LUONG = 3;
    private static final int MENU_ID_PHAN_QUYEN = 4;
    private static final int MENU_ID_LUONG_THUONG_PHAT = 5;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_cskh, container, false);

        bindViews(view);
        setupListeners();
        setupTabLayout();
        setupRecyclerView();

        return view;
    }

    // =========================
    // ÁNH XẠ VIEW
    // =========================
    private void bindViews(View view) {
        btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer_cskh);
        tabLayout = view.findViewById(R.id.tabLayout_cskh);
        btnMoPhieuHoTro = view.findViewById(R.id.btn_mo_phieu_ho_tro);
        recyclerView = view.findViewById(R.id.recycler_support_tickets);
    }

    // =========================
    // LISTENER
    // =========================
    private void setupListeners() {
        btnMenuDrawer.setOnClickListener(this::showDrawerMenu);

        btnMoPhieuHoTro.setOnClickListener(v ->
                Toast.makeText(getContext(), "Mở form Tạo Phiếu Hỗ trợ", Toast.LENGTH_SHORT).show()
        );

        View card = getView() != null
                ? getView().findViewById(R.id.card_sentiment_analysis)
                : null;

        if (card != null) {
            card.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Báo cáo cảm xúc KH", Toast.LENGTH_SHORT).show()
            );
        }
    }

    // =========================
    // POPUP MENU
    // =========================
    private void showDrawerMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);

        popup.getMenu().add(0, MENU_ID_CSKH, 0, "CSKH & Hỗ trợ");
        popup.getMenu().add(0, MENU_ID_QL_NHAN_SU, 1, "Quản lý nhân sự");
        popup.getMenu().add(0, MENU_ID_BANG_LUONG, 2, "Bảng lương");
        popup.getMenu().add(0, MENU_ID_PHAN_QUYEN, 3, "Phân quyền");
        popup.getMenu().add(0, MENU_ID_LUONG_THUONG_PHAT, 4, "Lương thưởng / Phạt");

        popup.setOnMenuItemClickListener(this::onMenuItemSelected);
        popup.show();
    }

    private boolean onMenuItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_CSKH:
                Toast.makeText(getContext(), "Bạn đang ở CSKH", Toast.LENGTH_SHORT).show();
                return true;

            case MENU_ID_QL_NHAN_SU:
                openFragment(new QuanLyNhanSuFragment());
                return true;

            case MENU_ID_BANG_LUONG:
                openFragment(new BangLuongVaPhuCapFragment());
                return true;

            case MENU_ID_PHAN_QUYEN:
                openFragment(new PhanQuyenTruyCapFragment());
                return true;

            case MENU_ID_LUONG_THUONG_PHAT:
                openFragment(new LuongThuongPhatFragment());
                return true;
        }
        return false;
    }

    // =========================
    // CHUYỂN FRAGMENT (KHÔNG BASE)
    // =========================
    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction =
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction();

        transaction.replace(R.id.main_content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // =========================
    // TAB + RECYCLER
    // =========================
    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(getContext(),
                        "Lọc: " + tab.getText(),
                        Toast.LENGTH_SHORT).show();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        List<SupportTicket> list = new ArrayList<>();
        list.add(new SupportTicket("1205", "Lỗi thanh toán", "Nguyễn Văn A", "Đã xử lý", "2h"));
        list.add(new SupportTicket("1204", "Đổi ngày tour", "Trần Thị B", "Đang xử lý", "5p"));
        list.add(new SupportTicket("1203", "Lỗi app", "Lê Văn C", "Mới", "1 ngày"));

        adapter = new SupportTicketAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
}
