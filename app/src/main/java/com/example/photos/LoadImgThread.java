package com.example.photos;

import android.graphics.Bitmap;

import com.example.photos.Activities.MainActivity;

import java.io.IOException;
import java.util.ArrayList;


public class LoadImgThread extends Thread
{
    private MainActivity aty;
    public static int index = 30;
    private int all = FileUtils.images.size();
    private ArrayList<Bitmap> tmp = new ArrayList<>();
    private int i = 0;
    private int j = 0;
    public static final Object lock = new Object();

    public LoadImgThread(MainActivity a)
    {
        this.aty = a;
    }

    @Override
    public void run()
    {
        synchronized (lock)
        {
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
        aty.runOnUiThread(new Runnable()
        {
            @Override
            public void run() {
                synchronized (lock)
                {
                    for (;j < tmp.size(); j++)
                    {
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
