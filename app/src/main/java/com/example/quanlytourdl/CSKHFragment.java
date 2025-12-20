package com.example.quanlytourdl;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.SupportTicketAdapter;
import com.example.quanlytourdl.adapter.WarningAdapter;
import com.example.quanlytourdl.model.SupportTicket;
import com.example.quanlytourdl.model.Warning;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CSKHFragment extends Fragment {

    private ImageButton btnMenuDrawer;
    private TabLayout tabLayout;
    private MaterialButton btnMoPhieuHoTro;
    private Button btnGuiCanhBao;
    private TextView tvViewHistory;
    private RecyclerView recyclerView;
    private SupportTicketAdapter adapter;
    private List<SupportTicket> mListTicket;

    // Khai báo Firebase
    private FirebaseFirestore db;

    // MENU ID
    private static final int MENU_ID_CSKH = 1;
    private static final int MENU_ID_QL_NHAN_SU = 2;
    private static final int MENU_ID_BANG_LUONG = 3;
    private static final int MENU_ID_PHAN_QUYEN = 4;
    private static final int MENU_ID_LUONG_THUONG_PHAT = 5;
    private static final int MENU_ID_PHAN_HOI = 6;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cskh, container, false);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        bindViews(view);
        setupListeners();
        setupRecyclerView(); // Load dữ liệu Phiếu hỗ trợ
        setupTabLayout();    // Setup tab chuyển sang Khiếu nại

        return view;
    }

    private void bindViews(View view) {
        btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer_cskh);
        tabLayout = view.findViewById(R.id.tabLayout_cskh);
        btnMoPhieuHoTro = view.findViewById(R.id.btn_mo_phieu_ho_tro);
        btnGuiCanhBao = view.findViewById(R.id.btn_gui_canh_bao);
        tvViewHistory = view.findViewById(R.id.tv_view_history);
        recyclerView = view.findViewById(R.id.recycler_support_tickets);
    }

    private void setupListeners() {
        // 1. Menu Drawer
        btnMenuDrawer.setOnClickListener(this::showDrawerMenu);

        // 2. Nút Mở phiếu hỗ trợ
        btnMoPhieuHoTro.setOnClickListener(v -> showCreateTicketDialog());

        // 3. Nút Gửi cảnh báo
        if (btnGuiCanhBao != null) {
            btnGuiCanhBao.setOnClickListener(v -> showWarningDialog());
        }

        // 4. Nút Xem lịch sử cảnh báo
        if (tvViewHistory != null) {
            tvViewHistory.setOnClickListener(v -> showHistoryDialog());
        }

        // 5. [CẬP NHẬT] Thẻ Phân tích Cảm xúc -> Chuyển sang màn hình Phân tích
        View card = getView() != null ? getView().findViewById(R.id.card_sentiment_analysis) : null;
        if (card != null) {
            card.setOnClickListener(v -> {
                // Mở Fragment Phân Tích Cảm Xúc
                openFragment(new PhanTichCamXucFragment());
            });
        }
    }

    // ==========================================
    // 1. CHỨC NĂNG GỬI CẢNH BÁO
    // ==========================================
    private void showWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.fragment_gui_canh_bao, null); // Hoặc dialog_gui_canh_bao tùy tên file bạn đặt
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText etTitle = view.findViewById(R.id.et_warn_title);
        TextInputEditText etContent = view.findViewById(R.id.et_warn_content);
        Spinner spLevel = view.findViewById(R.id.sp_warn_level);
        Spinner spTarget = view.findViewById(R.id.sp_warn_target);
        Button btnCancel = view.findViewById(R.id.btn_cancel_warn);
        Button btnSend = view.findViewById(R.id.btn_send_warn);

        // Setup Spinner Dữ liệu
        String[] levels = {"⚠️ Thông tin", "🟠 Cảnh báo", "🔴 KHẨN CẤP"};
        ArrayAdapter<String> adapterLevel = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, levels);
        spLevel.setAdapter(adapterLevel);

        String[] targets = {"Tất cả khách hàng", "Khách đang đi Tour", "Nội bộ nhân viên"};
        ArrayAdapter<String> adapterTarget = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, targets);
        spTarget.setAdapter(adapterTarget);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            String level = spLevel.getSelectedItem().toString();
            String target = spTarget.getSelectedItem().toString();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tiêu đề và nội dung!", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = "WARN-" + System.currentTimeMillis();
            String time = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());

            Warning warning = new Warning(id, title, content, target, "ALL", level, time);

            db.collection("Warnings").document(id).set(warning)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Đã gửi cảnh báo thành công!", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi gửi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    // ==========================================
    // 2. CHỨC NĂNG XEM LỊCH SỬ CẢNH BÁO
    // ==========================================
    private void showHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.fragment_lich_su_canh_bao, null); // Hoặc dialog_lich_su_canh_bao
        builder.setView(view);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView rvHistory = view.findViewById(R.id.rv_history_warning);
        Button btnClose = view.findViewById(R.id.btn_close_history);

        List<Warning> listWarnings = new ArrayList<>();
        WarningAdapter warningAdapter = new WarningAdapter(listWarnings);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(warningAdapter);

        // Load dữ liệu từ Firebase
        db.collection("Warnings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listWarnings.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Warning w = doc.toObject(Warning.class);
                        if (w != null) listWarnings.add(w);
                    }
                    warningAdapter.notifyDataSetChanged();

                    if (listWarnings.isEmpty()) {
                        Toast.makeText(getContext(), "Chưa có cảnh báo nào!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi tải lịch sử!", Toast.LENGTH_SHORT).show());

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ==========================================
    // 3. SETUP DANH SÁCH & LOAD PHIẾU HỖ TRỢ
    // ==========================================
    private void setupRecyclerView() {
        mListTicket = new ArrayList<>();

        adapter = new SupportTicketAdapter(mListTicket,
                ticket -> openTicketDetail(ticket.getId(), ticket.getDescription(), ticket.getTime(), ticket.getCustomerName()),
                (ticket, position) -> showActionDialog(ticket)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        db.collection("SupportTickets")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        mListTicket.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            SupportTicket ticket = doc.toObject(SupportTicket.class);
                            if (ticket != null) {
                                mListTicket.add(ticket);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // ==========================================
    // 4. XỬ LÝ PHIẾU HỖ TRỢ (DUYỆT / XÓA)
    // ==========================================
    private void showActionDialog(SupportTicket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Xử lý phiếu: " + ticket.getId());

        String[] options = {"✅ Đã giải quyết", "❌ Từ chối hỗ trợ", "🗑️ Xóa phiếu"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    updateTicketStatus(ticket.getId(), "Đã giải quyết");
                    break;
                case 1:
                    updateTicketStatus(ticket.getId(), "Đã từ chối");
                    break;
                case 2:
                    deleteTicket(ticket.getId());
                    break;
            }
        });
        builder.show();
    }

    private void updateTicketStatus(String ticketId, String newStatus) {
        db.collection("SupportTickets").document(ticketId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã cập nhật: " + newStatus, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi cập nhật!", Toast.LENGTH_SHORT).show());
    }

    private void deleteTicket(String ticketId) {
        db.collection("SupportTickets").document(ticketId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã xóa phiếu!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi xóa phiếu!", Toast.LENGTH_SHORT).show());
    }

    // ==========================================
    // 5. TẠO PHIẾU HỖ TRỢ MỚI
    // ==========================================
    private void showCreateTicketDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.fragment_tao_phieu_ho_tro, null); // Hoặc dialog_tao_phieu_ho_tro
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etName = view.findViewById(R.id.et_customer_name);
        EditText etDesc = view.findViewById(R.id.et_problem_desc);
        Button btnCancel = view.findViewById(R.id.btn_cancel_dialog);
        Button btnCreate = view.findViewById(R.id.btn_create_dialog);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDesc.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            Random random = new Random();
            int randomNum = 10000 + random.nextInt(90000);
            String ticketId = "#HT-" + randomNum;
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            SupportTicket newTicket = new SupportTicket(ticketId, description, name, "Chờ xử lý", currentDate);

            db.collection("SupportTickets").document(ticketId)
                    .set(newTicket)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Tạo phiếu thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi tạo phiếu!", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    // ==========================================
    // 6. CHUYỂN TAB & NAVIGATION
    // ==========================================
    private void setupTabLayout() {
        if (tabLayout.getTabCount() > 0) {
            tabLayout.getTabAt(0).select();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Tab "Phản hồi & Khiếu nại"
                if (tab.getPosition() == 1) {
                    View parentView = (View) getView().getParent();
                    if (parentView != null) {
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        // Chuyển sang Danh sách Khiếu nại
                        transaction.replace(parentView.getId(), new DanhSachKhieuNaiFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    View parentView = (View) getView().getParent();
                    if (parentView != null) {
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(parentView.getId(), new DanhSachKhieuNaiFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            }
        });
    }

    private void openTicketDetail(String id, String desc, String date, String customerName) {
        View parentView = (View) getView().getParent();
        if (parentView == null) return;
        int containerId = parentView.getId();

        ChiTietPhieuHoTroFragment fragment = new ChiTietPhieuHoTroFragment();
        Bundle args = new Bundle();
        args.putString("TICKET_ID", id);
        args.putString("TICKET_DESC", desc);
        args.putString("TICKET_DATE", date);
        args.putString("TICKET_CUSTOMER", customerName);
        fragment.setArguments(args);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(containerId, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // ==========================================
    // 7. MENU DRAWER
    // ==========================================
    private void showDrawerMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, MENU_ID_CSKH, 0, "CSKH & Hỗ trợ");
        popup.getMenu().add(0, MENU_ID_QL_NHAN_SU, 1, "Quản lý nhân sự");
        popup.getMenu().add(0, MENU_ID_BANG_LUONG, 2, "Bảng lương");
        popup.getMenu().add(0, MENU_ID_PHAN_QUYEN, 3, "Phân quyền");
        popup.getMenu().add(0, MENU_ID_LUONG_THUONG_PHAT, 4, "Lương thưởng / Phạt");
        popup.getMenu().add(0, MENU_ID_PHAN_HOI, 5, "Phản hồi khách hàng");
        popup.setOnMenuItemClickListener(this::onMenuItemSelected);
        popup.show();
    }

    private boolean onMenuItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_CSKH: return true;
            case MENU_ID_QL_NHAN_SU: openFragment(new QuanLyNhanSuFragment()); return true;
            case MENU_ID_BANG_LUONG: openFragment(new BangLuongVaPhuCapFragment()); return true;
            case MENU_ID_PHAN_QUYEN: openFragment(new PhanQuyenTruyCapFragment()); return true;
            case MENU_ID_LUONG_THUONG_PHAT: openFragment(new LuongThuongPhatFragment()); return true;
            case MENU_ID_PHAN_HOI: openFragment(new PhanHoiFragment()); return true;
        }
        return false;
    }

    private void openFragment(Fragment fragment) {
        View parentView = (View) getView().getParent();
        if (parentView == null) return;
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(parentView.getId(), fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}