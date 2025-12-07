package com.example.quanlytourdl.model;

import java.util.Date;
import java.util.List;

/**
 * Lớp Tour model, được cập nhật để tương thích với Firestore
 * và bao gồm đầy đủ thông tin chi tiết, quản lý và phê duyệt.
 */
public class Tour {
    // ------------------- THÔNG TIN CƠ BẢN VÀ GIÁ -------------------
    private String maTour;
    private String tenTour;
    private String diemKhoiHanh; // Địa điểm khởi hành
    private String diemDen;      // Địa điểm kết thúc
    private Date ngayKhoiHanh;   // Ngày khởi hành
    private long giaTour;

    // ------------------- THÔNG TIN CHI TIẾT -------------------
    private int soNgay;               // Số ngày của tour (ví dụ: 5)
    private String diemDenChinh;      // Điểm đến nổi bật nhất/chính (dùng cho hiển thị card)
    private int soLuongToiDa;         // Sức chứa tối đa của tour (Capacity)
    private int soLuongKhach;         // Số lượng khách đã đặt (Booked Count) - Cần thiết cho việc tính toán còn chỗ
    private String lichTrinhChiTiet;  // Mô tả lịch trình chi tiết (dạng text hoặc JSON string)
    private String phuongTien;        // Phương tiện di chuyển (Ví dụ: Máy bay, Tàu hỏa)
    private String tenNhaCungCap;     // Tên đơn vị cung cấp/tổ chức tour (đại lý)

    // ------------------- TRƯỜNG QUẢN LÝ VÀ PHÊ DUYỆT -------------------
    private String nguoiTao; // ID người tạo
    private Date ngayTao;    // Ngày tạo tour
    private String status;   // Trạng thái: CHO_PHE_DUYET, DANG_MO_BAN, DA_TU_CHOI

    // ------------------- TRƯỜNG MARKETING VÀ HÌNH ẢNH -------------------
    private String seoDescription;
    private boolean isFeatured; // Tour nổi bật (featured)
    private List<String> imageUrls;
    private String anhThumbnailUrl; // Giữ lại cho tiện (thường là imageUrls[0])


    // QUAN TRỌNG: Constructor rỗng (No-argument constructor) BẮT BUỘNG cho Firestore
    public Tour() {
        // Khởi tạo mặc định
    }


    // ------------------- GETTERS VÀ SETTERS CƠ BẢN -------------------

    public String getMaTour() { return maTour; }
    public void setMaTour(String maTour) { this.maTour = maTour; }

    public String getTenTour() { return tenTour; }
    public void setTenTour(String tenTour) { this.tenTour = tenTour; }

    public String getDiemKhoiHanh() { return diemKhoiHanh; }
    public void setDiemKhoiHanh(String diemKhoiHanh) { this.diemKhoiHanh = diemKhoiHanh; }

    public String getDiemDen() { return diemDen; }
    public void setDiemDen(String diemDen) { this.diemDen = diemDen; }

    public Date getNgayKhoiHanh() { return ngayKhoiHanh; }
    public void setNgayKhoiHanh(Date ngayKhoiHanh) { this.ngayKhoiHanh = ngayKhoiHanh; }

    public long getGiaTour() { return giaTour; }
    public void setGiaTour(long giaTour) { this.giaTour = giaTour; }


    // ------------------- GETTERS VÀ SETTERS CHI TIẾT -------------------

    public int getSoNgay() { return soNgay; }
    public void setSoNgay(int soNgay) { this.soNgay = soNgay; }

    public String getDiemDenChinh() { return diemDenChinh; }
    public void setDiemDenChinh(String diemDenChinh) { this.diemDenChinh = diemDenChinh; }

    public int getSoLuongToiDa() { return soLuongToiDa; }
    public void setSoLuongToiDa(int soLuongToiDa) { this.soLuongToiDa = soLuongToiDa; }

    public int getSoLuongKhach() { return soLuongKhach; }
    public void setSoLuongKhach(int soLuongKhach) { this.soLuongKhach = soLuongKhach; }

    public String getLichTrinhChiTiet() { return lichTrinhChiTiet; }
    public void setLichTrinhChiTiet(String lichTrinhChiTiet) { this.lichTrinhChiTiet = lichTrinhChiTiet; }

    public String getPhuongTien() { return phuongTien; }
    public void setPhuongTien(String phuongTien) { this.phuongTien = phuongTien; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }


    // ------------------- GETTERS VÀ SETTERS QUẢN LÝ & MARKETING -------------------

    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String nguoiTao) { this.nguoiTao = nguoiTao; }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSeoDescription() { return seoDescription; }
    public void setSeoDescription(String seoDescription) { this.seoDescription = seoDescription; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getAnhThumbnailUrl() { return anhThumbnailUrl; }
    public void setAnhThumbnailUrl(String anhThumbnailUrl) { this.anhThumbnailUrl = anhThumbnailUrl; }
}