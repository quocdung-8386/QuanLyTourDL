package com.example.quanlytourdl.model;

public class SupportTicket {
    private String id;
    private String title;
    private String customerInfo;
    private String status;
    private String time;

    public SupportTicket(String id, String title, String customerInfo, String status, String time) {
        this.id = id;
        this.title = title;
        this.customerInfo = customerInfo;
        this.status = status;
        this.time = time;
    }

    // Getters (Bạn có thể tự thêm Setters nếu cần)
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCustomerInfo() { return customerInfo; }
    public String getStatus() { return status; }
    public String getTime() { return time; }
}
