package com.example.quanlytourdl.model;

import com.google.firebase.firestore.PropertyName;

public class ChiTietItem {

    // Ánh xạ field "tenTour" trên Firebase vào biến "tenDichVu"
    @PropertyName("tenTour")
    private String tenDichVu;

    private String moTa;

    // Lưu ý: Firebase có thể trả về String hoặc Long, ta sẽ xử lý lúc get
    private Object soLuong;

    private double giaTien;

    // Field dùng để liên kết (có thể có hoặc không trên Firebase, nhưng cần để query)
    private String maDonHang;

    // Constructor rỗng (BẮT BUỘC)
    public ChiTietItem() { }

    public ChiTietItem(String tenDichVu, String moTa, Object soLuong, double giaTien, String maDonHang) {
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.soLuong = soLuong;
        this.giaTien = giaTien;
        this.maDonHang = maDonHang;
    }

    // --- GETTER & SETTER (QUAN TRỌNG: PHẢI CÓ ĐỦ) ---

    @PropertyName("tenTour")
    public String getTenDichVu() { return tenDichVu; }

    @PropertyName("tenTour")
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    // Xử lý thông minh: Dù Firebase lưu số hay chữ đều lấy được ra chuỗi
    public String getSoLuong() {
        if (soLuong == null) return "0";
        return String.valueOf(soLuong);
    }

    // Hàm phụ để lấy số lượng dạng int cho tính toán
    public int getSoLuongAsInt() {
        try {
            if (soLuong == null) return 0;
            if (soLuong instanceof Long) return ((Long) soLuong).intValue();
            if (soLuong instanceof String) return Integer.parseInt((String) soLuong);
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public void setSoLuong(Object soLuong) { this.soLuong = soLuong; }

    public double getGiaTien() { return giaTien; }
    public void setGiaTien(double giaTien) { this.giaTien = giaTien; }

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
}