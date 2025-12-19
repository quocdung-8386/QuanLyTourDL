package com.example.quanlytourdl;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.TextView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DanhSachHoaDonFragment extends Fragment {

    // Views
    private RecyclerView rvHoaDon;
    private HoaDonAdapter adapter;
    private ImageView btnBack, btnAdd;
    private EditText edtSearch;
    private ImageButton btnFilter; // Nút phễu lọc ngày
    private AppCompatButton btnFilterAll, btnFilterPaid, btnFilterUnpaid;

    // Data List
    private List<HoaDon> listHoaDonOriginal; // Danh sách gốc từ DB
    private List<HoaDon> listHoaDonDisplay;  // Danh sách đang hiển thị (đã lọc)

    // Filter Variables
    private int currentStatusMode = 0; // 0: All, 1: Paid, 2: Unpaid
    private Date filterFromDate = null;
    private Date filterToDate = null;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danh_sach_hoa_don, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);
        setupRecyclerView();
        setupEvents();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load lại dữ liệu mỗi khi quay lại màn hình này
        loadDataFromFirestore();
    }

    private void initViews(View view) {
        rvHoaDon = view.findViewById(R.id.rvHoaDon);
        btnBack = view.findViewById(R.id.btnBack);
        btnAdd = view.findViewById(R.id.btnAdd);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnFilter = view.findViewById(R.id.btnFilter);

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterPaid = view.findViewById(R.id.btnFilterPaid);
        btnFilterUnpaid = view.findViewById(R.id.btnFilterUnpaid);
    }

    private void setupRecyclerView() {
        rvHoaDon.setLayoutManager(new LinearLayoutManager(getContext()));
        listHoaDonOriginal = new ArrayList<>();
        listHoaDonDisplay = new ArrayList<>();

        adapter = new HoaDonAdapter(getContext(), listHoaDonDisplay, this::openDetailFragment);
        rvHoaDon.setAdapter(adapter);
    }

    private void setupEvents() {
        // Nút Back
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // Nút Thêm mới
        btnAdd.setOnClickListener(v -> {
            int containerId = ((ViewGroup) getView().getParent()).getId();
            getParentFragmentManager().beginTransaction()
                    .replace(containerId, new TaoDonHangFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Tìm kiếm Text
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplayData(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filter Status Buttons
        btnFilterAll.setOnClickListener(v -> updateFilterStatusMode(0, btnFilterAll));
        btnFilterPaid.setOnClickListener(v -> updateFilterStatusMode(1, btnFilterPaid));
        btnFilterUnpaid.setOnClickListener(v -> updateFilterStatusMode(2, btnFilterUnpaid));

        // Filter Date Button (Nút Phễu)
        btnFilter.setOnClickListener(v -> showDateFilterDialog());
    }

    // --- LOGIC LOAD DỮ LIỆU ---
    private void loadDataFromFirestore() {
        db.collection("DonHang")
                .orderBy("ngayTao", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listHoaDonOriginal.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                HoaDon hd = new HoaDon();
                                hd.setMaHoaDon(document.getId());

                                // Tên khách hàng
                                if (document.contains("tenKhachHang"))
                                    hd.setTenKhachHang(document.getString("tenKhachHang"));

                                // Tổng tiền
                                if (document.contains("tongTien"))
                                    hd.setTongTien(document.getDouble("tongTien"));

                                // Ngày tạo (Xử lý cả String và Timestamp)
                                if (document.contains("ngayTao")) {
                                    Object rawDate = document.get("ngayTao");
                                    if (rawDate instanceof Timestamp) {
                                        Date date = ((Timestamp) rawDate).toDate();
                                        hd.setNgayTao(sdf.format(date));
                                    } else if (rawDate instanceof String) {
                                        hd.setNgayTao((String) rawDate);
                                    } else {
                                        hd.setNgayTao("");
                                    }
                                }

                                // Trạng thái (Xử lý cả Number và String cũ)
                                if (document.contains("trangThai")) {
                                    Object rawStatus = document.get("trangThai");
                                    if (rawStatus instanceof Number) {
                                        hd.setTrangThai(((Number) rawStatus).intValue());
                                    } else if (rawStatus instanceof String) {
                                        hd.setTrangThai(convertStatusStringToInt((String) rawStatus));
                                    } else {
                                        hd.setTrangThai(2);
                                    }
                                } else {
                                    hd.setTrangThai(2); // Mặc định chờ xử lý
                                }

                                listHoaDonOriginal.add(hd);
                            } catch (Exception e) {
                                Log.e("ListInvoice", "Error parsing: " + e.getMessage());
                            }
                        }
                        // Sau khi load xong thì chạy lọc
                        filterAndDisplayData(edtSearch.getText().toString());
                    } else {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- LOGIC LỌC NGÀY (DIALOG) ---
    private void showDateFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter_date, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvStartDate = view.findViewById(R.id.tvStartDate);
        TextView tvEndDate = view.findViewById(R.id.tvEndDate);
        AppCompatButton btnReset = view.findViewById(R.id.btnResetFilter);
        AppCompatButton btnApply = view.findViewById(R.id.btnApplyFilter);

        // Hiển thị ngày đã chọn (nếu có)
        if (filterFromDate != null) tvStartDate.setText(sdf.format(filterFromDate));
        if (filterToDate != null) tvEndDate.setText(sdf.format(filterToDate));

        // Sự kiện chọn Ngày Bắt Đầu
        tvStartDate.setOnClickListener(v -> {
            showDatePicker((date) -> {
                // Set giờ về 00:00:00
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                // Lưu tạm vào tag view để lấy sau
                tvStartDate.setTag(cal.getTime());
                tvStartDate.setText(sdf.format(cal.getTime()));
            });
        });

        // Sự kiện chọn Ngày Kết Thúc
        tvEndDate.setOnClickListener(v -> {
            showDatePicker((date) -> {
                // Set giờ về 23:59:59
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);

                tvEndDate.setTag(cal.getTime());
                tvEndDate.setText(sdf.format(cal.getTime()));
            });
        });

        // Nút Đặt lại (Reset)
        btnReset.setOnClickListener(v -> {
            filterFromDate = null;
            filterToDate = null;
            filterAndDisplayData(edtSearch.getText().toString());
            Toast.makeText(getContext(), "Đã hủy bộ lọc ngày", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Nút Áp dụng (Apply)
        btnApply.setOnClickListener(v -> {
            // Ưu tiên lấy từ Tag (chứa giờ chính xác), nếu không có thì parse từ text
            Object startTag = tvStartDate.getTag();
            Object endTag = tvEndDate.getTag();

            try {
                if (startTag != null) filterFromDate = (Date) startTag;
                else if (!tvStartDate.getText().toString().contains("Chọn"))
                    filterFromDate = sdf.parse(tvStartDate.getText().toString());

                if (endTag != null) filterToDate = (Date) endTag;
                else if (!tvEndDate.getText().toString().contains("Chọn")) {
                    Date d = sdf.parse(tvEndDate.getText().toString());
                    Calendar c = Calendar.getInstance();
                    c.setTime(d);
                    c.set(Calendar.HOUR_OF_DAY, 23);
                    c.set(Calendar.MINUTE, 59);
                    c.set(Calendar.SECOND, 59);
                    filterToDate = c.getTime();
                }

                if (filterFromDate != null && filterToDate != null) {
                    if (filterFromDate.after(filterToDate)) {
                        Toast.makeText(getContext(), "Ngày bắt đầu phải nhỏ hơn ngày kết thúc", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    filterAndDisplayData(edtSearch.getText().toString());
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Vui lòng chọn đủ ngày", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        dialog.show();
    }

    // Helper hiển thị DatePicker
    private interface OnDateSelected { void onSelect(Date date); }
    private void showDatePicker(OnDateSelected callback) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            callback.onSelect(cal.getTime());
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // --- LOGIC LỌC TỔNG HỢP (CORE) ---
    private void filterAndDisplayData(String keyword) {
        listHoaDonDisplay.clear();
        String key = keyword.toLowerCase().trim();

        for (HoaDon item : listHoaDonOriginal) {
            // 1. Điều kiện Từ khóa
            boolean matchKey = key.isEmpty() ||
                    (item.getMaHoaDon().toLowerCase().contains(key)) ||
                    (item.getTenKhachHang() != null && item.getTenKhachHang().toLowerCase().contains(key));

            // 2. Điều kiện Trạng thái
            boolean matchStatus = false;
            if (currentStatusMode == 0) matchStatus = true; // All
            else if (currentStatusMode == 1) matchStatus = (item.getTrangThai() == 1); // Paid
            else if (currentStatusMode == 2) matchStatus = (item.getTrangThai() != 1); // Unpaid

            // 3. Điều kiện Thời gian
            boolean matchDate = true;
            if (filterFromDate != null && filterToDate != null && item.getNgayTao() != null) {
                try {
                    Date itemDate = sdf.parse(item.getNgayTao());
                    if (itemDate != null) {
                        // Kiểm tra nằm trong khoảng
                        if (itemDate.before(filterFromDate) || itemDate.after(filterToDate)) {
                            matchDate = false;
                        }
                    }
                } catch (ParseException e) {
                    // Nếu ngày lỗi format thì bỏ qua điều kiện ngày (hoặc set false tùy logic)
                    matchDate = false;
                }
            }

            // Kết hợp tất cả điều kiện
            if (matchKey && matchStatus && matchDate) {
                listHoaDonDisplay.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    // --- UI HELPERS ---
    private void updateFilterStatusMode(int mode, AppCompatButton selectedBtn) {
        this.currentStatusMode = mode;
        // Reset style
        resetBtnStyle(btnFilterAll);
        resetBtnStyle(btnFilterPaid);
        resetBtnStyle(btnFilterUnpaid);
        // Set active style
        selectedBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0EA5E9")));
        selectedBtn.setTextColor(Color.WHITE);
        // Run filter
        filterAndDisplayData(edtSearch.getText().toString());
    }

    private void resetBtnStyle(AppCompatButton btn) {
        btn.setBackgroundTintList(null);
        btn.setBackgroundResource(R.drawable.bg_search_input);
        btn.setTextColor(Color.parseColor("#4B5563"));
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

    private int convertStatusStringToInt(String status) {
        if ("DA_THANH_TOAN".equals(status)) return 1;
        if ("CHO_XU_LY".equals(status)) return 2;
        if ("HUY".equals(status)) return 3;
        return 2;
    }
}