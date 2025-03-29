package com.example.filearchiver;

import android.net.Uri;

public class FileItem {
    private Uri uri;
    private String name;
    private String size;
    private int iconResId;
    private boolean isArchived;

    public FileItem(Uri uri, String name, String size, int iconResId) {
        this.uri = uri;
        this.name = name;
        this.size = size;
        this.iconResId = iconResId;
        this.isArchived = false;
    }

    public boolean isArchived(){
        return this.isArchived;
    }

    public void setArchived(boolean isArchived){
        this.isArchived = isArchived;
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

    public void setSize(String size){
        this.size = size;
    }

    public int getIconResId() {
        return iconResId;
    }
}