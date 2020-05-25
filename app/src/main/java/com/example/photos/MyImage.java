package com.example.photos;

import java.util.Date;

public class MyImage
{
    private String path;
    private Date dateModified;
    private String displayName;

    public MyImage(String path, Date dateModified, String displayName) {
        this.path = path;
        this.dateModified = dateModified;
        this.displayName = displayName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

