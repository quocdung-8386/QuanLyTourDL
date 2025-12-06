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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fragment Quản lý Hợp đồng, hiển thị danh sách hợp đồng NCC.
 */
public class QuanLyHopDongFragment extends Fragment implements HopDongAdapter.OnItemActionListener {

    private static final String TAG = "QuanLyHopDongFragment";

    private RecyclerView recyclerView;
    private HopDongAdapter adapter;
    private List<HopDong> hopDongList;

    private FirebaseFirestore db;
    private CollectionReference hopDongRef;
    private ListenerRegistration listenerRegistration;

    // UI elements
    private EditText etSearch;
    // Thêm ImageButton cho nút back
    private ImageButton btnBack;
    private Button btnFilterAll, btnFilterActive, btnFilterUpcoming, btnFilterExpired;
    private String currentFilter = "Tất cả"; // Biến lưu trạng thái lọc hiện tại

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // KHỞI TẠO FIRESTORE
        db = FirebaseFirestore.getInstance();
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
        // Nút Quay lại (MỚI)
        btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Sử dụng popBackStack để quay lại Fragment trước đó
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                } else {
                    // Fallback nếu không có Parent Fragment Manager (ít xảy ra)
                    requireActivity().onBackPressed();
                }
            });
        }

        // Thanh tìm kiếm
        etSearch = view.findViewById(R.id.et_search);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            // TODO: Xử lý tìm kiếm khi người dùng nhấn Enter/Search
            String query = etSearch.getText().toString();
            Toast.makeText(requireContext(), "Tìm kiếm: " + query, Toast.LENGTH_SHORT).show();
            // loadHopDongData(currentFilter, query); // Kích hoạt tìm kiếm
            return true;
        });

        // Nút Thêm mới Hợp đồng (FAB)
        View fabAddContract = view.findViewById(R.id.fab_add_contract);
        if (fabAddContract != null) {
            fabAddContract.setOnClickListener(v -> {
                openCreateContractFragment();
            });
        }

        // Nút Thông báo (optional, for completeness)
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
            // TRUYỀN 'this' (FRAGMENT) LÀM LISTENER CHO ADAPTER
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
            // Đặt lại màu sắc cho tất cả các nút về trạng thái không được chọn
            resetFilterButtonStyles();

            Button clickedButton = (Button) v;
            // Đổi màu sắc cho nút được chọn
            // Giả định R.drawable.bg_filter_button_selected là background khi được chọn (ví dụ: nền màu xanh)
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
        // Giả định R.drawable.bg_filter_button_selected là background khi được chọn
        btnFilterAll.setBackgroundResource(R.drawable.bg_filter_button_selected);
        btnFilterAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }

    private void resetFilterButtonStyles() {
        // Giả định R.drawable.bg_filter_button là background mặc định (ví dụ: nền trắng, border xám)
        int defaultBg = R.drawable.bg_filter_button;
        // Giả định R.color.grey_text là màu chữ mặc định
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


    // --- HÀM BỔ SUNG: TẢI DỮ LIỆU TỪ FIRESTORE (REAL-TIME) ---
    private void loadHopDongData(String filterStatus) {
        // Xóa listener cũ nếu có
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        Query query = hopDongRef;

        // Thêm điều kiện lọc
        if ("Đang hiệu lực".equals(filterStatus) || "Đã hết hạn".equals(filterStatus)) {
            // Lọc theo trường "trangThai" (trường hợp này yêu cầu trường trangThai phải tồn tại)
            query = query.whereEqualTo("trangThai", filterStatus);
        } else if ("Sắp hết hạn".equals(filterStatus)) {
            // TODO: Logic phức tạp hơn để lọc "Sắp hết hạn" cần ngày hết hạn (ngayHetHan)
            // Hiện tại, nếu dữ liệu không có trường boolean/string cho trạng thái này, sẽ cần logic tính toán
            // Ví dụ: Lọc ngày hết hạn lớn hơn ngày hiện tại và nhỏ hơn (ngày hiện tại + 30 ngày)
            // query = query.whereGreaterThan("ngayHetHan", new Date()).whereLessThan("ngayHetHan", thirtyDaysLater);
            Log.w(TAG, "Lọc 'Sắp hết hạn' cần logic Firestore phức tạp hơn hoặc tính toán client.");
        }


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
                    for (QueryDocumentSnapshot document : snapshots) {
                        try {
                            // Chuyển đổi Document thành đối tượng HopDong
                            HopDong hd = document.toObject(HopDong.class);
                            hd.setMaHopDong(document.getId());
                            // TODO: Thêm logic tính toán trạng thái "Sắp hết hạn" ở đây nếu cần (tạm thời dựa vào data field)

                            // Chỉ thêm vào list nếu là filter "Tất cả" HOẶC khớp với trạng thái đã lọc (nếu không phải là "Sắp hết hạn")
                            if ("Tất cả".equals(filterStatus) || filterStatus.equals(hd.getTrangThai())) {
                                hopDongList.add(hd);
                            }
                            // Nếu filter là "Sắp hết hạn", cần logic kiểm tra ngày hết hạn thực tế ở đây.

                        } catch (Exception ex) {
                            Log.e(TAG, "Lỗi chuyển đổi dữ liệu document ID " + document.getId() + ": " + ex.getMessage());
                        }
                    }
                    adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                    Log.d(TAG, "Đã tải thành công " + hopDongList.size() + " Hợp Đồng với filter: " + filterStatus);
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
        openTerminateContractFragment(hopDong.getMaNhaCungCap(), hopDong.getMaHopDong());
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
            bundle.putString("supplier_id", supplierId);
            bundle.putString("contract_id", contractId);

            ChamDutHopDongFragment terminateFragment = new ChamDutHopDongFragment();
            terminateFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            // Đảm bảo sử dụng ID Frame layout chứa Fragment
            transaction.replace(R.id.main_content_frame, terminateFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();

            Log.d(TAG, "Chuyển sang màn hình Chấm dứt Hợp đồng. NCC ID: " + supplierId + ", Contract ID: " + contractId);
        }
    }
}