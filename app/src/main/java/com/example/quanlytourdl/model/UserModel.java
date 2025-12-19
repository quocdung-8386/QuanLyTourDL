package com.example.quanlytourdl.model; // Hoặc package của bạn

public class UserModel {
    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String avatar;

    public UserModel() { } // Constructor rỗng bắt buộc cho Firebase

    public UserModel(String uid, String fullName, String email, String phone, String role, String avatar) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.avatar = avatar;
    }

    // Getter và Setter
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}