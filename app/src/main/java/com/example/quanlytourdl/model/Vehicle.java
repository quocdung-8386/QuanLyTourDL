package com.example.quanlytourdl.model;

import java.io.Serializable; // ⭐ IMPORT MỚI

// ⭐ Vehicle implements Serializable
public class Vehicle implements Serializable {

    // ⭐ Thêm serialVersionUID
    private static final long serialVersionUID = 1L;

    private String id;
    private String bienSoXe;
    private String loaiPhuongTien;
    private String hangXe;
    private int soChoNgoi;
    private String tinhTrangBaoDuong;

    // --- CÁC TRƯỜNG BỔ SUNG CHO PHÂN CÔNG ---
    private String driverId;    // ID của tài xế được gán
    private String driverName;  // Tên tài xế được gán (để hiển thị nhanh)

    // Thuộc tính Transient (Chỉ dùng trong UI/Adapter)
    private boolean isAvailable = false; // Trạng thái sẵn sàng (Trống/Vướng lịch)
    private boolean isSelected = false;  // Trạng thái được chọn trong RecyclerView


    public Vehicle() {}

    // --- GETTERS & SETTERS CŨ ---
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


    // --- GETTERS & SETTERS MỚI ---
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    // --- GETTERS & SETTERS CHO UI/ADAPTER ---
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }


    @Override
    public String toString() {
        return "PT: " + hangXe + " (" + bienSoXe + ") - Tình trạng: " + tinhTrangBaoDuong;
    }
}