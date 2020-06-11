package com.example.photos;

import java.util.Date;

/**
 * @author DHP
 * @date 2020-05-22 08:40
 */

//图片相关的实体POJO类，记录了一幅图片的路径，创建时间，名称以及id
//每一幅在手机里的图片都相当于一个MyImage对象
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

