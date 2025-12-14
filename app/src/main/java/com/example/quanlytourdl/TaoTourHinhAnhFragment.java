package com.example.quanlytourdl;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.quanlytourdl.model.Tour;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaoTourHinhAnhFragment extends Fragment implements TaoTourDetailFullFragment.TourStepDataCollector {

    private static final String TAG = "TaoTourHinhAnhFragment";
    private static final int MIN_SEO_LENGTH = 50;
    private static final int MAX_IMAGE_COUNT = 5;

    private final Tour tour;

    // THÀNH PHẦN UI
    private LinearLayout llImageListContainer;
    private MaterialCardView cardAddImageButton;
    private TextView tvImageCount;
    private TextInputEditText etSeoDescription;
    private SwitchMaterial switchPublishStatus;
    private SwitchMaterial switchFeaturedTour;

    // ACTIVITY RESULT LAUNCHER CHO VIỆC CHỌN ẢNH
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public TaoTourHinhAnhFragment(Tour tour) {
        this.tour = tour;
        // Khởi tạo danh sách ảnh nếu chưa có
        if (tour.getDanhSachHinhAnh() == null) {
            tour.setDanhSachHinhAnh(new ArrayList<>());
        }
    }

    public TaoTourHinhAnhFragment() {
        this(new Tour());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ĐĂNG KÝ RESULT LAUNCHER
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        handleImagePickResult(result.getData());
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Toast.makeText(getContext(), "Đã hủy chọn ảnh.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    // ⭐ ĐÃ SỬA LỖI COMPILER: Thay ViewGroup viewGroup bằng Bundle savedInstanceState
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hinhanh_xb, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ÁNH XẠ VIEWS
        llImageListContainer = view.findViewById(R.id.ll_image_list_container);
        cardAddImageButton = view.findViewById(R.id.card_add_image_button);
        tvImageCount = view.findViewById(R.id.tv_image_count);
        etSeoDescription = view.findViewById(R.id.et_seo_description);
        switchPublishStatus = view.findViewById(R.id.switch_publish_status);
        switchFeaturedTour = view.findViewById(R.id.switch_featured_tour);

        // Thiết lập Listener cho nút tải ảnh
        cardAddImageButton.setOnClickListener(v -> {
            openImageChooser();
        });

        // Tải ảnh hiện có (nếu có)
        loadExistingImages();
        updateDisplay();
    }

    // --- LOGIC CHỌN ẢNH ---

    /**
     * Mở giao diện chọn ảnh/thư viện ảnh.
     */
    private void openImageChooser() {
        if (getAvailableImageSlots() <= 0) {
            Toast.makeText(getContext(), "Đã đạt giới hạn " + MAX_IMAGE_COUNT + " ảnh.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            imagePickerLauncher.launch(intent);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ứng dụng chọn ảnh.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Xử lý kết quả trả về từ Image Picker.
     */
    private void handleImagePickResult(Intent data) {
        List<String> imageList = tour.getDanhSachHinhAnh();
        int availableSlots = getAvailableImageSlots();

        if (data.getClipData() != null) {
            // Trường hợp chọn NHIỀU ẢNH
            int selectedCount = data.getClipData().getItemCount();
            int limit = Math.min(selectedCount, availableSlots);

            for (int i = 0; i < limit; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                addImageToTour(imageUri);
            }

            if (selectedCount > availableSlots) {
                Toast.makeText(getContext(),
                        String.format("Chỉ thêm được %d ảnh. Đã đạt giới hạn %d.", limit, MAX_IMAGE_COUNT),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), String.format("Đã thêm %d ảnh.", limit), Toast.LENGTH_SHORT).show();
            }

        } else if (data.getData() != null) {
            // Trường hợp chọn MỘT ẢNH
            if (availableSlots > 0) {
                Uri imageUri = data.getData();
                addImageToTour(imageUri);
                Toast.makeText(getContext(), "Đã thêm 1 ảnh.", Toast.LENGTH_SHORT).show();
            }
        }

        updateDisplay();
    }

    /**
     * Thêm Uri ảnh vào danh sách của Tour (Lưu dưới dạng String Uri).
     */
    private void addImageToTour(Uri imageUri) {
        List<String> imageList = tour.getDanhSachHinhAnh();
        if (imageList != null && imageList.size() < MAX_IMAGE_COUNT) {
            imageList.add(imageUri.toString());
            // TODO: Triển khai createImageView để hiển thị ảnh
        }
    }

    private int getAvailableImageSlots() {
        List<String> imageList = tour.getDanhSachHinhAnh();
        int currentCount = imageList != null ? imageList.size() : 0;
        return MAX_IMAGE_COUNT - currentCount;
    }

    private void loadExistingImages() {
        List<String> imageList = tour.getDanhSachHinhAnh();
        if (imageList != null) {
            for (String uriString : imageList) {
                // TODO: createImageView(Uri.parse(uriString));
            }
        }
    }
    private void updateDisplay() {
        if (tour != null) {
            // SEO
            if (tour.getMoTaSeo() != null) {
                etSeoDescription.setText(tour.getMoTaSeo());
            }

            // Trạng thái
            switchPublishStatus.setChecked(tour.getIsXuatBan());
            switchFeaturedTour.setChecked(tour.getIsNoiBat()); // ⭐ Đã sửa lỗi logic: dùng getIsNoiBat()

            // Hình ảnh
            List<String> imageList = tour.getDanhSachHinhAnh();
            int imageCount = imageList != null ? imageList.size() : 0;

            tvImageCount.setText(String.format("Đã tải: %d/%d ảnh. Ảnh đầu tiên sẽ là ảnh bìa.", imageCount, MAX_IMAGE_COUNT));

            // Ẩn/hiện nút thêm ảnh nếu đạt giới hạn
            if (imageCount >= MAX_IMAGE_COUNT) {
                cardAddImageButton.setVisibility(View.GONE);
            } else {
                cardAddImageButton.setVisibility(View.VISIBLE);
            }

            // TODO: Logic hiển thị ImageView vào llImageListContainer
        }
    }

    /**
     * Thực hiện Thu thập và Validation dữ liệu của Bước 4.
     */
    @Override
    public boolean collectDataAndValidate(Tour tour) {

        String seoDescription = Objects.requireNonNull(etSeoDescription.getText()).toString().trim();
        boolean isPublished = switchPublishStatus.isChecked();
        boolean isFeatured = switchFeaturedTour.isChecked();

        List<String> imageList = tour.getDanhSachHinhAnh();
        int imageCount = imageList != null ? imageList.size() : 0;

        // 1. Validation
        if (imageCount == 0) {
            Toast.makeText(getContext(), "Vui lòng tải lên ít nhất 1 ảnh cho Tour.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (seoDescription.length() < MIN_SEO_LENGTH) {
            Toast.makeText(getContext(), "Mô tả SEO phải có ít nhất " + MIN_SEO_LENGTH + " ký tự để tối ưu.", Toast.LENGTH_SHORT).show();
            etSeoDescription.setError("Mô tả quá ngắn. Tối thiểu " + MIN_SEO_LENGTH + " ký tự.");
            return false;
        }

        // 2. Gán dữ liệu
        tour.setMoTaSeo(seoDescription);
        tour.setIsXuatBan(isPublished);
        tour.setIsNoiBat(isFeatured);

        // Gán hình ảnh chính
        if (imageCount > 0) {
            tour.setHinhAnhChinhUrl(imageList.get(0));
        }

        Log.d(TAG, "Bước 4 - Hình ảnh: Thu thập thành công. Xuất bản: " + isPublished);
        return true;
    }
}