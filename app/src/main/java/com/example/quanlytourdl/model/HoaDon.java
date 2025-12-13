package com.example.quanlytourdl.model;

public class HoaDon {
    private String maHoaDon;
    private String ngayTao;
    private String tenKhachHang;
    private double tongTien;
    private int trangThai; // 1: Đã thanh toán, 2: Chờ thanh toán, 3: Quá hạn

    public HoaDon(String maHoaDon, String ngayTao, String tenKhachHang, double tongTien, int trangThai) {
        this.maHoaDon = maHoaDon;
        this.ngayTao = ngayTao;
        this.tenKhachHang = tenKhachHang;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
    }

    // Getters
    public String getMaHoaDon() { return maHoaDon; }
    public String getNgayTao() { return ngayTao; }
    public String getTenKhachHang() { return tenKhachHang; }
    public double getTongTien() { return tongTien; }
    public int getTrangThai() { return trangThai; }
}