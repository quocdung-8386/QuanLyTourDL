package com.example.quanlytourdl.model;

public class Guide {
    private String id;
    private String fullName;
    private String guideCode;
    private String sdt;
    private String email;
    private String trangThai;
    public Guide() {}

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

    @Override
    public String toString() {
        return "HDV: " + fullName + " - Mã: " + guideCode + " - Trạng thái: " + trangThai;
    }
}