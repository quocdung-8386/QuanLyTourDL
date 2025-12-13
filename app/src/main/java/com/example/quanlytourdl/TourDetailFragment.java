package com.example.quanlytourdl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

// Giả định mô hình Tour đã được định nghĩa
import com.example.quanlytourdl.model.Tour;
import com.example.quanlytourdl.model.TourMockData; // Giả định có lớp tạo dữ liệu mẫu

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TourDetailFragment extends Fragment {

    private static final String ARG_TOUR_ID = "tour_id";
    private String tourId;

    // UI Components
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView imgTourHeader;
    private TextView tvTourTitle, tvTourSubtitle, tvTourLocation, tvTourRating;
    private TabLayout tabLayout;
    private Button btnViewStatistics, btnEditTour;
    private LinearLayout tourFeaturesContainer, keyMetricsContainer;
    private FrameLayout fragmentContainer; // Dùng cho nội dung Tab

    // Data
    private Tour currentTour;
    private final SimpleDateFormat durationFormat = new SimpleDateFormat("d 'Ngày' d 'Đêm'", new Locale("vi", "VN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));


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
        // ⭐ Tải dữ liệu Tour (Tạm thời dùng Mock Data)
        currentTour = TourMockData.getTourById(tourId);
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
        btnViewStatistics = view.findViewById(R.id.btn_view_statistics);
        btnEditTour = view.findViewById(R.id.btn_edit_tour);
        tourFeaturesContainer = view.findViewById(R.id.tour_features_container); // Giữ lại nếu cần xử lý động
        keyMetricsContainer = view.findViewById(R.id.key_metrics_container); // Giữ lại nếu cần xử lý động
        fragmentContainer = view.findViewById(R.id.fragment_container);

        // 2. Thiết lập Toolbar (Cần thiết lập cho Activity nếu Fragment nhúng trong Activity)
        setupToolbar();

        // 3. Đổ dữ liệu Tour lên UI
        if (currentTour != null) {
            bindTourData(currentTour);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy thông tin Tour: " + tourId, Toast.LENGTH_LONG).show();
            // Quay lại màn hình trước
            if (getActivity() != null) getActivity().onBackPressed();
            return view;
        }

        // 4. Thiết lập TabLayout và View Container
        setupTabLayout();

        // 5. Xử lý sự kiện click
        btnViewStatistics.setOnClickListener(v -> handleViewStatistics());
        btnEditTour.setOnClickListener(v -> handleEditTour());

        return view;
    }

    private void setupToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setTitle(""); // Đặt tiêu đề ban đầu là rỗng
            }
        }
        // Xử lý nút Back/Navigation Icon
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void bindTourData(Tour tour) {
        // Collapsing Toolbar Title
        collapsingToolbar.setTitle(tour.getTenTour());

        // Header Image (Sử dụng Glide/Picasso trong thực tế)
        imgTourHeader.setImageResource(R.drawable.tour_placeholder_danang);

        // Main Info
        tvTourTitle.setText(tour.getTenTour());
        // Giả sử tvTourSubtitle hiển thị Số ngày/đêm
        tvTourSubtitle.setText(String.format("%d Ngày %d Đêm", tour.getSoNgay(), tour.getSoDem()));
        tvTourLocation.setText(tour.getDiemDen());
        // Giả sử có trường rating
        tvTourRating.setText("4.8"); // Thay bằng tour.getRating()

        // Metrics (Cần ánh xạ chi tiết các TextView/ImageView bên trong các <include> layouts)
        // Ví dụ: updateMetricItem(view.findViewById(R.id.metric_ma_tour), "Mã Tour", tour.getMaTour());
    }

    private void setupTabLayout() {
        // Thêm các tab (Đảm bảo các tab này khớp với nội dung bạn muốn load vào FrameLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Lịch trình"));
        tabLayout.addTab(tabLayout.newTab().setText("Dịch vụ"));
        tabLayout.addTab(tabLayout.newTab().setText("Đánh giá"));

        // Load Fragment mặc định (Lịch trình)
        loadTabFragment("Lịch trình");

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

    /**
     * Tải Fragment nội dung vào FrameLayout bên trong NestedScrollView
     */
    private void loadTabFragment(String tabTitle) {
        Fragment fragment;
        switch (tabTitle) {
            case "Dịch vụ":
                // Giả định bạn có TourServiceFragment
                fragment = new TourServiceFragment();
                break;
            case "Đánh giá":
                // Giả định bạn có TourReviewFragment
                fragment = new TourReviewFragment();
                break;
            case "Lịch trình":
            default:
                // Giả định bạn có TourItineraryFragment
                fragment = TourItineraryFragment.newInstance(currentTour.getLichTrinhChiTiet());
                break;
        }

        FragmentManager childFragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = childFragmentManager.beginTransaction();

        // Sử dụng FrameLayout ID từ layout XML
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void handleViewStatistics() {
        Toast.makeText(getContext(), "Chức năng: Mở màn hình Thống kê cho Tour " + tourId, Toast.LENGTH_SHORT).show();
        // Triển khai logic mở Fragment/Activity Thống kê
    }

    private void handleEditTour() {
        Toast.makeText(getContext(), "Chức năng: Mở màn hình Chỉnh sửa Tour " + tourId, Toast.LENGTH_SHORT).show();
        // Triển khai logic chuyển sang Fragment Chỉnh sửa Tour (ví dụ: TaoTourFragment)
    }
}
// Giả định các Fragment con: TourServiceFragment, TourReviewFragment, TourItineraryFragment tồn tại.
// Giả định lớp TourMockData và Tour tồn tại.