package com.example.photos.Activities;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.photos.AdaptersAndView.ImgAdapter;
import com.example.photos.FileUtils;
import com.example.photos.LoadImgThread;
import com.example.photos.NetWorking.tcp.receiver.TcpReciver;
import com.example.photos.NetWorking.udp.recevier.impl.DeviceWaitingFinder;
import com.example.photos.R;

import java.io.IOException;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    private RecyclerView grids = null;
    public ImgAdapter adapter = null;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                    Manifest.permission.INTERNET};//新增网络请求连接
    private long millisec = -1;

    //局域网设备搜索服务
    private DeviceWaitingFinder deviceWaitingFinder;//在局域网中上线，为同一局域网内其他设备提供响应服务
    private static final int DEVICE_RESP_PORT = 9999;//局域网响应服务端口

    //文件接收服务
    private TcpReciver tcpReciver;//监听文件传输服务
    private static final int RECV_PORT = 8888;//文件传输监听端口

//    接收广播udp报文设置
    private WifiManager.MulticastLock lock;

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
        verifyStoragePermissions(MainActivity.this);
        FileUtils.picture(MainActivity.this.getContentResolver());
        grids = findViewById(R.id.recyclerView);
        ArrayList<Bitmap> thumbnails = new ArrayList<>();
        for (int i = 0; i < 30 && i < FileUtils.images.size(); i++) {
            try {
                thumbnails.add(FileUtils.generateThumbnails(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        adapter = new ImgAdapter(MainActivity.this, thumbnails);
        GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 3);
        adapter.setOnItemClickListener(new ImgAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MainActivity.this, ImgActivity.class);
                intent.putExtra("pos", position);
                startActivityForResult(intent,0);
            }
        });
        grids.setAdapter(adapter);
        grids.setLayoutManager(manager);


        grids.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
            {
                if(!grids.canScrollVertically(1) && LoadImgThread.index < FileUtils.images.size()) {
                    LoadImgThread loadImgThread = new LoadImgThread(MainActivity.this);
                    loadImgThread.start();
                }
            }
        });

//        开启接收广播udp报文设置
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lock= wifiManager.createMulticastLock("localWifi");
        lock.acquire();//在destroy方法中记得释放锁

        //创建局域网响应服务线程
        deviceWaitingFinder = new DeviceWaitingFinder("Tester", DEVICE_RESP_PORT, MainActivity.this);
        //服务启动
        deviceWaitingFinder.start();

        //创建文件接收服务线程
        tcpReciver = new TcpReciver(RECV_PORT, "", MainActivity.this);
        //启动线程(注：暂未确定线程适用策略，没有线程池服务，暂时先直接创建匿名线程开启服务)
        new Thread(tcpReciver).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        switch(resultCode)
        {
            case 1:
                int pos = data.getIntExtra("pos",-1);
                if(pos != -1)
                {
                    //Toast.makeText(MainActivity.this,"您正在尝试删除第"+pos+"张图片",Toast.LENGTH_SHORT).show();
                    adapter.deleteBitmap(pos);
                    FileUtils.deleteBitmap(getContentResolver(),pos);
                    adapter.notifyItemRemoved(pos);
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lock.release();
    }
}
