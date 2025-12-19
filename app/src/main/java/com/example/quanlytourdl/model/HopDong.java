package com.example.quanlytourdl.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Model class đại diện cho Hợp Đồng.
 * Đã sửa lỗi Mapping Timestamp để tránh crash AndroidRuntime.
 */
public class HopDong implements Serializable {

    private String maHopDong;

    @Exclude
    private String documentId;

    private String nhaCungCap;
    private String ngayKyKet;
    private String ngayHetHan;
    private String trangThai;
    private String noiDung;
    private String dieuKhoanThanhToan;

    // ⭐ Đã sửa: Chuyển từ String sang Timestamp để khớp với dữ liệu Firestore
    private Timestamp ngayCapNhat;

    private String supplierId;
    private String ghiChuChamDut;
    private String lyDoChamDut;

    public HopDong() { }

    // --- Getters và Setters ---

    public String getMaHopDong() { return maHopDong; }
    public void setMaHopDong(String maHopDong) { this.maHopDong = maHopDong; }

    @Exclude
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    @PropertyName("tenNhaCungCap")
    public String getNhaCungCap() { return nhaCungCap; }
    @PropertyName("tenNhaCungCap")
    public void setNhaCungCap(String nhaCungCap) { this.nhaCungCap = nhaCungCap; }

    @PropertyName("ngayKy")
    public String getNgayKyKet() { return ngayKyKet; }
    @PropertyName("ngayKy")
    public void setNgayKyKet(String ngayKyKet) { this.ngayKyKet = ngayKyKet; }

    public String getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(String ngayHetHan) { this.ngayHetHan = ngayHetHan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getDieuKhoanThanhToan() { return dieuKhoanThanhToan; }
    public void setDieuKhoanThanhToan(String dieuKhoanThanhToan) { this.dieuKhoanThanhToan = dieuKhoanThanhToan; }

    // ⭐ Getter/Setter mới sử dụng kiểu Timestamp
    public Timestamp getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Timestamp ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    /**
     * Helper method để lấy chuỗi ngày tháng định dạng dd/MM/yyyy HH:mm từ Timestamp
     */
    @Exclude
    public String getNgayCapNhatFormatted() {
        if (ngayCapNhat == null) return "Chưa cập nhật";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(ngayCapNhat.toDate());
    }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getGhiChuChamDut() { return ghiChuChamDut; }
    public void setGhiChuChamDut(String ghiChuChamDut) { this.ghiChuChamDut = ghiChuChamDut; }

    public String getLyDoChamDut() { return lyDoChamDut; }
    public void setLyDoChamDut(String lyDoChamDut) { this.lyDoChamDut = lyDoChamDut; }
}