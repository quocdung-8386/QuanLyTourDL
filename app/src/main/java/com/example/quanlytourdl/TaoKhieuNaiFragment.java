package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.quanlytourdl.model.KhieuNai;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class TaoKhieuNaiFragment extends Fragment {

    private AutoCompleteTextView actvCustomer, actvTour;
    private TextInputEditText etDate, etContent;
    private Spinner spPriority, spStatus;
    private LinearLayout btnUpload;
    private TextView tvUploadHint;
    private ImageView ivPreview;
    private Uri selectedImageUri;

    private FirebaseFirestore db;
    private List<String> listCustomerNames = new ArrayList<>();
    private List<String> listTourIds = new ArrayList<>();

    // Launcher để chọn ảnh từ thư viện
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == -1 && result.getData() != null) { // -1 là RESULT_OK
                    selectedImageUri = result.getData().getData();
                    ivPreview.setImageURI(selectedImageUri);
                    tvUploadHint.setText("Đã chọn file: " + selectedImageUri.getLastPathSegment());
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tao_khieu_nai, container, false);
        db = FirebaseFirestore.getInstance();

        initViews(view);
        loadDataForAutocomplete(); // Tải danh sách gợi ý từ Firebase
        setupSpinners();

        return view;
    }

    private void initViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_khieu_nai);
        actvCustomer = view.findViewById(R.id.actv_customer);
        actvTour = view.findViewById(R.id.actv_tour);
        etDate = view.findViewById(R.id.et_date_incident);
        etContent = view.findViewById(R.id.et_content_complaint);
        spPriority = view.findViewById(R.id.spinner_priority);
        spStatus = view.findViewById(R.id.spinner_status);
        btnUpload = view.findViewById(R.id.layout_upload);
        tvUploadHint = view.findViewById(R.id.tv_upload_hint);
        ivPreview = view.findViewById(R.id.iv_preview);

        Button btnCreate = view.findViewById(R.id.btn_create_ticket);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        TextView tvSaveTop = view.findViewById(R.id.tv_save_top);

        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        etDate.setOnClickListener(v -> showDatePicker());

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        View.OnClickListener saveAction = v -> saveComplaint();
        btnCreate.setOnClickListener(saveAction);
        tvSaveTop.setOnClickListener(saveAction);

        btnCancel.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });
    }

    private void setupSpinners() {
        String[] priorities = {"Thấp", "Trung bình", "Cao"};
        ArrayAdapter<String> adapterPriority = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, priorities);
        spPriority.setAdapter(adapterPriority);
        spPriority.setSelection(1);

        String[] statuses = {"Mới", "Đang xử lý", "Đã giải quyết", "Hủy"};
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, statuses);
        spStatus.setAdapter(adapterStatus);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            etDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // [CẬP NHẬT] Hàm tải dữ liệu đã sửa để lấy từ collection "Tours"
    private void loadDataForAutocomplete() {
        // 1. Load danh sách KH từ Firebase "Users"
        db.collection("Users").get().addOnSuccessListener(snapshots -> {
            listCustomerNames.clear();
            for (DocumentSnapshot doc : snapshots) {
                String name = doc.getString("fullName");
                if (name != null) listCustomerNames.add(name);
            }
            if (getContext() != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, listCustomerNames);
                actvCustomer.setAdapter(adapter);
            }
        });

        // 2. Load danh sách Tour từ Firebase "Tours"
        db.collection("Tours").get().addOnSuccessListener(snapshots -> {
            listTourIds.clear();
            for (DocumentSnapshot doc : snapshots) {
                // Lấy ID của Document (Ví dụ: TOUR001)
                // Nếu bạn lưu ID trong một trường cụ thể (ví dụ "maTour"), hãy dùng doc.getString("maTour")
                String tourId = doc.getId();
                listTourIds.add(tourId);
            }
            if (getContext() != null) {
                ArrayAdapter<String> adapterTour = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, listTourIds);
                actvTour.setAdapter(adapterTour);
            }
        }).addOnFailureListener(e -> {
            // Xử lý nếu không tải được tour (tùy chọn)
            Toast.makeText(getContext(), "Không tải được danh sách Tour", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveComplaint() {
        String customer = actvCustomer.getText().toString().trim();
        String tour = actvTour.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String priority = spPriority.getSelectedItem().toString();
        String status = spStatus.getSelectedItem().toString();

        if (customer.isEmpty() || content.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên khách và nội dung khiếu nại!", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = "#KN-" + (1000 + new Random().nextInt(9000));
        String uriString = selectedImageUri != null ? selectedImageUri.toString() : "";

        KhieuNai khieuNai = new KhieuNai(id, customer, tour, date, content, uriString, priority, status);

        db.collection("Complaints").document(id).set(khieuNai)
                .addOnSuccessListener(v -> {
                    Toast.makeText(getContext(), "Tạo phiếu khiếu nại thành công!", Toast.LENGTH_SHORT).show();
                    if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}