package com.example.quanlytourdl.model;

public class Warning {
    private String id;
    private String title;       // Tiêu đề (VD: Bão khẩn cấp)
    private String content;     // Nội dung chi tiết
    private String targetType;  // Đối tượng: "Tất cả", "Theo Tour", "Cá nhân"
    private String targetValue; // Giá trị: "ALL", "Mã Tour", "Tên Khách"
    private String level;       // Mức độ: "Tin tin", "Cảnh báo", "Khẩn cấp"
    private String timestamp;

    public Warning() {}

    public Warning(String id, String title, String content, String targetType, String targetValue, String level, String timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.targetType = targetType;
        this.targetValue = targetValue;
        this.level = level;
        this.timestamp = timestamp;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}