package com.example.quanlytourdl.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.Date;

/**
 * Lớp Model đại diện cho một Hợp đồng (HopDong)
 */
public class HopDong {
    @Exclude
    private String maHopDong; // ID của document trong Firestore
    private String maNhaCungCap;
    private String tenNhaCungCap; // Tên hiển thị (để tránh phải query NCC)
    private Date ngayKy;
    private Date ngayHetHan;
    private String trangThai; // Ví dụ: "Đang hiệu lực", "Sắp hết hạn", "Đã hết hạn"

    public HopDong() {
        // Cần có constructor rỗng cho Firestore
    }

    // Constructor đầy đủ (tùy chọn)
    public HopDong(String maNhaCungCap, String tenNhaCungCap, Date ngayKy, Date ngayHetHan, String trangThai) {
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.ngayKy = ngayKy;
        this.ngayHetHan = ngayHetHan;
        this.trangThai = trangThai;
    }

    // Getters and Setters
    @Exclude
    public String getMaHopDong() {
        return maHopDong;
    }

    @Exclude
    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
    }

    @PropertyName("maNhaCungCap")
    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    @PropertyName("maNhaCungCap")
    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    @PropertyName("tenNhaCungCap")
    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    @PropertyName("tenNhaCungCap")
    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    @PropertyName("ngayKy")
    public Date getNgayKy() {
        return ngayKy;
    }

    @PropertyName("ngayKy")
    public void setNgayKy(Date ngayKy) {
        this.ngayKy = ngayKy;
    }

    @PropertyName("ngayHetHan")
    public Date getNgayHetHan() {
        return ngayHetHan;
    }

    @PropertyName("ngayHetHan")
    public void setNgayHetHan(Date ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    @PropertyName("trangThai")
    public String getTrangThai() {
        return trangThai;
    }

    @PropertyName("trangThai")
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}