package com.example.photos;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FileUtils
{
    public static int numberLoaded = 0;
    public static int maxLoad = 500;
    public static ArrayList<MyImage> images = new ArrayList<MyImage>();
    private  static String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};

    public static void  picture(ContentResolver cr)
    {
        Uri originalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = cr.query(originalUri, projection, null, null, "date_modified desc");
        if (cursor == null) {
            return;
        }
        while(cursor.moveToNext())
        {
            String path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
            Date dateModified = new Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))));
            String displayName = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"));
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            MyImage img = new MyImage(id,path,dateModified,displayName);
            images.add(img);
        }
    }

    public static void deleteBitmap(ContentResolver cr,int pos)
    {
        MyImage toBeDeleted = images.get(pos);
        Uri originalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String tmp = "_data='"+toBeDeleted.getPath()+"'";
        cr.delete(originalUri,tmp,null);
        images.remove(pos);
    }

    public static Bitmap generateThumbnails(int i) throws IOException {
        if(numberLoaded >= maxLoad)
            return null;
        MyImage myImage = FileUtils.images.get(i);
        String path = myImage.getId() + "$" + myImage.getDisplayName();
        try {
            FileInputStream inputStream = MyApplication.getContext().openFileInput(path);
            Bitmap thumbnail = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            if(thumbnail != null)
                return thumbnail;
        }
        catch (IOException e)
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(myImage.getPath(), options);
            float realWidth = options.outWidth;
            float realHeight = options.outHeight;
            int scale = (int) ((realHeight > realWidth ? realHeight : realWidth) / 150);
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(myImage.getPath(), options);
            FileOutputStream outputStream = MyApplication.getContext().openFileOutput(path,Context.MODE_PRIVATE);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            outputStream.write(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
            outputStream.close();
            numberLoaded++;
            return bitmap;
        }
        return null;
    }

    public static boolean saveBitmap(Context ctx,Bitmap bitmap)
    {
        boolean result = false;
        int name = (int) (Math.random() * 10000000);
        String path = "/storage/emulated/0/Pictures/" + name + ".JPEG";
        File file = new File(path);
        OutputStream os = null;
        try
        {
            os = new FileOutputStream(file);
            result = bitmap.compress(Bitmap.CompressFormat.JPEG,100,os);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if(os != null)
                try {
                    os.close();
                }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, path);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        ctx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(path)));
        return result;
    }

}
