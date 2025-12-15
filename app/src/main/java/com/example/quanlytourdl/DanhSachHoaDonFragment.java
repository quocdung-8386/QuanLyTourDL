package com.example.quanlytourdl;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlytourdl.adapter.HoaDonAdapter;
import com.example.quanlytourdl.model.HoaDon;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DanhSachHoaDonFragment extends Fragment {

    // --- Khai báo View ---
    private RecyclerView rvHoaDon;
    private HoaDonAdapter adapter;
    private List<HoaDon> listHoaDonOriginal; // Danh sách gốc (chứa tất cả dữ liệu từ DB)
    private List<HoaDon> listHoaDonDisplay;  // Danh sách hiển thị (đã qua lọc)

    private ImageView btnBack, btnAdd;
    private EditText edtSearch;
    private ImageButton btnFilter;

    // Các nút trạng thái (Chips)
    private AppCompatButton btnFilterAll, btnFilterPaid, btnFilterUnpaid;

    // Biến lưu trạng thái lọc hiện tại: 0=Tất cả, 1=Đã thanh toán, 2=Chưa thanh toán
    private int currentStatusMode = 0;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danh_sach_hoa_don, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);

        // Thiết lập RecyclerView và Adapter
        rvHoaDon.setLayoutManager(new LinearLayoutManager(getContext()));
        listHoaDonOriginal = new ArrayList<>();
        listHoaDonDisplay = new ArrayList<>();

        // Adapter liên kết với listHoaDonDisplay
        adapter = new HoaDonAdapter(getContext(), listHoaDonDisplay, new HoaDonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HoaDon hoaDon) {
                openDetailFragment(hoaDon);
            }
        });
        rvHoaDon.setAdapter(adapter);

        loadDataFromFirestore();
        setupEvents();

        return view;
    }

    private void initViews(View view) {
        rvHoaDon = view.findViewById(R.id.rvHoaDon);
        btnBack = view.findViewById(R.id.btnBack);
        btnAdd = view.findViewById(R.id.btnAdd);

        edtSearch = view.findViewById(R.id.edtSearch);
        btnFilter = view.findViewById(R.id.btnFilter);

        // Ánh xạ các nút lọc (Đảm bảo ID trong XML đã có)
        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterPaid = view.findViewById(R.id.btnFilterPaid);
        btnFilterUnpaid = view.findViewById(R.id.btnFilterUnpaid);
    }

    // --- HÀM TẢI DỮ LIỆU TỪ FIREBASE ---
    // Thay thế toàn bộ hàm loadDataFromFirestore cũ bằng hàm này
    private void loadDataFromFirestore() {
        db.collection("DonHang") // 1. Kiểm tra kỹ tên Collection trên Firebase có phải là "DonHang" không?
                .orderBy("ngayTao", Query.Direction.DESCENDING)
                // .limit(5) // Tạm thời bỏ limit để test xem có dữ liệu không
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listHoaDonOriginal.clear();

                        // Debug: Kiểm tra xem lấy được bao nhiêu dòng
                        Log.d("DEBUG_APP", "Số lượng đơn tìm thấy: " + task.getResult().size());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                HoaDon hd = new HoaDon();
                                hd.setMaHoaDon(document.getId());

                                // Map dữ liệu an toàn
                                if (document.contains("tenKhachHang"))
                                    hd.setTenKhachHang(document.getString("tenKhachHang"));

                                // Lưu ý: Nếu Firestore lưu số nguyên (vd: 500000), dùng getDouble vẫn được
                                if (document.contains("tongTien"))
                                    hd.setTongTien(document.getDouble("tongTien"));

                                // Xử lý Ngày tháng (Timestamp)
                                if (document.contains("ngayTao")) {
                                    Timestamp timestamp = document.getTimestamp("ngayTao");
                                    if (timestamp != null) {
                                        Date date = timestamp.toDate();
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        hd.setNgayTao(sdf.format(date));
                                    } else {
                                        hd.setNgayTao("---");
                                    }
                                }

                                // Xử lý trạng thái (Sửa tên hàm cho đúng với bên dưới)
                                if (document.contains("trangThai")) {
                                    String statusStr = document.getString("trangThai");
                                    hd.setTrangThai(convertStatusStringToInt(statusStr));
                                }

                                listHoaDonOriginal.add(hd);
                            } catch (Exception e) {
                                Log.e("DEBUG_APP", "Lỗi parse data: " + e.getMessage());
                            }
                        }

                        // QUAN TRỌNG NHẤT: Phải gọi hàm này để đổ dữ liệu sang list hiển thị
                        filterAndDisplayData(edtSearch.getText().toString());

                    } else {
                        Log.e("DEBUG_APP", "Lỗi tải dữ liệu: ", task.getException());
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper: Chuyển trạng thái String -> Int
    private int convertStatusStringToInt(String status) {
        if ("DA_THANH_TOAN".equals(status)) return 1;
        if ("CHO_XU_LY".equals(status)) return 2;
        if ("HUY".equals(status)) return 3;
        return 2; // Mặc định là chờ
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> { if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack(); });

        btnAdd.setOnClickListener(v -> {
            // Mở fragment tạo đơn
            int containerId = ((ViewGroup) getView().getParent()).getId();
            getParentFragmentManager().beginTransaction()
                    .replace(containerId, new TaoDonHangFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 1. Sự kiện Tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Mỗi khi gõ phím, gọi hàm lọc
                filterAndDisplayData(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 2. Sự kiện Click nút Lọc
        btnFilterAll.setOnClickListener(v -> updateFilterMode(0, btnFilterAll));
        btnFilterPaid.setOnClickListener(v -> updateFilterMode(1, btnFilterPaid));
        btnFilterUnpaid.setOnClickListener(v -> updateFilterMode(2, btnFilterUnpaid));
    }

    // Hàm cập nhật chế độ lọc và đổi màu nút
    private void updateFilterMode(int mode, AppCompatButton selectedBtn) {
        this.currentStatusMode = mode;

        // Reset màu tất cả nút về mặc định (Xám/Trắng)
        resetButtonStyle(btnFilterAll);
        resetButtonStyle(btnFilterPaid);
        resetButtonStyle(btnFilterUnpaid);

        // Đổi màu nút được chọn thành Xanh dương
        selectedBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0EA5E9")));
        selectedBtn.setTextColor(Color.WHITE);

        // Gọi lại hàm lọc dữ liệu
        filterAndDisplayData(edtSearch.getText().toString());
    }

    private void resetButtonStyle(AppCompatButton btn) {
        btn.setBackgroundTintList(null); // Xóa tint để hiện drawable gốc
        btn.setBackgroundResource(R.drawable.bg_search_input); // Viền xám nền trắng
        btn.setTextColor(Color.parseColor("#4B5563")); // Chữ xám đậm
    }

    // --- LOGIC LỌC TRUNG TÂM ---
    // Kết hợp cả từ khóa tìm kiếm VÀ trạng thái nút bấm
    private void filterAndDisplayData(String keyword) {
        listHoaDonDisplay.clear();
        String key = keyword.toLowerCase().trim();

        for (HoaDon item : listHoaDonOriginal) {
            // 1. Kiểm tra từ khóa (Mã hoặc Tên)
            boolean matchKey = key.isEmpty() ||
                    (item.getMaHoaDon().toLowerCase().contains(key)) ||
                    (item.getTenKhachHang().toLowerCase().contains(key));

            // 2. Kiểm tra trạng thái (Dựa vào nút đang chọn)
            boolean matchStatus = false;
            if (currentStatusMode == 0) {
                matchStatus = true; // "Tất cả" -> Lấy hết
            } else if (currentStatusMode == 1) {
                matchStatus = (item.getTrangThai() == 1); // Chỉ lấy Đã thanh toán
            } else if (currentStatusMode == 2) {
                matchStatus = (item.getTrangThai() != 1); // Lấy Chờ xử lý hoặc Hủy
            }

            // Nếu thỏa mãn CẢ HAI điều kiện -> Thêm vào list hiển thị
            if (matchKey && matchStatus) {
                listHoaDonDisplay.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void openDetailFragment(HoaDon hoaDon) {
        ChiTietHoaDonFragment detailFragment = new ChiTietHoaDonFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("hoa_don_data", hoaDon);
        detailFragment.setArguments(bundle);

        int containerId = ((ViewGroup) getView().getParent()).getId();
        getParentFragmentManager().beginTransaction()
                .replace(containerId, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}