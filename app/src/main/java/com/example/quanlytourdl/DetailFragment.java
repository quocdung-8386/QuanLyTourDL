package com.example.quanlytourdl;

import android.os.Bundle;
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
import androidx.core.content.ContextCompat;

import com.example.quanlytourdl.model.Guide;
import com.example.quanlytourdl.model.Vehicle;
import com.example.quanlytourdl.model.NhaCungCap;
import com.example.quanlytourdl.model.HopDong;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DetailFragment extends Fragment {

    private static final String ARG_DATA = "arg_generic_data";
    private Object dataModel;

    private ImageButton btnCloseDetail;
    private TextView tvDetailTitle;
    private LinearLayout detailContainer;
    private MaterialButton btnEditDetail;

    public DetailFragment() { }

    public static DetailFragment newInstance(Serializable data) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATA, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dataModel = getArguments().getSerializable(ARG_DATA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_dialog, container, false);
        mapViews(view);
        setupListeners();
        renderContent(inflater);
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
        btnEditDetail.setOnClickListener(v -> handleEditAction());
    }

    private void renderContent(LayoutInflater inflater) {
        detailContainer.removeAllViews();
        if (dataModel instanceof Guide) {
            displayGuideDetails(inflater, (Guide) dataModel);
        } else if (dataModel instanceof Vehicle) {
            displayVehicleDetails(inflater, (Vehicle) dataModel);
        } else if (dataModel instanceof NhaCungCap) {
            displayNhaCungCapDetails(inflater, (NhaCungCap) dataModel);
        } else if (dataModel instanceof HopDong) {
            displayHopDongDetails(inflater, (HopDong) dataModel);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy dữ liệu!", Toast.LENGTH_SHORT).show();
            closeFragment();
        }
    }

    // --- 1. CHI TIẾT HƯỚNG DẪN VIÊN ---
    private void displayGuideDetails(LayoutInflater inflater, Guide guide) {
        tvDetailTitle.setText("Chi tiết Hướng dẫn viên");
        View v = inflater.inflate(R.layout.view_guide_details, detailContainer, false);
        detailContainer.addView(v);

        setDetailRow(v.findViewById(R.id.row_full_name), "Họ và tên", guide.getFullName());
        setDetailRow(v.findViewById(R.id.row_guide_code), "Mã số HDV", guide.getGuideCode());
        setDetailRow(v.findViewById(R.id.row_sdt), "Số điện thoại", guide.getPhoneNumber());
        setDetailRow(v.findViewById(R.id.row_email), "Email", guide.getEmail());
        setDetailRow(v.findViewById(R.id.row_experience_years), "Kinh nghiệm", guide.getExperienceYears() + " năm");

        String status = guide.isApproved() ? "Đã phê duyệt" : "Chờ phê duyệt";
        int color = guide.isApproved() ? R.color.green_700 : R.color.orange_700;
        setDetailRowColored(v.findViewById(R.id.row_trang_thai), "Trạng thái", status, color);

        addChipsToGroup(v.findViewById(R.id.chip_group_languages_detail), guide.getLanguages());
    }

    // --- 2. CHI TIẾT PHƯƠNG TIỆN ---
    private void displayVehicleDetails(LayoutInflater inflater, Vehicle vehicle) {
        tvDetailTitle.setText("Chi tiết Phương tiện");
        View v = inflater.inflate(R.layout.view_vehicle_details, detailContainer, false);
        detailContainer.addView(v);

        setDetailRow(v.findViewById(R.id.row_bien_so_xe), "Biển số xe", vehicle.getBienSoXe());
        setDetailRow(v.findViewById(R.id.row_loai_phuong_tien), "Loại xe", vehicle.getLoaiPhuongTien());
        setDetailRow(v.findViewById(R.id.row_hang_xe), "Hãng sản xuất", vehicle.getHangXe());
        setDetailRow(v.findViewById(R.id.row_so_cho_ngoi), "Số chỗ ngồi", String.valueOf(vehicle.getSoChoNgoi()) + " chỗ");

        // Cập nhật lấy thông tin tài xế từ Model Vehicle mới
        setDetailRow(v.findViewById(R.id.row_ten_tai_xe), "Tên tài xế", vehicle.getTenTaiXe());
        setDetailRow(v.findViewById(R.id.row_sdt_tai_xe), "SĐT tài xế", vehicle.getSoDienThoaiTaiXe());

        String tinhTrang = vehicle.getTinhTrangBaoDuong();
        int color = "Hoạt động tốt".equalsIgnoreCase(tinhTrang) ? R.color.green_700 : R.color.orange_700;
        setDetailRowColored(v.findViewById(R.id.row_tinh_trang_bao_duong), "Tình trạng", tinhTrang, color);
    }

    // --- 3. CHI TIẾT NHÀ CUNG CẤP ---
    private void displayNhaCungCapDetails(LayoutInflater inflater, NhaCungCap ncc) {
        tvDetailTitle.setText("Chi tiết Đối tác");
        View v = inflater.inflate(R.layout.view_nha_cung_cap_details, detailContainer, false);
        detailContainer.addView(v);

        setDetailRow(v.findViewById(R.id.row_ma_ncc), "Mã định danh", ncc.getMaNhaCungCap());
        setDetailRow(v.findViewById(R.id.row_ten_ncc), "Tên đơn vị", ncc.getTenNhaCungCap());
        setDetailRow(v.findViewById(R.id.row_loai_dich_vu), "Dịch vụ", ncc.getLoaiDichVu());
        setDetailRow(v.findViewById(R.id.row_dia_chi), "Địa chỉ", ncc.getDiaChi());
        setDetailRow(v.findViewById(R.id.row_nguoi_lien_he), "Người liên hệ", ncc.getNguoiLienHe());
        setDetailRow(v.findViewById(R.id.row_sdt), "Số điện thoại", ncc.getSoDienThoai());
        setDetailRow(v.findViewById(R.id.row_email), "Email", ncc.getEmail());

        // Thông tin hợp đồng liên quan
        setDetailRow(v.findViewById(R.id.row_ma_hd_active), "Mã HĐ hiện tại", ncc.getMaHopDong());
        setDetailRow(v.findViewById(R.id.row_ma_hd_gan_nhat), "HĐ gần nhất", ncc.getMaHopDongGanNhat());
        setDetailRow(v.findViewById(R.id.row_nguoi_dung_tao), "Người tạo", ncc.getMaNguoiDungTao());

        int statusColor = "Đang hiệu lực".equalsIgnoreCase(ncc.getTrangThaiHopDong()) ? R.color.green_700 : R.color.red_700;
        setDetailRowColored(v.findViewById(R.id.row_trang_thai_hd), "Trạng thái HĐ", ncc.getTrangThaiHopDong(), statusColor);

        // Các trường mới: Đánh giá hiệu suất
        String diemStr = ncc.getDiemHieuSuat() > 0 ? ncc.getDiemHieuSuat() + " / 5.0" : null;
        setDetailRow(v.findViewById(R.id.row_diem_hieu_suat), "Điểm hiệu suất", diemStr);
        setDetailRow(v.findViewById(R.id.row_nhan_xet_hieu_suat), "Nhận xét", ncc.getNhanXetHieuSuat());

        if (ncc.getNgayDanhGia() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            setDetailRow(v.findViewById(R.id.row_ngay_danh_gia), "Ngày đánh giá", sdf.format(ncc.getNgayDanhGia().toDate()));
        }
    }

    // --- 4. CHI TIẾT HỢP ĐỒNG ---
    private void displayHopDongDetails(LayoutInflater inflater, HopDong hopDong) {
        tvDetailTitle.setText("Chi tiết Hợp đồng");
        View v = inflater.inflate(R.layout.view_hop_dong_details, detailContainer, false);
        detailContainer.addView(v);

        setDetailRow(v.findViewById(R.id.row_ten_ncc), "Đối tác", hopDong.getNhaCungCap());
        setDetailRow(v.findViewById(R.id.row_ma_hd_active), "Mã hợp đồng", hopDong.getMaHopDong());
        setDetailRow(v.findViewById(R.id.row_ngay_ky), "Ngày ký kết", hopDong.getNgayKyKet());
        setDetailRow(v.findViewById(R.id.row_ngay_het_han), "Hết hạn", hopDong.getNgayHetHan());
        setDetailRow(v.findViewById(R.id.row_dieu_khoan), "Thanh toán", hopDong.getDieuKhoanThanhToan());
        setDetailRow(v.findViewById(R.id.row_ngay_cap_nhat), "Cập nhật lúc", hopDong.getNgayCapNhatFormatted());

        int statusColor = R.color.grey_text;
        String trangThai = hopDong.getTrangThai();
        if ("Đang hiệu lực".equalsIgnoreCase(trangThai)) statusColor = R.color.green_700;
        else if ("Đã chấm dứt".equalsIgnoreCase(trangThai)) statusColor = R.color.red_700;
        else if ("Hết hạn".equalsIgnoreCase(trangThai)) statusColor = R.color.orange_700;

        setDetailRowColored(v.findViewById(R.id.row_trang_thai_hd), "Trạng thái", trangThai, statusColor);
        setDetailRow(v.findViewById(R.id.row_ly_do_cham_dut), "Lý do chấm dứt", hopDong.getLyDoChamDut());

        TextView tvNoiDung = v.findViewById(R.id.tv_noi_dung_hop_dong);
        if (tvNoiDung != null && hopDong.getNoiDung() != null) {
            tvNoiDung.setText(hopDong.getNoiDung());
        }
    }

    // --- HELPER METHODS ---

    private void setDetailRow(View rowView, String label, String value) {
        if (rowView == null) return;
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("null") || value.equals("0.0 / 5.0")) {
            rowView.setVisibility(View.GONE);
            return;
        }
        rowView.setVisibility(View.VISIBLE);
        TextView tvLabel = rowView.findViewById(R.id.tv_label);
        TextView tvValue = rowView.findViewById(R.id.tv_value);
        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null) tvValue.setText(value);
    }

    private void setDetailRowColored(View rowView, String label, String value, int colorResId) {
        setDetailRow(rowView, label, value);
        if (rowView.getVisibility() == View.VISIBLE) {
            TextView tvValue = rowView.findViewById(R.id.tv_value);
            if (tvValue != null && isAdded()) {
                tvValue.setTextColor(ContextCompat.getColor(requireContext(), colorResId));
                tvValue.setTypeface(null, android.graphics.Typeface.BOLD);
            }
        }
    }

    private void addChipsToGroup(ChipGroup chipGroup, List<String> items) {
        if (chipGroup == null) return;
        chipGroup.removeAllViews();
        if (items == null || items.isEmpty()) {
            chipGroup.setVisibility(View.GONE);
            return;
        }
        chipGroup.setVisibility(View.VISIBLE);
        for (String item : items) {
            Chip chip = new Chip(requireContext());
            chip.setText(item);
            chip.setCheckable(false);
            chipGroup.addView(chip);
        }
    }

    private void handleEditAction() {
        Fragment target;
        String id;
        if (dataModel instanceof Guide) {
            id = ((Guide) dataModel).getId();
            target = SuaHdvFragment.newInstance(id);
        } else if (dataModel instanceof Vehicle) {
            id = ((Vehicle) dataModel).getId();
            target = SuaPhuongTienFragment.newInstance(id);
        } else if (dataModel instanceof NhaCungCap) {
            id = ((NhaCungCap) dataModel).getMaNhaCungCap();
            target = SuaNhaCungCapFragment.newInstance(id);
        } else if (dataModel instanceof HopDong) {
            id = ((HopDong) dataModel).getDocumentId();
            target = SuaHopDongFragment.newInstance(id);
        } else return;

        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.main_content_frame, target)
                .addToBackStack(null)
                .commit();
    }

    private void closeFragment() {
        if (isAdded()) requireActivity().getSupportFragmentManager().popBackStack();
    }
}