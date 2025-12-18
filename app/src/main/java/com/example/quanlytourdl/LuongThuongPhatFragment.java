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
import android.text.Editable;
import android.text.TextWatcher;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LuongThuongPhatFragment extends Fragment {

    private RecyclerView rvList;
    private LuongThuongPhatAdapter adapter;
    private List<LuongThuongPhat> mListGoc;      // Dữ liệu gốc từ Firebase
    private List<LuongThuongPhat> mListHienThi;  // Dữ liệu hiển thị (đã lọc)
    private FirebaseFirestore db;

    private Spinner spDepartmentFilter;
    private EditText etSearch;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_luong_thuong_phat, container, false);

        db = FirebaseFirestore.getInstance();
        mListGoc = new ArrayList<>();
        mListHienThi = new ArrayList<>();

        initViews(view);

        // Khởi tạo Adapter với Interface lắng nghe sự kiện click
        adapter = new LuongThuongPhatAdapter(getContext(), mListHienThi, new LuongThuongPhatAdapter.OnActionClickListener() {
            @Override
            public void onApprove(LuongThuongPhat item) {
                updateStatus(item, "Đã phê duyệt");
            }

            @Override
            public void onReject(LuongThuongPhat item) {
                updateStatus(item, "Đã từ chối");
            }
        });

        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvList.setAdapter(adapter);

        loadData();

        return view;
    }

    private void initViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        rvList = view.findViewById(R.id.rv_list);
        spDepartmentFilter = view.findViewById(R.id.sp_department_filter);
        etSearch = view.findViewById(R.id.et_search);
        ImageView ivExport = view.findViewById(R.id.iv_export_pdf);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);

        // 1. Nút Back quay về màn hình trước (CSKH)
        toolbar.setNavigationOnClickListener(v -> {
            if (isAdded() && getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // 2. Setup Spinner Lọc Phòng Ban
        String[] departments = {"Tất cả", "Kinh doanh", "Nhân sự", "Kế toán", "IT", "Hướng dẫn viên"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, departments);
        spDepartmentFilter.setAdapter(spAdapter);
        spDepartmentFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterData();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 3. Setup Tìm kiếm theo tên
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 4. Sự kiện Thêm mới
        fabAdd.setOnClickListener(v -> showAddDialog());

        // 5. Sự kiện Xuất PDF (kèm kiểm tra quyền)
        ivExport.setOnClickListener(v -> checkPermissionAndCreatePdf());
    }

    // --- TẢI DỮ LIỆU TỪ FIREBASE ---
    private void loadData() {
        // Lấy dữ liệu từ collection "RewardPunishment", sắp xếp theo ngày mới nhất
        db.collection("RewardPunishment").orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    mListGoc.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        LuongThuongPhat item = doc.toObject(LuongThuongPhat.class);
                        if (item != null) {
                            item.setId(doc.getId()); // Gán ID document để update sau này
                            mListGoc.add(item);
                        }
                    }
                    filterData(); // Gọi lọc lại dữ liệu mỗi khi có thay đổi
                });
    }

    // --- LOGIC LỌC DỮ LIỆU (SEARCH + SPINNER) ---
    private void filterData() {
        String keyword = etSearch.getText().toString().toLowerCase().trim();
        String selectedDept = spDepartmentFilter.getSelectedItem() != null ? spDepartmentFilter.getSelectedItem().toString() : "Tất cả";

        mListHienThi.clear();

        for (LuongThuongPhat item : mListGoc) {
            // Kiểm tra tên nhân viên
            boolean matchName = item.getEmployeeName() != null && item.getEmployeeName().toLowerCase().contains(keyword);

            // Kiểm tra phòng ban
            boolean matchDept = selectedDept.equals("Tất cả") || (item.getDepartment() != null && item.getDepartment().equals(selectedDept));

            if (matchName && matchDept) {
                mListHienThi.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    // --- LOGIC CẬP NHẬT TRẠNG THÁI (DUYỆT/TỪ CHỐI) ---
    private void updateStatus(LuongThuongPhat item, String newStatus) {
        if (item.getId() == null) return;

        db.collection("RewardPunishment").document(item.getId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Cập nhật thành công: " + newStatus, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi cập nhật!", Toast.LENGTH_SHORT).show());
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // --- CẬP NHẬT TÊN FILE TẠI ĐÂY ---
        View v = getLayoutInflater().inflate(R.layout.fragment_tao_thuong_phat, null);

        builder.setView(v);

        // Ánh xạ View
        EditText etName = v.findViewById(R.id.et_name);
        EditText etAmount = v.findViewById(R.id.et_amount);
        EditText etReason = v.findViewById(R.id.et_reason);
        Spinner spType = v.findViewById(R.id.sp_type);
        Spinner spDept = v.findViewById(R.id.sp_dept_dialog);

        // Nạp dữ liệu cho Spinner
        String[] types = {"Thưởng", "Phạt"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, types);
        spType.setAdapter(typeAdapter);

        String[] departments = {"Kinh doanh", "Nhân sự", "CSKH", "IT", "Hướng dẫn viên"};
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, departments);
        spDept.setAdapter(deptAdapter);

        builder.setTitle("Tạo quyết định mới")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    try {
                        String name = etName.getText().toString().trim();
                        String amountStr = etAmount.getText().toString().trim();
                        String reason = etReason.getText().toString().trim();

                        if (name.isEmpty() || amountStr.isEmpty()) {
                            Toast.makeText(getContext(), "Vui lòng nhập tên và số tiền!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double amount = Double.parseDouble(amountStr);

                        Map<String, Object> data = new HashMap<>();
                        data.put("employeeName", name);
                        data.put("amount", amount);
                        data.put("reason", reason);
                        data.put("type", spType.getSelectedItem().toString());
                        data.put("department", spDept.getSelectedItem().toString());
                        data.put("status", "Chờ phê duyệt");
                        data.put("date", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

                        db.collection("RewardPunishment").add(data)
                                .addOnSuccessListener(d -> Toast.makeText(getContext(), "Đã thêm thành công!", Toast.LENGTH_SHORT).show());

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Lỗi nhập liệu! Số tiền phải là số.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- LOGIC XUẤT PDF ---
    private void checkPermissionAndCreatePdf() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                createPdfReport();
            }
        } else {
            createPdfReport();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createPdfReport();
        }
    }

    private void createPdfReport() {
        if (mListHienThi.isEmpty()) {
            Toast.makeText(getContext(), "Không có dữ liệu để xuất!", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(myPageInfo);
        Canvas canvas = myPage.getCanvas();

        // Vẽ tiêu đề
        titlePaint.setTextSize(20);
        titlePaint.setColor(Color.BLUE);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);
        canvas.drawText("BÁO CÁO THƯỞNG / PHẠT", 297, 50, titlePaint);

        paint.setTextSize(14);
        paint.setColor(Color.BLACK);

        int y = 100;
        DecimalFormat df = new DecimalFormat("#,### VNĐ");

        for (LuongThuongPhat item : mListHienThi) {
            String line1 = String.format("%s - %s (%s)", item.getDate(), item.getEmployeeName(), item.getDepartment());
            String line2 = String.format("Loại: %s | Số tiền: %s", item.getType(), df.format(item.getAmount()));
            String line3 = "Lý do: " + item.getReason();
            String line4 = "Trạng thái: " + item.getStatus();

            canvas.drawText(line1, 50, y, paint);
            y += 20;
            canvas.drawText(line2, 50, y, paint);
            y += 20;
            canvas.drawText(line3, 50, y, paint);
            y += 20;

            // Đổi màu chữ cho dòng trạng thái
            Paint statusPaint = new Paint(paint);
            if(item.getStatus().contains("duyệt")) statusPaint.setColor(Color.GREEN);
            else if(item.getStatus().contains("từ chối")) statusPaint.setColor(Color.RED);
            else statusPaint.setColor(Color.parseColor("#FFC107")); // Vàng

            canvas.drawText(line4, 50, y, statusPaint);

            // Vẽ đường kẻ ngăn cách
            y += 10;
            paint.setStrokeWidth(1);
            paint.setColor(Color.LTGRAY);
            canvas.drawLine(50, y, 545, y, paint);
            paint.setColor(Color.BLACK); // Reset màu đen

            y += 30;

            if (y > 800) {
                pdfDocument.finishPage(myPage);
                myPage = pdfDocument.startPage(myPageInfo);
                canvas = myPage.getCanvas();
                y = 50;
            }
        }
        pdfDocument.finishPage(myPage);

        // Lưu file
        String fileName = "BaoCao_ThuongPhat_" + System.currentTimeMillis() + ".pdf";
        savePdfToStorage(pdfDocument, fileName);
    }

    private void savePdfToStorage(PdfDocument pdfDocument, String fileName) {
        try {
            OutputStream outputStream = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = requireContext().getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) outputStream = requireContext().getContentResolver().openOutputStream(uri);
            } else {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                outputStream = new FileOutputStream(file);
            }

            if (outputStream != null) {
                pdfDocument.writeTo(outputStream);
                outputStream.close();
                Toast.makeText(getContext(), "Đã xuất PDF thành công!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Lỗi tạo file PDF", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }
}