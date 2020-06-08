package com.example.photos;
/**
 * @author DHP
 *
 */
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

//图像文件相关操作的工具类，相当于一个简单的DAO
public class FileUtils
{
    //保存所有MyImage对象的列表
    public static ArrayList<MyImage> images = new ArrayList<MyImage>();
    //查询数据库哪些字段
    private  static String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};

    //从手机媒体数据库中获取所有的图像
    public static void  picture(ContentResolver cr)
    {
        Uri originalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        //执行数据库查询
        Cursor cursor = cr.query(originalUri, projection, null, null, "date_modified desc");
        if (cursor == null) {
            return;
        }
        while(cursor.moveToNext())
        {
            //根据查询建立所有的MyImage对象，保存到images列表中
            String path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
            Date dateModified = new Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))));
            String displayName = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"));
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            MyImage img = new MyImage(id,path,dateModified,displayName);
            images.add(img);
        }
    }

    //从手机媒体数据库中删除图像
    public static void deleteBitmap(ContentResolver cr,int pos)
    {
        //获取图像index
        MyImage toBeDeleted = images.get(pos);
        //删除图像
        Uri originalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String tmp = "_data='"+toBeDeleted.getPath()+"'";
        cr.delete(originalUri,tmp,null);
        images.remove(pos);
    }

    //读取或生成图像缩略图，生成缩略图的步骤是为了使得recyclerView不卡顿
    public static Bitmap generateThumbnails(int i) throws IOException {
        MyImage myImage = FileUtils.images.get(i);
        String path = myImage.getId() + "$" + myImage.getDisplayName();
        //尝试从内部存储中读取缓存的缩略图
        try {
            FileInputStream inputStream = MyApplication.getContext().openFileInput(path);
            Bitmap thumbnail = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            if(thumbnail != null)
                return thumbnail;
        }
        //假如没能读取到，即该图像的缩略图没有被保存，那么创造一个缩略图
        catch (IOException e)
        {
            //利用options方法计算放缩参数，生成图片的缩略图
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(myImage.getPath(), options);
            float realWidth = options.outWidth;
            float realHeight = options.outHeight;
            int scale = (int) ((realHeight > realWidth ? realHeight : realWidth) / 150);
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(myImage.getPath(), options);
            //生成缩略图之后将其保存到本地内部存储，这样下次程序运行就不用重复生成了，可以直接在内部存储中读取
            FileOutputStream outputStream = MyApplication.getContext().openFileOutput(path,Context.MODE_PRIVATE);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            outputStream.write(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
            outputStream.close();
            return bitmap;
        }
        return null;
    }

    //保存修改后的图像（涂鸦或旋转之后）
    public static boolean saveBitmap(Context ctx,Bitmap bitmap)
    {
        boolean result = false;
        //保存图像的名字为当前时间
        SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String path = "/storage/emulated/0/Pictures/" + date.toString() + ".JPEG";
        File file = new File(path);
        OutputStream os = null;
        //创建输出流保存图像
        try
        {
            os = new FileOutputStream(file);
            result = bitmap.compress(Bitmap.CompressFormat.JPEG,100,os);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            //关闭文件输出流
            if(os != null)
                try {
                    os.close();
                }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        //将图像保存信息告知媒体数据库，插入一条数据库记录
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, path);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        ctx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(path)));
        return result;
    }

}
