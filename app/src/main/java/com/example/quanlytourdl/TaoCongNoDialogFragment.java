package com.example.quanlytourdl;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quanlytourdl.model.CongNoModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaoCongNoDialogFragment extends BottomSheetDialogFragment {

    private Spinner spinnerNcc, spinnerContractCode;
    private EditText edtAmount, edtContent, edtDueDate;
    private LinearLayout btnUpload;
    private Button btnCreate, btnCancel;
    private ImageButton btnClose;
    private TextView tvStatusTag;

    private FirebaseFirestore db;
    private String base64Image = "";

    private List<String> nccList;
    private ArrayAdapter<String> nccAdapter;
    private List<String> contractList;
    private ArrayAdapter<String> contractAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_tao_cong_no_moi, container, false);
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupSpinners();
        loadNccData();
        setupDatePicker();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        spinnerNcc = view.findViewById(R.id.spinnerNcc);
        spinnerContractCode = view.findViewById(R.id.spinnerContractCode); // Đảm bảo ID này là Spinner trong XML

        edtAmount = view.findViewById(R.id.edtAmount);
        edtContent = view.findViewById(R.id.edtContent);
        edtDueDate = view.findViewById(R.id.edtDueDate);
        btnUpload = view.findViewById(R.id.btnUpload);
        btnCreate = view.findViewById(R.id.btnCreate);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnClose = view.findViewById(R.id.btnClose);
        tvStatusTag = view.findViewById(R.id.tvStatusTag);
    }

    private void setupSpinners() {
        nccList = new ArrayList<>();
        nccList.add("--- Chọn nhà cung cấp ---");
        nccAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nccList);
        nccAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNcc.setAdapter(nccAdapter);

        contractList = new ArrayList<>();
        contractList.add("--- Chọn mã hợp đồng ---");
        contractAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, contractList);
        contractAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerContractCode.setAdapter(contractAdapter);
    }

    private void loadNccData() {
        db.collection("NhaCungCap").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("tenNhaCungCap");
                if (name != null) nccList.add(name);
            }
            nccAdapter.notifyDataSetChanged();
        });
    }

    private void loadContractDataByNcc(String selectedNcc) {
        contractList.clear();
        contractList.add("--- Đang tải dữ liệu ---");
        contractAdapter.notifyDataSetChanged();

        // Bước 1: Lấy thông tin "maHopDongGanNhat" từ NhaCungCap trước
        db.collection("NhaCungCap")
                .whereEqualTo("tenNhaCungCap", selectedNcc)
                .get()
                .addOnSuccessListener(nccSnapshots -> {
                    String maGanNhat = "";
                    if (!nccSnapshots.isEmpty()) {
                        maGanNhat = nccSnapshots.getDocuments().get(0).getString("maHopDongGanNhat");
                    }

                    // Bước 2: Tải tất cả hợp đồng của NCC đó
                    String finalMaGanNhat = maGanNhat;
                    db.collection("HopDong")
                            .whereEqualTo("tenNhaCungCap", selectedNcc)
                            .get()
                            .addOnSuccessListener(hdSnapshots -> {
                                contractList.clear();
                                int selectionIndex = 0;
                                for (QueryDocumentSnapshot doc : hdSnapshots) {
                                    String code = doc.getString("maHopDong");
                                    if (code != null) {
                                        contractList.add(code);
                                        // Kiểm tra nếu mã này trùng với mã hợp đồng gần nhất
                                        if (code.equals(finalMaGanNhat)) {
                                            selectionIndex = contractList.size() - 1;
                                        }
                                    }
                                }
                                contractAdapter.notifyDataSetChanged();

                                // Tự động chọn mã hợp đồng gần nhất
                                if (selectionIndex > 0) {
                                    spinnerContractCode.setSelection(selectionIndex);
                                }
                            });
                });
    }

    private void setupListeners() {
        spinnerNcc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    loadContractDataByNcc(nccList.get(position));
                } else {
                    contractList.clear();
                    contractList.add("--- Chọn mã hợp đồng ---");
                    contractAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnCreate.setOnClickListener(v -> saveToFirestore());
        btnClose.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveToFirestore() {
        String amountStr = edtAmount.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (spinnerNcc.getSelectedItemPosition() == 0 || amountStr.isEmpty() || content.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ các trường (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        CongNoModel model = new CongNoModel();
        model.setTenNcc(spinnerNcc.getSelectedItem().toString());
        model.setSoTien(Double.parseDouble(amountStr));
        model.setNoiDung(content);
        model.setNgayHan(edtDueDate.getText().toString());
        String mhd = spinnerContractCode.getSelectedItemPosition() <= 0 ? "" : spinnerContractCode.getSelectedItem().toString();
        model.setMaHopDong(mhd);
        model.setTrangThai("Chờ duyệt");
        model.setFileUrl(base64Image);

        db.collection("CongNo").add(model).addOnSuccessListener(documentReference -> {
            Toast.makeText(getContext(), "Đã tạo công nợ thành công!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    base64Image = encodeImageToBase64(imageUri);
                    Toast.makeText(getContext(), "Ảnh đã được đính kèm", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) { return ""; }
    }

    private void setupDatePicker() {
        edtDueDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                edtDueDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }
}