package com.example.quanlytourdl.model;

public class SupportTicket {
    private String id;
    private String description;
    private String customerName;
    private String status;
    private String time;

    // [BẮT BUỘC] Constructor rỗng cho Firebase
    public SupportTicket() { }

    public SupportTicket(String id, String description, String customerName, String status, String time) {
        this.id = id;
        this.description = description;
        this.customerName = customerName;
        this.status = status;
        this.time = time;
    }

    // Getters
    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getCustomerName() { return customerName; }
    public String getStatus() { return status; }
    public String getTime() { return time; }

    // [BẮT BUỘC] Setters cho Firebase
    public void setId(String id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setStatus(String status) { this.status = status; }
    public void setTime(String time) { this.time = time; }
}