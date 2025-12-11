package com.example.quanlytourdl.model;

import java.io.Serializable;

public class KhachHang implements Serializable {
    private String id;
    private String ten;
    private String sdt;
    private String ngaySinh;
    private String email;
    // Các trường mới bổ sung
    private String diaChi;
    private String gioiTinh;
    private String cccd;
    private String ghiChu;
    private String quocTich;

    private int avatarResId; // Tạm thời dùng resource ID

    // Constructor rỗng (Bắt buộc cho Firestore)
    public KhachHang() {
    }

    // Constructor đầy đủ
    public KhachHang(String id, String ten, String sdt, String ngaySinh, String email,
                     String diaChi, String gioiTinh, String cccd, String ghiChu, String quocTich, int avatarResId) {
        this.id = id;
        this.ten = ten;
        this.sdt = sdt;
        this.ngaySinh = ngaySinh;
        this.email = email;
        this.diaChi = diaChi;
        this.gioiTinh = gioiTinh;
        this.cccd = cccd;
        this.ghiChu = ghiChu;
        this.quocTich = quocTich;
        this.avatarResId = avatarResId;
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getQuocTich() { return quocTich; }
    public void setQuocTich(String quocTich) { this.quocTich = quocTich; }

    public int getAvatarResId() { return avatarResId; }
    public void setAvatarResId(int avatarResId) { this.avatarResId = avatarResId; }
}