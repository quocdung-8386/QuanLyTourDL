package com.example.quanlytourdl.model;

import java.io.Serializable;

public class KhachHang implements Serializable {
    private String id;
    private String ten;
    private String sdt;
    private String ngaySinh;
    private String email; // <--- Đã thêm trường Email
    private int avatarResId;

    // Bắt buộc: Constructor rỗng cho Firebase
    public KhachHang() {
    }

    // Constructor đầy đủ (Đã cập nhật thêm tham số email)
    public KhachHang(String id, String ten, String sdt, String ngaySinh, String email, int avatarResId) {
        this.id = id;
        this.ten = ten;
        this.sdt = sdt;
        this.ngaySinh = ngaySinh;
        this.email = email; // <--- Gán giá trị email
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

    // Getter & Setter cho Email (Mới)
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAvatarResId() { return avatarResId; }
    public void setAvatarResId(int avatarResId) { this.avatarResId = avatarResId; }
}