package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

// Lưu ý: Cần import R.layout.fragment_kinhdoanh
// Ví dụ: import com.example.quanlytourdl.R;

public class KinhDoanhFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kinhdoanh, container, false);

        // 1. Xử lý Menu Dấu 3 Gạch
        ImageButton btnMenuDrawer = view.findViewById(R.id.btn_menu_drawer);
        btnMenuDrawer.setOnClickListener(v -> {
            // Logic mở Navigation Drawer (hoặc tùy chọn menu)
            Toast.makeText(getContext(), "Mở Menu chính (Navigation Drawer)", Toast.LENGTH_SHORT).show();
        });

        // 2. Xử lý FAB
        view.findViewById(R.id.fab_add_provider).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuyển sang màn hình Thêm Nhà Cung Cấp", Toast.LENGTH_SHORT).show();
        });

        // 3. Cập nhật nội dung Quick Actions (Nếu không dùng Data Binding)
        setupQuickActions(view);

        // 4. Khởi tạo RecyclerView cho Danh sách Nhà cung cấp
        RecyclerView recyclerView = view.findViewById(R.id.recycler_providers);
        // Cần setup LayoutManager và Adapter ở đây
        // Ví dụ: recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // recyclerView.setAdapter(new ProviderAdapter(dataList));

        return view;
    }

    private void setupQuickActions(View view) {
        // Quản lý hợp đồng
        View actionContract = view.findViewById(R.id.action_contract);
        ((ImageView) actionContract.findViewById(R.id.action_icon)).setImageResource(R.drawable.ic_document); // Giả sử có icon ic_document
        ((TextView) actionContract.findViewById(R.id.action_title)).setText("Quản lý hợp đồng");

        // Đánh giá hiệu suất
        View actionPerformance = view.findViewById(R.id.action_performance);
        ((ImageView) actionPerformance.findViewById(R.id.action_icon)).setImageResource(R.drawable.ic_assessment); // Giả sử có icon ic_assessment (Analytics)
        ((TextView) actionPerformance.findViewById(R.id.action_title)).setText("Đánh giá hiệu suất");

        // Bộ lọc
        View actionFilter = view.findViewById(R.id.action_filter);
        ((ImageView) actionFilter.findViewById(R.id.action_icon)).setImageResource(R.drawable.ic_filter); // Giả sử có icon ic_filter
        ((TextView) actionFilter.findViewById(R.id.action_title)).setText("Bộ lọc");
    }
}