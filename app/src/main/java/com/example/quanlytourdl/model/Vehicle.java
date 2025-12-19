package com.example.quanlytourdl.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

/**
 * Model class đại diện cho Phương tiện (Xe).
 * Được tối ưu để hiển thị chi tiết và phân công tài xế.
 */
public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String bienSoXe;
    private String loaiPhuongTien;
    private String hangXe;
    private int soChoNgoi;
    private String tinhTrangBaoDuong;

    // --- TRƯỜNG DỮ LIỆU TÀI XẾ ---
    private String driverId;
    private String driverName;  // Tên tài xế
    private String driverPhone; // Số điện thoại tài xế (Bổ sung để hiển thị chi tiết)

    // Thuộc tính Transient (Không lưu lên Firestore nếu cần)
    @Exclude
    private boolean isAvailable = false;
    @Exclude
    private boolean isSelected = false;


    public Vehicle() {}

    // --- GETTERS & SETTERS CƠ BẢN ---
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

    // --- GETTERS & SETTERS TÀI XẾ (Đồng bộ với DetailFragment) ---

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    // Hàm này giúp DetailFragment gọi hopDong.getTenTaiXe()
    public String getTenTaiXe() { return driverName; }
    public void setTenTaiXe(String driverName) { this.driverName = driverName; }

    // Hàm này giúp DetailFragment gọi hopDong.getSoDienThoaiTaiXe()
    public String getSoDienThoaiTaiXe() { return driverPhone; }
    public void setSoDienThoaiTaiXe(String driverPhone) { this.driverPhone = driverPhone; }

    // Duy trì các getter cũ nếu bạn đang dùng ở các màn hình khác
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    // --- UI STATES ---
    @Exclude
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Exclude
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }

    @Override
    public String toString() {
        return hangXe + " [" + bienSoXe + "]";
    }
}