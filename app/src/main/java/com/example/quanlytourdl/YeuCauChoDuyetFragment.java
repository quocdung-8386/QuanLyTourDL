package com.example.quanlytourdl;

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

public class YeuCauChoDuyetFragment extends Fragment {

    // Khai báo các View cần tương tác
    private ImageButton btnBack;
    private ImageButton btnSearch;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;

    // Lưu loại yêu cầu đang được chọn (mặc định là HDV)
    private String currentRequestType = "Hướng Dẫn Viên";

    public YeuCauChoDuyetFragment() {
        // Constructor rỗng bắt buộc
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_yeu_cau_cho_duyet, container, false);

        // 1. Ánh xạ các thành phần
        btnBack = view.findViewById(R.id.btn_back);
        btnSearch = view.findViewById(R.id.btn_search);
        tabLayout = view.findViewById(R.id.tab_layout_yeu_cau);
        recyclerView = view.findViewById(R.id.recycler_yeu_cau);

        // 2. Thiết lập trạng thái ban đầu và dữ liệu mặc định
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Tải danh sách mặc định (Hướng dẫn viên)
        loadRequestList(currentRequestType);

        // 3. Xử lý sự kiện Toolbar
        btnBack.setOnClickListener(v -> {
            // Xử lý quay lại
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnSearch.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở giao diện Tìm kiếm", Toast.LENGTH_SHORT).show();
        });

        // 4. Xử lý sự kiện Tab (Hướng dẫn viên / Phương tiện)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Lấy loại yêu cầu (chỉ lấy phần chữ, bỏ qua số lượng)
                String fullText = tab.getText().toString();
                if (fullText.contains("(")) {
                    currentRequestType = fullText.substring(0, fullText.indexOf("(")).trim();
                } else {
                    currentRequestType = fullText.trim();
                }

                // 🚀 GỌI HÀM TẢI DỮ LIỆU
                loadRequestList(currentRequestType);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }

    /**
     * Hàm giả lập việc tải và hiển thị danh sách yêu cầu chờ duyệt
     * @param type Loại yêu cầu: "Hướng Dẫn Viên" hoặc "Phương Tiện"
     */
    private void loadRequestList(String type) {
        // Trong ứng dụng thực tế, bạn sẽ gọi ViewModel/Repository ở đây để fetch data

        Toast.makeText(getContext(), "Đang tải danh sách: " + type, Toast.LENGTH_SHORT).show();

        // Ví dụ:
        // List<Request> filteredData = dataRepository.getPendingRequests(type);
        // recyclerView.setAdapter(new YeuCauAdapter(filteredData));

        // Cần tạo Adapter cho RecyclerView (YeuCauAdapter) để hiển thị item_yeu_cau_cho_duyet.xml
    }
}