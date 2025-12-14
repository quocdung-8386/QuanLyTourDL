package com.example.quanlytourdl.model;

import java.io.Serializable;

public class HoaDon implements Serializable {
    private String maHoaDon; // ID document Firestore
    private String ngayTao;
    private String tenKhachHang;
    private double tongTien;
    private int trangThai; // 1: Đã thanh toán, 2: Chờ thanh toán, 3: Quá hạn
    private String tenTour;
    private String sdt;
    private String diaChi;

    // 1. Constructor rỗng (BẮT BUỘC CHO FIRESTORE)
    public HoaDon() {
    }

    public HoaDon(String maHoaDon, String ngayTao, String tenKhachHang, double tongTien, int trangThai, String tenTour) {
        this.maHoaDon = maHoaDon;
        this.ngayTao = ngayTao;
        this.tenKhachHang = tenKhachHang;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.tenTour = tenTour;
    }

    // 2. Thêm đầy đủ Setters (BẮT BUỘC)
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }
    public void setTenTour(String tenTour) { this.tenTour = tenTour; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    // Getters giữ nguyên
    public String getSdt() { return sdt; }
    public String getDiaChi() { return diaChi; }
    public String getMaHoaDon() { return maHoaDon; }
    public String getNgayTao() { return ngayTao; }
    public String getTenKhachHang() { return tenKhachHang; }
    public double getTongTien() { return tongTien; }
    public int getTrangThai() { return trangThai; }
    public String getTenTour() { return tenTour; }
}