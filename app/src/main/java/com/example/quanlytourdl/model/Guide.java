package com.example.quanlytourdl.model;

import java.io.Serializable; // ⭐ IMPORT MỚI
import java.util.List;

// ⭐ Guide implements Serializable
public class Guide implements Serializable {

    // ⭐ Thêm serialVersionUID để đảm bảo tính tương thích khi deserialization
    private static final long serialVersionUID = 1L;

    private String id;
    private String fullName;
    private String guideCode;
    private String sdt;
    private String email;
    private String trangThai; // Trạng thái làm việc chung (ACTIVE, INACTIVE)

    // --- CÁC TRƯỜNG BỔ SUNG CHO PHÂN CÔNG ---
    private List<String> languages;     // Ngôn ngữ HDV sử dụng
    private double rating = 0.0;        // Điểm đánh giá trung bình (Mặc định 0.0)
    private int experienceYears = 0;    // Số năm kinh nghiệm

    // Thuộc tính Transient (Không cần tuần tự hóa, nhưng không gây lỗi)
    private boolean isAvailable = false; // Trạng thái sẵn sàng (Trống/Vướng lịch)
    private boolean isSelected = false;  // Trạng thái được chọn trong RecyclerView

    public Guide() {}

    // --- GETTERS & SETTERS CŨ ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getGuideCode() { return guideCode; }
    public void setGuideCode(String guideCode) { this.guideCode = guideCode; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }


    // --- GETTERS & SETTERS MỚI ---
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    // --- GETTERS & SETTERS CHO UI/ADAPTER ---
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }


    @Override
    public String toString() {
        return "HDV: " + fullName + " - Mã: " + guideCode + " - Trạng thái: " + trangThai;
    }
}