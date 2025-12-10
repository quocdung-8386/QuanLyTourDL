package com.example.quanlytourdl.model;

import java.io.Serializable;

public class KhachHang implements Serializable {
    private String id;
    private String ten;
    private String sdt;
    private String ngaySinh; // Mới thêm: Ngày sinh
    private int avatarResId;

    // Bắt buộc: Constructor rỗng cho Firebase
    public KhachHang() {
    }

    // Constructor đầy đủ
    public KhachHang(String id, String ten, String sdt, String ngaySinh, int avatarResId) {
        this.id = id;
        this.ten = ten;
        this.sdt = sdt;
        this.ngaySinh = ngaySinh;
        this.avatarResId = avatarResId;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public int getAvatarResId() { return avatarResId; }
    public void setAvatarResId(int avatarResId) { this.avatarResId = avatarResId; }
}