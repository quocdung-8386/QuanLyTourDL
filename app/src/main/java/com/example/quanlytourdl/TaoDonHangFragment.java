package com.example.quanlytourdl;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quanlytourdl.model.KhachHang;
import com.example.quanlytourdl.model.Tour;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
// import com.bumptech.glide.Glide; // Bỏ comment nếu bạn dùng Glide để load ảnh

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaoDonHangFragment extends Fragment {

    // --- KHAI BÁO VIEW THEO XML ---
    private ImageView btnBack;

    // Khách hàng
    private EditText edtSearchKhach;
    private ImageButton btnAddKhachMoi;

    // Chọn Tour
    private RelativeLayout btnChonTour;
    private TextView tvSelectedTour; // Text hiển thị trong ô dropdown

    // CardView Thông tin Tour
    private ImageView imgTourThumb;
    private TextView tvTenTour, tvMaTour, tvGiaTourDisplay, tvSlotConLai;

    // Số lượng khách
    private EditText edtNguoiLon, edtTreEm;

    // Thời gian
    private EditText edtNgayDi, edtNgayVe;

    // Nút xác nhận
    private Button btnSubmit;

    // --- BIẾN DỮ LIỆU ---
    private FirebaseFirestore db;
    private Tour selectedTour = null;
    private KhachHang selectedKhachHang = null;
    private List<Tour> tourList = new ArrayList<>();
    private List<KhachHang> khachHangList = new ArrayList<>();
    private NumberFormat currencyFormatter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tao_don_hang, container, false);

        db = FirebaseFirestore.getInstance();
        currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        initViews(view);
        setupEvents();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);

        // Khách hàng
        edtSearchKhach = view.findViewById(R.id.edtSearchKhach);
        btnAddKhachMoi = view.findViewById(R.id.btnAddKhachMoi);

        // Chọn Tour & Hiển thị
        btnChonTour = view.findViewById(R.id.btnChonTour);
        tvSelectedTour = view.findViewById(R.id.tvSelectedTour);

        imgTourThumb = view.findViewById(R.id.imgTourThumb);
        tvTenTour = view.findViewById(R.id.tvTenTour);
        tvMaTour = view.findViewById(R.id.tvMaTour);
        tvGiaTourDisplay = view.findViewById(R.id.tvGiaTourDisplay);
        tvSlotConLai = view.findViewById(R.id.tvSlotConLai);

        // Số lượng
        edtNguoiLon = view.findViewById(R.id.edtNguoiLon);
        edtTreEm = view.findViewById(R.id.edtTreEm);

        // Ngày tháng
        edtNgayDi = view.findViewById(R.id.edtNgayDi);
        edtNgayVe = view.findViewById(R.id.edtNgayVe);

        btnSubmit = view.findViewById(R.id.btnSubmit);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // Chọn ngày
        edtNgayDi.setOnClickListener(v -> showDatePicker(edtNgayDi));
        edtNgayVe.setOnClickListener(v -> showDatePicker(edtNgayVe));

        // Chọn Tour
        btnChonTour.setOnClickListener(v -> showTourSelectionDialog());

        // Chọn Khách Hàng (Click vào ô search hoặc nút thêm đều mở dialog chọn)
        View.OnClickListener selectCustomerAction = v -> showCustomerSelectionDialog();
        btnAddKhachMoi.setOnClickListener(selectCustomerAction);
        edtSearchKhach.setOnClickListener(selectCustomerAction);

        // Tạo Đơn
        btnSubmit.setOnClickListener(v -> submitOrder());
    }

    // =========================================================================
    // LOGIC CHỌN TOUR
    // =========================================================================
    private void showTourSelectionDialog() {
        // Load tour trạng thái DANG_MO_BAN
        db.collection("Tours")
                .whereEqualTo("status", "DANG_MO_BAN")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tourList.clear();
                        List<String> tourNames = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Tour tour = document.toObject(Tour.class);
                                tour.setMaTour(document.getId());
                                tourList.add(tour);
                                // Hiển thị tên trong dialog
                                tourNames.add(tour.getTenTour());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (tourList.isEmpty()) {
                            Toast.makeText(getContext(), "Không có tour nào khả dụng.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Hiển thị Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Chọn Tour Du Lịch");
                        builder.setItems(tourNames.toArray(new String[0]), (dialog, which) -> {
                            selectedTour = tourList.get(which);
                            updateTourUI(selectedTour);
                        });
                        builder.show();
                    }
                });
    }

    private void updateTourUI(Tour tour) {
        if (tour == null) return;

        // Cập nhật ô chọn (Dropdown box)
        tvSelectedTour.setText(tour.getTenTour());

        // Cập nhật CardView chi tiết
        tvTenTour.setText(tour.getTenTour());
        tvMaTour.setText("Mã: " + (tour.getMaTour() != null ? tour.getMaTour() : "---"));

        String gia = currencyFormatter.format(tour.getGiaNguoiLon());
        tvGiaTourDisplay.setText(gia + " đ/khách");

        // Tính slot còn lại
        int conTrong = tour.getSoLuongKhachToiDa() - tour.getSoLuongKhachHienTai();
        if (conTrong < 0) conTrong = 0;
        tvSlotConLai.setText("Còn trống: " + conTrong);

        // Load ảnh (Nếu bạn có dùng thư viện Glide hoặc Picasso)
        /*
        if (tour.getHinhAnhChinhUrl() != null && !tour.getHinhAnhChinhUrl().isEmpty()) {
            Glide.with(getContext()).load(tour.getHinhAnhChinhUrl()).into(imgTourThumb);
        }
        */

        // Tự động điền ngày đi/về nếu trong Tour có sẵn (Tùy chọn)
        // if (tour.getNgayKhoiHanh() != null) ...
    }

    // =========================================================================
    // LOGIC CHỌN KHÁCH HÀNG
    // =========================================================================
    private void showCustomerSelectionDialog() {
        db.collection("khachhang").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                khachHangList.clear();
                List<String> customerInfos = new ArrayList<>();

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    KhachHang kh = doc.toObject(KhachHang.class);
                    kh.setId(doc.getId());
                    khachHangList.add(kh);
                    customerInfos.add(kh.getTen() + " - " + kh.getSdt());
                }

                if (khachHangList.isEmpty()) {
                    Toast.makeText(getContext(), "Chưa có dữ liệu khách hàng.", Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Chọn Khách Hàng");
                builder.setItems(customerInfos.toArray(new String[0]), (dialog, which) -> {
                    selectedKhachHang = khachHangList.get(which);
                    // Hiển thị lên EditText Search
                    edtSearchKhach.setText(selectedKhachHang.getTen() + " (" + selectedKhachHang.getSdt() + ")");
                });
                builder.show();
            }
        });
    }

    // =========================================================================
    // LOGIC TẠO ĐƠN VÀ TÍNH TIỀN
    // =========================================================================
    private void submitOrder() {
        // 1. Validate
        if (selectedTour == null) {
            Toast.makeText(getContext(), "Vui lòng chọn Tour!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedKhachHang == null) {
            Toast.makeText(getContext(), "Vui lòng chọn Khách hàng!", Toast.LENGTH_SHORT).show();
            return;
        }
        String ngayDi = edtNgayDi.getText().toString().trim();
        if (TextUtils.isEmpty(ngayDi)) {
            edtNgayDi.setError("Chọn ngày đi");
            return;
        }

        // 2. Lấy số lượng khách
        int slNguoiLon = 0;
        int slTreEm = 0;
        try {
            slNguoiLon = Integer.parseInt(edtNguoiLon.getText().toString().trim());
            String strTreEm = edtTreEm.getText().toString().trim();
            if (!strTreEm.isEmpty()) slTreEm = Integer.parseInt(strTreEm);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Vui lòng nhập số lượng hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (slNguoiLon <= 0) {
            edtNguoiLon.setError("Tối thiểu 1 người lớn");
            return;
        }

        // Check slot còn đủ không
        int conTrong = selectedTour.getSoLuongKhachToiDa() - selectedTour.getSoLuongKhachHienTai();
        int tongKhach = slNguoiLon + slTreEm;
        if (tongKhach > conTrong) {
            Toast.makeText(getContext(), "Tour chỉ còn đủ chỗ cho " + conTrong + " người!", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Tính Tổng Tiền
        // Công thức: (SL Người lớn * Giá NL) + (SL Trẻ em * Giá TE)
        long tongTien = (slNguoiLon * selectedTour.getGiaNguoiLon())
                + (slTreEm * selectedTour.getGiaTreEm());

        // 4. Đẩy lên Firestore
        btnSubmit.setEnabled(false);
        Toast.makeText(getContext(), "Đang xử lý...", Toast.LENGTH_SHORT).show();

        Map<String, Object> order = new HashMap<>();
        // Thông tin chung
        order.put("ngayTao", new Date());
        order.put("trangThai", "CHO_XU_LY");

        // Thông tin Tour
        order.put("tourId", selectedTour.getMaTour());
        order.put("tenTour", selectedTour.getTenTour());
        order.put("ngayDi", ngayDi);
        order.put("ngayVe", edtNgayVe.getText().toString().trim());

        // Thông tin Khách
        order.put("khachHangId", selectedKhachHang.getId());
        order.put("tenKhachHang", selectedKhachHang.getTen());
        order.put("sdtKhachHang", selectedKhachHang.getSdt());

        // Thông tin Chi tiết đơn
        order.put("soLuongNguoiLon", slNguoiLon);
        order.put("soLuongTreEm", slTreEm);
        order.put("tongTien", tongTien);

        db.collection("DonHang").add(order)
                .addOnSuccessListener(doc -> {
                    // Update lại số lượng khách hiện tại trong Tour (Tùy chọn, nên làm transaction)
                    updateTourSlots(selectedTour.getMaTour(), tongKhach);

                    Toast.makeText(getContext(), "Đặt Tour thành công!", Toast.LENGTH_LONG).show();
                    if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                });
    }

    // Hàm phụ: Cập nhật số lượng khách đã đặt vào bảng Tours
    private void updateTourSlots(String tourId, int soLuongThem) {
        if (tourId == null) return;
        // Tăng trường 'soLuongKhachHienTai' trong document Tour
        db.collection("Tours").document(tourId)
                .update("soLuongKhachHienTai", com.google.firebase.firestore.FieldValue.increment(soLuongThem));
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                    target.setText(date);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }
}