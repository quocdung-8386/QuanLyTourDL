package com.example.quanlytourdl.model;

public class KhachHang {
    private String ten;
    private String sdt;
    private int avatarResId; // Dùng resource ID cho ảnh demo

    public KhachHang(String ten, String sdt, int avatarResId) {
        this.ten = ten;
        this.sdt = sdt;
        this.avatarResId = avatarResId;
    }

    public String getTen() { return ten; }
    public String getSdt() { return sdt; }
    public int getAvatarResId() { return avatarResId; }
}