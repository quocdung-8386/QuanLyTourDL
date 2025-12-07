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
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

// FIREBASE & MODEL IMPORTS
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TaoTourDetailFullFragment extends Fragment {

    private static final String TAG = "TaoTourDetailFragment";

    // Trạng thái (status) cho Tour mới tạo
    public static final String STATUS_PENDING_APPROVAL = "CHO_PHE_DUYET";
    // Trạng thái sau khi admin phê duyệt
    public static final String STATUS_APPROVED = "DANG_MO_BAN";

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MaterialButton btnPrevStep;
    private MaterialButton btnNextStep;
    private TextView btnLuuNhap;

    private final String[] tabTitles = {"1. Thông tin", "2. Lịch trình", "3. Chi phí", "4. Hình ảnh & XB"};

    public static TaoTourDetailFullFragment newInstance() {
        return new TaoTourDetailFullFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tao_tour_detail_full, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ Views
        Toolbar toolbar = view.findViewById(R.id.toolbar_tao_tour_detail);
        viewPager = view.findViewById(R.id.view_pager_tour_steps);
        tabLayout = view.findViewById(R.id.tab_layout_tour_steps);
        btnPrevStep = view.findViewById(R.id.btn_prev_step);
        btnNextStep = view.findViewById(R.id.btn_next_step);
        btnLuuNhap = view.findViewById(R.id.btn_luu_nhap);

        // Thiết lập Toolbar
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        TourStepsAdapter adapter = new TourStepsAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.setUserInputEnabled(false);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavigationButtons(position);
            }
        });

        btnLuuNhap.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đã lưu nháp dữ liệu tour (Logic đang chờ triển khai).", Toast.LENGTH_SHORT).show();
        });

        btnPrevStep.setOnClickListener(v -> navigateToPrevStep());
        btnNextStep.setOnClickListener(v -> navigateToNextStep());

        updateNavigationButtons(0);
    }

    // --- Logic Điều hướng & Lưu Dữ Liệu ---

    private void updateNavigationButtons(int position) {
        int totalSteps = tabTitles.length;

        btnPrevStep.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);

        if (position == totalSteps - 1) { // Bước cuối cùng
            btnNextStep.setText("Xuất bản Tour");
            btnNextStep.setIcon(null);
            if (getContext() != null) {
                btnNextStep.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
        } else {
            btnNextStep.setText("Tiếp tục");
            try {
                // Giả định ic_arrow_right_24 tồn tại
                btnNextStep.setIconResource(R.drawable.ic_arrow_right_24);
                if (getContext() != null) {
                    btnNextStep.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                }
            } catch (Exception e) {
                Log.w(TAG, "Không tìm thấy ic_arrow_right_24. Dùng icon mặc định.");
                btnNextStep.setIcon(null);
            }
            btnNextStep.setIconGravity(MaterialButton.ICON_GRAVITY_END);
        }
    }

    private void navigateToPrevStep() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem > 0) {
            viewPager.setCurrentItem(currentItem - 1, true);
        }
    }

    private void navigateToNextStep() {
        int currentItem = viewPager.getCurrentItem();
        int totalSteps = tabTitles.length;

        TourStepsAdapter adapter = (TourStepsAdapter) viewPager.getAdapter();
        if (adapter == null) return;
        Fragment currentFragment = adapter.getFragment(currentItem);

        // B1: Thực hiện Validation
        // ...

        // B2: Chuyển trang hoặc Xuất bản
        if (currentItem < totalSteps - 1) {
            viewPager.setCurrentItem(currentItem + 1, true);
        } else {
            // BƯỚC CUỐI CÙNG: Xuất bản
            Toast.makeText(getContext(), "Đang tiến hành Xuất bản Tour...", Toast.LENGTH_SHORT).show();
            publishTourAndSaveToFirestore(adapter);
        }
    }

    /**
     * Thu thập dữ liệu từ tất cả các bước, lưu vào Firestore với trạng thái chờ phê duyệt, và quay lại màn hình trước đó.
     */
    private void publishTourAndSaveToFirestore(TourStepsAdapter adapter) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous_creator";
        String tourId = UUID.randomUUID().toString();
        Date now = new Date();

        // 1. Khởi tạo đối tượng Tour và gán dữ liệu quản lý
        Tour newTour = new Tour();
        newTour.setMaTour(tourId);
        newTour.setNguoiTao(userId);
        newTour.setNgayTao(now);
        newTour.setStatus(STATUS_PENDING_APPROVAL);

        // 2. Gán dữ liệu giả định/placeholders (Cần được thay thế bằng dữ liệu thực tế từ các Fragment)
        newTour.setTenTour("Tour Mới Cần Duyệt " + tourId.substring(0, 4));
        newTour.setDiemKhoiHanh("Hà Nội");
        newTour.setDiemDen("Sapa");
        newTour.setNgayKhoiHanh(new Date(now.getTime() + (7 * 24 * 60 * 60 * 1000)));
        newTour.setSoLuongKhach(10);
        newTour.setGiaTour(5000000);
        newTour.setSeoDescription("Mô tả SEO của tour");
        newTour.setFeatured(false);
        newTour.setImageUrls(Collections.singletonList("https://placehold.co/600x400/000000/FFFFFF?text=Tour+Thumbnail"));
        newTour.setAnhThumbnailUrl(newTour.getImageUrls().get(0));


        // 3. LƯU VÀO FIRESTORE
        db.collection("Tours")
                .document(tourId)
                .set(newTour)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Tour successfully written with ID: " + tourId);

                    // THÔNG BÁO VÀ QUAY LẠI MÀN HÌNH DANH SÁCH
                    Toast.makeText(getContext(), "Tour đã được gửi thành công và đang chờ Ban Quản Trị phê duyệt. Tour sẽ xuất hiện ở mục đang mở bán sau khi được duyệt.", Toast.LENGTH_LONG).show();

                    // Quay lại màn hình trước đó (thoát khỏi form tạo tour)
                    if (getActivity() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing Tour document", e);
                    Toast.makeText(getContext(), "Lỗi Xuất bản Tour: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // --- Adapter cho ViewPager2 ---

    private static class TourStepsAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();

        public TourStepsAdapter(@NonNull Fragment fragment) {
            super(fragment);
            // THAY THẾ DƯỚI ĐÂY BẰNG CÁC CLASS FRAGMENT THỰC TẾ CỦA BẠN
            fragmentList.add(new TaoTourThongTinFragment());      // 1. Thông tin chung
            fragmentList.add(new TaoTourLichTrinhFragment());    // 2. Lịch trình
            fragmentList.add(new TaoTourChiPhiFragment());       // 3. Chi phí
            fragmentList.add(new TaoTourHinhAnhFragment());      // 4. Hình ảnh & Xuất bản
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        public Fragment getFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }
}