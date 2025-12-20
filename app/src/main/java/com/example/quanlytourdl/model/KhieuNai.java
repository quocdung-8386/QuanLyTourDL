package com.example.quanlytourdl.model;

public class KhieuNai {
    private String id;
    private String customerName;
    private String tourId;
    private String dateIncident; // Ngày xảy ra sự cố
    private String content;
    private String evidenceUri;  // Đường dẫn ảnh/file
    private String priority;     // Mức độ ưu tiên
    private String status;       // Trạng thái

    public KhieuNai() {} // Constructor rỗng bắt buộc cho Firebase

    public KhieuNai(String id, String customerName, String tourId, String dateIncident, String content, String evidenceUri, String priority, String status) {
        this.id = id;
        this.customerName = customerName;
        this.tourId = tourId;
        this.dateIncident = dateIncident;
        this.content = content;
        this.evidenceUri = evidenceUri;
        this.priority = priority;
        this.status = status;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }
    public String getDateIncident() { return dateIncident; }
    public void setDateIncident(String dateIncident) { this.dateIncident = dateIncident; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getEvidenceUri() { return evidenceUri; }
    public void setEvidenceUri(String evidenceUri) { this.evidenceUri = evidenceUri; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}