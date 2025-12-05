package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.TourAdapter;
import com.example.quanlytourdl.firebase.FirebaseRepository; // Đổi đường dẫn repository thành .data
import com.example.quanlytourdl.model.Tour;

import java.util.ArrayList;
import java.util.List;

public class ChoPheDuyetTourFragment extends Fragment {

    private Toolbar toolbar;
    private TextView subtitleTextView;
    private RecyclerView recyclerView;
    private TourAdapter tourAdapter;
    private List<Tour> tourList;
    private FirebaseRepository repository;

    // Lưu tham chiếu View
    private View fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // SỬA: Đảm bảo tên layout khớp với file XML thực tế
        fragmentView = inflater.inflate(R.layout.fragment_cho_duyet_tour, container, false);
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar();
        setupRecyclerView();
        loadTourData();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_cho_phe_duyet);
        subtitleTextView = view.findViewById(R.id.text_subtitle);
        recyclerView = view.findViewById(R.id.recycler_tour_cho_phe_duyet);
        // SỬA: Thay đổi đường dẫn package repository thành .data (đã thống nhất ở bước trước)
        repository = new FirebaseRepository();
        tourList = new ArrayList<>();
    }

    private void setupToolbar() {
        // Xử lý sự kiện nút Quay lại
        toolbar.setNavigationOnClickListener(v -> {
            // CẢI TIẾN: Sử dụng requireActivity() an toàn hơn
            requireActivity().onBackPressed();
        });

        // Xử lý sự kiện nút Menu 3 gạch
        // CẢI TIẾN: Sử dụng requireView() để truy cập View an toàn
        requireView().findViewById(R.id.btn_side_menu).setOnClickListener(v -> {
            // CẢI TIẾN: Sử dụng requireContext() an toàn hơn
            Toast.makeText(requireContext(), "Mở Menu Tùy Chọn", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        // CẢI TIẾN: Sử dụng requireContext() an toàn hơn
        tourAdapter = new TourAdapter(requireContext(), tourList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(tourAdapter);
    }

    private void loadTourData() {
        repository.getToursChoPheDuyet().observe(getViewLifecycleOwner(), tours -> {
            if (tours != null) {
                // Cập nhật Adapter khi dữ liệu thay đổi
                tourAdapter.updateList(tours);
                // Cập nhật Subtitle
                subtitleTextView.setText("Có " + tours.size() + " tour đang chờ");
            } else {
                subtitleTextView.setText("Không có tour nào đang chờ");
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu hoặc không có tour chờ duyệt.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}