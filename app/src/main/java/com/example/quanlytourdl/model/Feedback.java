package com.example.quanlytourdl.model;

public class Feedback {
    private String id;
    private String tourName;    // Tên tour
    private String content;     // Nội dung nhận xét
    private String sentiment;   // "Tích cực", "Trung lập", "Tiêu cực"
    private int rating;         // Số sao (1 - 5)
    private String date;        // Ngày đánh giá (dd/MM/yyyy)
    private long timestamp;     // Dùng để sắp xếp thời gian

    // Constructor rỗng (BẮT BUỘC cho Firebase)
    public Feedback() { }

    // Constructor đầy đủ
    public Feedback(String id, String tourName, String content, String sentiment, int rating, String date, long timestamp) {
        this.id = id;
        this.tourName = tourName;
        this.content = content;
        this.sentiment = sentiment;
        this.rating = rating;
        this.date = date;
        this.timestamp = timestamp;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTourName() { return tourName; }
    public void setTourName(String tourName) { this.tourName = tourName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}