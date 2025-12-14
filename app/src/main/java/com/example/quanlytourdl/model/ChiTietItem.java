package com.example.quanlytourdl.model;

public class ChiTietItem {
    private String tenDichVu;
    private String moTa;     // Ví dụ: Ngày khởi hành hoặc loại vé
    private String soLuong;  // Ví dụ: x2 Người lớn
    private double giaTien;

    public ChiTietItem() { } // Constructor rỗng cho Firebase

    public ChiTietItem(String tenDichVu, String moTa, String soLuong, double giaTien) {
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.soLuong = soLuong;
        this.giaTien = giaTien;
    }

    // Getters
    public String getTenDichVu() { return tenDichVu; }
    public String getMoTa() { return moTa; }
    public String getSoLuong() { return soLuong; }
    public double getGiaTien() { return giaTien; }
}