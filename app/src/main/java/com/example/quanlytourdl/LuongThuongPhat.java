package com.example.quanlytourdl;

import java.io.Serializable;

public class LuongThuongPhat implements Serializable {
    private String id;              // ID Firestore
    private String employeeName;    // Tên nhân viên
    private String department;      // Phòng ban (Kinh doanh, IT...)
    private String type;            // "Thưởng" hoặc "Phạt"
    private double amount;          // Số tiền
    private String reason;          // Lý do
    private String status;          // "Chờ duyệt", "Đã duyệt", "Đã từ chối"
    private String date;            // Ngày tạo (dd/MM/yyyy)

    public LuongThuongPhat() { } // Constructor rỗng cho Firebase

    public LuongThuongPhat(String id, String employeeName, String department, String type, double amount, String reason, String status, String date) {
        this.id = id;
        this.employeeName = employeeName;
        this.department = department;
        this.type = type;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.date = date;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}