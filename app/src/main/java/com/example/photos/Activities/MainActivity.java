package com.example.photos.Activities;


/**
 * @author DHP
 *
 */
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.photos.AdaptersAndView.ImgAdapter;
import com.example.photos.FileUtils;
import com.example.photos.LoadImgThread;
import com.example.photos.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


//显示图片缩略图以及recyclerView的主要Aty
public class MainActivity extends AppCompatActivity {

    private RecyclerView grids = null;
    public ImgAdapter adapter = null;

    //访存权限相关
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //记录用户上一次按退出是什么时候的变量，实现连续按两次退出键退出的功能
    private long millisec = -1;

    //获取用户权限
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取用户权限
        verifyStoragePermissions(MainActivity.this);
        FileUtils.picture(MainActivity.this.getContentResolver());

        //初始化recyclerView
        grids = findViewById(R.id.recyclerView);
        ArrayList<Bitmap> thumbnails = new ArrayList<>();
        //开始先load30张图片，免得加载太多造成系统卡顿
        for (int i = 0; i < 30 && i < FileUtils.images.size(); i++) {
            try {
                thumbnails.add(FileUtils.generateThumbnails(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //为recyclerView初始化adapter，布局以及设置点击事件监听
        adapter = new ImgAdapter(MainActivity.this, thumbnails);
        GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 3);

        //点击图片缩略图，进入图片大图Aty
        adapter.setOnItemClickListener(new ImgAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MainActivity.this, ImgActivity.class);
                intent.putExtra("pos", position);//设置Intent，告知是哪张图片被点击了
                startActivityForResult(intent,0);
            }
        });
        grids.setAdapter(adapter);
        grids.setLayoutManager(manager);


        //设置滑动事件监听
        grids.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
            {
                //当recyclerView被滑动到底部时，采用一个新的线程来load 30张图片
                if(!grids.canScrollVertically(1) && LoadImgThread.index < FileUtils.images.size()) {
                    LoadImgThread loadImgThread = new LoadImgThread(MainActivity.this);
                    loadImgThread.start();
                }
            }
        });
    }

    //这里接收ImageActivity结束传回来的参数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        switch(resultCode)
        {
            //假如有图片被删除，code为1
            case 1:
                int pos = data.getIntExtra("pos",-1);//获取要删除的图片的index
                if(pos != -1)
                {
                    //Toast.makeText(MainActivity.this,"您正在尝试删除第"+pos+"张图片",Toast.LENGTH_SHORT).show();
                    adapter.deleteBitmap(pos);//实际删除这个图片
                    FileUtils.deleteBitmap(getContentResolver(),pos);
                    adapter.notifyItemRemoved(pos);//在recyclerView中删除这个图片
                }
                break;
        }
    }

    //下面两个函数是为了实现连续点击两下退出键才退出app的
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            exit_main();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit_main()
    {
        if((System.currentTimeMillis()-millisec)>2000)
        //假如当前时间与上次按下退出键的时间差大于2sec，则弹出提示：“再按一次退出键退出”
        {
            Toast.makeText(getApplicationContext(),
                    "再按一次退出我的图像", Toast.LENGTH_SHORT).show();
            millisec = System.currentTimeMillis();
        }
        else//两次按下退出键之间的时间小于两秒，直接结束应用。
        {
            finish();
            System.exit(0);
        }
    }
}
