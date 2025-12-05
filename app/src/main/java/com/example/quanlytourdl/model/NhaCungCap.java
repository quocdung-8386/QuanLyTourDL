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
    // Thêm trường này để dễ dàng quản lý nếu sau này muốn thêm bảo mật
    private String maNguoiDungTao;

    // Constructor mặc định (bắt buộc cho Firebase Firestore/Realtime Database)
    public NhaCungCap() {
    }

    // Constructor đầy đủ
    public NhaCungCap(String tenNhaCungCap, String diaChi, String soDienThoai, String email, String nguoiLienHe, String loaiDichVu, String maNguoiDungTao) {
        this.tenNhaCungCap = tenNhaCungCap;
        this.diaChi = diaChi;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.nguoiLienHe = nguoiLienHe;
        this.loaiDichVu = loaiDichVu;
        this.maNguoiDungTao = maNguoiDungTao;
    }

    // --- Getters and Setters ---

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