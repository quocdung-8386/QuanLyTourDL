package com.example.quanlytourdl;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlytourdl.adapter.ChiTietAdapter;
import com.example.quanlytourdl.model.ChiTietItem;
import com.example.quanlytourdl.model.HoaDon;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiTietHoaDonFragment extends Fragment {

    private HoaDon hoaDonData;
    private FirebaseFirestore db;

    // Views
    private ImageView btnBack, btnExportPDF; // Khai báo thêm nút xuất PDF
    private TextView tvInvoiceCode, tvCreatedDate, tvDueDate, tvPaymentMethod, tvStatusDetail;
    private TextView tvCustomerName, tvCustomerDetail, tvBigTotal;
    private MaterialButton btnThanhToan;
    private RecyclerView rvChiTietDonHang;
    private ChiTietAdapter chiTietAdapter;
    private List<ChiTietItem> listChiTiet;

    // Biến lưu thông tin chi tiết để in
    private String printedContactInfo = "";
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chi_tiet_hoa_don, container, false);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            hoaDonData = (HoaDon) getArguments().getSerializable("hoa_don_data");
        }

        initViews(view);
        displayBasicInfo();
        loadOrderDetails();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        // 1. Ánh xạ nút xuất PDF (đảm bảo ID khớp với file XML)
        btnExportPDF = view.findViewById(R.id.btnExportPDF);

        tvInvoiceCode = view.findViewById(R.id.tvInvoiceCode);
        tvCreatedDate = view.findViewById(R.id.tvCreatedDate);
        tvDueDate = view.findViewById(R.id.tvDueDate);
        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        tvStatusDetail = view.findViewById(R.id.tvStatusDetail);
        tvBigTotal = view.findViewById(R.id.tvBigTotal);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvCustomerDetail = view.findViewById(R.id.tvCustomerDetail);
        btnThanhToan = view.findViewById(R.id.btnThanhToan);

        rvChiTietDonHang = view.findViewById(R.id.rvChiTietDonHang);
        rvChiTietDonHang.setLayoutManager(new LinearLayoutManager(getContext()));
        listChiTiet = new ArrayList<>();
        chiTietAdapter = new ChiTietAdapter(listChiTiet);
        rvChiTietDonHang.setAdapter(chiTietAdapter);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        btnThanhToan.setOnClickListener(v -> chuyenSangManHinhThanhToan());

        // 2. Sự kiện Click nút xuất PDF
        btnExportPDF.setOnClickListener(v -> {
            if (checkPermission()) {
                createPdf();
            } else {
                requestPermission();
            }
        });
    }

    // --- CÁC HÀM CŨ (displayBasicInfo, updateStatusView, chuyenSangManHinhThanhToan...) GIỮ NGUYÊN ---

    private void displayBasicInfo() {
        if (hoaDonData != null) {
            String rawId = hoaDonData.getMaHoaDon();
            String shortId = "#" + (rawId.length() > 8 ? rawId.substring(0, 8).toUpperCase() : rawId);
            tvInvoiceCode.setText(shortId);
            tvCustomerName.setText(hoaDonData.getTenKhachHang());
            updateUIForTotal(hoaDonData.getTongTien());
            updateStatusView(hoaDonData.getTrangThai());
        }
    }

    private void loadOrderDetails() {
        if (hoaDonData == null) return;

        db.collection("DonHang").document(hoaDonData.getMaHoaDon())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listChiTiet.clear();
                        String tenTour = documentSnapshot.getString("tenTour");
                        Long slNguoiLon = documentSnapshot.getLong("soLuongNguoiLon");
                        Long slTreEm = documentSnapshot.getLong("soLuongTreEm");
                        Double tongTien = documentSnapshot.getDouble("tongTien");
                        String sdt = documentSnapshot.getString("sdtKhachHang");
                        String email = documentSnapshot.getString("emailKhachHang");
                        com.google.firebase.Timestamp timestampNgayDat = documentSnapshot.getTimestamp("ngayDat");

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date dateCreated = (timestampNgayDat != null) ? timestampNgayDat.toDate() : new Date();

                        tvCreatedDate.setText(sdf.format(dateCreated));

                        long sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000;
                        Date dateDue = new Date(dateCreated.getTime() + sevenDaysInMillis);
                        tvDueDate.setText(sdf.format(dateDue));

                        // Lưu thông tin liên hệ để in PDF
                        String contactInfo = "";
                        if (sdt != null && !sdt.isEmpty()) contactInfo += sdt;
                        if (email != null && !email.isEmpty()) {
                            if (!contactInfo.isEmpty()) contactInfo += " - ";
                            contactInfo += email;
                        }
                        if (contactInfo.isEmpty()) contactInfo = "Thông tin liên hệ đang cập nhật";
                        tvCustomerDetail.setText(contactInfo);
                        printedContactInfo = contactInfo; // Lưu vào biến toàn cục

                        long slNL = (slNguoiLon != null) ? slNguoiLon : 0;
                        long slTE = (slTreEm != null) ? slTreEm : 0;

                        if (slNL > 0) {
                            ChiTietItem itemNL = new ChiTietItem();
                            itemNL.setTenDichVu("Vé người lớn - " + tenTour);
                            itemNL.setSoLuong((int) slNL);
                            itemNL.setGiaTien(0);
                            listChiTiet.add(itemNL);
                        }

                        if (slTE > 0) {
                            ChiTietItem itemTE = new ChiTietItem();
                            itemTE.setTenDichVu("Vé trẻ em - " + tenTour);
                            itemTE.setSoLuong((int) slTE);
                            itemTE.setGiaTien(0);
                            listChiTiet.add(itemTE);
                        }

                        chiTietAdapter.notifyDataSetChanged();

                        if (tongTien != null) {
                            updateUIForTotal(tongTien);
                            hoaDonData.setTongTien(tongTien);
                        }
                    }
                });
    }

    // --- LOGIC XUẤT PDF ---

    private void createPdf() {
        if (hoaDonData == null) {
            Toast.makeText(getContext(), "Chưa có dữ liệu để in!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo document
        PdfDocument document = new PdfDocument();
        // Trang A4 tiêu chuẩn: 595 x 842 pixel (tương đối)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // 2. Bắt đầu vẽ nội dung lên canvas
        int x = 40, y = 50;

        // Tiêu đề
        paint.setColor(Color.BLACK);
        paint.setTextSize(24);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("HÓA ĐƠN DU LỊCH", x, y, paint);

        y += 40;
        paint.setTextSize(14);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Mã hóa đơn: " + tvInvoiceCode.getText().toString(), x, y, paint);

        y += 20;
        canvas.drawText("Ngày lập: " + tvCreatedDate.getText().toString(), x, y, paint);

        // Đường kẻ
        y += 30;
        paint.setStrokeWidth(1);
        canvas.drawLine(x, y, 550, y, paint);

        // Thông tin khách hàng
        y += 30;
        paint.setTextSize(16);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Khách hàng:", x, y, paint);

        y += 25;
        paint.setTextSize(14);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Tên: " + hoaDonData.getTenKhachHang(), x, y, paint);
        y += 20;
        canvas.drawText("Liên hệ: " + printedContactInfo, x, y, paint);

        // Danh sách dịch vụ
        y += 40;
        paint.setTextSize(16);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Chi tiết dịch vụ:", x, y, paint);

        y += 30;
        paint.setTextSize(14);
        // Tiêu đề cột
        canvas.drawText("Dịch vụ", x, y, paint);
        canvas.drawText("SL", 400, y, paint);

        paint.setTypeface(Typeface.DEFAULT);
        y += 10;
        canvas.drawLine(x, y, 550, y, paint);
        y += 20;

        // Loop qua list chi tiết
        for (ChiTietItem item : listChiTiet) {
            // Tên dịch vụ có thể dài, cần xử lý cắt chuỗi nếu cần, ở đây vẽ đơn giản
            canvas.drawText(item.getTenDichVu(), x, y, paint);
            canvas.drawText(String.valueOf(item.getSoLuong()), 400, y, paint);
            y += 25;
        }

        // Tổng tiền
        y += 30;
        canvas.drawLine(x, y, 550, y, paint);
        y += 30;
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.RED);
        canvas.drawText("Tổng thanh toán: " + tvBigTotal.getText().toString(), x, y, paint);

        // Trạng thái
        y += 30;
        paint.setTextSize(14);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        canvas.drawText("Trạng thái: " + tvStatusDetail.getText().toString(), x, y, paint);

        // Kết thúc trang
        document.finishPage(page);

        // 3. Lưu file
        String fileName = "HoaDon_" + hoaDonData.getMaHoaDon() + ".pdf";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(getContext(), "Đã xuất PDF tại thư mục Download!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi lưu file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        document.close();
    }

    // --- CHECK QUYỀN GHI FILE (Cho Android cũ) ---
    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10 trở lên dùng Scoped Storage, ghi vào Download không cần quyền
            return true;
        }
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    // --- CÁC HÀM HỖ TRỢ KHÁC GIỮ NGUYÊN ---
    private void updateStatusView(int status) {
        // ... (Giữ nguyên code cũ)
        switch (status) {
            case 1:
                tvStatusDetail.setText("ĐÃ THANH TOÁN");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_paid_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_paid_pill);
                btnThanhToan.setText("ĐÃ THANH TOÁN");
                btnThanhToan.setEnabled(false);
                break;
            case 2:
                tvStatusDetail.setText("ĐANG CHỜ");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_pending_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_pending);
                btnThanhToan.setEnabled(true);
                break;
            case 3:
                tvStatusDetail.setText("ĐÃ HỦY");
                tvStatusDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.status_overdue_text));
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_overdue);
                btnThanhToan.setText("ĐƠN ĐÃ HỦY");
                btnThanhToan.setEnabled(false);
                break;
            default:
                tvStatusDetail.setText("KHÔNG XÁC ĐỊNH");
                break;
        }
    }

    private void updateUIForTotal(double totalAmount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String moneyString = formatter.format(totalAmount) + "đ";
        tvBigTotal.setText(moneyString);
        if (hoaDonData != null && hoaDonData.getTrangThai() != 1 && hoaDonData.getTrangThai() != 3) {
            btnThanhToan.setText("Thanh toán • " + moneyString);
        }
    }

    private void chuyenSangManHinhThanhToan() {
        if (hoaDonData == null || hoaDonData.getTrangThai() == 1) {
            if (hoaDonData != null) Toast.makeText(getContext(), "Đơn hàng đã được thanh toán!", Toast.LENGTH_SHORT).show();
            return;
        }
        FragmentThanhToan fragmentThanhToan = new FragmentThanhToan();
        Bundle bundle = new Bundle();
        bundle.putString("maHoaDonHienTai", hoaDonData.getMaHoaDon());
        bundle.putString("tenTour", hoaDonData.getTenTour());
        bundle.putDouble("giaTour", hoaDonData.getTongTien());
        bundle.putString("ngayKhoiHanh", tvCreatedDate.getText().toString());
        bundle.putBoolean("isThanhToanHoaDonCu", true);
        fragmentThanhToan.setArguments(bundle);

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(((View) getView().getParent()).getId(), fragmentThanhToan)
                    .addToBackStack(null)
                    .commit();
        }
    }
}