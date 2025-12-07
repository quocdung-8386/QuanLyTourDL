package com.example.quanlytourdl.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class NhaCungCap implements Serializable {
    @Exclude
    private String maNhaCungCap;

    private String tenNhaCungCap;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private String nguoiLienHe;
    private String loaiDichVu;
    // ID của hợp đồng đang hoạt động (active contract ID)
    private String maHopDong;
    // Thêm trường này để dễ dàng quản lý nếu sau này muốn thêm bảo mật
    private String maNguoiDungTao;

    // 🌟 THÊM MỚI: Trạng thái hợp đồng (để fix cảnh báo Firestore)
    private String trangThaiHopDong;

    // 🌟 THÊM MỚI: Mã hợp đồng gần nhất (để fix cảnh báo Firestore)
    private String maHopDongGanNhat;


    // Constructor mặc định (bắt buộc cho Firebase Firestore/Realtime Database)
    public NhaCungCap() {
    }

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
        this.trangThaiHopDong = trangThaiHopDong; // Khởi tạo trường mới
        this.maHopDongGanNhat = maHopDongGanNhat; // Khởi tạo trường mới
    }

    public NhaCungCap(String ten, String diaChi, String sdt, String email, String nguoiLH, String loaiDV, String maHopDongActive, String maNguoiDungTao) {
    }

    // --- Getters and Setters ---

    // Getter và Setter cho trường mới trangThaiHopDong (FIX)
    public String getTrangThaiHopDong() {
        return trangThaiHopDong;
    }

    public void setTrangThaiHopDong(String trangThaiHopDong) {
        this.trangThaiHopDong = trangThaiHopDong;
    }

    // Getter và Setter cho trường mới maHopDongGanNhat (FIX)
    public String getMaHopDongGanNhat() {
        return maHopDongGanNhat;
    }

    public void setMaHopDongGanNhat(String maHopDongGanNhat) {
        this.maHopDongGanNhat = maHopDongGanNhat;
    }

    // Các Getters/Setters cũ vẫn giữ nguyên...

    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDongActive) {
        this.maHopDong = maHopDongActive;
    }

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