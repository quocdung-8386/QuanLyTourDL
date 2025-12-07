package com.example.quanlytourdl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fragment thứ 4: Xử lý Hình ảnh Tour, Mô tả SEO và Thiết lập Xuất bản.
 * Đã bổ sung các kiểm tra Null và ghi Log an toàn để tránh crash khi findViewById không tìm thấy ID.
 */
public class TaoTourHinhAnhFragment extends Fragment {

    private static final String TAG = "TourHinhAnhFragment";

    private SwitchMaterial switchPublishStatus;
    private SwitchMaterial switchFeaturedTour;
    private TextInputEditText etSeoDescription;
    private TextInputLayout tilSeoDescription;
    private MaterialCardView cardImageUploadArea;

    // Giả lập lưu trữ danh sách các đường dẫn ảnh (hoặc URI)
    private List<String> uploadedImageUrls = new ArrayList<>();
    private OnDataCollectedListener dataListener;

    // Interface để truyền dữ liệu về Activity/Fragment cha
    public interface OnDataCollectedListener {
        /**
         * Phương thức này được gọi khi người dùng hoàn tất bước 4 và nhấn nút "Xuất bản" (hoặc "Hoàn tất").
         * Đây là TRIGGER cho Activity cha để TỔNG HỢP tất cả dữ liệu Tour và tạo đối tượng ChoDuyetTour.
         */
        void onPublishDataCollected(PublishData data);
    }

    // Lớp Dữ liệu (Data Model) để gói gọn đầu ra của Fragment này
    public static class PublishData {
        public String seoDescription;
        public boolean isPublished;
        public boolean isFeatured;
        public List<String> imageUrls;

        public PublishData(String seoDescription, boolean isPublished, boolean isFeatured, List<String> imageUrls) {
            this.seoDescription = seoDescription;
            this.isPublished = isPublished;
            this.isFeatured = isFeatured;
            this.imageUrls = imageUrls;
        }
    }

    // Layout ID được cung cấp bởi người dùng
    private static final int LAYOUT_RES_ID = R.layout.fragment_hinhanh_xb;

    public TaoTourHinhAnhFragment() {
        // Constructor rỗng bắt buộc
    }

    // Gán Listener khi Fragment được gắn vào Context (Activity)
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            // Context (thường là Activity) phải implement OnDataCollectedListener
            dataListener = (OnDataCollectedListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, context.toString() + " must implement OnDataCollectedListener", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(LAYOUT_RES_ID, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        try {
            switchPublishStatus = view.findViewById(R.id.switch_publish_status);
            switchFeaturedTour = view.findViewById(R.id.switch_featured_tour);
            etSeoDescription = view.findViewById(R.id.et_seo_description);
            tilSeoDescription = view.findViewById(R.id.til_seo_description);
            cardImageUploadArea = view.findViewById(R.id.card_image_upload_area);
        } catch (Exception e) {
            // Bắt lỗi nếu View ID không khớp (nguyên nhân gây crash phổ biến nhất)
            Log.e(TAG, "Lỗi khi ánh xạ View: " + e.getMessage());
            return;
        }

        // Kiểm tra an toàn: Đảm bảo các View cốt lõi được tìm thấy.
        if (switchPublishStatus == null || etSeoDescription == null || cardImageUploadArea == null) {
            Log.e(TAG, "LỖI CRITICAL: Một hoặc nhiều ID View bị thiếu trong fragment_hinhanh_xb.xml.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "LỖI: Layout không đúng cấu trúc. Kiểm tra logcat!", Toast.LENGTH_LONG).show();
            }
            return;
        }

        // 2. Thiết lập sự kiện (Logic giả lập tải ảnh)
        cardImageUploadArea.setOnClickListener(v -> {
            // Giả lập việc chọn và tải ảnh thành công
            if (uploadedImageUrls.size() < 5) {
                uploadedImageUrls.add("url/mock/image_" + (uploadedImageUrls.size() + 1));
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã thêm 1 ảnh. Tổng: " + uploadedImageUrls.size() + "/5", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã đạt tối đa 5 ảnh.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * PHƯƠNG THỨC NÀY ĐƯỢC GỌI BỞI ACTIVITY KHI NGƯỜI DÙNG NHẤN NÚT XUẤT BẢN.
     * Nó thu thập dữ liệu và chuyển lên tầng xử lý cao hơn (Activity).
     * @return true nếu dữ liệu hợp lệ và đã được chuyển giao.
     */
    public boolean saveImagesAndPublishData() {
        // 1. Kiểm tra an toàn trước khi gọi các phương thức
        if (etSeoDescription == null || tilSeoDescription == null || switchPublishStatus == null) {
            Log.e(TAG, "Lỗi: Không thể lưu dữ liệu vì các View chưa được khởi tạo đầy đủ.");
            return false;
        }

        // 2. Kiểm tra tính hợp lệ
        if (!isFormValid()) {
            return false;
        }

        // 3. Lấy dữ liệu đã được xác thực
        PublishData data = getPublishData();

        // 4. Truyền dữ liệu về Activity/Fragment cha thông qua Listener
        if (dataListener != null) {
            // Activity cha sẽ nhận `data` này và TỔNG HỢP với các dữ liệu Tour khác,
            // sau đó tạo đối tượng ChoDuyetTour cuối cùng để lưu vào Database/Server.
            dataListener.onPublishDataCollected(data);
        } else {
            Log.e(TAG, "Lỗi: dataListener bị NULL. Activity cha có thể chưa implement Listener hoặc Fragment bị Detach.");
            return false;
        }

        return true;
    }

    /**
     * Thu thập tất cả dữ liệu từ các trường nhập liệu và switch.
     * @return Đối tượng PublishData chứa dữ liệu hiện tại.
     */
    public PublishData getPublishData() {
        // Dùng Objects.requireNonNull an toàn do đã có kiểm tra null ở hàm gọi
        String seoDescription = Objects.requireNonNull(etSeoDescription.getText()).toString().trim();
        boolean isPublished = switchPublishStatus.isChecked();

        // Kiểm tra an toàn cho switchFeaturedTour
        boolean isFeatured = switchFeaturedTour != null && switchFeaturedTour.isChecked();

        return new PublishData(seoDescription, isPublished, isFeatured, uploadedImageUrls);
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu nhập: độ dài SEO và số lượng ảnh.
     * @return true nếu tất cả các trường hợp lệ.
     */
    private boolean isFormValid() {
        String seoDescription = Objects.requireNonNull(etSeoDescription.getText()).toString().trim();
        boolean isValid = true;

        // 1. Kiểm tra độ dài Mô tả SEO
        if (seoDescription.length() > 300) {
            tilSeoDescription.setError("Mô tả SEO không được vượt quá 300 ký tự.");
            isValid = false;
        } else {
            tilSeoDescription.setError(null);
        }

        // 2. Kiểm tra số lượng ảnh tối thiểu (Ví dụ: Yêu cầu ít nhất 1 ảnh)
        if (uploadedImageUrls.isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng tải lên ít nhất 1 ảnh cho tour.", Toast.LENGTH_LONG).show();
            }
            isValid = false;
        }

        return isValid;
    }

    // Đảm bảo không giữ lại tham chiếu listener khi Fragment bị hủy liên kết
    @Override
    public void onDetach() {
        super.onDetach();
        dataListener = null;
    }
}