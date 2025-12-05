package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

// Giả sử Adapter và Model đã tồn tại
// import com.example.quanlytourdl.adapter.TourAdapter;
// import com.example.quanlytourdl.model.Tour;

public class ChoPheDuyetTourFragment extends Fragment {

    private Toolbar toolbar;
    private TextView subtitleTextView;
    private ImageButton sideMenuButton;
    private EditText searchEditText;
    private ChipGroup filtersChipGroup;
    private RecyclerView recyclerView;

    // Giả sử:
    // private TourAdapter tourAdapter;
    // private List<Tour> tourList;

    public ChoPheDuyetTourFragment() {
        // Constructor rỗng bắt buộc
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout đã được thiết kế (fragment_cho_phe_duyet_tour.xml)
        return inflater.inflate(R.layout.fragment_cho_phe_duyet_tour, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo View
        initViews(view);

        // 2. Thiết lập Toolbar và sự kiện nút
        setupToolbar();

        // 3. Thiết lập RecyclerView
        setupRecyclerView();

        // 4. Thiết lập sự kiện tìm kiếm và lọc (tùy chọn)
        setupListeners();

        // 5. Load dữ liệu (ví dụ)
        loadTourData();
    }

    // ------------------- PRIVATE METHODS -------------------

    private void initViews(View view) {
        // Toolbar
        toolbar = view.findViewById(R.id.toolbar_cho_phe_duyet);
        subtitleTextView = view.findViewById(R.id.text_subtitle);
        sideMenuButton = view.findViewById(R.id.btn_side_menu);

        // Search & Filters
        searchEditText = view.findViewById(R.id.edit_search_tour);
        filtersChipGroup = view.findViewById(R.id.chip_group_filters);

        // Content
        recyclerView = view.findViewById(R.id.recycler_tour_cho_phe_duyet);
    }

    private void setupToolbar() {
        // Xử lý sự kiện nút Quay lại (Back)
        toolbar.setNavigationOnClickListener(v -> {
            // Thực hiện hành động quay lại, ví dụ:
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Xử lý sự kiện nút Menu 3 gạch
        sideMenuButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở Menu Tùy Chọn", Toast.LENGTH_SHORT).show();
            // Thêm logic mở PopupMenu hoặc Navigation Drawer tại đây
        });
    }

    private void setupRecyclerView() {
        // 1. Khởi tạo danh sách (nếu chưa có)
        // tourList = new ArrayList<>();

        // 2. Khởi tạo Adapter
        // tourAdapter = new TourAdapter(tourList);

        // 3. Thiết lập Adapter cho RecyclerView
        // recyclerView.setAdapter(tourAdapter);

        // Ghi chú: LayoutManager đã được khai báo trong XML (LinearLayoutManager)
    }

    private void setupListeners() {
        // Ví dụ: Xử lý khi chọn các Chip lọc
        if (filtersChipGroup != null) {
            filtersChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    int checkedId = checkedIds.get(0);
                    // Dựa vào checkedId để gọi hàm lọc dữ liệu
                    // filterTours(checkedId);
                }
            });
        }

        // Ví dụ: Xử lý tìm kiếm (TextWatcher)
        // searchEditText.addTextChangedListener(new TextWatcher() { ... });
    }

    private void loadTourData() {
        // Load dữ liệu từ database hoặc API
        // Ví dụ:
        // tourList.add(new Tour("Tour Hà Nội", "HN001", ...));
        // tourAdapter.notifyDataSetChanged();

        // Cập nhật Subtitle
        int count = 2; // Giả sử load được 2 tour
        subtitleTextView.setText("Có " + count + " tour đang chờ");
    }

    // Ghi chú: BottomNavigationView thường được xử lý trong Activity chứa Fragment này.
}