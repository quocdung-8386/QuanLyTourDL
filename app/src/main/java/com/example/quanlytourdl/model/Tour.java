package com.example.quanlytourdl.model;

import java.util.Date;

public class Tour {
    private String maTour;
    private String tenTour;
    private String diemKhoiHanh;
    private String diemDen;
    private Date ngayKhoiHanh;
    private int soLuongKhach;
    private long giaTour;

    // CÁC TRƯỜNG DỮ LIỆU MỚI ĐƯỢC THÊM VÀO:
    private String nguoiTao;
    private Date ngayTao;
    private String anhThumbnailUrl;

    // Constructor Hoàn Chỉnh (Chứa tất cả các trường cần thiết cho Adapter chi tiết)
    public Tour(String maTour, String tenTour, String diemKhoiHanh, String diemDen, Date ngayKhoiHanh,
                int soLuongKhach, long giaTour, String nguoiTao, Date ngayTao, String anhThumbnailUrl) {

        this.maTour = maTour;
        this.tenTour = tenTour;
        this.diemKhoiHanh = diemKhoiHanh;
        this.diemDen = diemDen;
        this.ngayKhoiHanh = ngayKhoiHanh;
        this.soLuongKhach = soLuongKhach;
        this.giaTour = giaTour;

        // Khởi tạo các trường mới
        this.nguoiTao = nguoiTao;
        this.ngayTao = ngayTao;
        this.anhThumbnailUrl = anhThumbnailUrl;
    }

    // Constructor đơn giản hơn (Tùy chọn)
    public Tour(String maTour, String tenTour, String nguoiTao, Date ngayTao, String anhThumbnailUrl) {
        this(maTour, tenTour, null, null, null, 0, 0, nguoiTao, ngayTao, anhThumbnailUrl);
    }

    // ------------------- GETTERS VÀ SETTERS CŨ -------------------

    public String getMaTour() {
        return maTour;
    }

    public void setMaTour(String maTour) {
        this.maTour = maTour;
    }

    public String getTenTour() {
        return tenTour;
    }

    public void setTenTour(String tenTour) {
        this.tenTour = tenTour;
    }

    public String getDiemKhoiHanh() {
        return diemKhoiHanh;
    }

    public void setDiemKhoiHanh(String diemKhoiHanh) {
        this.diemKhoiHanh = diemKhoiHanh;
    }

    public String getDiemDen() {
        return diemDen;
    }

    public void setDiemDen(String diemDen) {
        this.diemDen = diemDen;
    }

    public Date getNgayKhoiHanh() {
        return ngayKhoiHanh;
    }

    public void setNgayKhoiHanh(Date ngayKhoiHanh) {
        this.ngayKhoiHanh = ngayKhoiHanh;
    }

    public int getSoLuongKhach() {
        return soLuongKhach;
    }

    public void setSoLuongKhach(int soLuongKhach) {
        this.soLuongKhach = soLuongKhach;
    }

    public long getGiaTour() {
        return giaTour;
    }

    public void setGiaTour(long giaTour) {
        this.giaTour = giaTour;
    }

    // ------------------- GETTERS VÀ SETTERS MỚI -------------------

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getAnhThumbnailUrl() {
        return anhThumbnailUrl;
    }

    public void setAnhThumbnailUrl(String anhThumbnailUrl) {
        this.anhThumbnailUrl = anhThumbnailUrl;
    }
}