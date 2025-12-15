package com.example.quanlytourdl;

public class NhanVien {
    private String avatar;
    private String name;
    private String id;
    private String department;

    public NhanVien(String avatar, String name, String id, String department) {
        this.avatar = avatar;
        this.name = name;
        this.id = id;
        this.department = department;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}