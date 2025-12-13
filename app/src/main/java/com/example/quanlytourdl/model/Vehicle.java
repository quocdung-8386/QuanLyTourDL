package com.example.quanlytourdl.model;
public class Vehicle {
    private String id;
    private String bienSoXe;
    private String loaiPhuongTien;
    private String hangXe;
    private int soChoNgoi;
    private String tinhTrangBaoDuong;
    public Vehicle() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBienSoXe() { return bienSoXe; }
    public void setBienSoXe(String bienSoXe) { this.bienSoXe = bienSoXe; }

    public String getLoaiPhuongTien() { return loaiPhuongTien; }
    public void setLoaiPhuongTien(String loaiPhuongTien) { this.loaiPhuongTien = loaiPhuongTien; }

    public String getHangXe() { return hangXe; }
    public void setHangXe(String hangXe) { this.hangXe = hangXe; }

    public int getSoChoNgoi() { return soChoNgoi; }
    public void setSoChoNgoi(int soChoNgoi) { this.soChoNgoi = soChoNgoi; }

    public String getTinhTrangBaoDuong() { return tinhTrangBaoDuong; }
    public void setTinhTrangBaoDuong(String tinhTrangBaoDuong) { this.tinhTrangBaoDuong = tinhTrangBaoDuong; }

    @Override
    public String toString() {
        return "PT: " + hangXe + " (" + bienSoXe + ") - Tình trạng: " + tinhTrangBaoDuong;
    }
}