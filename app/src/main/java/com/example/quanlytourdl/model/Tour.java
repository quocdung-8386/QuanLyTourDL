package com.example.quanlytourdl.model;

import java.util.Date;
import java.util.List;

/**
 * Lớp mô hình Tour du lịch.
 */
public class Tour {
    private String maTour;
    private String tenTour;
    private String loaiTour;
    private String diemKhoiHanh;
    private String diemDen;
    private Date ngayKhoiHanh;
    private Date ngayKetThuc;
    private int soNgay;
    private int soDem;
    private String moTa;

    private int soLuongKhachToiDa;
    // ⭐ ĐÃ THÊM: Số lượng khách thực tế đã đặt (cần cho hiển thị 30/35)
    private int soLuongKhachHienTai;

    // THÔNG TIN ĐỊNH GIÁ & CHI PHÍ
    private long giaNguoiLon;
    private long giaTreEm;
    // ... (các trường khác giữ nguyên)
    private long giaEmBe;
    private double giaNuocNgoai;

    private long tongGiaVon;
    private long giaVonPerPax;
    private double tySuatLoiNhuan;

    // DỊCH VỤ BAO GỒM / KHÔNG BAO GỒM
    private String dichVuBaoGom;
    private String dichVuKhongBaoGom;

    private String lichTrinhChiTiet;

    // HÌNH ẢNH & SEO
    private String hinhAnhChinhUrl;
    private List<String> danhSachHinhAnh;
    private String moTaSeo;
    private boolean isXuatBan;
    private boolean isNoiBat;

    // THÔNG TIN HỆ THỐNG
    private String nguoiTao;
    private Date ngayTao;
    private String status; // Ví dụ: CHO_PHE_DUYET, DANG_MO_BAN, NHAP

    // Constructor rỗng bắt buộc cho Firestore
    public Tour() {}

    // ⭐ CONSTRUCTOR ĐẦY ĐỦ THAM SỐ (Đã cập nhật để bao gồm soLuongKhachHienTai)
    public Tour(String maTour, String tenTour, String loaiTour, String diemKhoiHanh, String diemDen, Date ngayKhoiHanh, Date ngayKetThuc, int soNgay, int soDem, String moTa, int soLuongKhachToiDa, int soLuongKhachHienTai, long giaNguoiLon, long giaTreEm, long giaEmBe, double giaNuocNgoai, long tongGiaVon, long giaVonPerPax, double tySuatLoiNhuan, String dichVuBaoGom, String dichVuKhongBaoGom, String lichTrinhChiTiet, String hinhAnhChinhUrl, List<String> danhSachHinhAnh, String moTaSeo, boolean isXuatBan, boolean isNoiBat, String nguoiTao, Date ngayTao, String status) {
        this.maTour = maTour;
        this.tenTour = tenTour;
        this.loaiTour = loaiTour;
        this.diemKhoiHanh = diemKhoiHanh;
        this.diemDen = diemDen;
        this.ngayKhoiHanh = ngayKhoiHanh;
        this.ngayKetThuc = ngayKetThuc;
        this.soNgay = soNgay;
        this.soDem = soDem;
        this.moTa = moTa;
        this.soLuongKhachToiDa = soLuongKhachToiDa;
        this.soLuongKhachHienTai = soLuongKhachHienTai; // Cập nhật
        this.giaNguoiLon = giaNguoiLon;
        this.giaTreEm = giaTreEm;
        this.giaEmBe = giaEmBe;
        this.giaNuocNgoai = giaNuocNgoai;
        this.tongGiaVon = tongGiaVon;
        this.giaVonPerPax = giaVonPerPax;
        this.tySuatLoiNhuan = tySuatLoiNhuan;
        this.dichVuBaoGom = dichVuBaoGom;
        this.dichVuKhongBaoGom = dichVuKhongBaoGom;
        this.lichTrinhChiTiet = lichTrinhChiTiet;
        this.hinhAnhChinhUrl = hinhAnhChinhUrl;
        this.danhSachHinhAnh = danhSachHinhAnh;
        this.moTaSeo = moTaSeo;
        this.isXuatBan = isXuatBan;
        this.isNoiBat = isNoiBat;
        this.nguoiTao = nguoiTao;
        this.ngayTao = ngayTao;
        this.status = status;
    }

    // --------------------------------------------------------------------------------
    // Getters and Setters (Các trường cơ bản)
    // --------------------------------------------------------------------------------

    public String getMaTour() { return maTour; }
    public void setMaTour(String maTour) { this.maTour = maTour; }

    public String getTenTour() { return tenTour; }
    public void setTenTour(String tenTour) { this.tenTour = tenTour; }

    public String getLoaiTour() { return loaiTour; }
    public void setLoaiTour(String loaiTour) { this.loaiTour = loaiTour; }

    public String getDiemKhoiHanh() { return diemKhoiHanh; }
    public void setDiemKhoiHanh(String diemKhoiHanh) { this.diemKhoiHanh = diemKhoiHanh; }

    public String getDiemDen() { return diemDen; }
    public void setDiemDen(String diemDen) { this.diemDen = diemDen; }

    public Date getNgayKhoiHanh() { return ngayKhoiHanh; }
    public void setNgayKhoiHanh(Date ngayKhoiHanh) { this.ngayKhoiHanh = ngayKhoiHanh; }

    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public int getSoNgay() { return soNgay; }
    public void setSoNgay(int soNgay) { this.soNgay = soNgay; }

    public int getSoDem() { return soDem; }
    public void setSoDem(int soDem) { this.soDem = soDem; }

    public int getSoLuongKhachToiDa() { return soLuongKhachToiDa; }
    public void setSoLuongKhachToiDa(int soLuongKhachToiDa) { this.soLuongKhachToiDa = soLuongKhachToiDa; }

    // ⭐ GETTER & SETTER MỚI
    public int getSoLuongKhachHienTai() { return soLuongKhachHienTai; }
    public void setSoLuongKhachHienTai(int soLuongKhachHienTai) { this.soLuongKhachHienTai = soLuongKhachHienTai; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getLichTrinhChiTiet() { return lichTrinhChiTiet; }
    public void setLichTrinhChiTiet(String lichTrinhChiTiet) { this.lichTrinhChiTiet = lichTrinhChiTiet; }

    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String nguoiTao) { this.nguoiTao = nguoiTao; }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // --------------------------------------------------------------------------------
    // Getters and Setters (Các trường Định giá / Dịch vụ / SEO)
    // --------------------------------------------------------------------------------

    // Định giá
    public long getGiaNguoiLon() { return giaNguoiLon; }
    public void setGiaNguoiLon(long giaNguoiLon) { this.giaNguoiLon = giaNguoiLon; }

    public long getGiaTreEm() { return giaTreEm; }
    public void setGiaTreEm(long giaTreEm) { this.giaTreEm = giaTreEm; }

    public long getGiaEmBe() { return giaEmBe; }
    public void setGiaEmBe(long giaEmBe) { this.giaEmBe = giaEmBe; }

    public double getGiaNuocNgoai() { return giaNuocNgoai; }
    public void setGiaNuocNgoai(double giaNuocNgoai) { this.giaNuocNgoai = giaNuocNgoai; }

    // Giá Vốn / Lợi nhuận
    public long getTongGiaVon() { return tongGiaVon; }
    public void setTongGiaVon(long tongGiaVon) { this.tongGiaVon = tongGiaVon; }

    public long getGiaVonPerPax() { return giaVonPerPax; }
    public void setGiaVonPerPax(long giaVonPerPax) { this.giaVonPerPax = giaVonPerPax; }

    public double getTySuatLoiNhuan() { return tySuatLoiNhuan; }
    public void setTySuatLoiNhuan(double tySuatLoiNhuan) { this.tySuatLoiNhuan = tySuatLoiNhuan; }

    // Dịch vụ
    public String getDichVuBaoGom() { return dichVuBaoGom; }
    public void setDichVuBaoGom(String dichVuBaoGom) { this.dichVuBaoGom = dichVuBaoGom; }

    public String getDichVuKhongBaoGom() { return dichVuKhongBaoGom; }
    public void setDichVuKhongBaoGom(String dichVuKhongBaoGom) { this.dichVuKhongBaoGom = dichVuKhongBaoGom; }

    // Hình ảnh & SEO / Xuất bản
    public String getHinhAnhChinhUrl() { return hinhAnhChinhUrl; }
    public void setHinhAnhChinhUrl(String hinhAnhChinhUrl) { this.hinhAnhChinhUrl = hinhAnhChinhUrl; }

    public List<String> getDanhSachHinhAnh() { return danhSachHinhAnh; }
    public void setDanhSachHinhAnh(List<String> danhSachHinhAnh) { this.danhSachHinhAnh = danhSachHinhAnh; }

    public String getMoTaSeo() { return moTaSeo; }
    public void setMoTaSeo(String moTaSeo) { this.moTaSeo = moTaSeo; }

    public boolean isXuatBan() { return isXuatBan; }
    public void setXuatBan(boolean xuatBan) { isXuatBan = xuatBan; }

    public boolean isNoiBat() { return isNoiBat; }
    public void setNoiBat(boolean noiBat) { isNoiBat = noiBat; }
}