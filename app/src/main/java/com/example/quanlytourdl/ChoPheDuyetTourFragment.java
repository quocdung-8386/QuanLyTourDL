package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.example.quanlytourdl.adapter.TourAdapter;
import com.example.quanlytourdl.firebase.FirebaseRepository;
import com.example.quanlytourdl.model.Tour;
import com.example.quanlytourdl.TourDetailFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.example.quanlytourdl.adapter.TourAdapter.STATUS_APPROVED;
import static com.example.quanlytourdl.adapter.TourAdapter.STATUS_REJECTED;

/**
 * Fragment hiển thị danh sách các tour đang chờ Ban Quản Trị phê duyệt.
 * Triển khai TourAdapter.OnTourActionListener để xử lý các sự kiện click từ RecyclerView.
 */
public class ChoPheDuyetTourFragment extends Fragment implements TourAdapter.OnTourActionListener {

    private static final String TAG = "ChoPheDuyetTourFragment";

    private Toolbar toolbar;
    private TextView subtitleTextView;
    private RecyclerView recyclerView;

    private EditText editSearchTour;
    private Chip chipAll;
    private Chip chipDomestic;
    private ChipGroup chipGroupFilters;

    private TourAdapter tourAdapter;
    private List<Tour> fullTourList = new ArrayList<>();
    private List<Tour> filteredTourList = new ArrayList<>();

    private String currentChipFilter = "Tất cả";

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
            Log.e(TAG, "Lỗi inflate layout 'fragment_cho_phe_duyet'", e);
            TextView errorView = new TextView(getContext());
            errorView.setText("Lỗi: Không tìm thấy layout fragment_cho_phe_duyet");
            return errorView;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupToolbar();
        setupRecyclerView();
        setupSearchListener();
        setupChipListeners(view);
        loadTourData();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_cho_phe_duyet);
        subtitleTextView = view.findViewById(R.id.text_subtitle);
        recyclerView = view.findViewById(R.id.recycler_tour_cho_phe_duyet);

        editSearchTour = view.findViewById(R.id.edit_search_tour);
        chipAll = view.findViewById(R.id.chip_all);
        chipDomestic = view.findViewById(R.id.chip_domestic);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
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
            tourAdapter = new TourAdapter(requireContext(), filteredTourList, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(tourAdapter);
        } else {
            Log.e(TAG, "Lỗi: RecyclerView (recycler_tour_cho_phe_duyet) không được tìm thấy.");
        }
    }

    private void setupSearchListener() {
        if (editSearchTour != null) {
            editSearchTour.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String searchQuery = s.toString().trim();
                    filterDataList(searchQuery, currentChipFilter);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupChipListeners(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_filters);
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    currentChipFilter = "Tất cả";
                } else {
                    int checkedId = checkedIds.get(0);
                    Chip checkedChip = group.findViewById(checkedId);
                    if (checkedChip != null) {
                        currentChipFilter = checkedChip.getText().toString();
                    }
                }
                String searchQuery = editSearchTour != null ? editSearchTour.getText().toString().trim() : "";
                filterDataList(searchQuery, currentChipFilter);
            });
        }
    }

    private void loadTourData() {
        if (repository == null) {
            Log.e(TAG, "Lỗi: Repository chưa được khởi tạo.");
            return;
        }
        repository.getToursChoPheDuyet().observe(getViewLifecycleOwner(), tours -> {
            if (tours != null) {
                fullTourList.clear();
                fullTourList.addAll(tours);

                String searchQuery = editSearchTour != null ? editSearchTour.getText().toString().trim() : "";
                filterDataList(searchQuery, currentChipFilter);
            } else {
                if (subtitleTextView != null) {
                    subtitleTextView.setText("Không có tour nào đang chờ");
                }
            }
        });
    }

    private void filterDataList(String searchQuery, String chipFilter) {
        String lowerCaseQuery = searchQuery.toLowerCase(Locale.getDefault());

        List<Tour> chipFilteredList = fullTourList.stream()
                .filter(tour -> {
                    if (chipFilter.equals("Tất cả")) {
                        return true;
                    }

                    if (chipFilter.equals("Tour trong nước")) {
                        // Giả sử tour.isDomestic() là logic kiểm tra tour nội địa
                        return tour.isDomestic();
                    }

                    return false;
                })
                .collect(Collectors.toList());

        filteredTourList.clear();

        if (lowerCaseQuery.isEmpty()) {
            filteredTourList.addAll(chipFilteredList);
        } else {
            chipFilteredList.stream()
                    .filter(tour -> {
                        return (tour.getTenTour() != null && tour.getTenTour().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                                (tour.getMaTour() != null && tour.getMaTour().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery));
                    })
                    .forEach(filteredTourList::add);
        }

        if (tourAdapter != null) {
            tourAdapter.notifyDataSetChanged();
        }

        updateSubtitle(filteredTourList.size());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            repository.removeTourListener();
            Log.d(TAG, "Đã gọi removeTourListener() để dọn dẹp.");
        }
    }

    // --- Triển khai TourAdapter.OnTourActionListener (Callback) ---

    @Override
    public void onApproveReject(String tourId, String tourName, String newStatus, int position) {
        String action = newStatus.equals(STATUS_APPROVED) ? "phê duyệt" : "từ chối";

        repository.updateTourStatus(tourId, newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Đã " + action + " tour: " + tourName, Toast.LENGTH_SHORT).show();

                    if (position >= 0 && position < filteredTourList.size()) {
                        Tour removedTour = filteredTourList.remove(position);
                        fullTourList.remove(removedTour);
                        tourAdapter.notifyItemRemoved(position);

                        updateSubtitle(filteredTourList.size());
                    } else {
                        String searchQuery = editSearchTour != null ? editSearchTour.getText().toString().trim() : "";
                        filterDataList(searchQuery, currentChipFilter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi " + action + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Lỗi " + action + " tour " + tourId, e);
                });
    }

    /**
     * Mở màn hình chi tiết Tour.
     */
    @Override
    public void onViewDetails(Tour tour) {
        Toast.makeText(requireContext(), "Mở chi tiết tour: " + tour.getTenTour(), Toast.LENGTH_LONG).show();

        openTourDetailFragment(tour);
    }

    @Override
    public void onImageLoad(String imageUrl, ImageView targetView) {
        // TODO: Triển khai logic tải ảnh (Glide/Picasso)
        Log.d(TAG, "Yêu cầu tải ảnh: " + imageUrl);
    }

    // --- HÀM HỖ TRỢ CHUYỂN FRAGMENT VÀ UI ---

    private void updateSubtitle(int count) {
        String subtitleText = (count > 0)
                ? "Có " + count + " tour đang chờ"
                : "Không có tour nào đang chờ";
        if (subtitleTextView != null) {
            subtitleTextView.setText(subtitleText);
        }
    }

    /**
     * Hàm chuyển Fragment chung.
     */
    private void openFragment(Fragment targetFragment, String logMessage) {
        if (getParentFragmentManager() != null) {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // KHÔNG NÊN DÙNG getResources().getIdentifier vì nó chậm và có thể trả về 0 nếu không tìm thấy.
            // Nên sử dụng ID FrameLayout đã được định nghĩa.
            int frameId = R.id.main_content_frame; // ⭐ THAY THẾ ID FRAME LAYOUT CỦA ACTIVITY CHÍNH TẠI ĐÂY

            if (frameId != 0) {
                transaction.replace(frameId, targetFragment);
                transaction.addToBackStack(null);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
                Log.d(TAG, logMessage);
            } else {
                Log.e(TAG, "Không tìm thấy ID 'main_content_frame'. Không thể chuyển Fragment.");
            }
        }
    }

    /**
     * Mở Fragment Chi Tiết Tour và truyền ID Tour.
     * SỬA LỖI: Truyền tourId thay vì tour object và sử dụng newInstance an toàn.
     */
    private void openTourDetailFragment(Tour tour) {
        if (getParentFragmentManager() != null && tour != null && tour.getMaTour() != null) {

            // ⭐ SỬ DỤNG HÀM newInstance CÓ SẴN CỦA TourDetailFragment để truyền ID an toàn
            Fragment detailFragment = TourDetailFragment.newInstance(tour.getMaTour());

            // ⭐ THAY THẾ LOG MESSAGE ĐỂ PHẢN ÁNH ĐÚNG ID ĐƯỢC CHUYỂN
            openFragment(detailFragment, "Chuyển sang màn hình Chi Tiết Tour: MT-" + tour.getMaTour());

        } else {
            Log.e(TAG, "Lỗi: Không thể mở TourDetailFragment. Tour object hoặc MaTour bị null.");
            Toast.makeText(requireContext(), "Lỗi: Không có ID Tour để mở chi tiết.", Toast.LENGTH_SHORT).show();
        }
    }
}