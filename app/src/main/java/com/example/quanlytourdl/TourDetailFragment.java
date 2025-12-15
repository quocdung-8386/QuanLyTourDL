package com.example.quanlytourdl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.quanlytourdl.R;
import com.example.quanlytourdl.model.Tour;
// Các fragment con (TourServiceFragment, TourReviewFragment, TourItineraryFragment)
import com.example.quanlytourdl.TourServiceFragment;
import com.example.quanlytourdl.TourReviewFragment;
import com.example.quanlytourdl.TourItineraryFragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TourDetailFragment extends Fragment {

    private static final String TAG = "TourDetailFragment";
    private static final String ARG_TOUR_ID = "tour_id";
    private String tourId;

    // SỬA ID NÀY CHO PHÙ HỢP VỚI CONTAINER TRONG ACTIVITY CHÍNH
    private static final int FRAGMENT_CONTAINER_ID = R.id.main_content_frame;

    // UI Components
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView imgTourHeader;
    private TextView tvTourTitle, tvTourSubtitle, tvTourLocation, tvTourRating;
    private TabLayout tabLayout;
    private Button btnEditTour;
    private Button btnDeleteTour;
    private FrameLayout fragmentContainer;
    private LinearLayout keyMetricsContainer;
    private View loadingOverlay; // Giữ nguyên, giả định ID R.id.loading_overlay tồn tại

    // Data & Firebase
    private Tour currentTour;
    private FirebaseFirestore db;

    private final SimpleDateFormat durationFormat = new SimpleDateFormat("d 'Ngày' d 'Đêm'", new Locale("vi", "VN"));
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));


    public static TourDetailFragment newInstance(String tourId) {
        TourDetailFragment fragment = new TourDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOUR_ID, tourId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tourId = getArguments().getString(ARG_TOUR_ID);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tour_detail, container, false);

        // 1. Ánh xạ View
        toolbar = view.findViewById(R.id.toolbar);
        collapsingToolbar = view.findViewById(R.id.collapsing_toolbar);
        imgTourHeader = view.findViewById(R.id.img_tour_header);
        tvTourTitle = view.findViewById(R.id.tv_tour_title);
        tvTourSubtitle = view.findViewById(R.id.tv_tour_subtitle);
        tvTourLocation = view.findViewById(R.id.tv_tour_location);
        tvTourRating = view.findViewById(R.id.tv_tour_rating);
        tabLayout = view.findViewById(R.id.tab_layout);
        btnEditTour = view.findViewById(R.id.btn_edit_tour);
        btnDeleteTour = view.findViewById(R.id.btn_delete_tour);
        fragmentContainer = view.findViewById(R.id.fragment_container);
        keyMetricsContainer = view.findViewById(R.id.key_metrics_container);
        //loadingOverlay = view.findViewById(R.id.loading_overlay); // Khôi phục lại việc ánh xạ

        // 2. Thiết lập Toolbar
        setupToolbar();

        // 3. Ẩn TabLayout và container cho đến khi dữ liệu được tải
        tabLayout.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.GONE);

        // 4. Tải dữ liệu Tour từ Firebase
        if (tourId != null && !tourId.isEmpty()) {
            loadTourData(tourId, view);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không có ID Tour.", Toast.LENGTH_LONG).show();
            // ⭐ GIẢI PHÁP SỬA LỖI FATAL EXCEPTION ⭐
            // Hoãn việc gọi onBackPressed() để FragmentManager hoàn tất giao dịch tạo Fragment hiện tại.
            new Handler(Looper.getMainLooper()).post(() -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // 5. Xử lý sự kiện click
        btnEditTour.setOnClickListener(v -> handleEditTour());
        btnDeleteTour.setOnClickListener(v -> handleDeleteTour());

        return view;
    }

    private void loadTourData(String id, View view) {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        db.collection("Tours").document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            try {
                                currentTour = document.toObject(Tour.class);
                                if (currentTour != null) {
                                    currentTour.setMaTour(document.getId());
                                    bindTourData(currentTour, view);

                                    tabLayout.setVisibility(View.VISIBLE);
                                    fragmentContainer.setVisibility(View.VISIBLE);
                                    setupTabLayout();
                                } else {
                                    Toast.makeText(getContext(), "Lỗi mapping dữ liệu Tour.", Toast.LENGTH_LONG).show();
                                    // Sửa lỗi: Gọi onBackPressed an toàn
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        if (getActivity() != null) getActivity().onBackPressed();
                                    });
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi đối tượng Tour", e);
                                Toast.makeText(getContext(), "Lỗi cấu trúc dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                // Sửa lỗi: Gọi onBackPressed an toàn
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    if (getActivity() != null) getActivity().onBackPressed();
                                });
                            }
                        } else {
                            Log.d(TAG, "Không tìm thấy document Tour ID: " + id);
                            Toast.makeText(getContext(), "Không tìm thấy Tour: " + id, Toast.LENGTH_LONG).show();
                            // Sửa lỗi: Gọi onBackPressed an toàn
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (getActivity() != null) getActivity().onBackPressed();
                            });
                        }
                    } else {
                        Log.e(TAG, "Lỗi kết nối Firebase.", task.getException());
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        // Sửa lỗi: Gọi onBackPressed an toàn
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (getActivity() != null) getActivity().onBackPressed();
                        });
                    }
                });
    }

    private void setupToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setTitle("");
            }
        }
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void bindTourData(Tour tour, View view) {
        if (tour == null) return;

        collapsingToolbar.setTitle(tour.getTenTour());

        // Load ảnh (Placeholder)
        // Bạn nên thay thế R.drawable.tour_placeholder_danang bằng thư viện load ảnh (Glide/Picasso) và link URL của tour.
        imgTourHeader.setImageResource(R.drawable.tour_placeholder_danang);

        // Thông tin cơ bản
        tvTourTitle.setText(tour.getTenTour());
        tvTourSubtitle.setText(String.format("%d Ngày %d Đêm", tour.getSoNgay(), tour.getSoDem()));
        tvTourLocation.setText(tour.getDiemDen());
        tvTourRating.setText(String.format(Locale.getDefault(), "%.1f", tour.getRating()));

        // Xử lý hiển thị Metrics
        displayKeyMetrics(tour);
    }

    private void displayKeyMetrics(Tour tour) {
        if (keyMetricsContainer == null) return;

        String[] metricValues = {
                tour.getMaTour(),
                String.format(Locale.getDefault(), "%d/%d", tour.getSoNgay(), tour.getSoDem()),
                currencyFormat.format(tour.getGiaNguoiLon()),
                tour.getStatus()
        };

        String[] metricLabels = {
                "Mã Tour",
                "Ngày/Đêm",
                "Giá NL",
                "Trạng thái"
        };

        int childCount = keyMetricsContainer.getChildCount();

        for (int i = 0; i < childCount && i < metricValues.length; i++) {
            View metricItemView = keyMetricsContainer.getChildAt(i);

            TextView tvValue = metricItemView.findViewById(R.id.tv_metric_value);
            TextView tvLabel = metricItemView.findViewById(R.id.tv_metric_label);

            if (tvValue != null) {
                tvValue.setText(metricValues[i]);
            }
            if (tvLabel != null) {
                tvLabel.setText(metricLabels[i]);
            }
        }
    }

    private void setupTabLayout() {
        if (tabLayout.getTabCount() == 0) {
            tabLayout.addTab(tabLayout.newTab().setText("Lịch trình"));
            tabLayout.addTab(tabLayout.newTab().setText("Dịch vụ"));
            tabLayout.addTab(tabLayout.newTab().setText("Đánh giá"));

            loadTabFragment(tabLayout.getTabAt(0).getText().toString());
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    loadTabFragment(tab.getText().toString());
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadTabFragment(String tabTitle) {
        if (currentTour == null || !isAdded()) return;
        Fragment fragment;

        switch (tabTitle) {
            case "Dịch vụ":
                fragment = TourServiceFragment.newInstance(
                        currentTour.getDichVuBaoGom(),
                        currentTour.getDichVuKhongBaoGom()
                );
                break;
            case "Đánh giá":
                fragment = TourReviewFragment.newInstance(currentTour.getMaTour());
                break;
            case "Lịch trình":
            default:
                fragment = TourItineraryFragment.newInstance(currentTour.getLichTrinhChiTiet());
                break;
        }

        FragmentManager childFragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = childFragmentManager.beginTransaction();

        transaction.replace(R.id.fragment_container, fragment);
        transaction.commitAllowingStateLoss();
    }

    /**
     * ⭐ TRIỂN KHAI MỚI: Xử lý sự kiện click nút Chỉnh sửa Tour.
     */
    private void handleEditTour() {
        if (tourId == null) {
            Toast.makeText(getContext(), "Lỗi: Không có ID Tour để chỉnh sửa.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo EditTourFragment và truyền tourId
        Fragment editFragment = EditTourFragment.newInstance(tourId);

        // 2. Thực hiện giao dịch Fragment
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(FRAGMENT_CONTAINER_ID, editFragment)
                    .addToBackStack("TourDetailToEdit")
                    .commit();

            Log.d(TAG, "Đã chuyển sang EditTourFragment cho Tour ID: " + tourId);
        } else {
            Toast.makeText(getContext(), "Lỗi hệ thống: Không thể mở màn hình chỉnh sửa Tour.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hàm xử lý logic xóa Tour.
     */
    private void handleDeleteTour() {
        if (getContext() == null || currentTour == null) return;

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Xác nhận Xóa Tour")
                .setMessage("Bạn có chắc chắn muốn xóa Tour '" + currentTour.getTenTour() + "' (ID: " + tourId + ") không? Hành động này không thể hoàn tác.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa Tour", (dialog, which) -> {
                    // Thực hiện logic xóa Tour
                    performTourDeletion();
                })
                .show();
    }

    /**
     * Thực hiện xóa Tour khỏi Firestore.
     */
    private void performTourDeletion() {
        if (tourId == null || getContext() == null) return;

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        db.collection("Tours").document(tourId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Tour " + tourId + " đã được xóa thành công.", Toast.LENGTH_LONG).show();

                    // Quay lại màn hình trước đó (Danh sách Tour)
                    // ⭐ Sửa lỗi: Gọi onBackPressed an toàn
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    Log.e(TAG, "Lỗi xóa Tour " + tourId, e);
                    Toast.makeText(getContext(), "Lỗi xóa Tour: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}