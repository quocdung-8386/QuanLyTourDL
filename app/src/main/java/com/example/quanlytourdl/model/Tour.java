package com.example.quanlytourdl.model;

import com.google.firebase.firestore.DocumentId; // <--- THÊM IMPORT NÀY
import java.util.Date;
import java.util.List;

/**
 * Lớp Tour model, được cập nhật để tương thích với Firestore
 * và bao gồm đầy đủ thông tin chi tiết, quản lý và phê duyệt.
 */
public class Tour {
    // ------------------- THÔNG TIN CƠ BẢN VÀ GIÁ -------------------
    @DocumentId // <--- THÊM ANNOTATION NÀY để map Document ID vào maTour
    private String maTour;
    private String tenTour;
    private String diemKhoiHanh;
    private String diemDen;
    private Date ngayKhoiHanh;
    private long giaTour;

    // ------------------- THÔNG TIN CHI TIẾT -------------------
    private int soNgay;
    private String diemDenChinh;
    private int soLuongToiDa;
    private int soLuongKhach;
    private String lichTrinhChiTiet;
    private String phuongTien;
    private String tenNhaCungCap;

    // ------------------- TRƯỜNG QUẢN LÝ VÀ PHÊ DUYỆT -------------------
    private String nguoiTao;
    private Date ngayTao;
    private String status;

    // ------------------- TRƯỜNG MARKETING VÀ HÌNH ẢNH -------------------
    private String seoDescription;
    private boolean isFeatured;
    private List<String> imageUrls;
    private String anhThumbnailUrl;


    // QUAN TRỌNG: Constructor rỗng (No-argument constructor) BẮT BUỘC cho Firestore
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