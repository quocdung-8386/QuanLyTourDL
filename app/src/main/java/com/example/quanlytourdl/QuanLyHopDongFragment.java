package com.example.quanlytourdl;

import android.os.Bundle;
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
import com.example.quanlytourdl.model.HopDong;

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
 * FIX: Thêm logic tính toán trạng thái (Đang hiệu lực, Sắp hết hạn, Đã hết hạn) trên client.
 */
public class QuanLyHopDongFragment extends Fragment implements HopDongAdapter.OnItemActionListener {

    private static final String TAG = "QuanLyHopDongFragment";
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
        // Sử dụng layout fragment_quanlyhopdong
        View view = inflater.inflate(R.layout.fragment_quanlyhopdong, container, false);

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
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            // Xử lý tìm kiếm khi người dùng nhấn Enter/Search
            String query = etSearch.getText().toString();
            Toast.makeText(requireContext(), "Tìm kiếm: " + query, Toast.LENGTH_SHORT).show();
            // Tạm thời, ta chỉ lọc theo trạng thái, logic tìm kiếm nâng cao sẽ thêm sau
            // loadHopDongData(currentFilter, query);
            return true;
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
            adapter = new HopDongAdapter(requireContext(), hopDongList, this);
            recyclerView.setAdapter(adapter);
        } else {
            Log.e(TAG, "Không tìm thấy RecyclerView với ID: recycler_contracts");
        }
    }

    private void setupFilterButtons(View view) {
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
            clickedButton.setBackgroundResource(R.drawable.bg_filter_button_selected);
            clickedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            currentFilter = clickedButton.getText().toString();
            loadHopDongData(currentFilter);
        };

        btnFilterAll.setOnClickListener(filterClickListener);
        btnFilterActive.setOnClickListener(filterClickListener);
        btnFilterUpcoming.setOnClickListener(filterClickListener);
        btnFilterExpired.setOnClickListener(filterClickListener);

        // Thiết lập trạng thái ban đầu cho nút "Tất cả"
        btnFilterAll.setBackgroundResource(R.drawable.bg_filter_button_selected);
        btnFilterAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }

    private void resetFilterButtonStyles() {
        // Giả định R.drawable.bg_filter_button và R.color.grey_text tồn tại
        int defaultBg = R.drawable.bg_filter_button;
        int defaultTextColor = ContextCompat.getColor(requireContext(), R.color.grey_text);

        btnFilterAll.setBackgroundResource(defaultBg);
        btnFilterActive.setBackgroundResource(defaultBg);
        btnFilterUpcoming.setBackgroundResource(defaultBg);
        btnFilterExpired.setBackgroundResource(defaultBg);

        btnFilterAll.setTextColor(defaultTextColor);
        btnFilterActive.setTextColor(defaultTextColor);
        btnFilterUpcoming.setTextColor(defaultTextColor);
        btnFilterExpired.setTextColor(defaultTextColor);
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
    private String getContractStatus(HopDong hopDong) {
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
            // Không có ngày hết hạn, giả định đang hiệu lực
            return "Đang hiệu lực";
        }

        // 1. Đã hết hạn
        if (ngayHetHan.before(today)) {
            return "Đã hết hạn";
        }

        // 2. Sắp hết hạn (expires within 30 days)
        // Tính toán ngày 30 ngày tới
        long expiryTime = ngayHetHan.getTime();
        long thirtyDaysFromNow = today.getTime() + THIRTY_DAYS_IN_MILLIS;

        if (expiryTime <= thirtyDaysFromNow) {
            return "Sắp hết hạn";
        }

        // 3. Đang hiệu lực (expires later than 30 days from now)
        return "Đang hiệu lực";
    }

    // --- HÀM TẢI DỮ LIỆU TỪ FIRESTORE (REAL-TIME) ---
    private void loadHopDongData(String filterStatus) {
        // Xóa listener cũ nếu có
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        // Tải TẤT CẢ dữ liệu, vì việc lọc theo Date String trong Firestore không hiệu quả
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
                            // Chuyển đổi Document thành đối tượng HopDong (đã fix kiểu String cho ngày)
                            HopDong hd = document.toObject(HopDong.class);
                            hd.setMaHopDong(document.getId());

                            // TÍNH TOÁN TRẠNG THÁI TRÊN CLIENT
                            String calculatedStatus = getContractStatus(hd);

                            // ÁP DỤNG LỌC
                            boolean shouldAdd = false;

                            if ("Tất cả".equals(filterStatus)) {
                                // Nếu là "Tất cả", thêm tất cả hợp đồng
                                shouldAdd = true;
                            } else if (filterStatus.equals(calculatedStatus)) {
                                // Nếu filter khớp với trạng thái tính toán, thêm vào list
                                shouldAdd = true;
                            }

                            if (shouldAdd) {
                                hopDongList.add(hd);
                                successCount++;
                            }

                        } catch (Exception ex) {
                            // Lỗi này giờ chỉ xảy ra nếu có lỗi cấu trúc dữ liệu khác, không phải lỗi Date/String
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
        Toast.makeText(requireContext(), "Mở chi tiết Hợp đồng: " + hopDong.getMaHopDong(), Toast.LENGTH_SHORT).show();
        // TODO: openViewContractFragment(hopDong.getMaHopDong());
    }

    @Override
    public void onEditClick(HopDong hopDong) {
        Toast.makeText(requireContext(), "Mở màn hình Sửa Hợp đồng: " + hopDong.getMaHopDong(), Toast.LENGTH_SHORT).show();
        // TODO: openEditContractFragment(hopDong.getMaHopDong());
    }

    @Override
    public void onDeleteClick(HopDong hopDong) {
        // Tương tự logic cũ: Chuyển sang màn hình Chấm dứt/Xóa
        Toast.makeText(requireContext(), "Chuyển sang màn hình Chấm dứt Hợp đồng/Xóa: " + hopDong.getMaHopDong(), Toast.LENGTH_SHORT).show();
        // Sử dụng TenNhaCungCap và MaHopDong (đã fix)
        openTerminateContractFragment(hopDong.getTenNhaCungCap(), hopDong.getMaHopDong());
    }

    // --- HÀM HỖ TRỢ CHUYỂN FRAGMENT ---

    /**
     * Mở Fragment Tạo Hợp Đồng Mới.
     */
    private void openCreateContractFragment() {
        // Giả định có Fragment TaoHopDongFragment
        Toast.makeText(requireContext(), "Mở màn hình Tạo Hợp đồng mới", Toast.LENGTH_SHORT).show();
        if (getParentFragmentManager() != null) {
            TaoHopDongFragment createFragment = new TaoHopDongFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // Đảm bảo R.id.main_content_frame là ID Frame layout chứa Fragment chính
            transaction.replace(R.id.main_content_frame, createFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
        }
    }

    /**
     * Mở Fragment Chấm Dứt Hợp Đồng và truyền ID Nhà cung cấp VÀ ID Hợp đồng.
     */
    private void openTerminateContractFragment(String supplierId, String contractId) {
        if (getParentFragmentManager() != null) {
            Bundle bundle = new Bundle();
            // Sử dụng TenNhaCungCap thay vì ID Nhà cung cấp nếu đó là những gì bạn có
            bundle.putString("supplier_name", supplierId);
            bundle.putString("contract_id", contractId);

            ChamDutHopDongFragment terminateFragment = new ChamDutHopDongFragment();
            terminateFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // Đảm bảo sử dụng ID Frame layout chứa Fragment
            transaction.replace(R.id.main_content_frame, terminateFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();

            Log.d(TAG, "Chuyển sang màn hình Chấm dứt Hợp đồng. NCC: " + supplierId + ", Contract ID: " + contractId);
        }
    }
}