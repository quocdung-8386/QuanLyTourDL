package com.example.quanlytourdl.model; // Package model

public class DanhGia {
    private String id;
    private String userName;
    private String date;
    private float rating; // 1.0 đến 5.0
    private String comment;
    private String tourName;
    private String hdvName;
    private int avatarResId; // Dùng cho ảnh mẫu

    public DanhGia() { }

    public DanhGia(String id, String userName, String date, float rating, String comment, String tourName, String hdvName, int avatarResId) {
        this.id = id;
        this.userName = userName;
        this.date = date;
        this.rating = rating;
        this.comment = comment;
        this.tourName = tourName;
        this.hdvName = hdvName;
        this.avatarResId = avatarResId;
    }

    // Getters
    public String getId() { return id; }
    public String getUserName() { return userName; }
    public String getDate() { return date; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public String getTourName() { return tourName; }
    public String getHdvName() { return hdvName; }
    public int getAvatarResId() { return avatarResId; }

    // Setters (nếu cần dùng cho Firebase toObject)
    public void setId(String id) { this.id = id; }
    public void setRead(boolean read) { } // Placeholder cho logic đã đọc
    public boolean isRead() { return false; } // Mặc định chưa đọc
}