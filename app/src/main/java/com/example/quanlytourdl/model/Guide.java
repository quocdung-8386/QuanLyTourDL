package com.example.quanlytourdl.model;

/**
 * Model class đại diện cho đối tượng Hướng dẫn viên trong Firestore.
 * Collection: huongdanvien
 */
public class Guide {
    private String id;
    private String ten;
    private String sdt;
    private String email;
    private String trangThai; // "Sẵn sàng", "Đang đi tour", "Tạm nghỉ"

    // Constructor rỗng cần thiết cho Firestore
    public Guide() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "HDV: " + ten + " - Trạng thái: " + trangThai;
    }
}