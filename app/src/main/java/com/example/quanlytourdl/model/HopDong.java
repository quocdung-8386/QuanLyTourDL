package com.example.quanlytourdl.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

/**
 * Model class for HopDong (Contract).
 * FIX: 'ngayKy' và 'ngayHetHan' đã chuyển sang String để khớp với dữ liệu trong Firestore.
 * FIX: Đã thêm trường 'createdAt' để loại bỏ cảnh báo của Firestore.
 * FIX: Đã thêm trường 'trangThai' để lưu trạng thái Hợp đồng (Đang hiệu lực, Sắp hết hạn, Đã hết hạn)
 * được tính toán trên client.
 */
public class HopDong {

    @DocumentId
    private String documentId;

    private String maHopDong; // Mã Hợp Đồng
    private String tenNhaCungCap; // Tên Nhà Cung Cấp

    // Chuyển từ Date sang String để khớp với dữ liệu đã lưu
    private String ngayKy; // Ngày Ký
    private String ngayHetHan; // Ngày Hết Hạn

    private String noiDungDichVu; // Nội dung dịch vụ chính
    private String dieuKhoanThanhToan; // Điều khoản thanh toán
    private String dieuKhoanDichVuKhac; // Điều khoản dịch vụ khác

    // Trường này KHÔNG được lưu trong Firestore, nó dùng để lưu trạng thái tính toán
    // (ví dụ: "Đang hiệu lực", "Đã hết hạn") giữa Fragment và Adapter.
    @Exclude
    private String trangThai;

    // Thêm trường createdAt để loại bỏ cảnh báo (có thể là String, Date hoặc Object)
    private Object createdAt;

    // Constructor rỗng bắt buộc cho Firestore
    public HopDong() {}

    // --- Getters ---

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public String getMaHopDong() {
        return maHopDong;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public String getNgayKy() {
        return ngayKy;
    }

    public String getNgayHetHan() {
        return ngayHetHan;
    }

    public String getNoiDungDichVu() {
        return noiDungDichVu;
    }

    public String getDieuKhoanThanhToan() {
        return dieuKhoanThanhToan;
    }

    public String getDieuKhoanDichVuKhac() {
        return dieuKhoanDichVuKhac;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    // FIX: Getter trả về trạng thái tính toán (Bắt buộc cho Adapter)
    @Exclude
    public String getTrangThai() {
        return trangThai;
    }


    // --- Setters ---

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public void setNgayKy(String ngayKy) {
        this.ngayKy = ngayKy;
    }

    public void setNgayHetHan(String ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public void setNoiDungDichVu(String noiDungDichVu) {
        this.noiDungDichVu = noiDungDichVu;
    }

    public void setDieuKhoanThanhToan(String dieuKhoanThanhToan) {
        this.dieuKhoanThanhToan = dieuKhoanThanhToan;
    }

    public void setDieuKhoanDichVuKhac(String dieuKhoanDichVuKhac) {
        this.dieuKhoanDichVuKhac = dieuKhoanDichVuKhac;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    // FIX: Setter nhận trạng thái tính toán từ Fragment (Bắt buộc cho Fragment)
    @Exclude
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}