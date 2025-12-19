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

import com.example.quanlytourdl.model.Tour;
import com.example.quanlytourdl.TourServiceFragment;
import com.example.quanlytourdl.TourReviewFragment;
import com.example.quanlytourdl.TourItineraryFragment;

import java.text.NumberFormat;
import java.util.Locale;

public class TourDetailFragment extends Fragment {

    private static final String TAG = "TourDetailFragment";
    private static final String ARG_TOUR_ID = "tour_id";
    private String tourId;

    // QUAN TRỌNG: ID này dùng để chuyển sang màn hình Edit (nằm ở Activity chứa Fragment này)
    private static final int MAIN_ACTIVITY_CONTAINER_ID = R.id.main_content_frame;

    // UI Components
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView imgTourHeader;
    private TextView tvTourTitle, tvTourSubtitle, tvTourLocation, tvTourRating;
    private TabLayout tabLayout;
    private Button btnEditTour, btnDeleteTour;
    private FrameLayout tabContentContainer; // FrameLayout chứa nội dung của Tab
    private LinearLayout keyMetricsContainer;

    // Data & Firebase
    private Tour currentTour;
    private FirebaseFirestore db;
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

        // 1. Ánh xạ View (Đã sửa ID để khớp với XML của bạn)
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
        keyMetricsContainer = view.findViewById(R.id.key_metrics_container);

        // SỬA LỖI CRASH: Tìm đúng ID fragment_container từ file XML của bạn
        tabContentContainer = view.findViewById(R.id.fragment_container);

        // 2. Thiết lập Toolbar
        setupToolbar();

        // 3. Kiểm tra an toàn và ẩn UI cho đến khi nạp xong dữ liệu
        if (tabLayout != null) tabLayout.setVisibility(View.GONE);
        if (tabContentContainer != null) {
            tabContentContainer.setVisibility(View.GONE);
        } else {
            Log.e(TAG, "LỖI: Không tìm thấy R.id.fragment_container trong layout XML");
        }

        // 4. Tải dữ liệu
        if (tourId != null && !tourId.isEmpty()) {
            loadTourData(tourId, view);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không có ID Tour.", Toast.LENGTH_LONG).show();
            goBack();
        }

        // 5. Click listeners
        btnEditTour.setOnClickListener(v -> handleEditTour());
        btnDeleteTour.setOnClickListener(v -> handleDeleteTour());

        return view;
    }

    private void loadTourData(String id, View view) {
        db.collection("Tours").document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            currentTour = document.toObject(Tour.class);
                            if (currentTour != null) {
                                currentTour.setMaTour(document.getId());
                                bindTourData(currentTour);

                                if (tabLayout != null) tabLayout.setVisibility(View.VISIBLE);
                                if (tabContentContainer != null) tabContentContainer.setVisibility(View.VISIBLE);
                                setupTabLayout();
                            }
                        } else {
                            Toast.makeText(getContext(), "Không tìm thấy Tour", Toast.LENGTH_SHORT).show();
                            goBack();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lỗi kết nối Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindTourData(Tour tour) {
        collapsingToolbar.setTitle(tour.getTenTour());
        tvTourTitle.setText(tour.getTenTour());
        tvTourSubtitle.setText(String.format("%d Ngày %d Đêm", tour.getSoNgay(), tour.getSoDem()));
        tvTourLocation.setText(tour.getDiemDen());
        tvTourRating.setText(String.format(Locale.getDefault(), "%.1f", tour.getRating()));
        displayKeyMetrics(tour);
    }

    private void setupTabLayout() {
        if (tabLayout == null || tabLayout.getTabCount() > 0) return;

        tabLayout.addTab(tabLayout.newTab().setText("Lịch trình"));
        tabLayout.addTab(tabLayout.newTab().setText("Dịch vụ"));
        tabLayout.addTab(tabLayout.newTab().setText("Đánh giá"));

        loadTabFragment("Lịch trình");

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadTabFragment(tab.getText().toString());
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
                fragment = TourServiceFragment.newInstance(currentTour.getDichVuBaoGom(), currentTour.getDichVuKhongBaoGom());
                break;
            case "Đánh giá":
                fragment = TourReviewFragment.newInstance(currentTour.getMaTour());
                break;
            default:
                fragment = TourItineraryFragment.newInstance(currentTour.getLichTrinhChiTiet());
                break;
        }

        // SỬA LỖI: Sử dụng R.id.fragment_container thay vì main_content_frame
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    private void handleEditTour() {
        if (tourId == null) return;
        Fragment editFragment = EditTourFragment.newInstance(tourId);
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(MAIN_ACTIVITY_CONTAINER_ID, editFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void handleDeleteTour() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc muốn xóa tour này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("Tours").document(tourId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Đã xóa tour", Toast.LENGTH_SHORT).show();
                                goBack();
                            });
                }).show();
    }

    private void setupToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        toolbar.setNavigationOnClickListener(v -> goBack());
    }

    private void goBack() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void displayKeyMetrics(Tour tour) {
        if (keyMetricsContainer == null) return;
        String[] values = {tour.getMaTour(), tour.getSoNgay() + "/" + tour.getSoDem(),
                currencyFormat.format(tour.getGiaNguoiLon()), tour.getStatus()};
        String[] labels = {"Mã Tour", "Ngày/Đêm", "Giá NL", "Trạng thái"};

        for (int i = 0; i < keyMetricsContainer.getChildCount() && i < values.length; i++) {
            View item = keyMetricsContainer.getChildAt(i);
            TextView v = item.findViewById(R.id.tv_metric_value);
            TextView l = item.findViewById(R.id.tv_metric_label);
            if (v != null) v.setText(values[i]);
            if (l != null) l.setText(labels[i]);
        }
    }
}