package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.TicketInteractionAdapter;
import com.example.quanlytourdl.model.TicketInteraction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiTietPhieuHoTroFragment extends Fragment {

    private RecyclerView rvInteractions;
    private TicketInteractionAdapter adapter;
    private List<TicketInteraction> messageList;
    private EditText etNote;

    private TextView tvTicketId, tvDescription, tvCreatedDate, tvCustomerName; // [MỚI] Thêm tvCustomerName
    private String currentCustomerName = "Khách hàng"; // Biến lưu tên khách hiện tại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_phieu_ho_tro, container, false);

        initViews(view);

        Bundle args = getArguments();
        if (args != null) {
            String id = args.getString("TICKET_ID");
            String desc = args.getString("TICKET_DESC");
            String date = args.getString("TICKET_DATE");
            String customerName = args.getString("TICKET_CUSTOMER"); // [MỚI]

            if (id != null) tvTicketId.setText(id);
            if (desc != null) tvDescription.setText(desc);
            if (date != null) tvCreatedDate.setText(date);

            // [MỚI] Hiển thị tên khách hàng và lưu vào biến toàn cục
            if (customerName != null) {
                tvCustomerName.setText(customerName);
                currentCustomerName = customerName;
            }
        }

        setupRecyclerView();

        return view;
    }

    private void initViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        rvInteractions = view.findViewById(R.id.rv_interactions);
        etNote = view.findViewById(R.id.et_note);
        Button btnAddNote = view.findViewById(R.id.btn_add_note);
        Button btnCloseTicket = view.findViewById(R.id.btn_close_ticket);

        tvTicketId = view.findViewById(R.id.tv_ticket_id);
        tvDescription = view.findViewById(R.id.tv_description);
        tvCreatedDate = view.findViewById(R.id.tv_created_date);
        tvCustomerName = view.findViewById(R.id.tv_customer_name); // [MỚI] Ánh xạ (ID này đã có trong layout bước trước)

        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // 2. Xử lý nút Thêm Ghi chú (KHÁCH HÀNG PHẢN HỒI)
        btnAddNote.setOnClickListener(v -> {
            String note = etNote.getText().toString().trim();
            if (!note.isEmpty()) {
                String currentTime = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(new Date());

                // [QUAN TRỌNG] Sử dụng currentCustomerName làm tên người gửi
                TicketInteraction newMsg = new TicketInteraction(currentCustomerName, currentTime, note, R.drawable.ic_person, false);

                adapter.addMessage(newMsg);
                etNote.setText("");
                rvInteractions.smoothScrollToPosition(adapter.getItemCount() - 1);
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCloseTicket.setOnClickListener(v -> {
            String currentId = tvTicketId.getText().toString();
            Toast.makeText(getContext(), "Đã đóng phiếu hỗ trợ " + currentId, Toast.LENGTH_SHORT).show();
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        String dateStr = (tvCreatedDate.getText() != null) ? tvCreatedDate.getText().toString() : "20/12/2025";

        // Tin nhắn hệ thống (Giữ nguyên)
        messageList.add(new TicketInteraction("Hệ thống", "14:30 - " + dateStr,
                "Phiếu hỗ trợ đã được tạo. Đang chờ nhân viên tiếp nhận.", R.drawable.ic_settings, true));

        // Tin nhắn từ chính Khách hàng này (Ví dụ ban đầu)
        messageList.add(new TicketInteraction(currentCustomerName, "14:32 - " + dateStr,
                "Tôi cần hỗ trợ gấp vấn đề này.", R.drawable.ic_person, false));

        adapter = new TicketInteractionAdapter(messageList);
        rvInteractions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInteractions.setAdapter(adapter);
    }
}