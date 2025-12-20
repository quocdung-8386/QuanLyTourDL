package com.example.quanlytourdl.model; // Đổi package cho phù hợp

public class ChatMessage {
    private String message;
    private boolean isUser; // true: Người dùng, false: Bot

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() { return message; }
    public boolean isUser() { return isUser; }
}