package com.example.quanlytourdl.model;

public class TourHistory {
    private String tenTour;
    private String ngayDat;
    private String trangThai; // HOÀN THÀNH, ĐÃ HỦY, CHỜ...
    private String hinhAnh;
    private String moTa;

    public TourHistory() {} // Bắt buộc cho Firebase

    public TourHistory(String tenTour, String ngayDat, String trangThai, String hinhAnh, String moTa) {
        this.tenTour = tenTour;
        this.ngayDat = ngayDat;
        this.trangThai = trangThai;
        this.hinhAnh = hinhAnh;
        this.moTa = moTa;
    }

    // Getter và Setter
    public String getTenTour() { return tenTour; }
    public String getNgayDat() { return ngayDat; }
    public String getTrangThai() { return trangThai; }
    public String getHinhAnh() { return hinhAnh; }
    public String getMoTa() { return moTa; }
}