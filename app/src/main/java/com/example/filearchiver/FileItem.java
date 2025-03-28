package com.example.filearchiver;

import android.net.Uri;

public class FileItem {
    private Uri uri;
    private String name;
    private String size;
    private int iconResId;

    public FileItem(Uri uri, String name, String size, int iconResId) {
        this.uri = uri;
        this.name = name;
        this.size = size;
        this.iconResId = iconResId;
    }

    public Uri getUri(){
        return uri;
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