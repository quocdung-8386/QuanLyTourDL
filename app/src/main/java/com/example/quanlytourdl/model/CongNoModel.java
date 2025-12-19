package com.example.quanlytourdl.model;

import java.io.Serializable;

public class CongNoModel implements Serializable {
    private String id;          // ID Document của Firestore
    private String tenNcc;
    private String maHopDong;
    private String noiDung;
    private String ngayHan;
    private double soTien;
    private String trangThai;   // "Quá hạn" hoặc "Chờ thanh toán"
    private String loaiDichVu;  // "Thuê xe", "Khách sạn", v.v.
    private String fileUrl;     // Đường dẫn tải file từ Firebase Storage
    private String fileName;    // Tên file gốc để hiển thị (VD: hopdong.pdf)

    // Constructor trống bắt buộc cho Firebase
    public CongNoModel() {
    }

    public CongNoModel(String tenNcc, String maHopDong, String noiDung, String ngayHan, double soTien, String trangThai, String loaiDichVu) {
        this.tenNcc = tenNcc;
        this.maHopDong = maHopDong;
        this.noiDung = noiDung;
        this.ngayHan = ngayHan;
        this.soTien = soTien;
        this.trangThai = trangThai;
        this.loaiDichVu = loaiDichVu;
    }

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenNcc() { return tenNcc; }
    public void setTenNcc(String tenNcc) { this.tenNcc = tenNcc; }

    public String getMaHopDong() { return maHopDong; }
    public void setMaHopDong(String maHopDong) { this.maHopDong = maHopDong; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getNgayHan() { return ngayHan; }
    public void setNgayHan(String ngayHan) { this.ngayHan = ngayHan; }

    public double getSoTien() { return soTien; }
    public void setSoTien(double soTien) { this.soTien = soTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getLoaiDichVu() { return loaiDichVu; }
    public void setLoaiDichVu(String loaiDichVu) { this.loaiDichVu = loaiDichVu; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}