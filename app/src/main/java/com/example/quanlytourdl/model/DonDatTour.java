package com.example.quanlytourdl.model;

import com.google.firebase.firestore.DocumentId;
import java.util.Date;

public class DonDatTour {
    @DocumentId
    private String id; // ID đơn hàng

    private String maKhachHang; // Khóa ngoại: Link tới KhachHang
    private String maTour;      // Khóa ngoại: Link tới Tour

    // Thông tin hiển thị (Lưu lại để không cần query ngược bảng Tour nhiều lần)
    private String tenTourSnapshot; 
    private String maTourCode; // Ví dụ: T230815EU
    private String thoiGianKhoiHanh; // Ví dụ: "15/08/2023 - 25/08/2023"
    private String soLuongKhach; // Ví dụ: "2 người lớn"
    
    private String trangThai; // "Hoàn thành", "Sắp diễn ra", "Đã hủy"
    private Date ngayDat;

    public DonDatTour() { } // Constructor rỗng cho Firebase

    public DonDatTour(String maKhachHang, String maTour, String tenTourSnapshot, String maTourCode, String thoiGianKhoiHanh, String soLuongKhach, String trangThai, Date ngayDat) {
        this.maKhachHang = maKhachHang;
        this.maTour = maTour;
        this.tenTourSnapshot = tenTourSnapshot;
        this.maTourCode = maTourCode;
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
        this.soLuongKhach = soLuongKhach;
        this.trangThai = trangThai;
        this.ngayDat = ngayDat;
    }

    // Getters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMaKhachHang() { return maKhachHang; }
    public String getMaTour() { return maTour; }
    public String getTenTourSnapshot() { return tenTourSnapshot; }
    public String getMaTourCode() { return maTourCode; }
    public String getThoiGianKhoiHanh() { return thoiGianKhoiHanh; }
    public String getSoLuongKhach() { return soLuongKhach; }
    public String getTrangThai() { return trangThai; }
}