package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.quanlytourdl.firebase.FirebaseRepository;
import com.example.quanlytourdl.model.NhaCungCap;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class DanhGiaNhaCungCapFragment extends Fragment {

    private static final String TAG = "DanhGiaNCCFragment";
    private static final String ARG_NCC_ID = "ncc_id";

    private String nccId;
    private FirebaseRepository repository;

    // Views
    private Toolbar toolbar;
    private TextView tvName, tvId;
    private ImageView imgLogo;
    private RatingBar rbQuality, rbPrice, rbResponse;
    private TextInputEditText edtComment;
    private MaterialButton btnSubmit;

    public DanhGiaNhaCungCapFragment() {
        // Required empty public constructor
    }

    public static DanhGiaNhaCungCapFragment newInstance(String nccId) {
        DanhGiaNhaCungCapFragment fragment = new DanhGiaNhaCungCapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NCC_ID, nccId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new FirebaseRepository();
        if (getArguments() != null) {
            nccId = getArguments().getString(ARG_NCC_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_supplier_evaluation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupToolbar();
        loadSupplierInfo();

        btnSubmit.setOnClickListener(v -> submitEvaluation());
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_eval);
        tvName = view.findViewById(R.id.tv_supplier_name);
        tvId = view.findViewById(R.id.tv_supplier_id);
        imgLogo = view.findViewById(R.id.img_supplier_logo);

        // Đảm bảo ID này khớp với file XML fragment_supplier_evaluation.xml của bạn
        rbQuality = view.findViewById(R.id.rb_quality);
        rbPrice = view.findViewById(R.id.rb_price);
        rbResponse = view.findViewById(R.id.rb_response);

        edtComment = view.findViewById(R.id.edt_evaluation_comment);
        btnSubmit = view.findViewById(R.id.btn_submit_evaluation);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    /**
     * Tải thông tin nhà cung cấp (Sửa ánh xạ theo Model NhaCungCap mới)
     */
    private void loadSupplierInfo() {
        if (nccId == null) return;

        repository.getSupplierById(nccId).observe(getViewLifecycleOwner(), ncc -> {
            if (ncc != null) {
                // Sửa thành getTenNhaCungCap() và getMaNhaCungCap()
                tvName.setText(ncc.getTenNhaCungCap());
                tvId.setText("Mã đối tác: " + ncc.getMaNhaCungCap());

                // Nếu bạn có URL ảnh, có thể dùng Glide tại đây:
                // Glide.with(this).load(ncc.getUrlAnh()).into(imgLogo);
            }
        });
    }

    /**
     * Xử lý lưu đánh giá (Sửa khớp với Repository mới)
     */
    private void submitEvaluation() {
        float q = rbQuality.getRating();
        float p = rbPrice.getRating();
        float r = rbResponse.getRating();
        String comment = edtComment.getText().toString().trim();

        if (q == 0 || p == 0 || r == 0) {
            Toast.makeText(getContext(), "Vui lòng chọn số sao đánh giá!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tính điểm trung bình hiệu suất
        float averageScore = (q + p + r) / 3;

        // Gọi hàm repository với 3 tham số: ID, Điểm, Nhận xét
        repository.updateSupplierPerformance(nccId, averageScore, comment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã cập nhật hiệu suất nhà cung cấp!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi cập nhật:", e);
                    Toast.makeText(getContext(), "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}