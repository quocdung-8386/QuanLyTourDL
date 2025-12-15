package com.example.quanlytourdl;

public class BangLuong {
    private String salaryPeriod;
    private boolean isPaid;
    private String luongCoBan;
    private String tongPhuCap;
    private String thuongHoaHong;
    private String phatKhauTru;
    private String tongThuNhap;

    public BangLuong(String salaryPeriod, boolean isPaid, String luongCoBan, String tongPhuCap, String thuongHoaHong, String phatKhauTru, String tongThuNhap) {
        this.salaryPeriod = salaryPeriod;
        this.isPaid = isPaid;
        this.luongCoBan = luongCoBan;
        this.tongPhuCap = tongPhuCap;
        this.thuongHoaHong = thuongHoaHong;
        this.phatKhauTru = phatKhauTru;
        this.tongThuNhap = tongThuNhap;
    }

    public String getSalaryPeriod() {
        return salaryPeriod;
    }

    public void setSalaryPeriod(String salaryPeriod) {
        this.salaryPeriod = salaryPeriod;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public String getLuongCoBan() {
        return luongCoBan;
    }

    public void setLuongCoBan(String luongCoBan) {
        this.luongCoBan = luongCoBan;
    }

    public String getTongPhuCap() {
        return tongPhuCap;
    }

    public void setTongPhuCap(String tongPhuCap) {
        this.tongPhuCap = tongPhuCap;
    }

    public String getThuongHoaHong() {
        return thuongHoaHong;
    }

    public void setThuongHoaHong(String thuongHoaHong) {
        this.thuongHoaHong = thuongHoaHong;
    }

    public String getPhatKhauTru() {
        return phatKhauTru;
    }

    public void setPhatKhauTru(String phatKhauTru) {
        this.phatKhauTru = phatKhauTru;
    }

    public String getTongThuNhap() {
        return tongThuNhap;
    }

    public void setTongThuNhap(String tongThuNhap) {
        this.tongThuNhap = tongThuNhap;
    }
}