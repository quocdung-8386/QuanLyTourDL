package com.example.quanlytourdl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

public class HdvDetailFragment extends Fragment {

    // Khai báo các View cần tương tác
    private ImageButton btnBack;
    private ShapeableImageView imgHdvAvatar;
    private TextView textHdvName;
    private TextView textHdvCode;
    private TextView textHdvContact;
    private MaterialButton btnUpdateHoSo;

    // Các Item Giấy tờ (Chúng ta dùng include, nên cần ánh xạ từng item)
    private View itemDocument1;
    private View itemDocument2;
    private View itemDocument3;

    public HdvDetailFragment() {
        // Constructor rỗng bắt buộc
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout chi tiết HDV đã tạo trước đó
        View view = inflater.inflate(R.layout.fragment_hdv_detail, container, false);

        // 1. Ánh xạ các thành phần
        btnBack = view.findViewById(R.id.btn_back_detail);
        imgHdvAvatar = view.findViewById(R.id.img_hdv_avatar);
        textHdvName = view.findViewById(R.id.text_hdv_name);
        textHdvCode = view.findViewById(R.id.text_hdv_code);
        textHdvContact = view.findViewById(R.id.text_hdv_contact);
        btnUpdateHoSo = view.findViewById(R.id.btn_update_ho_so);

        // Ánh xạ các layout include cho giấy tờ
        itemDocument1 = view.findViewById(R.id.item_document_1);
        itemDocument2 = view.findViewById(R.id.item_document_2);
        itemDocument3 = view.findViewById(R.id.item_document_3);

        // 2. Tải dữ liệu (Giả lập)
        loadHdvDetails();
        loadDocumentDetails();

        // 3. Xử lý sự kiện
        btnBack.setOnClickListener(v -> {
            // Xử lý quay lại
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnUpdateHoSo.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở màn hình chỉnh sửa hồ sơ HDV", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    /**
     * Hàm giả lập tải dữ liệu chi tiết HDV
     */
    private void loadHdvDetails() {
        // Trong thực tế: Lấy ID HDV từ Bundle -> Gọi ViewModel/Repository -> Cập nhật UI

        // Giả lập dữ liệu
        textHdvName.setText("Vũ Hồng Quang");
        textHdvCode.setText("Mã HDV: 12345");
        textHdvContact.setText("SĐT: 0987 654 321 | Email: vhquang@email.com");

        // Thiết lập ảnh đại diện (giả lập)
        // imgHdvAvatar.setImageResource(R.drawable.hdv_quang_image);
    }

    /**
     * Hàm giả lập thiết lập dữ liệu và trạng thái cho các mục giấy tờ
     */
    private void loadDocumentDetails() {
        // 1. Thẻ Hướng dẫn viên (Còn hạn - Xanh)
        setupDocumentItem(itemDocument1, "Thẻ Hướng dẫn viên", "Hết hạn: 31/12/2025", "Còn hạn", "#4CAF50");

        // 2. Chứng chỉ Ngoại ngữ (Sắp hết hạn - Vàng)
        setupDocumentItem(itemDocument2, "Chứng chỉ Ngoại ngữ", "Hết hạn: 30/09/2024", "Sắp hết hạn", "#FFC107");

        // 3. Chứng nhận Sơ cứu (Đã hết hạn - Đỏ)
        setupDocumentItem(itemDocument3, "Chứng nhận Sơ cứu", "Hết hạn: 01/03/2024", "Đã hết hạn", "#F44336");
    }

    private void setupDocumentItem(View itemView, String title, String expiry, String status, String statusColor) {
        TextView textTitle = itemView.findViewById(R.id.text_doc_title);
        TextView textExpiry = itemView.findViewById(R.id.text_doc_expiry);
        TextView textStatus = itemView.findViewById(R.id.text_doc_status);
        // ImageView imgIcon = itemView.findViewById(R.id.img_doc_icon); // Nếu cần đổi icon

        textTitle.setText(title);
        textExpiry.setText(expiry);
        textStatus.setText(status);

        // Cài đặt màu sắc cho trạng thái (Lưu ý: Bạn phải tự tạo Drawable/Color cho màu này)
        textStatus.setTextColor(android.graphics.Color.parseColor(statusColor));

        // 💡 Để thiết lập DrawableStart (chấm tròn), bạn cần tìm ID của drawable tương ứng
        // Ví dụ: textStatus.setCompoundDrawablesWithIntrinsicBounds(getDotDrawable(statusColor), 0, 0, 0);
    }

    // 💡 Gợi ý: Bạn có thể viết thêm logic để xử lý Lịch trống (grid_calendar) ở đây.
}