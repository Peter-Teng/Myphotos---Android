package com.example.photos;

/**
 * @author DHP，TYD
 *
 */
import android.graphics.Bitmap;

import com.example.photos.Activities.MainActivity;

import java.io.IOException;
import java.util.ArrayList;

//加载图片的线程
public class LoadImgThread extends Thread
{
    private MainActivity aty;
    //加载index偏移量，初始为30是因为MainAty开始先加载了30张
    public static int index = 30;
    //图片总数
    private int all = FileUtils.images.size();
    private ArrayList<Bitmap> tmp = new ArrayList<>();
    //线程同步相关
    private int i = 0;
    private int j = 0;
    public static final Object lock = new Object();

    //构造函数
    public LoadImgThread(MainActivity a)
    {
        this.aty = a;
    }

    @Override
    public void run()
    {
        synchronized (lock)
        {
            //加载30张图片
            for (;i < 30 && all > (i + index); i++)
            {
                Bitmap bitmap = null;
                try {
                    bitmap = FileUtils.generateThumbnails(index + i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tmp.add(bitmap);
            }
            j = 0;
        }
        //调用UI线程更新UI
        aty.runOnUiThread(new Runnable()
        {
            @Override
            public void run() {
                synchronized (lock)
                {
                    for (;j < tmp.size(); j++)
                    {
                        //取得recylerView的adapter，插入所有更新的图像
                        aty.adapter.insertBitmap(tmp.get(j));
                        aty.adapter.notifyItemInserted(index);
                        index++;
                    }
                    tmp.clear();
                    i = 0;
                }
            }
        });
    }
}
