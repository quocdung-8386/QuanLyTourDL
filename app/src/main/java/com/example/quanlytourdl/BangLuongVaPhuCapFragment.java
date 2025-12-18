package com.example.quanlytourdl;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BangLuongVaPhuCapFragment extends Fragment {

    private RecyclerView rvBangLuong;
    private BangLuongAdapter adapter;

    // Danh sách dữ liệu
    private List<BangLuong> mListGoc;      // Dữ liệu gốc từ Firebase
    private List<BangLuong> mListHienThi;  // Dữ liệu đang hiển thị (đã lọc)

    private FirebaseFirestore db;
    private Spinner spinnerKyLuong;

    // Mã yêu cầu quyền (dùng cho Android < 10)
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bang_luong_va_phu_cap, container, false);

        db = FirebaseFirestore.getInstance();

        mListGoc = new ArrayList<>();
        mListHienThi = new ArrayList<>();

        initViews(view); // Ánh xạ và gắn sự kiện

        // Cấu hình RecyclerView
        adapter = new BangLuongAdapter(mListHienThi, this::showSalaryDialog);
        rvBangLuong.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBangLuong.setAdapter(adapter);

        loadData(); // Tải dữ liệu từ Firebase

        return view;
    }

    private void initViews(View view) {
        rvBangLuong = view.findViewById(R.id.rv_bang_luong);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ImageView ivDownload = view.findViewById(R.id.iv_download);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_salary);
        spinnerKyLuong = view.findViewById(R.id.spinner_ky_luong);

        // 1. Nút Back
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // 2. Nút Tải xuống (Gọi hàm kiểm tra quyền)
        ivDownload.setOnClickListener(v -> checkPermissionAndCreatePdf());

        // 3. Nút Thêm mới
        fabAdd.setOnClickListener(v -> showSalaryDialog(null));

        // 4. Sự kiện chọn Spinner (Lọc)
        spinnerKyLuong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedKy = parent.getItemAtPosition(position).toString();
                filterData(selectedKy);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    // --- LOGIC 1: KIỂM TRA QUYỀN TRƯỚC KHI TẠO PDF ---
    private void checkPermissionAndCreatePdf() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 9 trở xuống: Cần xin quyền WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Chưa có quyền -> Hiện bảng xin quyền
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                // Đã có quyền -> Tạo PDF
                createPdfReport();
            }
        } else {
            // Android 10 trở lên: Không cần quyền Runtime -> Tạo PDF luôn
            createPdfReport();
        }
    }

    // Nhận kết quả khi người dùng bấm "Cho phép" hoặc "Từ chối"
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Đã cấp quyền! Đang lưu file...", Toast.LENGTH_SHORT).show();
                createPdfReport();
            } else {
                Toast.makeText(getContext(), "Bạn cần cấp quyền bộ nhớ để lưu file!", Toast.LENGTH_LONG).show();
            }
        }
    }

    // --- LOGIC 2: TẠO VÀ LƯU FILE PDF ---
    private void createPdfReport() {
        if (mListHienThi.isEmpty()) {
            Toast.makeText(getContext(), "Không có dữ liệu để xuất file!", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        // Khổ giấy A4
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(myPageInfo);
        Canvas canvas = myPage.getCanvas();

        // Vẽ tiêu đề
        titlePaint.setTextSize(20);
        titlePaint.setColor(Color.BLUE);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("BÁO CÁO LƯƠNG NHÂN VIÊN", 297, 50, titlePaint);

        paint.setTextSize(14);
        paint.setColor(Color.BLACK);

        int y = 100;
        DecimalFormat df = new DecimalFormat("#,### VNĐ");

        // Vẽ danh sách lương
        for (BangLuong item : mListHienThi) {
            String line1 = "Kỳ: " + item.getSalaryPeriod() + " - " + (item.isPaid() ? "Đã TT" : "Chưa TT");
            String line2 = "Lương CB: " + df.format(item.getLuongCoBan()) + " | Tổng: " + df.format(item.getTongThuNhap());

            canvas.drawText(line1, 50, y, paint);
            y += 20;
            canvas.drawText(line2, 50, y, paint);
            y += 30;

            // Ngắt trang nếu dài quá (cơ bản)
            if (y > 800) break;
        }
        pdfDocument.finishPage(myPage);

        // Lưu file
        String fileName = "BangLuong_" + System.currentTimeMillis() + ".pdf";
        OutputStream outputStream = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: Dùng MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = requireContext().getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    outputStream = requireContext().getContentResolver().openOutputStream(uri);
                }
            } else {
                // Android 9-: Dùng FileOutputStream (Đã an toàn trong try-catch)
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                outputStream = new FileOutputStream(file);
            }

            if (outputStream != null) {
                pdfDocument.writeTo(outputStream);
                Toast.makeText(getContext(), "Đã lưu vào thư mục Download!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Lỗi tạo file!", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            // Đóng luồng
            try {
                if (outputStream != null) outputStream.close();
                pdfDocument.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // --- LOGIC 3: TẢI DỮ LIỆU TỪ FIREBASE ---
    private void loadData() {
        db.collection("Payrolls").orderBy("salaryPeriod", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    mListGoc.clear();

                    Set<String> uniquePeriods = new HashSet<>();
                    uniquePeriods.add("Tất cả");

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        BangLuong bl = doc.toObject(BangLuong.class);
                        if (bl != null) {
                            bl.setId(doc.getId()); // Gán ID document để sau này Sửa/Xóa
                            mListGoc.add(bl);
                            if (bl.getSalaryPeriod() != null) {
                                uniquePeriods.add(bl.getSalaryPeriod());
                            }
                        }
                    }
                    updateSpinner(new ArrayList<>(uniquePeriods));
                    filterData("Tất cả");
                });
    }

    private void updateSpinner(List<String> periods) {
        if (getContext() == null) return;
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, periods);
        spinnerKyLuong.setAdapter(spinAdapter);
    }

    private void filterData(String kyLuong) {
        mListHienThi.clear();
        if (kyLuong.equals("Tất cả")) {
            mListHienThi.addAll(mListGoc);
        } else {
            for (BangLuong item : mListGoc) {
                if (item.getSalaryPeriod() != null && item.getSalaryPeriod().equals(kyLuong)) {
                    mListHienThi.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // --- LOGIC 4: DIALOG THÊM / SỬA ---
    private void showSalaryDialog(@Nullable BangLuong item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // LƯU Ý: Đảm bảo tên file XML layout này đúng với file bạn đã tạo
        // Nếu file bạn tên dialog_add_edit_luong.xml thì sửa dòng dưới thành R.layout.dialog_add_edit_luong
        View v = getLayoutInflater().inflate(R.layout.fragment_them_sua_luong, null);
        builder.setView(v);

        EditText etPeriod = v.findViewById(R.id.et_salary_period);
        EditText etBasic = v.findViewById(R.id.et_luong_co_ban);
        EditText etAllowance = v.findViewById(R.id.et_tong_phu_cap);
        EditText etBonus = v.findViewById(R.id.et_thuong_hoa_hong);
        EditText etFine = v.findViewById(R.id.et_phat_khau_tru);
        SwitchCompat swPaid = v.findViewById(R.id.sw_is_paid);

        // Đổ dữ liệu cũ nếu là Sửa
        if (item != null) {
            etPeriod.setText(item.getSalaryPeriod());
            etBasic.setText(String.valueOf((long) item.getLuongCoBan()));
            etAllowance.setText(String.valueOf((long) item.getTongPhuCap()));
            etBonus.setText(String.valueOf((long) item.getThuongHoaHong()));
            etFine.setText(String.valueOf((long) item.getPhatKhauTru()));
            swPaid.setChecked(item.isPaid());
        }

        builder.setTitle(item == null ? "Thêm bảng lương" : "Chỉnh sửa")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", (dialog, which) -> saveSalaryToFirebase(item, etPeriod, etBasic, etAllowance, etBonus, etFine, swPaid));

        builder.show();
    }

    private void saveSalaryToFirebase(@Nullable BangLuong oldItem, EditText etPeriod, EditText etBasic,
                                      EditText etAllowance, EditText etBonus, EditText etFine, SwitchCompat swPaid) {
        String period = etPeriod.getText().toString().trim();
        if (TextUtils.isEmpty(period)) {
            Toast.makeText(getContext(), "Vui lòng nhập kỳ lương!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        try {
            data.put("salaryPeriod", period);
            data.put("luongCoBan", Double.parseDouble(etBasic.getText().toString()));
            data.put("tongPhuCap", Double.parseDouble(etAllowance.getText().toString()));
            data.put("thuongHoaHong", Double.parseDouble(etBonus.getText().toString()));
            data.put("phatKhauTru", Double.parseDouble(etFine.getText().toString()));
            data.put("paid", swPaid.isChecked()); // Key là "paid"

            if (oldItem == null) {
                // Thêm mới
                db.collection("Payrolls").add(data)
                        .addOnSuccessListener(doc -> Toast.makeText(getContext(), "Đã thêm!", Toast.LENGTH_SHORT).show());
            } else {
                // Cập nhật
                if (oldItem.getId() != null) {
                    db.collection("Payrolls").document(oldItem.getId()).update(data)
                            .addOnSuccessListener(a -> Toast.makeText(getContext(), "Đã cập nhật!", Toast.LENGTH_SHORT).show());
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}