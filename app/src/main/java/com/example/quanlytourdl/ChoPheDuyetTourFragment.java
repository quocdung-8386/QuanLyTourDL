package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;

// ⭐ STATIC IMPORT CÁC HẰNG SỐ TRẠNG THÁI TỪ TourAdapter
import static com.example.quanlytourdl.adapter.TourAdapter.STATUS_APPROVED;
import static com.example.quanlytourdl.adapter.TourAdapter.STATUS_REJECTED;

/**
 * Fragment hiển thị danh sách các tour đang chờ Ban Quản Trị phê duyệt.
 * Triển khai TourAdapter.OnTourActionListener để xử lý các sự kiện click từ RecyclerView.
 */
// ⭐ ĐÃ SỬA: implements TourAdapter.OnTourActionListener
public class ChoPheDuyetTourFragment extends Fragment implements TourAdapter.OnTourActionListener {

    private static final String TAG = "ChoPheDuyetTourFragment";

    private Toolbar toolbar;
    private TextView subtitleTextView;
    private RecyclerView recyclerView;

    private TourAdapter tourAdapter;
    private final List<Tour> tourList = new ArrayList<>();

    private FirebaseRepository repository;

    public ChoPheDuyetTourFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new FirebaseRepository();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutId = R.layout.fragment_cho_duyet_tour;
        try {
            return inflater.inflate(layoutId, container, false);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi inflate layout 'fragment_cho_duyet_tour'", e);
            TextView errorView = new TextView(getContext());
            errorView.setText("Lỗi: Không tìm thấy layout fragment_cho_duyet_tour");
            return errorView;
        }
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
    }

    private void setupToolbar() {
        if (toolbar != null) {
            try {
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            } catch (Exception e) {
                Log.w(TAG, "Lỗi tài nguyên: Icon không tồn tại.");
            }
            toolbar.setNavigationOnClickListener(v -> {
                getParentFragmentManager().popBackStack();
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
            // Truyền `this` (Fragment) là listener
            tourAdapter = new TourAdapter(requireContext(), tourList, this);
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
        repository.getToursChoPheDuyet().observe(getViewLifecycleOwner(), tours -> {
            if (tours != null && tourAdapter != null) {
                tourAdapter.updateList(tours);
                String subtitleText = (tours.size() > 0)
                        ? "Có " + tours.size() + " tour đang chờ"
                        : "Không có tour nào đang chờ";
                if (subtitleTextView != null) {
                    subtitleTextView.setText(subtitleText);
                }
            } else {
                if (subtitleTextView != null) {
                    subtitleTextView.setText("Không có tour nào đang chờ");
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            // Phương thức này hiện tại chỉ log warning vì không có Listener toàn cục
            repository.removeTourListener();
            Log.d(TAG, "Đã gọi removeTourListener() để dọn dẹp.");
        }
    }

    // --- Triển khai TourAdapter.OnTourActionListener (Callback) ---

    @Override
    public void onApproveReject(String tourId, String tourName, String newStatus, int position) {

        String action = newStatus.equals(STATUS_APPROVED) ? "phê duyệt" : "từ chối";

        // GỌI REPOSITORY VÀ XỬ LÝ KẾT QUẢ TASK (Đã sửa lỗi Task/getMessage)
        repository.updateTourStatus(tourId, newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Đã " + action + " tour: " + tourName, Toast.LENGTH_SHORT).show();

                    // Cập nhật UI ngay lập tức: Xóa item khỏi danh sách
                    if (tourAdapter != null) {
                        tourAdapter.removeItem(position);
                    }
                })
                .addOnFailureListener(e -> {
                    // Đảm bảo e là Throwable/Exception để gọi getMessage()
                    Toast.makeText(requireContext(), "Lỗi " + action + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Lỗi " + action + " tour " + tourId, e);
                });
    }

    @Override
    public void onViewDetails(Tour tour) {
        Toast.makeText(requireContext(), "Mở chi tiết tour: " + tour.getTenTour() + " (ID: " + tour.getMaTour() + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onImageLoad(String imageUrl, ImageView targetView) {
        // TODO: Triển khai logic tải ảnh (Glide/Picasso)
        Log.d(TAG, "Yêu cầu tải ảnh: " + imageUrl);
    }
}