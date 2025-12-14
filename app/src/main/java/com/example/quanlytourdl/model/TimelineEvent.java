package com.example.quanlytourdl.model;

import java.util.List;

public class TimelineEvent {
    private String time;
    private String title;
    private String description;
    private String iconType;
    private List<String> imageUrls;

    public TimelineEvent() {}

    public TimelineEvent(String time, String title, String description, String iconType, List<String> imageUrls) {
        this.time = time;
        this.title = title;
        this.description = description;
        this.iconType = iconType;
        this.imageUrls = imageUrls;
    }

    // Getters and Setters (Giữ nguyên như đã cung cấp)
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}