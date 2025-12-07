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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    private List<HopDong> hopDongList;

    private FirebaseFirestore db;
    private CollectionReference hopDongRef;
    private ListenerRegistration listenerRegistration;

    // UI elements
    private EditText etSearch;
    private ImageButton btnBack;
    private Button btnFilterAll, btnFilterActive, btnFilterUpcoming, btnFilterExpired;
    private String currentFilter = "Tất cả"; // Biến lưu trạng thái lọc hiện tại

    // Khai báo các hằng số trạng thái
    private static final String TRANG_THAI_TAT_CA = "Tất cả";
    private static final String TRANG_THAI_DANG_HIEU_LUC = "Đang hiệu lực";
    private static final String TRANG_THAI_SAP_HET_HAN = "Sắp hết hạn";
    private static final String TRANG_THAI_DA_HET_HAN = "Đã hết hạn";
    private static final String TRANG_THAI_DA_CHAM_DUT = "Đã Chấm dứt"; // Trạng thái này lưu trong DB

    // Hằng số trạng thái cho Hợp đồng có NCC đã bị xóa (được set từ KinhDoanhFragment)
    private static final String TRANG_THAI_NCC_DA_XOA = "Nhà Cung Cấp Đã Xóa";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // KHỞI TẠO FIRESTORE
        db = FirebaseFirestore.getInstance();
        // Giả định tên collection là "HopDong"
        hopDongRef = db.collection("HopDong");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout fragment_quanlyhopdong (Giả định ID tồn tại)
        int layoutId = getResources().getIdentifier("fragment_quanlyhopdong", "layout", requireContext().getPackageName());
        if (layoutId == 0) {
            Log.e(TAG, "Không tìm thấy layout ID 'fragment_quanlyhopdong'.");
            return null;
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
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().onBackPressed();
                }
            });
        }

        // Thanh tìm kiếm
        etSearch = view.findViewById(R.id.et_search);
        // Thiết lập lắng nghe thay đổi nội dung tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: Triển khai logic tìm kiếm/lọc trên client khi người dùng gõ
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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
            hopDongList = new ArrayList<>();
            // Truyền 'this' (Fragment) để xử lý các sự kiện click
            adapter = new HopDongAdapter(requireContext(), hopDongList, this);
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

        // Thiết lập sự kiện click cho các nút lọc
        View.OnClickListener filterClickListener = v -> {
            // Đặt lại màu sắc cho tất cả các nút
            resetFilterButtonStyles();

            Button clickedButton = (Button) v;
            // Đổi màu sắc cho nút được chọn (Giả định ID R.drawable.bg_filter_button_selected tồn tại)
            int selectedBgId = getResources().getIdentifier("bg_filter_button_selected", "drawable", requireContext().getPackageName());
            int whiteColorId = getResources().getIdentifier("white", "color", requireContext().getPackageName());


            if (selectedBgId != 0) {
                clickedButton.setBackgroundResource(selectedBgId);
            }
            if (whiteColorId != 0) {
                clickedButton.setTextColor(ContextCompat.getColor(requireContext(), whiteColorId));
            }


            currentFilter = clickedButton.getText().toString();
            loadHopDongData(currentFilter);
        };

        btnFilterAll.setOnClickListener(filterClickListener);
        btnFilterActive.setOnClickListener(filterClickListener);
        btnFilterUpcoming.setOnClickListener(filterClickListener);
        btnFilterExpired.setOnClickListener(filterClickListener);

        // Thiết lập trạng thái ban đầu cho nút "Tất cả"
        int selectedBgId = getResources().getIdentifier("bg_filter_button_selected", "drawable", requireContext().getPackageName());
        int whiteColorId = getResources().getIdentifier("white", "color", requireContext().getPackageName());

        if (btnFilterAll != null && selectedBgId != 0) {
            btnFilterAll.setBackgroundResource(selectedBgId);
        }
        if (btnFilterAll != null && whiteColorId != 0) {
            btnFilterAll.setTextColor(ContextCompat.getColor(requireContext(), whiteColorId));
        }
    }

    private void resetFilterButtonStyles() {
        // Giả định R.drawable.bg_filter_button và R.color.grey_text tồn tại
        int defaultBg = getResources().getIdentifier("bg_filter_button", "drawable", requireContext().getPackageName());
        int defaultTextColor = getResources().getIdentifier("grey_text", "color", requireContext().getPackageName());

        if (defaultBg == 0 || defaultTextColor == 0) return;

        int defaultColor = ContextCompat.getColor(requireContext(), defaultTextColor);

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

    /**
     * Chuyển đổi String ngày tháng (dd/MM/yyyy) thành đối tượng Date.
     * @param dateString Chuỗi ngày tháng.
     * @return Đối tượng Date hoặc null nếu lỗi.
     */
    @Nullable
    private Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            // Sử dụng DATE_FORMAT (dd/MM/yyyy) đã định nghĩa
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Lỗi phân tích cú pháp ngày: " + dateString, e);
            return null;
        }
    }

    /**
     * Tính toán trạng thái hợp đồng dựa trên ngày hết hạn (ngayHetHan).
     * @param hopDong Đối tượng HopDong.
     * @return Trạng thái: "Đang hiệu lực", "Sắp hết hạn", hoặc "Đã hết hạn".
     */
    private String getCalculatedContractStatus(HopDong hopDong) {
        Date ngayHetHan = parseDate(hopDong.getNgayHetHan());
        Date currentDate = new Date();

        // Loại bỏ phần giờ, phút, giây khỏi ngày hiện tại để so sánh chính xác hơn
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();

        if (ngayHetHan == null) {
            return TRANG_THAI_DANG_HIEU_LUC;
        }

        // 1. Đã hết hạn
        if (ngayHetHan.before(today)) {
            return TRANG_THAI_DA_HET_HAN;
        }

        // 2. Sắp hết hạn (expires within 30 days)
        // Tính toán ngày 30 ngày tới
        long expiryTime = ngayHetHan.getTime();
        long thirtyDaysFromNow = today.getTime() + THIRTY_DAYS_IN_MILLIS;

        if (expiryTime <= thirtyDaysFromNow) {
            return TRANG_THAI_SAP_HET_HAN;
        }

        // 3. Đang hiệu lực (expires later than 30 days from now)
        return TRANG_THAI_DANG_HIEU_LUC;
    }

    // --- HÀM TẢI DỮ LIỆU TỪ FIRESTORE (REAL-TIME) ---
    private void loadHopDongData(String filterStatus) {
        // Xóa listener cũ nếu có
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        Query query = hopDongRef;

        // Bắt đầu lắng nghe real-time
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
                    hopDongList.clear();
                    int successCount = 0;

                    for (QueryDocumentSnapshot document : snapshots) {
                        try {
                            // Chuyển đổi Document thành đối tượng HopDong
                            HopDong hd = document.toObject(HopDong.class);

                            // Lưu ID Document Firestore (chuỗi ngẫu nhiên) vào một trường riêng
                            hd.setDocumentId(document.getId());

                            // --- KIỂM TRA VÀ BỎ QUA HỢP ĐỒNG CỦA NCC ĐÃ XÓA ---
                            // Điều này loại bỏ các hợp đồng đã bị soft-delete (trước khi hard-delete được triển khai)
                            if (TRANG_THAI_NCC_DA_XOA.equals(hd.getTrangThai())) {
                                Log.d(TAG, "Bỏ qua Hợp Đồng ID " + hd.getDocumentId() + " vì NCC đã xóa.");
                                continue; // Bỏ qua document này và không thêm vào list
                            }


                            String finalStatus;

                            // 1. Ưu tiên trạng thái đã lưu trong DB (đặc biệt là Đã Chấm dứt)
                            if (TRANG_THAI_DA_CHAM_DUT.equals(hd.getTrangThai())) {
                                finalStatus = TRANG_THAI_DA_CHAM_DUT;
                            } else {
                                // 2. Nếu không phải Đã Chấm dứt, tính toán trạng thái dựa trên ngày hết hạn
                                finalStatus = getCalculatedContractStatus(hd);
                                // Gán lại trạng thái đã tính toán cho đối tượng để hiển thị đúng trên UI
                                hd.setTrangThai(finalStatus);
                            }


                            // ÁP DỤNG LỌC HIỂN THỊ
                            boolean shouldAdd = false;

                            if (TRANG_THAI_TAT_CA.equals(filterStatus)) {
                                // Nếu là "Tất cả", thêm tất cả hợp đồng (trừ hợp đồng đã bị loại ở trên)
                                shouldAdd = true;
                            } else if (filterStatus.equals(finalStatus)) {
                                // Nếu filter khớp với trạng thái tính toán, thêm vào list
                                shouldAdd = true;
                            }

                            if (shouldAdd) {
                                hopDongList.add(hd);
                                successCount++;
                            }

                        } catch (Exception ex) {
                            Log.e(TAG, "Lỗi chuyển đổi dữ liệu document ID " + document.getId() + ": " + ex.getMessage());
                        }
                    }
                    adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                    Log.d(TAG, "Đã tải thành công " + successCount + " Hợp Đồng với filter: " + filterStatus);
                }
            }
        });
    }

    // Xóa listener khi Fragment bị hủy để tránh rò rỉ bộ nhớ
    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    // --- TRIỂN KHAI PHƯƠNG THỨC CỦA INTERFACE HopDongAdapter.OnItemActionListener ---

    @Override
    public void onViewClick(HopDong hopDong) {
        // hopDong.getMaHopDong() lúc này sẽ là mã custom (HD-YYYY-XXXXXX)
        Toast.makeText(requireContext(), "Mở chi tiết Hợp đồng: " + hopDong.getMaHopDong(), Toast.LENGTH_SHORT).show();
        // TODO: openViewContractFragment(hopDong.getDocumentId()); // Sử dụng Document ID cho thao tác DB
    }

    @Override
    public void onEditClick(HopDong hopDong) {
        // hopDong.getMaHopDong() lúc này sẽ là mã custom (HD-YYYY-XXXXXX)
        Toast.makeText(requireContext(), "Mở màn hình Sửa Hợp đồng: " + hopDong.getMaHopDong(), Toast.LENGTH_SHORT).show();
        // TODO: openEditContractFragment(hopDong.getDocumentId()); // Sử dụng Document ID cho thao tác DB
    }

    @Override
    public void onDeleteClick(HopDong hopDong) {
        String supplierId = hopDong.getSupplierId();
        // Lấy ID Document thực tế (đã lưu ở bước load data) để chấm dứt/xóa
        String contractId = hopDong.getDocumentId();

        if (supplierId == null || contractId == null) {
            String supplierName = hopDong.getNhaCungCap() != null ? hopDong.getNhaCungCap() : "Không xác định";
            Log.e(TAG, "Lỗi: supplierId hoặc contractId bị thiếu.");
            Toast.makeText(requireContext(), "Lỗi dữ liệu: Không có đủ ID (NCC hoặc Hợp đồng) để chấm dứt.", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(requireContext(), "Chuyển sang màn hình Chấm dứt Hợp đồng. Mã HĐ: " + hopDong.getMaHopDong(), Toast.LENGTH_SHORT).show();
        openTerminateContractFragment(supplierId, contractId);
    }

    // --- HÀM HỖ TRỢ CHUYỂN FRAGMENT ---

    /**
     * Mở Fragment Tạo Hợp Đồng Mới.
     */
    private void openCreateContractFragment() {
        Toast.makeText(requireContext(), "Mở màn hình Tạo Hợp đồng mới", Toast.LENGTH_SHORT).show();
        if (getParentFragmentManager() != null) {
            // Giả định class TaoHopDongFragment tồn tại
            Fragment createFragment = new TaoHopDongFragment();

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // Đảm bảo R.id.main_content_frame là ID Frame layout chứa Fragment chính
            int frameId = getResources().getIdentifier("main_content_frame", "id", requireContext().getPackageName());
            if (frameId != 0) {
                transaction.replace(frameId, createFragment);
                transaction.addToBackStack(null);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
            } else {
                Log.e(TAG, "Không tìm thấy ID 'main_content_frame'. Không thể chuyển Fragment.");
            }
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
            // Key này phải khớp với key mà ChamDutHopDongFragment mong đợi
            bundle.putString("supplier_id", supplierDocumentId);
            bundle.putString("contract_id", contractId);

            // Giả định ChamDutHopDongFragment tồn tại
            ChamDutHopDongFragment terminateFragment = new ChamDutHopDongFragment();
            terminateFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // Đảm bảo sử dụng ID Frame layout chứa Fragment
            int frameId = getResources().getIdentifier("main_content_frame", "id", requireContext().getPackageName());

            if (frameId != 0) {
                transaction.replace(frameId, terminateFragment);
                transaction.addToBackStack(null);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();

                Log.d(TAG, "Chuyển sang màn hình Chấm dứt Hợp đồng. NCC ID: " + supplierDocumentId + ", Contract ID (Document ID): " + contractId);
            } else {
                Log.e(TAG, "Không tìm thấy ID 'main_content_frame'. Không thể chuyển Fragment.");
            }
        }
    }
}