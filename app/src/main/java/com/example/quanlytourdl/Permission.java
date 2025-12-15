package com.example.quanlytourdl;

public class Permission {
    private String name;
    private String description;
    private boolean isEnabled;

    public Permission(String name, String description, boolean isEnabled) {
        this.name = name;
        this.description = description;
        this.isEnabled = isEnabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}