package com.example.quanlytourdl;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Imports Firebase Firestore
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import com.example.quanlytourdl.adapter.HopDongAdapter;
import com.example.quanlytourdl.model.HopDong; // Import model HopDong
// ⭐ Import các Fragment và Model giả định cần thiết cho DetailFragment
import com.example.quanlytourdl.DetailFragment;
import com.example.quanlytourdl.TaoHopDongFragment;
import com.example.quanlytourdl.SuaHopDongFragment;
import com.example.quanlytourdl.ChamDutHopDongFragment;
import com.example.quanlytourdl.model.Guide;
import com.example.quanlytourdl.model.Vehicle;
import com.example.quanlytourdl.model.NhaCungCap;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors; // Import cần thiết cho lọc Stream

/**
 * Fragment Quản lý Hợp đồng, hiển thị danh sách hợp đồng NCC.
 */
public class QuanLyHopDongFragment extends Fragment implements HopDongAdapter.OnItemActionListener {

    private static final String TAG = "QuanLyHopDongFragment";
    // Đảm bảo định dạng ngày tháng này khớp với dữ liệu trong Firestore
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final long THIRTY_DAYS_IN_MILLIS = TimeUnit.DAYS.toMillis(30);

    private RecyclerView recyclerView;
    private HopDongAdapter adapter;
    private List<HopDong> fullHopDongList; // Danh sách gốc TỪ FIRESTORE (sau khi lọc NCC đã xóa)
    private List<HopDong> filteredHopDongList; // Danh sách đang hiển thị (sau khi lọc Trạng thái và Tìm kiếm)

    private FirebaseFirestore db;
    private CollectionReference hopDongRef;
    private ListenerRegistration listenerRegistration;

    // UI elements
    private EditText etSearch;
    private ImageButton btnBack;
    private Button btnFilterAll, btnFilterActive, btnFilterUpcoming, btnFilterExpired;
    private String currentFilter = "Tất cả"; // Biến lưu trạng thái lọc hiện tại

    // Khai báo các hằng số trạng thái (Sử dụng tên hiển thị trên nút để đồng bộ)
    private static final String TRANG_THAI_TAT_CA = "Tất cả";
    private static final String TRANG_THAI_DANG_HIEU_LUC = "Đang hiệu lực"; // Trùng với tên nút Active
    private static final String TRANG_THAI_SAP_HET_HAN = "Sắp hết hạn"; // Trùng với tên nút Upcoming
    private static final String TRANG_THAI_DA_HET_HAN = "Đã hết hạn"; // Trùng với tên nút Expired

    // Trạng thái LƯU trong DB
    private static final String TRANG_THAI_DA_CHAM_DUT_DB = "Đã Chấm dứt";

    // Hằng số trạng thái cho Hợp đồng có NCC đã bị xóa (được set từ KinhDoanhFragment)
    private static final String TRANG_THAI_NCC_DA_XOA = "Nhà Cung Cấp Đã Xóa";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // KHỞI TẠO FIRESTORE
        db = FirebaseFirestore.getInstance();
        hopDongRef = db.collection("HopDong");

        fullHopDongList = new ArrayList<>();
        filteredHopDongList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout fragment_quanlyhopdong (Giả định ID tồn tại)
        int layoutId = getResources().getIdentifier("fragment_quanlyhopdong", "layout", requireContext().getPackageName());
        if (layoutId == 0) {
            Log.e(TAG, "Không tìm thấy layout ID 'fragment_quanlyhopdong'.");
            // Có thể return một View trống để tránh crash nếu layout không tìm thấy
            return new View(requireContext());
        }

        View view = inflater.inflate(layoutId, container, false);

        // Ánh xạ View và Thiết lập RecyclerView
        setupViews(view);
        setupRecyclerView(view);
        setupFilterButtons(view);

        // Tải dữ liệu ban đầu
        loadHopDongData(currentFilter);

        return view;
    }

    private void setupViews(View view) {
        // Nút Quay lại
        btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null && getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().onBackPressed();
                }
            });
        }

        // Thanh tìm kiếm
        etSearch = view.findViewById(R.id.et_search);
        // Thiết lập lắng nghe thay đổi nội dung tìm kiếm
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Áp dụng lọc ngay khi người dùng gõ
                    String searchQuery = s.toString().trim();
                    filterDataList(searchQuery); // <-- GỌI HÀM LỌC TÌM KIẾM
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }


        // Nút Thêm mới Hợp đồng (FAB)
        View fabAddContract = view.findViewById(R.id.fab_add_contract);
        if (fabAddContract != null) {
            fabAddContract.setOnClickListener(v -> {
                openCreateContractFragment();
            });
        }

        // Nút Thông báo
        ImageButton btnNotification = view.findViewById(R.id.btn_notification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Mở thông báo", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_contracts);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            // Sử dụng filteredHopDongList cho adapter
            adapter = new HopDongAdapter(requireContext(), filteredHopDongList, this);
            recyclerView.setAdapter(adapter);
        } else {
            Log.e(TAG, "Không tìm thấy RecyclerView với ID: recycler_contracts");
        }
    }

    private void setupFilterButtons(View view) {
        // Giả định các ID nút lọc tồn tại
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterActive = view.findViewById(R.id.btn_filter_active);
        btnFilterUpcoming = view.findViewById(R.id.btn_filter_upcoming);
        btnFilterExpired = view.findViewById(R.id.btn_filter_expired);

        // Lấy ID tài nguyên màu và drawable
        int selectedBgId = getResources().getIdentifier("bg_filter_button_selected", "drawable", requireContext().getPackageName());
        int defaultBg = getResources().getIdentifier("bg_filter_button", "drawable", requireContext().getPackageName());
        int whiteColorId = getResources().getIdentifier("white", "color", requireContext().getPackageName());
        int defaultTextColor = getResources().getIdentifier("grey_text", "color", requireContext().getPackageName());

        if (selectedBgId == 0 || defaultBg == 0 || whiteColorId == 0 || defaultTextColor == 0) {
            Log.e(TAG, "Thiếu tài nguyên màu hoặc drawable cho nút lọc.");
            return;
        }

        int whiteColor = ContextCompat.getColor(requireContext(), whiteColorId);
        int defaultColor = ContextCompat.getColor(requireContext(), defaultTextColor);

        View.OnClickListener filterClickListener = v -> {
            // Đặt lại màu sắc cho tất cả các nút
            resetFilterButtonStyles(defaultBg, defaultColor);

            Button clickedButton = (Button) v;
            // Đổi màu sắc cho nút được chọn
            clickedButton.setBackgroundResource(selectedBgId);
            clickedButton.setTextColor(whiteColor);

            currentFilter = clickedButton.getText().toString();
            // Áp dụng lọc ngay trên danh sách đã tải
            String searchQuery = etSearch != null ? etSearch.getText().toString().trim() : "";
            filterDataList(searchQuery);
        };

        if (btnFilterAll != null) btnFilterAll.setOnClickListener(filterClickListener);
        if (btnFilterActive != null) btnFilterActive.setOnClickListener(filterClickListener);
        if (btnFilterUpcoming != null) btnFilterUpcoming.setOnClickListener(filterClickListener);
        if (btnFilterExpired != null) btnFilterExpired.setOnClickListener(filterClickListener);

        // Thiết lập trạng thái ban đầu cho nút "Tất cả"
        if (btnFilterAll != null) {
            btnFilterAll.setBackgroundResource(selectedBgId);
            btnFilterAll.setTextColor(whiteColor);
        }
    }

    private void resetFilterButtonStyles(int defaultBg, int defaultColor) {
        if (btnFilterAll != null) {
            btnFilterAll.setBackgroundResource(defaultBg);
            btnFilterAll.setTextColor(defaultColor);
        }
        if (btnFilterActive != null) {
            btnFilterActive.setBackgroundResource(defaultBg);
            btnFilterActive.setTextColor(defaultColor);
        }
        if (btnFilterUpcoming != null) {
            btnFilterUpcoming.setBackgroundResource(defaultBg);
            btnFilterUpcoming.setTextColor(defaultColor);
        }
        if (btnFilterExpired != null) {
            btnFilterExpired.setBackgroundResource(defaultBg);
            btnFilterExpired.setTextColor(defaultColor);
        }
    }

    // --- HÀM TIỆN ÍCH XỬ LÝ NGÀY THÁNG VÀ TRẠNG THÁI ---

    @Nullable
    private Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            // Chú ý: Định dạng mặc định là dd/MM/yyyy
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Lỗi parse ngày: " + dateString, e);
            return null;
        }
    }

    // ⭐ Hàm Tối Ưu Hóa: Thay thế hàm cũ và nhận các giá trị ngày tháng đã tính toán trước
    private String getCalculatedContractStatus(HopDong hopDong, Date today, long thirtyDaysFromNow) {
        // Ưu tiên trạng thái Đã Chấm dứt nếu đã được lưu trong DB
        if (TRANG_THAI_DA_CHAM_DUT_DB.equals(hopDong.getTrangThai())) {
            return TRANG_THAI_DA_CHAM_DUT_DB;
        }

        Date ngayHetHan = parseDate(hopDong.getNgayHetHan());

        if (ngayHetHan == null) {
            // Nếu không có ngày hết hạn, mặc định là đang hiệu lực
            return TRANG_THAI_DANG_HIEU_LUC;
        }

        // 1. Đã hết hạn
        if (ngayHetHan.before(today)) {
            return TRANG_THAI_DA_HET_HAN;
        }

        // 2. Sắp hết hạn (expires within 30 days)
        long expiryTime = ngayHetHan.getTime();

        if (expiryTime <= thirtyDaysFromNow) {
            return TRANG_THAI_SAP_HET_HAN;
        }

        // 3. Đang hiệu lực
        return TRANG_THAI_DANG_HIEU_LUC;
    }


    // --- HÀM TẢI DỮ LIỆU TỪ FIRESTORE (REAL-TIME) ---
    private void loadHopDongData(String filterStatus) {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        Query query = hopDongRef;

        listenerRegistration = query.addSnapshotListener(new com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable com.google.firebase.firestore.QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Lỗi lắng nghe Firestore: ", e);
                    Toast.makeText(requireContext(), "Lỗi tải dữ liệu real-time: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (snapshots != null) {
                    fullHopDongList.clear(); // Xóa danh sách gốc để nạp lại
                    int totalCount = 0;

                    // ⭐ TỐI ƯU HÓA: Tính toán ngày hiện tại VÀ 30 ngày từ hiện tại MỘT LẦN
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    final Date today = cal.getTime();
                    final long thirtyDaysFromNow = today.getTime() + THIRTY_DAYS_IN_MILLIS;

                    for (QueryDocumentSnapshot document : snapshots) {
                        try {
                            HopDong hd = document.toObject(HopDong.class);
                            hd.setDocumentId(document.getId());

                            // Bỏ qua Hợp đồng của NCC đã bị xóa (soft-delete)
                            if (TRANG_THAI_NCC_DA_XOA.equals(hd.getTrangThai())) {
                                continue;
                            }

                            // Gán trạng thái đã tính toán (hoặc Đã Chấm dứt) vào trường 'trangThai'
                            // ⭐ GỌI HÀM ĐÃ TỐI ƯU VỚI THAM SỐ NGÀY THÁNG ĐÃ TÍNH TOÁN
                            hd.setTrangThai(getCalculatedContractStatus(hd, today, thirtyDaysFromNow));

                            // Thêm vào danh sách GỐC (full list)
                            fullHopDongList.add(hd);
                            totalCount++;

                        } catch (Exception ex) {
                            Log.e(TAG, "Lỗi chuyển đổi dữ liệu document ID " + document.getId() + ": " + ex.getMessage());
                        }
                    }

                    // ⭐ SAU KHI NẠP DỮ LIỆU GỐC, ÁP DỤNG LỌC TRẠNG THÁI VÀ TÌM KIẾM ⭐
                    String searchQuery = etSearch != null ? etSearch.getText().toString().trim() : "";
                    filterDataList(searchQuery);

                    Log.d(TAG, "Đã tải thành công " + totalCount + " Hợp Đồng. Áp dụng filter: " + filterStatus);
                }
            }
        });
    }

    private void filterDataList(String searchQuery) {
        String lowerCaseQuery = searchQuery.toLowerCase(Locale.getDefault());

        // 1. Lọc theo Trạng thái (currentFilter)
        List<HopDong> statusFilteredList = fullHopDongList.stream()
                .filter(hd -> {
                    if (TRANG_THAI_TAT_CA.equals(currentFilter)) {
                        return true;
                    }

                    String hdStatus = hd.getTrangThai();

                    // Nếu người dùng chọn lọc "Đã hết hạn", chúng ta bao gồm cả trạng thái "Đã Chấm dứt"
                    if (TRANG_THAI_DA_HET_HAN.equals(currentFilter)) {
                        return TRANG_THAI_DA_HET_HAN.equals(hdStatus) || TRANG_THAI_DA_CHAM_DUT_DB.equals(hdStatus);
                    }

                    return currentFilter.equals(hdStatus);
                })
                .collect(Collectors.toList());

        // 2. Lọc tiếp theo Chuỗi tìm kiếm (Search)
        filteredHopDongList.clear();

        if (lowerCaseQuery.isEmpty()) {
            filteredHopDongList.addAll(statusFilteredList);
        } else {
            statusFilteredList.stream()
                    .filter(hd -> {
                        // Tìm kiếm theo Mã Hợp đồng hoặc Tên Nhà Cung Cấp
                        return (hd.getMaHopDong() != null && hd.getMaHopDong().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                                (hd.getNhaCungCap() != null && hd.getNhaCungCap().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery));
                    })
                    .forEach(filteredHopDongList::add);
        }

        // 3. Cập nhật RecyclerView
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    // --- TRIỂN KHAI PHƯƠNG THỨC CỦA INTERFACE HopDongAdapter.OnItemActionListener ---

    /**
     * ⭐ CẬP NHẬT: Xử lý sự kiện xem chi tiết bằng DetailFragment.
     * Sử dụng phương thức newInstance với 4 tham số (Guide, Vehicle, NhaCungCap, HopDong).
     * Chỉ truyền đối tượng HopDong (tham số thứ tư), các tham số khác là null.
     */
    @Override
    public void onViewClick(HopDong hopDong) {
        Toast.makeText(requireContext(), "Mở chi tiết Hợp đồng: " + hopDong.getMaHopDong(), Toast.LENGTH_SHORT).show();

        // Khai báo các đối tượng null để đảm bảo đủ 4 tham số (Guide, Vehicle, NhaCungCap)
        final Guide nullGuide = null;
        final Vehicle nullVehicle = null;
        final NhaCungCap nullNcc = null; // Giả định NhaCungCap đã được import

        if (getParentFragmentManager() != null) {
            // Giả định DetailFragment tồn tại và có phương thức newInstance 4 tham số
            // DetailFragment.newInstance(Guide, Vehicle, NhaCungCap, HopDong)
            Fragment detailFragment = DetailFragment.newInstance(nullGuide, nullVehicle, nullNcc, hopDong);

            // Sử dụng hàm chuyển Fragment chung
            openFragment(detailFragment, "Mở màn hình Chi tiết Hợp đồng: " + hopDong.getMaHopDong());
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không thể mở màn hình chi tiết (Fragment Manager null).", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditClick(HopDong hopDong) {
        Toast.makeText(requireContext(), "Mở màn hình Sửa Hợp đồng: " + hopDong.getMaHopDong(), Toast.LENGTH_SHORT).show();
        openEditContractFragment(hopDong.getDocumentId()); // Gọi hàm mới
    }

    @Override
    public void onDeleteClick(HopDong hopDong) {
        String supplierId = hopDong.getSupplierId();
        String contractId = hopDong.getDocumentId();

        if (supplierId == null || contractId == null) {
            Toast.makeText(requireContext(), "Lỗi dữ liệu: Không có đủ ID (NCC hoặc Hợp đồng) để chấm dứt.", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(requireContext(), "Chuyển sang màn hình Chấm dứt Hợp đồng. Mã HĐ: " + hopDong.getMaHopDong(), Toast.LENGTH_SHORT).show();
        openTerminateContractFragment(supplierId, contractId);
    }

    // --- HÀM HỖ TRỢ CHUYỂN FRAGMENT ---

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
     * Mở Fragment Tạo Hợp Đồng Mới.
     */
    private void openCreateContractFragment() {
        // Giả định class TaoHopDongFragment tồn tại
        Fragment createFragment = new TaoHopDongFragment();
        openFragment(createFragment, "Mở màn hình Tạo Hợp đồng mới");
    }

    /**
     * Mở Fragment Sửa Hợp Đồng và truyền ID Document của Hợp đồng.
     * @param contractId ID Document Firebase của Hợp Đồng.
     */
    private void openEditContractFragment(String contractId) {
        if (getParentFragmentManager() != null) {
            Bundle bundle = new Bundle();
            // Key này phải khớp với key mà SuaHopDongFragment mong đợi
            bundle.putString("contract_id", contractId);

            // Giả định SuaHopDongFragment tồn tại
            Fragment editFragment = new SuaHopDongFragment();
            editFragment.setArguments(bundle);

            openFragment(editFragment, "Chuyển sang màn hình Sửa Hợp đồng. Contract ID (Document ID): " + contractId);
        }
    }


    /**
     * Mở Fragment Chấm Dứt Hợp Đồng và truyền ID Nhà cung cấp VÀ ID Hợp đồng.
     * @param supplierDocumentId ID Document Firebase của Nhà Cung Cấp.
     * @param contractId ID Document Firebase của Hợp Đồng (Document ID).
     */
    private void openTerminateContractFragment(String supplierDocumentId, String contractId) {
        if (getParentFragmentManager() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("supplier_id", supplierDocumentId);
            bundle.putString("contract_id", contractId);

            // Giả định ChamDutHopDongFragment tồn tại
            ChamDutHopDongFragment terminateFragment = new ChamDutHopDongFragment();
            terminateFragment.setArguments(bundle);

            openFragment(terminateFragment, "Chuyển sang màn hình Chấm dứt Hợp đồng. NCC ID: " + supplierDocumentId + ", Contract ID (Document ID): " + contractId);
        }
    }
}