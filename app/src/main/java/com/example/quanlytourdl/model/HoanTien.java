package com.example.quanlytourdl.model;

public class HoanTien {
    private String id;
    private String tenKhach;
    private String maDon;
    private String soTien;
    private String tenTour;
    private String ngayYeuCau;
    private String trangThai; // "cho_xu_ly", "da_hoan_tien", "da_tu_choi"

    public HoanTien() { } // Constructor rỗng bắt buộc cho Firebase

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenKhach() { return tenKhach; }
    public String getMaDon() { return maDon; }
    public String getSoTien() { return soTien; }
    public String getTenTour() { return tenTour; }
    public String getNgayYeuCau() { return ngayYeuCau; }
    public String getTrangThai() { return trangThai; }
}