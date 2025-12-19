package com.example.quanlytourdl.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Guide implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id; // Document ID trên Firestore
    private String fullName;
    private String guideCode;
    private String phoneNumber;
    private String email;
    private String address;
    private String gender;
    private String birthDate;

    private boolean isApproved; // Khớp với field Boolean trên Firebase
    private List<String> languages;
    private int experienceYears = 0;
    private double rating = 0.0;

    // Field này dùng cho logic UI (ví dụ chọn nhiều HDV), không lưu lên Firebase
    @Exclude
    private boolean isSelected = false;

    public Guide() {
        this.languages = new ArrayList<>();
    }

    // --- Getters & Setters ---

    @Exclude // ID thường được lấy từ document.getId(), không nên lưu trùng trong field
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() {
        return fullName != null ? fullName : "Chưa cập nhật";
    }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGuideCode() { return guideCode != null ? guideCode : ""; }
    public void setGuideCode(String guideCode) { this.guideCode = guideCode; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    @PropertyName("isApproved")
    public boolean isApproved() { return isApproved; }

    @PropertyName("isApproved")
    public void setApproved(boolean approved) { isApproved = approved; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    @Exclude
    public boolean isSelected() { return isSelected; }
    @Exclude
    public void setSelected(boolean selected) { isSelected = selected; }

    /**
     * Helper để lấy trạng thái dạng Text hiển thị nhanh lên UI
     */
    @Exclude
    public String getStatusDisplayText() {
        return isApproved ? "Sẵn sàng" : "Chờ phê duyệt";
    }
}