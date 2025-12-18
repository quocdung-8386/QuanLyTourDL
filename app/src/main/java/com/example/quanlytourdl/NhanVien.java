package com.example.quanlytourdl;

import java.io.Serializable;

public class NhanVien implements Serializable {
    private String documentId; // ID của document trên Firestore
    private String fullName;   // QUAN TRỌNG: Đã đổi từ 'name' -> 'fullName' cho khớp Firebase
    private String id;         // Mã nhân viên
    private String department;
    private String email;
    private String role;

    // 3 quyền truy cập (Mặc định false)
    private boolean accessTour;
    private boolean accessCustomer;
    private boolean accessReport;

    public NhanVien() { } // Constructor rỗng bắt buộc

    // Constructor Đầy đủ (9 tham số - Dùng cho màn hình Phân quyền)
    public NhanVien(String documentId, String fullName, String id, String department, String email, String role, boolean accessTour, boolean accessCustomer, boolean accessReport) {
        this.documentId = documentId;
        this.fullName = fullName;
        this.id = id;
        this.department = department;
        this.email = email;
        this.role = role;
        this.accessTour = accessTour;
        this.accessCustomer = accessCustomer;
        this.accessReport = accessReport;
    }

    // Constructor Cũ (6 tham số - Giữ lại để KHÔNG LỖI màn hình Quản lý nhân sự)
    public NhanVien(String documentId, String fullName, String id, String department, String email, String role) {
        this.documentId = documentId;
        this.fullName = fullName;
        this.id = id;
        this.department = department;
        this.email = email;
        this.role = role;
        // Mặc định các quyền là false
        this.accessTour = false;
        this.accessCustomer = false;
        this.accessReport = false;
    }

    // --- GETTERS & SETTERS ---
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    // Chú ý: Getter/Setter cho fullName
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Getter/Setter cho quyền hạn
    public boolean isAccessTour() { return accessTour; }
    public void setAccessTour(boolean accessTour) { this.accessTour = accessTour; }

    public boolean isAccessCustomer() { return accessCustomer; }
    public void setAccessCustomer(boolean accessCustomer) { this.accessCustomer = accessCustomer; }

    public boolean isAccessReport() { return accessReport; }
    public void setAccessReport(boolean accessReport) { this.accessReport = accessReport; }
}