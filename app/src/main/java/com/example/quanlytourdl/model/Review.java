package com.example.quanlytourdl.model;

import java.util.Date;

/**
 * Lớp mô hình Review (Đánh giá Tour).
 */
public class Review {
    // ID của document Review
    private String maDanhGia;

    // ID của Tour mà đánh giá này thuộc về
    private String maTour;

    // ID của Khách hàng/Người dùng đã đánh giá
    private String maKhachHang;

    // Tên người đánh giá (hoặc tên hiển thị)
    private String tenNguoiDanhGia;

    // Điểm đánh giá (thường là 1.0 đến 5.0)
    private float rating;

    // Nội dung chi tiết của đánh giá
    private String comment;

    // Ngày đánh giá
    private Date ngayDanhGia;

    // Ảnh đại diện của người đánh giá (URL từ Firebase Storage)
    private String avatarUrl;

    // Các trường khác có thể có: list ảnh đính kèm, phản hồi từ Admin/Tour, v.v.

    // Constructor rỗng (BẮT BUỘC cho Firestore)
    public Review() {}

    // Constructor đầy đủ tham số (Tùy chọn)
    public Review(String maDanhGia, String maTour, String maKhachHang, String tenNguoiDanhGia, float rating, String comment, Date ngayDanhGia, String avatarUrl) {
        this.maDanhGia = maDanhGia;
        this.maTour = maTour;
        this.maKhachHang = maKhachHang;
        this.tenNguoiDanhGia = tenNguoiDanhGia;
        this.rating = rating;
        this.comment = comment;
        this.ngayDanhGia = ngayDanhGia;
        this.avatarUrl = avatarUrl;
    }

    // --- Getters và Setters (BẮT BUỘC cho Firestore) ---

    public String getMaDanhGia() { return maDanhGia; }
    public void setMaDanhGia(String maDanhGia) { this.maDanhGia = maDanhGia; }

    public String getMaTour() { return maTour; }
    public void setMaTour(String maTour) { this.maTour = maTour; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenNguoiDanhGia() { return tenNguoiDanhGia; }
    public void setTenNguoiDanhGia(String tenNguoiDanhGia) { this.tenNguoiDanhGia = tenNguoiDanhGia; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getNgayDanhGia() { return ngayDanhGia; }
    public void setNgayDanhGia(Date ngayDanhGia) { this.ngayDanhGia = ngayDanhGia; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}