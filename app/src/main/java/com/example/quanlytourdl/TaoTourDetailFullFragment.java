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
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TaoTourDetailFullFragment extends Fragment {

    private static final String TAG = "TaoTourDetailFragment";

    // 💡 Hằng số Trạng thái
    public static final String STATUS_PENDING_APPROVAL = "CHO_PHE_DUYET";
    public static final String STATUS_DRAFT = "NHAP";
    public static final String STATUS_APPROVED = "DANG_MO_BAN";

    // ⭐ Thành phần View
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MaterialButton btnPrevStep;
    private MaterialButton btnNextStep;
    private TextView btnLuuNhap;

    // ⭐ Dữ liệu
    private final String[] tabTitles = {"1. Thông tin", "2. Lịch trình", "3. Chi phí", "4. Hình ảnh & XB"};
    // Đối tượng Tour TẠM THỜI để lưu trữ dữ liệu (Trong thực tế nên dùng ViewModel)
    private final Tour currentTourData = new Tour();

    // ⭐ Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // --- Interface Bắt buộc cho các Fragment Bước Con ---

    /**
     * Interface bắt buộc các Fragment bước con phải implement để Fragment cha có thể
     * yêu cầu thu thập dữ liệu và validation.
     */
    public interface TourStepDataCollector {
        /**
         * Thu thập dữ liệu từ Fragment này và gán vào đối tượng Tour đã cho.
         * @param tour Đối tượng Tour để gán dữ liệu vào.
         * @return true nếu dữ liệu hợp lệ và đã được gán, false nếu validation thất bại.
         */
        boolean collectDataAndValidate(Tour tour);
    }

    public static TaoTourDetailFullFragment newInstance() {
        return new TaoTourDetailFullFragment();
    }

    // --- Life Cycle ---

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Khởi tạo các trường quản lý cơ bản cho Tour mới
        String tourId = UUID.randomUUID().toString();
        currentTourData.setMaTour(tourId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Giả định R.layout.fragment_tao_tour_detail_full tồn tại
        return inflater.inflate(R.layout.fragment_tao_tour_detail_full, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar(view);
        setupViewPager();
        setupListeners();

        updateNavigationButtons(0);
    }

    private void initViews(View view) {
        viewPager = view.findViewById(R.id.view_pager_tour_steps);
        tabLayout = view.findViewById(R.id.tab_layout_tour_steps);
        btnPrevStep = view.findViewById(R.id.btn_prev_step);
        btnNextStep = view.findViewById(R.id.btn_next_step);
        btnLuuNhap = view.findViewById(R.id.btn_luu_nhap);
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_tao_tour_detail);
        if (toolbar != null) {
            // Giả định R.drawable.ic_arrow_back_24 tồn tại
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }
    }

    private void setupViewPager() {
        TourStepsAdapter adapter = new TourStepsAdapter(this);
        viewPager.setAdapter(adapter);

        // Chặn vuốt ngang, chỉ cho phép chuyển trang bằng nút bấm
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
    }

    private void setupListeners() {
        TourStepsAdapter adapter = (TourStepsAdapter) viewPager.getAdapter();
        if (adapter == null) return;

        btnLuuNhap.setOnClickListener(v -> saveTourAsDraft(adapter));
        btnPrevStep.setOnClickListener(v -> navigateToPrevStep());
        btnNextStep.setOnClickListener(v -> navigateToNextStep(adapter));
    }


    // --- Logic Điều hướng & UI ---

    private void updateNavigationButtons(int position) {
        int totalSteps = tabTitles.length;

        // Nút quay lại chỉ hiện từ bước 1 trở đi
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
                // Giả định R.drawable.ic_arrow_right_24 tồn tại
                btnNextStep.setIconResource(R.drawable.ic_arrow_right_24);
                if (getContext() != null) {
                    btnNextStep.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                }
            } catch (Exception e) {
                Log.w(TAG, "Missing icon resource for next button.");
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

    private void navigateToNextStep(TourStepsAdapter adapter) {
        int currentItem = viewPager.getCurrentItem();
        int totalSteps = tabTitles.length;

        Fragment currentFragment = adapter.getFragment(currentItem);

        // 1. Thực hiện Validation và Thu thập dữ liệu cho bước hiện tại
        if (!(currentFragment instanceof TourStepDataCollector)) {
            Log.e(TAG, "Fragment step " + currentItem + " does not implement TourStepDataCollector.");
            Toast.makeText(getContext(), "Lỗi hệ thống: Fragment thiếu cơ chế thu thập dữ liệu.", Toast.LENGTH_LONG).show();
            return;
        }

        TourStepDataCollector collector = (TourStepDataCollector) currentFragment;

        if (collector.collectDataAndValidate(currentTourData)) {
            // Dữ liệu hợp lệ:
            if (currentItem < totalSteps - 1) {
                // CHUYỂN BƯỚC: Sang trang tiếp theo
                viewPager.setCurrentItem(currentItem + 1, true);
            } else {
                // BƯỚC CUỐI CÙNG: Xuất bản
                Toast.makeText(getContext(), "Đang tiến hành Xuất bản Tour...", Toast.LENGTH_SHORT).show();
                publishTourAndSaveToFirestore(currentTourData);
            }
        } else {
            // Dữ liệu không hợp lệ: Validation thất bại, Fragment con nên đã hiển thị lỗi.
            Toast.makeText(getContext(), "Vui lòng hoàn thành đầy đủ và chính xác các thông tin ở bước này.", Toast.LENGTH_SHORT).show();
        }
    }


    // --- Logic Lưu và Xuất bản Tour ---

    /**
     * Lặp qua tất cả Fragment đang hoạt động để thu thập dữ liệu và lưu Tour với status "NHAP".
     */
    private void saveTourAsDraft(TourStepsAdapter adapter) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous_creator";
        Date now = new Date();

        // 1. Chuẩn bị đối tượng Tour (Sử dụng ID đã tạo trong onCreate)
        currentTourData.setNguoiTao(userId);
        currentTourData.setNgayTao(now); // Cập nhật ngày tạo/cập nhật nháp
        currentTourData.setStatus(STATUS_DRAFT);

        // 2. Thu thập dữ liệu hiện có từ các bước đã hoàn thành
        for (int i = 0; i < adapter.getItemCount(); i++) {
            Fragment fragment = adapter.getFragment(i);
            if (fragment instanceof TourStepDataCollector) {
                // Thu thập dữ liệu, bỏ qua kết quả validation nghiêm ngặt
                ((TourStepDataCollector) fragment).collectDataAndValidate(currentTourData);
            }
        }

        // 3. LƯU VÀO FIRESTORE
        saveTourToFirestore(currentTourData, "Đã lưu nháp Tour thành công!", "Lỗi lưu nháp Tour: ");
    }


    /**
     * Chuẩn bị Tour để xuất bản (status: CHO_PHE_DUYET) và lưu vào Firestore.
     */
    private void publishTourAndSaveToFirestore(Tour tourToPublish) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous_creator";
        Date now = new Date();

        // 1. Gán lại các trường quản lý cuối cùng
        tourToPublish.setNguoiTao(userId);
        tourToPublish.setNgayTao(now);
        tourToPublish.setStatus(STATUS_PENDING_APPROVAL);

        // 2. LƯU VÀO FIRESTORE
        String successMsg = "Tour đã được gửi thành công và đang chờ Ban Quản Trị phê duyệt.";
        String errorMsgPrefix = "Lỗi Xuất bản Tour: ";

        saveTourToFirestore(tourToPublish, successMsg, errorMsgPrefix);
    }

    /**
     * Logic chung để lưu đối tượng Tour vào Firestore.
     */
    private void saveTourToFirestore(Tour tour, String successToastMessage, String failureToastPrefix) {
        if (tour.getMaTour() == null || tour.getMaTour().isEmpty()) {
            Log.e(TAG, "Tour ID cannot be null or empty during save.");
            Toast.makeText(getContext(), "Lỗi hệ thống: Không thể tạo ID cho Tour.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("Tours")
                .document(tour.getMaTour())
                .set(tour)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Tour successfully saved with ID: " + tour.getMaTour());
                    Toast.makeText(getContext(), successToastMessage, Toast.LENGTH_LONG).show();

                    // Nếu xuất bản thành công, thoát khỏi form tạo tour
                    if (tour.getStatus().equals(STATUS_PENDING_APPROVAL) && getActivity() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving Tour document", e);
                    Toast.makeText(getContext(), failureToastPrefix + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // --- Adapter cho ViewPager2 ---

    private static class TourStepsAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final FragmentManager fragmentManager;

        public TourStepsAdapter(@NonNull Fragment fragment) {
            super(fragment);
            // Ép kiểu để truy cập đối tượng Tour được chia sẻ
            TaoTourDetailFullFragment parentFragment = (TaoTourDetailFullFragment) fragment;
            this.fragmentManager = fragment.getChildFragmentManager();

            // ⭐ TRUYỀN ĐỐI TƯỢNG TOUR VÀO CONSTRUCTOR CỦA MỖI BƯỚC
            // (Giả lập các Fragment con, cần tạo file thực tế)
            fragmentList.add(new TaoTourThongTinFragment(parentFragment.currentTourData));
            fragmentList.add(new TaoTourLichTrinhFragment(parentFragment.currentTourData));
            fragmentList.add(new TaoTourChiPhiFragment(parentFragment.currentTourData));
            fragmentList.add(new TaoTourHinhAnhFragment(parentFragment.currentTourData));
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        /**
         * Lấy Fragment đang hoạt động (đã được ViewPager2 khởi tạo) bằng Tag mặc định.
         * @param position Vị trí Fragment.
         * @return Fragment instance.
         */
        public Fragment getFragment(int position) {
            // FragmentStateAdapter sử dụng tag dạng "f" + itemId
            String tag = "f" + getItemId(position);
            return fragmentManager.findFragmentByTag(tag);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }
}