package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
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
import com.example.quanlytourdl.firebase.FirebaseRepository;
import com.example.quanlytourdl.model.Tour;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChoPheDuyetTourFragment extends Fragment {

    private static final String TAG = "ChoPheDuyetTourFragment";

    private Toolbar toolbar;
    private TextView subtitleTextView;
    private RecyclerView recyclerView;
    private TourAdapter tourAdapter;
    private List<Tour> tourList;
    private FirebaseRepository repository;
    private FirebaseFirestore db;

    public ChoPheDuyetTourFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Giả định layout ID là fragment_cho_duyet_tour
        int layoutId = getResources().getIdentifier("fragment_cho_duyet_tour", "layout", requireContext().getPackageName());
        if (layoutId == 0) {
            Log.e(TAG, "Không tìm thấy layout ID 'fragment_cho_duyet_tour'.");
            // Trả về view cơ bản nếu không tìm thấy layout
            TextView errorView = new TextView(getContext());
            errorView.setText("Lỗi: Không tìm thấy layout fragment_cho_duyet_tour");
            return errorView;
        }
        return inflater.inflate(layoutId, container, false);
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

        // Khởi tạo Repository
        repository = new FirebaseRepository();
        tourList = new ArrayList<>();
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().onBackPressed();
                }
            });

            View sideMenuButton = toolbar.findViewById(R.id.btn_side_menu);
            if (sideMenuButton != null) {
                sideMenuButton.setOnClickListener(v -> {
                    Toast.makeText(requireContext(), "Mở Menu Tùy Chọn", Toast.LENGTH_SHORT).show();
                });
            }
        } else {
            Log.e(TAG, "Lỗi: Toolbar (toolbar_cho_phe_duyet) không được tìm thấy.");
        }
    }

    private void setupRecyclerView() {
        if (recyclerView != null) {
            tourAdapter = new TourAdapter(requireContext(), tourList);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(tourAdapter);
        } else {
            Log.e(TAG, "Lỗi: RecyclerView (recycler_tour_cho_phe_duyet) không được tìm thấy.");
        }
    }

    private void loadTourData() {
        if (repository == null) {
            Log.e(TAG, "Lỗi: Repository chưa được khởi tạo.");
            return;
        }

        // Lấy dữ liệu từ LiveData (dùng Firestore)
        repository.getToursChoPheDuyet().observe(getViewLifecycleOwner(), tours -> {
            if (tours != null) {
                if (tourAdapter != null) {
                    tourAdapter.updateList(tours);
                }
                if (subtitleTextView != null) {
                    subtitleTextView.setText("Có " + tours.size() + " tour đang chờ");
                }
                Log.d(TAG, "Cập nhật Tour chờ duyệt thành công, số lượng: " + tours.size());

            } else {
                if (subtitleTextView != null) {
                    subtitleTextView.setText("Không có tour nào đang chờ");
                }
                Log.w(TAG, "Lỗi tải dữ liệu hoặc không có tour chờ duyệt.");
            }
        });
    }

    /**
     * Gỡ bỏ Listener khi Fragment bị hủy để tránh rò rỉ bộ nhớ.
     * Đây là phần bổ sung bắt buộc sau khi chuyển sang Firestore Listener.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            repository.removeTourListener();
            Log.d(TAG, "Đã gọi removeTourListener() để dọn dẹp.");
        }
    }
}