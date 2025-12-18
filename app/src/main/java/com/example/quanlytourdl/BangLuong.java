package com.example.quanlytourdl;

import java.io.Serializable;

public class BangLuong implements Serializable {
    private String id;              // ID Document từ Firestore
    private String salaryPeriod;    // Kỳ lương
    private boolean paid;           // ĐỔI TÊN BIẾN: isPaid -> paid (để khớp với Firestore)
    private double luongCoBan;
    private double tongPhuCap;
    private double thuongHoaHong;
    private double phatKhauTru;

    public BangLuong() {
        // Constructor rỗng bắt buộc cho Firebase
    }

    public BangLuong(String id, String salaryPeriod, boolean paid, double luongCoBan, double tongPhuCap, double thuongHoaHong, double phatKhauTru) {
        this.id = id;
        this.salaryPeriod = salaryPeriod;
        this.paid = paid; // Lưu ý chỗ này
        this.luongCoBan = luongCoBan;
        this.tongPhuCap = tongPhuCap;
        this.thuongHoaHong = thuongHoaHong;
        this.phatKhauTru = phatKhauTru;
    }

    // Hàm tự động tính Tổng thu nhập
    public double getTongThuNhap() {
        return luongCoBan + tongPhuCap + thuongHoaHong - phatKhauTru;
    }

    // --- Getters và Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSalaryPeriod() { return salaryPeriod; }
    public void setSalaryPeriod(String salaryPeriod) { this.salaryPeriod = salaryPeriod; }

    // Getter cho boolean thường đặt là isPaid(), nhưng biến nội bộ là 'paid'
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public double getLuongCoBan() { return luongCoBan; }
    public void setLuongCoBan(double luongCoBan) { this.luongCoBan = luongCoBan; }

    public double getTongPhuCap() { return tongPhuCap; }
    public void setTongPhuCap(double tongPhuCap) { this.tongPhuCap = tongPhuCap; }

    public double getThuongHoaHong() { return thuongHoaHong; }
    public void setThuongHoaHong(double thuongHoaHong) { this.thuongHoaHong = thuongHoaHong; }

    public double getPhatKhauTru() { return phatKhauTru; }
    public void setPhatKhauTru(double phatKhauTru) { this.phatKhauTru = phatKhauTru; }
}