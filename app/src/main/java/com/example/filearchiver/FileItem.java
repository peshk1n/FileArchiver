package com.example.filearchiver;

public class FileItem {
    private String name;
    private String size;
    private int iconResId;

    public FileItem(String name, String size, int iconResId) {
        this.name = name;
        this.size = size;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public int getIconResId() {
        return iconResId;
    }
}