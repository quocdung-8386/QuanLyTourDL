package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable; // Thêm import cho TextWatcher
import android.text.TextWatcher; // Thêm import cho TextWatcher
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText; // Thêm import cho EditText
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction; // Thêm import FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip; // Thêm import Chip
import com.google.android.material.chip.ChipGroup; // Thêm import ChipGroup

import com.example.quanlytourdl.adapter.TourAdapter;
import com.example.quanlytourdl.firebase.FirebaseRepository;
import com.example.quanlytourdl.model.Tour;
import com.example.quanlytourdl.TourDetailFragment; // Giả định Fragment chi tiết Tour

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors; // Thêm import cho lọc Stream

// ⭐ STATIC IMPORT CÁC HẰNG SỐ TRẠNG THÁI TỪ TourAdapter
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

    // ⭐ THÊM VIEWS TÌM KIẾM VÀ LỌC
    private EditText editSearchTour;
    private Chip chipAll;
    private Chip chipDomestic;
    private ChipGroup chipGroupFilters; // Thêm ChipGroup

    private TourAdapter tourAdapter;
    private List<Tour> fullTourList = new ArrayList<>(); // Danh sách gốc TỪ FIRESTORE
    private List<Tour> filteredTourList = new ArrayList<>(); // Danh sách đang hiển thị

    private String currentChipFilter = "Tất cả"; // Lưu trạng thái lọc chip hiện tại

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
        // ⭐ SỬ DỤNG LAYOUT ĐÃ CUNG CẤP (Giả định là R.layout.fragment_cho_phe_duyet)
        int layoutId = R.layout.fragment_cho_duyet_tour;
        try {
            return inflater.inflate(layoutId, container, false);
        } catch (Exception e) {
            // ⭐ Đã sửa lại Log và View hiển thị lỗi
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
        setupSearchListener(); // ⭐ THIẾT LẬP LẮNG NGHE TÌM KIẾM
        setupChipListeners(view); // ⭐ THIẾT LẬP LẮNG NGHE CHIP
        loadTourData();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_cho_phe_duyet);
        subtitleTextView = view.findViewById(R.id.text_subtitle);
        recyclerView = view.findViewById(R.id.recycler_tour_cho_phe_duyet);

        // ⭐ Ánh xạ Views mới
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
            // ⭐ Sử dụng filteredTourList cho adapter
            tourAdapter = new TourAdapter(requireContext(), filteredTourList, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(tourAdapter);
        } else {
            Log.e(TAG, "Lỗi: RecyclerView (recycler_tour_cho_phe_duyet) không được tìm thấy.");
        }
    }

    // ⭐ HÀM XỬ LÝ SỰ KIỆN TÌM KIẾM
    private void setupSearchListener() {
        if (editSearchTour != null) {
            editSearchTour.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Áp dụng lọc ngay khi người dùng gõ
                    String searchQuery = s.toString().trim();
                    filterDataList(searchQuery, currentChipFilter);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    // ⭐ HÀM XỬ LÝ SỰ KIỆN CHIP LỌC
    private void setupChipListeners(View view) {
        // Thiết lập OnCheckedChangeListener cho Chip All và Domestic (và các chip khác nếu có)

        ChipGroup chipGroup = view.findViewById(R.id.chip_group_filters);
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    // Xử lý trường hợp không có chip nào được chọn (có thể chọn chip 'Tất cả' mặc định)
                    currentChipFilter = "Tất cả";
                } else {
                    int checkedId = checkedIds.get(0); // Lấy ID của chip được chọn đầu tiên
                    Chip checkedChip = group.findViewById(checkedId);
                    if (checkedChip != null) {
                        currentChipFilter = checkedChip.getText().toString();
                    }
                }
                // Áp dụng lại bộ lọc
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
        // Listener real-time từ Repository
        repository.getToursChoPheDuyet().observe(getViewLifecycleOwner(), tours -> {
            if (tours != null) {
                fullTourList.clear();
                fullTourList.addAll(tours); // Cập nhật danh sách gốc

                // ⭐ Áp dụng lọc ban đầu sau khi tải xong dữ liệu
                String searchQuery = editSearchTour != null ? editSearchTour.getText().toString().trim() : "";
                filterDataList(searchQuery, currentChipFilter);
            } else {
                if (subtitleTextView != null) {
                    subtitleTextView.setText("Không có tour nào đang chờ");
                }
            }
        });
    }

    /**
     * ⭐ HÀM LỌC TỔNG HỢP: Áp dụng cả lọc Chip và Tìm kiếm.
     */
    private void filterDataList(String searchQuery, String chipFilter) {
        String lowerCaseQuery = searchQuery.toLowerCase(Locale.getDefault());

        // 1. Lọc theo Trạng thái Chip (Tour trong nước/Tất cả) trên fullTourList
        List<Tour> chipFilteredList = fullTourList.stream()
                .filter(tour -> {
                    if (chipFilter.equals("Tất cả")) {
                        return true;
                    }

                    // ⭐ Giả định Tour có trường 'isDomestic' hoặc tương tự
                    if (chipFilter.equals("Tour trong nước")) {
                        // Giả sử tour.isDomestic() là logic kiểm tra tour nội địa
                        // Nếu không có, bạn cần logic kiểm tra dựa trên dữ liệu Tour
                        return tour.isDomestic();
                    }

                    // Thêm các loại lọc khác tại đây
                    return false;
                })
                .collect(Collectors.toList());

        // 2. Lọc tiếp theo Chuỗi tìm kiếm (Search)
        filteredTourList.clear();

        if (lowerCaseQuery.isEmpty()) {
            filteredTourList.addAll(chipFilteredList);
        } else {
            chipFilteredList.stream()
                    .filter(tour -> {
                        // Tìm kiếm theo tên hoặc mã tour (phải khớp với hint trong XML)
                        return (tour.getTenTour() != null && tour.getTenTour().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                                (tour.getMaTour() != null && tour.getMaTour().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery));
                    })
                    .forEach(filteredTourList::add);
        }

        // 3. Cập nhật UI
        if (tourAdapter != null) {
            tourAdapter.notifyDataSetChanged();
        }

        // Cập nhật phụ đề (Subtitle)
        String subtitleText = (filteredTourList.size() > 0)
                ? "Có " + filteredTourList.size() + " tour đang chờ"
                : "Không có tour nào đang chờ";
        if (subtitleTextView != null) {
            subtitleTextView.setText(subtitleText);
        }
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

                    // ⭐ Cập nhật danh sách gốc và áp dụng lại bộ lọc
                    // Loại bỏ item khỏi danh sách gốc (fullTourList)
                    // Lưu ý: Việc loại bỏ khỏi filteredTourList cần được xử lý cẩn thận nếu đang dùng LiveData
                    // Cách an toàn nhất là chờ LiveData trigger update (nhưng cần thời gian)
                    // Hoặc xóa thủ công và áp dụng lại filter:
                    if (position >= 0 && position < filteredTourList.size()) {
                        Tour removedTour = filteredTourList.remove(position);
                        fullTourList.remove(removedTour);
                        tourAdapter.notifyItemRemoved(position);

                        // Cập nhật lại subtitle
                        updateSubtitle(filteredTourList.size());
                    } else {
                        // Nếu không tìm thấy, buộc phải tải lại hoặc áp dụng lại filter
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
            int frameId = getResources().getIdentifier("main_content_frame", "id", requireContext().getPackageName());

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
     * Mở Fragment Chi Tiết Tour và truyền đối tượng Tour.
     * GIẢ ĐỊNH: ChiTietTourFragment tồn tại và có thể nhận Tour qua Bundle (Serializable).
     */
    private void openTourDetailFragment(Tour tour) {
        if (getParentFragmentManager() != null) {
            Bundle bundle = new Bundle();
            // Lưu ý: Tour cần phải triển khai Parcelable hoặc Serializable
            bundle.putSerializable("tour_object", tour);

            // Giả định ChiTietTourFragment tồn tại
            Fragment detailFragment = new TourDetailFragment();
            detailFragment.setArguments(bundle);

            openFragment(detailFragment, "Chuyển sang màn hình Chi Tiết Tour: " + tour.getMaTour());
        }
    }
}