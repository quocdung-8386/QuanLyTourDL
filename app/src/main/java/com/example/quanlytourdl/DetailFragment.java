package com.example.quanlytourdl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat; // Để sử dụng màu sắc an toàn hơn

import com.example.quanlytourdl.model.Guide;
import com.example.quanlytourdl.model.Vehicle;
import com.example.quanlytourdl.model.NhaCungCap;
import com.example.quanlytourdl.model.HopDong; // ⭐ IMPORT MODEL HOPDONG
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class DetailFragment extends Fragment {

    private static final String TAG = "DetailFragment";
    private static final String ARG_GUIDE = "arg_guide";
    private static final String ARG_VEHICLE = "arg_vehicle";
    private static final String ARG_NHACUNGCAP = "arg_nha_cung_cap";
    private static final String ARG_HOPDONG = "arg_hop_dong"; // ⭐ HẰNG SỐ MỚI CHO HỢP ĐỒNG

    private Guide guide;
    private Vehicle vehicle;
    private NhaCungCap nhaCungCap;
    private HopDong hopDong; // ⭐ ĐỐI TƯỢNG MỚI CHO HỢP ĐỒNG

    // UI Components
    private ImageButton btnCloseDetail;
    private TextView tvDetailTitle;
    private LinearLayout detailContainer;
    private MaterialButton btnEditDetail;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Phương thức khởi tạo Fragment chính (4 tham số).
     */
    public static DetailFragment newInstance(Guide guide, Vehicle vehicle, NhaCungCap nhaCungCap, HopDong hopDong) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        if (guide != null) {
            args.putSerializable(ARG_GUIDE, guide);
        } else if (vehicle != null) {
            args.putSerializable(ARG_VEHICLE, vehicle);
        } else if (nhaCungCap != null) {
            args.putSerializable(ARG_NHACUNGCAP, nhaCungCap);
        } else if (hopDong != null) {
            args.putSerializable(ARG_HOPDONG, hopDong);
        }
        fragment.setArguments(args);
        return fragment;
    }

    // ⭐ PHƯƠNG THỨC QUÁ TẢI MỚI: Chỉ dành cho Guide
    public static DetailFragment newInstance(Guide guide) {
        return newInstance(guide, null, null, null);
    }

    // ⭐ PHƯƠNG THỨC QUÁ TẢI MỚI: Chỉ dành cho Vehicle
    public static DetailFragment newInstance(Vehicle vehicle) {
        return newInstance(null, vehicle, null, null);
    }

    // ⭐ PHƯƠNG THỨC QUÁ TẢI MỚI: Chỉ dành cho NhaCungCap
    public static DetailFragment newInstance(NhaCungCap nhaCungCap) {
        return newInstance(null, null, nhaCungCap, null);
    }

    // ⭐ Hàm newInstance cho riêng đối tượng HopDong (Đã có, giữ nguyên)
    public static DetailFragment newInstance(HopDong hopDong) {
        return newInstance(null, null, null, hopDong);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            guide = (Guide) getArguments().getSerializable(ARG_GUIDE);
            vehicle = (Vehicle) getArguments().getSerializable(ARG_VEHICLE);
            nhaCungCap = (NhaCungCap) getArguments().getSerializable(ARG_NHACUNGCAP);
            hopDong = (HopDong) getArguments().getSerializable(ARG_HOPDONG); // ⭐ ĐỌC DỮ LIỆU HOPDONG MỚI
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_dialog, container, false);
        mapViews(view);
        setupListeners();

        if (guide != null) {
            displayGuideDetails(inflater);
        } else if (vehicle != null) {
            displayVehicleDetails(inflater);
        } else if (nhaCungCap != null) {
            displayNhaCungCapDetails(inflater);
        } else if (hopDong != null) { // ⭐ GỌI HÀM HIỂN THỊ HỢP ĐỒNG
            displayHopDongDetails(inflater);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy dữ liệu để hiển thị chi tiết.", Toast.LENGTH_LONG).show();
            closeFragment();
        }

        return view;
    }

    private void mapViews(View view) {
        btnCloseDetail = view.findViewById(R.id.btn_close_detail);
        tvDetailTitle = view.findViewById(R.id.tv_detail_title);
        detailContainer = view.findViewById(R.id.detail_container);
        btnEditDetail = view.findViewById(R.id.btn_edit_detail);
    }

    private void setupListeners() {
        btnCloseDetail.setOnClickListener(v -> closeFragment());

        btnEditDetail.setOnClickListener(v -> {
            if (guide != null) {
                openEditFragment(guide.getId(), 0); // 0: Guide
            } else if (vehicle != null) {
                openEditFragment(vehicle.getId(), 1); // 1: Vehicle
            } else if (nhaCungCap != null) {
                openEditFragment(nhaCungCap.getMaNhaCungCap(), 2); // 2: NhaCungCap
            } else if (hopDong != null) {
                openEditFragment(hopDong.getDocumentId(), 3); // ⭐ 3: HopDong (Sử dụng DocumentId hoặc MaHopDong)
            }
        });
    }

    /**
     * Điền dữ liệu cho Hướng dẫn viên
     */
    private void displayGuideDetails(LayoutInflater inflater) {
        tvDetailTitle.setText("Chi tiết Hướng dẫn viên");

        // 1. Inflate layout chi tiết HDV
        View guideDetailsView = inflater.inflate(R.layout.view_guide_details, detailContainer, false);
        detailContainer.addView(guideDetailsView);

        // 2. Điền dữ liệu
        setDetailRow(guideDetailsView.findViewById(R.id.row_id), "Mã định danh (ID)", guide.getId());
        setDetailRow(guideDetailsView.findViewById(R.id.row_full_name), "Tên đầy đủ", guide.getFullName());
        setDetailRow(guideDetailsView.findViewById(R.id.row_guide_code), "Mã HDV", guide.getGuideCode());
        setDetailRow(guideDetailsView.findViewById(R.id.row_sdt), "Số điện thoại", guide.getSdt());
        setDetailRow(guideDetailsView.findViewById(R.id.row_email), "Email", guide.getEmail());
        setDetailRow(guideDetailsView.findViewById(R.id.row_trang_thai), "Trạng thái", guide.getTrangThai());
        setDetailRow(guideDetailsView.findViewById(R.id.row_experience_years), "Kinh nghiệm (năm)", String.valueOf(guide.getExperienceYears()));
        setDetailRow(guideDetailsView.findViewById(R.id.row_rating), "Đánh giá (Rating)", String.format(Locale.getDefault(), "%.1f sao", guide.getRating()));

        // 3. Xử lý ChipGroup Ngôn ngữ
        ChipGroup chipGroup = guideDetailsView.findViewById(R.id.chip_group_languages_detail);
        if (guide.getLanguages() != null) {
            addChipsToGroup(chipGroup, guide.getLanguages());
        }
    }

    /**
     * Điền dữ liệu cho Phương tiện
     */
    private void displayVehicleDetails(LayoutInflater inflater) {
        tvDetailTitle.setText("Chi tiết Phương tiện");

        // 1. Inflate layout chi tiết PT
        View vehicleDetailsView = inflater.inflate(R.layout.view_vehicle_details, detailContainer, false);
        detailContainer.addView(vehicleDetailsView);

        // 2. Điền dữ liệu
        setDetailRow(vehicleDetailsView.findViewById(R.id.row_bien_so_xe), "Biển số xe", vehicle.getBienSoXe());
        setDetailRow(vehicleDetailsView.findViewById(R.id.row_loai_phuong_tien), "Loại phương tiện", vehicle.getLoaiPhuongTien());
        setDetailRow(vehicleDetailsView.findViewById(R.id.row_hang_xe), "Hãng xe", vehicle.getHangXe());
        setDetailRow(vehicleDetailsView.findViewById(R.id.row_so_cho_ngoi), "Số chỗ ngồi", String.valueOf(vehicle.getSoChoNgoi()));
        setDetailRow(vehicleDetailsView.findViewById(R.id.row_tinh_trang_bao_duong), "Tình trạng bảo dưỡng", vehicle.getTinhTrangBaoDuong());

        // Tài xế được gán
        String driverName = (vehicle.getDriverName() != null && !vehicle.getDriverName().isEmpty()) ? vehicle.getDriverName() : "Chưa gán";
        setDetailRow(vehicleDetailsView.findViewById(R.id.row_driver_name), "Tài xế được gán", driverName);
    }

    /**
     * Điền dữ liệu cho Nhà Cung Cấp
     */
    private void displayNhaCungCapDetails(LayoutInflater inflater) {
        tvDetailTitle.setText("Chi tiết Nhà Cung Cấp");

        // 1. Inflate layout chi tiết NCC
        View nccDetailsView = inflater.inflate(R.layout.view_nha_cung_cap_details, detailContainer, false);
        detailContainer.addView(nccDetailsView);

        // 2. Điền dữ liệu
        // Nhóm Thông tin chung
        setDetailRow(nccDetailsView.findViewById(R.id.row_ma_ncc), "Mã NCC (ID)", nhaCungCap.getMaNhaCungCap());
        setDetailRow(nccDetailsView.findViewById(R.id.row_ten_ncc), "Tên Nhà Cung Cấp", nhaCungCap.getTenNhaCungCap());
        setDetailRow(nccDetailsView.findViewById(R.id.row_loai_dich_vu), "Loại Dịch Vụ", nhaCungCap.getLoaiDichVu());
        setDetailRow(nccDetailsView.findViewById(R.id.row_dia_chi), "Địa Chỉ", nhaCungCap.getDiaChi());
        setDetailRow(nccDetailsView.findViewById(R.id.row_nguoi_lien_he), "Người Liên Hệ", nhaCungCap.getNguoiLienHe());

        // Nhóm Thông tin liên hệ
        setDetailRow(nccDetailsView.findViewById(R.id.row_sdt), "Số Điện Thoại", nhaCungCap.getSoDienThoai());
        setDetailRow(nccDetailsView.findViewById(R.id.row_email), "Email", nhaCungCap.getEmail());

        // Nhóm Thông tin Hợp đồng
        setDetailRow(nccDetailsView.findViewById(R.id.row_trang_thai_hd), "Trạng Thái Hợp Đồng", nhaCungCap.getTrangThaiHopDong());
        setDetailRow(nccDetailsView.findViewById(R.id.row_ma_hd_active), "Mã HĐ Hoạt động", nhaCungCap.getMaHopDong());
        setDetailRow(nccDetailsView.findViewById(R.id.row_ma_hd_gan_nhat), "Mã HĐ Gần Nhất", nhaCungCap.getMaHopDongGanNhat());
        setDetailRow(nccDetailsView.findViewById(R.id.row_nguoi_dung_tao), "Mã Người Dùng Tạo", nhaCungCap.getMaNguoiDungTao());
    }

    /**
     * ⭐ Điền dữ liệu cho Hợp đồng
     */
    private void displayHopDongDetails(LayoutInflater inflater) {
        tvDetailTitle.setText("Chi tiết Hợp đồng");

        // 1. Inflate layout chi tiết Hợp đồng (Giả định layout view_hop_dong_details tồn tại)
        View contractDetailsView = inflater.inflate(R.layout.view_hop_dong_details, detailContainer, false);
        detailContainer.addView(contractDetailsView);

        // 2. Điền dữ liệu
        // Nhóm Thông tin Nhà Cung Cấp (Lấy từ HopDong, giả định tên phương thức get trong model đã được điều chỉnh)
        // Lưu ý: ID row_ma_ncc và row_ten_ncc được dùng lại từ item_detail_row
        setDetailRow(contractDetailsView.findViewById(R.id.row_supplier_id), "Mã NCC", hopDong.getSupplierId());
        setDetailRow(contractDetailsView.findViewById(R.id.row_ten_ncc), "Tên NCC", hopDong.getNhaCungCap());

        // Nhóm Thông tin Hợp đồng
        setDetailRow(contractDetailsView.findViewById(R.id.row_ma_hd_active), "Mã HĐ (ID)", hopDong.getMaHopDong());
        setDetailRow(contractDetailsView.findViewById(R.id.row_trang_thai_hd), "Trạng Thái", hopDong.getTrangThai());
        setDetailRow(contractDetailsView.findViewById(R.id.row_ngay_ky), "Ngày Ký", hopDong.getNgayKyKet());
        setDetailRow(contractDetailsView.findViewById(R.id.row_ngay_het_han), "Ngày Hết Hạn", hopDong.getNgayHetHan());
        setDetailRow(contractDetailsView.findViewById(R.id.row_dieu_khoan), "Điều khoản TT", hopDong.getDieuKhoanThanhToan());
        setDetailRow(contractDetailsView.findViewById(R.id.row_ngay_cap_nhat), "Ngày Cập nhật", hopDong.getNgayCapNhat());

        // Xử lý thông tin Chấm dứt
        View rowLyDoChamDut = contractDetailsView.findViewById(R.id.row_ly_do_cham_dut);
        View rowGhiChuChamDut = contractDetailsView.findViewById(R.id.row_ghi_chu_cham_dut);

        if ("Chấm dứt".equalsIgnoreCase(hopDong.getTrangThai()) && rowLyDoChamDut != null && rowGhiChuChamDut != null) {
            setDetailRow(rowLyDoChamDut, "Lý do Chấm dứt", hopDong.getLyDoChamDut());
            setDetailRow(rowGhiChuChamDut, "Ghi chú Chấm dứt", hopDong.getGhiChuChamDut());
            rowLyDoChamDut.setVisibility(View.VISIBLE);
            rowGhiChuChamDut.setVisibility(View.VISIBLE);
        } else if (rowLyDoChamDut != null && rowGhiChuChamDut != null) {
            rowLyDoChamDut.setVisibility(View.GONE);
            rowGhiChuChamDut.setVisibility(View.GONE);
        }

        // Xử lý Nội dung hợp đồng (TextView lớn)
        TextView tvNoiDung = contractDetailsView.findViewById(R.id.tv_noi_dung_hop_dong);
        if (tvNoiDung != null) {
            String noiDung = hopDong.getNoiDung();
            if (noiDung == null || noiDung.trim().isEmpty()) {
                tvNoiDung.setText("Không có nội dung chi tiết được ghi nhận.");
                tvNoiDung.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            } else {
                tvNoiDung.setText(noiDung);
                tvNoiDung.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            }
        }
    }

    /**
     * Hàm hỗ trợ điền dữ liệu cho một hàng (item_detail_row.xml)
     */
    private void setDetailRow(View rowView, String label, String value) {
        // ⭐ Đảm bảo ID được sử dụng đúng theo item_detail_row.xml
        TextView tvLabel = rowView.findViewById(R.id.tv_label);
        TextView tvValue = rowView.findViewById(R.id.tv_value);

        if (tvLabel == null || tvValue == null) {
            Log.e(TAG, "Lỗi: setDetailRow không tìm thấy ID tv_label hoặc tv_value trong rowView.");
            return;
        }

        tvLabel.setText(label);
        // Kiểm tra null hoặc rỗng cho giá trị
        if (value == null || value.trim().isEmpty()) {
            tvValue.setText("N/A");
            tvValue.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        } else {
            tvValue.setText(value);
            tvValue.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        }
    }

    /**
     * Hàm hỗ trợ thêm Chips vào ChipGroup (Chỉ dùng cho HDV)
     */
    private void addChipsToGroup(ChipGroup chipGroup, List<String> items) {
        chipGroup.removeAllViews();
        if (items == null || items.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText("Không có ngôn ngữ nào được ghi nhận.");
            tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            chipGroup.addView(tv);
            return;
        }

        for (String item : items) {
            Chip chip = (Chip) LayoutInflater.from(getContext()).inflate(R.layout.chip_item_readonly, chipGroup, false);
            chip.setText(item);
            chipGroup.addView(chip);
        }
    }

    /**
     * Chuyển sang Fragment Sửa tương ứng
     * Thay đổi logic để dùng mã type (0: Guide, 1: Vehicle, 2: NhaCungCap, 3: HopDong)
     */
    private void openEditFragment(String id, int type) {
        Fragment targetFragment;

        switch (type) {
            case 0: // Guide
                // Giả định SuaHdvFragment.newInstance(String id) tồn tại
                targetFragment = SuaHdvFragment.newInstance(id);
                Log.d(TAG, "Mở màn hình Sửa HDV ID: " + id);
                break;
            case 1: // Vehicle
                // Giả định SuaPhuongTienFragment.newInstance(String id) tồn tại
                targetFragment = SuaPhuongTienFragment.newInstance(id);
                Log.d(TAG, "Mở màn hình Sửa PT ID: " + id);
                break;
            case 2: // NhaCungCap
                // Giả định SuaNhaCungCapFragment.newInstance(String id) tồn tại
                targetFragment = SuaNhaCungCapFragment.newInstance(id);
                Log.d(TAG, "Mở màn hình Sửa NCC ID: " + id);
                break;
            case 3: // ⭐ HopDong
                // Giả định SuaHopDongFragment.newInstance(String contractId) tồn tại
                targetFragment = SuaHopDongFragment.newInstance(id);
                Log.d(TAG, "Mở màn hình Sửa HĐ ID: " + id);
                break;
            default:
                Toast.makeText(getContext(), "Lỗi: Không xác định được loại đối tượng cần sửa.", Toast.LENGTH_SHORT).show();
                return;
        }

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content_frame, targetFragment) // Thay thế frame chính
                    .addToBackStack(null) // Thêm giao dịch vào back stack
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    private void closeFragment() {
        if (getActivity() != null) {
            // Quay lại Fragment trước đó
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}