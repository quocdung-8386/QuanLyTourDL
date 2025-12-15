package com.example.quanlytourdl.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

/**
 * Model class đại diện cho Hợp Đồng.
 * Đã điều chỉnh để lưu trữ:
 * 1. Mã Hợp đồng Tự tạo (maHopDong) - Được ánh xạ từ field trong Firestore.
 * 2. ID Document Firebase (documentId) - Được @Exclude vì nó là ID, không phải field.
 */
public class HopDong  implements Serializable {

    // 1. Mã Hợp đồng tự tạo (Ví dụ: HD-2025-A1B2C3).
    // Trường này được ánh xạ tự động từ field "maHopDong" trong Firestore.
    private String maHopDong;

    // 2. ID Document Firebase (Chuỗi ngẫu nhiên). Dùng cho các thao tác DB như Delete/Update.
    // Dùng @Exclude vì đây là ID của document, không phải là một field bên trong document.
    private String documentId;

    // --- CÁC TRƯỜNG CẦN ÁNH XẠ TỪ FIRESTORE ---

    // FIRESTORE KEY: "tenNhaCungCap" -> JAVA FIELD: nhaCungCap
    private String nhaCungCap;

    // FIRESTORE KEY: "ngayKy" -> JAVA FIELD: ngayKyKet
    private String ngayKyKet;

    private String ngayHetHan;
    private String trangThai;
    private String noiDung;
    private String dieuKhoanThanhToan;
    private String ngayCapNhat;
    private String supplierId;
    private String ghiChuChamDut;
    private String lyDoChamDut;


    // Constructor rỗng bắt buộc cho Firestore's toObject()
    public HopDong() { }

    // ---------------------------------------------------------------------
    // Getters và Setters
    // ---------------------------------------------------------------------

    // 1. Getters/Setters cho MÃ HỢP ĐỒNG TỰ TẠO (Field trong Firestore)
    // Bỏ @Exclude ở đây.
    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
    }

    // 2. Getters/Setters cho ID DOCUMENT FIRESTORE (ID dùng để thao tác DB)
    // Thêm @Exclude cho Document ID để nó không bị ghi vào Firestore
    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }


    // Ánh xạ Firestore key "tenNhaCungCap" vào trường nhaCungCap
    @PropertyName("tenNhaCungCap")
    public String getNhaCungCap() {
        return nhaCungCap;
    }

    @PropertyName("tenNhaCungCap")
    public void setNhaCungCap(String nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }

    // Ánh xạ Firestore key "ngayKy" vào trường ngayKyKet
    @PropertyName("ngayKy")
    public String getNgayKyKet() {
        return ngayKyKet;
    }

    @PropertyName("ngayKy")
    public void setNgayKyKet(String ngayKyKet) {
        this.ngayKyKet = ngayKyKet;
    }

    public String getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(String ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getDieuKhoanThanhToan() {
        return dieuKhoanThanhToan;
    }

    public void setDieuKhoanThanhToan(String dieuKhoanThanhToan) {
        this.dieuKhoanThanhToan = dieuKhoanThanhToan;
    }

    public String getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(String ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getGhiChuChamDut() {
        return ghiChuChamDut;
    }

    public void setGhiChuChamDut(String ghiChuChamDut) {
        this.ghiChuChamDut = ghiChuChamDut;
    }

    public String getLyDoChamDut() {
        return lyDoChamDut;
    }

    public void setLyDoChamDut(String lyDoChamDut) {
        this.lyDoChamDut = lyDoChamDut;
    }
}