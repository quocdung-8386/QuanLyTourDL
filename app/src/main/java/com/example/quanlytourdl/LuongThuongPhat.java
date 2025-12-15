package com.example.quanlytourdl;

public class LuongThuongPhat {
    private String type;
    private String employeeName;
    private String description;
    private String status;
    private boolean showButtons;

    public LuongThuongPhat(String type, String employeeName, String description, String status, boolean showButtons) {
        this.type = type;
        this.employeeName = employeeName;
        this.description = description;
        this.status = status;
        this.showButtons = showButtons;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isShowButtons() {
        return showButtons;
    }

    public void setShowButtons(boolean showButtons) {
        this.showButtons = showButtons;
    }
}