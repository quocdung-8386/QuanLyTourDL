package com.example.quanlytourdl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class PheDuyetCongNoDialogFragment extends BottomSheetDialogFragment {

    private String congNoId;
    private FirebaseFirestore db;

    // View Mapping khớp chính xác với XML mới
    private TextView tvNccName, tvAmount, tvContent, tvDueDate, tvTicketCode;
    private ShapeableImageView ivEvidence, ivNccAvatar;
    private EditText edtNote;
    private MaterialButton btnApprove, btnReject; // Đổi sang MaterialButton cho khớp XML
    private ImageButton btnClose;

    public static PheDuyetCongNoDialogFragment newInstance(String id) {
        PheDuyetCongNoDialogFragment fragment = new PheDuyetCongNoDialogFragment();
        Bundle args = new Bundle();
        args.putString("CONG_NO_ID", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Style giúp Dialog bo góc tròn (nếu bạn có định nghĩa trong styles.xml)
        // setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_phe_duyet_cong_no, container, false);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            congNoId = getArguments().getString("CONG_NO_ID");
        }

        initViews(view);
        loadCongNoData();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        // Ánh xạ TextFields
        tvNccName = view.findViewById(R.id.tvNccName);
        tvAmount = view.findViewById(R.id.tvAmount);
        tvContent = view.findViewById(R.id.tvContent);
        tvDueDate = view.findViewById(R.id.tvDueDate);
        tvTicketCode = view.findViewById(R.id.tvTicketCode);

        // Ánh xạ Images
        ivEvidence = view.findViewById(R.id.ivEvidence);
        ivNccAvatar = view.findViewById(R.id.ivNccAvatar);

        // Ánh xạ Inputs & Buttons
        edtNote = view.findViewById(R.id.edtNote);
        btnApprove = view.findViewById(R.id.btnApprove);
        btnReject = view.findViewById(R.id.btnReject);
        btnClose = view.findViewById(R.id.btnClose);
    }

    private void loadCongNoData() {
        if (congNoId == null) return;

        db.collection("CongNo").document(congNoId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Hiển thị thông tin NCC
                String tenNCC = documentSnapshot.getString("tenNhaCungCap");
                tvNccName.setText(tenNCC != null ? tenNCC : "N/A");

                // Hiển thị nội dung chi tiết
                tvContent.setText(documentSnapshot.getString("noiDung"));
                tvDueDate.setText(documentSnapshot.getString("ngayHan"));

                // Mã phiếu (Nếu null hiển thị mặc định)
                String maPhieu = documentSnapshot.getString("maHopDong");
                tvTicketCode.setText(maPhieu != null ? "# " + maPhieu : "# ---");

                // Định dạng tiền tệ VND: 1.000.000 đ
                Double soTien = documentSnapshot.getDouble("soTien");
                if (soTien != null) {
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                    tvAmount.setText(formatter.format(soTien) + " đ");
                }

                // Xử lý ảnh minh chứng (Base64)
                String base64Image = documentSnapshot.getString("fileUrl");
                if (base64Image != null && !base64Image.isEmpty()) {
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    if (bitmap != null) {
                        ivEvidence.setImageBitmap(bitmap);
                        ivEvidence.setScaleType(ShapeableImageView.ScaleType.CENTER_CROP);
                    }
                } else {
                    // Nếu không có ảnh, có thể set ảnh mặc định hoặc ẩn đi
                    ivEvidence.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
        });
    }

    private Bitmap decodeBase64ToBitmap(String base64Str) {
        try {
            // Xử lý nếu chuỗi có chứa prefix "data:image/png;base64,"
            if (base64Str.contains(",")) {
                base64Str = base64Str.split(",")[1];
            }
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        btnApprove.setOnClickListener(v -> updateStatus("Đã phê duyệt"));

        btnReject.setOnClickListener(v -> {
            String note = edtNote.getText().toString().trim();
            if (note.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập lý do từ chối vào ghi chú", Toast.LENGTH_SHORT).show();
                edtNote.requestFocus();
            } else {
                updateStatus("Bị từ chối");
            }
        });
    }

    private void updateStatus(String status) {
        String ghiChu = edtNote.getText().toString().trim();

        db.collection("CongNo").document(congNoId)
                .update("trangThai", status, "ghiChuPheDuyet", ghiChu)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Kết quả: " + status, Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi cập nhật trạng thái", Toast.LENGTH_SHORT).show());
    }
}