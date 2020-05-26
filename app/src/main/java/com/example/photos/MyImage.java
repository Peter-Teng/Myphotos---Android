package com.example.photos;

import java.util.Date;

public class MyImage
{
    private String path;
    private Date dateAdded;
    private String displayName;
    private long id;

    public MyImage(long id,String path, Date dateModified, String displayName) {
        this.id = id;
        this.path = path;
        this.dateAdded = dateModified;
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

