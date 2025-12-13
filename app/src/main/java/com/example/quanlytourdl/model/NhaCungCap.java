package com.example.quanlytourdl.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class NhaCungCap implements Serializable {

    // Thuộc tính này được @Exclude để không lưu vào Firestore,
    // nhưng được dùng để lưu trữ Document ID khi đọc từ Firestore.
    @Exclude
    private String maNhaCungCap;

    private String tenNhaCungCap;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private String nguoiLienHe;
    private String loaiDichVu;
    private String maHopDong; // ID của hợp đồng đang hoạt động (maHopDongActive)
    private String maNguoiDungTao;
    private String trangThaiHopDong;
    private String maHopDongGanNhat;


    // Constructor mặc định (BẮT BUỘC cho Firebase)
    public NhaCungCap() {
    }

    /**
     * Constructor đầy đủ cho NhaCungCap
     * Lưu ý: maNhaCungCap KHÔNG được truyền vào đây vì nó được quản lý bởi Firestore
     */
    public NhaCungCap(String tenNhaCungCap, String diaChi, String soDienThoai, String email,
                      String nguoiLienHe, String loaiDichVu, String maHopDong, String maNguoiDungTao,
                      String trangThaiHopDong, String maHopDongGanNhat) {
        this.tenNhaCungCap = tenNhaCungCap;
        this.diaChi = diaChi;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.nguoiLienHe = nguoiLienHe;
        this.loaiDichVu = loaiDichVu;
        this.maHopDong = maHopDong;
        this.maNguoiDungTao = maNguoiDungTao;
        this.trangThaiHopDong = trangThaiHopDong;
        this.maHopDongGanNhat = maHopDongGanNhat;
    }

    // --- Getters và Setters ---

    @Exclude
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

    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
    }

    public String getMaNguoiDungTao() {
        return maNguoiDungTao;
    }

    public void setMaNguoiDungTao(String maNguoiDungTao) {
        this.maNguoiDungTao = maNguoiDungTao;
    }

    public String getTrangThaiHopDong() {
        return trangThaiHopDong;
    }

    public void setTrangThaiHopDong(String trangThaiHopDong) {
        this.trangThaiHopDong = trangThaiHopDong;
    }

    public String getMaHopDongGanNhat() {
        return maHopDongGanNhat;
    }

    public void setMaHopDongGanNhat(String maHopDongGanNhat) {
        this.maHopDongGanNhat = maHopDongGanNhat;
    }
}