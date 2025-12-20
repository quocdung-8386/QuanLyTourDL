package com.example.quanlytourdl.model;

public class TicketInteraction {
    private String name;
    private String time;
    private String content;
    private int avatarResId; // ID ảnh (R.drawable...)
    private boolean isSystem; // Để xác định là hệ thống hay người dùng

    public TicketInteraction(String name, String time, String content, int avatarResId, boolean isSystem) {
        this.name = name;
        this.time = time;
        this.content = content;
        this.avatarResId = avatarResId;
        this.isSystem = isSystem;
    }

    public String getName() { return name; }
    public String getTime() { return time; }
    public String getContent() { return content; }
    public int getAvatarResId() { return avatarResId; }
    public boolean isSystem() { return isSystem; }
}