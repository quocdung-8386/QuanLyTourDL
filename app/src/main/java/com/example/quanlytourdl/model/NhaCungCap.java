package com.example.quanlytourdl.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class NhaCungCap implements Serializable {
    // Sử dụng @Exclude để Firebase không lưu trường ID này bên trong document
    // và chúng ta sẽ lấy ID từ key của document
    @Exclude
    private String maNhaCungCap;

    private String tenNhaCungCap;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private String nguoiLienHe;
    private String loaiDichVu;
    // THÊM: ID của hợp đồng đang hoạt động (active contract ID)
    private String maHopDong;
    // Thêm trường này để dễ dàng quản lý nếu sau này muốn thêm bảo mật
    private String maNguoiDungTao;

    // Constructor mặc định (bắt buộc cho Firebase Firestore/Realtime Database)
    public NhaCungCap() {
    }

    public NhaCungCap(String tenNhaCungCap, String diaChi, String soDienThoai, String email,
                      String nguoiLienHe, String loaiDichVu, String maHopDong, String maNguoiDungTao) {
        this.tenNhaCungCap = tenNhaCungCap;
        this.diaChi = diaChi;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.nguoiLienHe = nguoiLienHe;
        this.loaiDichVu = loaiDichVu;
        this.maHopDong = maHopDong; // Khởi tạo trường mới
        this.maNguoiDungTao = maNguoiDungTao;
    }

    // --- Getters and Setters ---

    // Getter và Setter cho trường mới
    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDongActive) {
        this.maHopDong = maHopDongActive;
    }

    // Các Getters/Setters cũ vẫn giữ nguyên...

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNguoiLienHe() {
        return nguoiLienHe;
    }

    public void setNguoiLienHe(String nguoiLienHe) {
        this.nguoiLienHe = nguoiLienHe;
    }

    public String getLoaiDichVu() {
        return loaiDichVu;
    }

    public void setLoaiDichVu(String loaiDichVu) {
        this.loaiDichVu = loaiDichVu;
    }

    public String getMaNguoiDungTao() {
        return maNguoiDungTao;
    }

    public void setMaNguoiDungTao(String maNguoiDungTao) {
        this.maNguoiDungTao = maNguoiDungTao;
    }
}