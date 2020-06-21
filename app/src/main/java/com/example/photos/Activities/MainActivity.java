package com.example.photos.Activities;


/**
 * @author DHP SJX
 *
 */
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
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
import com.example.photos.NetWorking.udp.sender.impl.DeviceFinder;
import com.example.photos.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//显示图片缩略图以及recyclerView的主要Aty
public class MainActivity extends AppCompatActivity {

    private RecyclerView grids = null;
    public ImgAdapter adapter = null;
    private int deleted = 0;//记录当前删除了多少张照片，以免传pos的时候错位


    //访存权限相关
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                    Manifest.permission.INTERNET};//新增网络请求连接
    private long millisec = -1;

    //局域网设备搜索服务
    private DeviceWaitingFinder deviceWaitingFinder;//在局域网中上线，为同一局域网内其他设备提供响应服务
    public static final int DEVICE_RESP_PORT = 9999;//局域网响应服务端口

    //文件接收服务
    private TcpReciver tcpReciver;//监听文件传输服务
    public static final int RECV_PORT = 8888;//文件传输监听端口
    public static final String RECV_FILE_PATH = "/storage/emulated/0/Pictures/";//文件接收路径

//    接收广播udp报文设置
    private WifiManager.MulticastLock lock;
//      线程池
    private static final int POOL_SIZE = 5;//线程池数量
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(POOL_SIZE);
//      获取设备品牌及型号作为设备名称
    private static final String DEVICE_NAME = android.os.Build.BRAND+" "+android.os.Build.MODEL;
//      主界面图片宽度（dp）
    private static final int PICTURE_SIZE = 136;

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
        final ArrayList<Bitmap> thumbnails = new ArrayList<>();
        //开始先load30张图片，免得加载太多造成系统卡顿
        for (int i = 0; i < 30 && i < FileUtils.images.size(); i++) {
            try {
                thumbnails.add(FileUtils.generateThumbnails(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//      获取设备宽度
        Point outSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(outSize);
        int x = outSize.x;
//      根据设备宽度以及图片宽度计算网格布局列数
        final float scale = MainActivity.this.getResources().getDisplayMetrics().density;
        int spanCount = (int) (x/(PICTURE_SIZE*scale+0.5f))+1;

        //为recyclerView初始化adapter，布局以及设置点击事件监听
        adapter = new ImgAdapter(MainActivity.this, thumbnails);
        GridLayoutManager manager = new GridLayoutManager(MainActivity.this,spanCount);
        //点击图片缩略图，进入图片大图Aty
        adapter.setOnItemClickListener(new ImgAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MainActivity.this, ImgActivity.class);
                intent.putExtra("pos", position - deleted);//设置Intent，告知是哪张图片被点击了
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

//        开启接收广播udp报文设置
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lock= wifiManager.createMulticastLock("localWifi");
        lock.acquire();//在destroy方法中记得释放锁

        //创建局域网响应服务线程
        deviceWaitingFinder = new DeviceWaitingFinder(DEVICE_NAME, DEVICE_RESP_PORT, MainActivity.this);
        //服务启动
        fixedThreadPool.submit(deviceWaitingFinder);

        //创建文件接收服务线程
        tcpReciver = new TcpReciver(RECV_PORT, RECV_FILE_PATH, MainActivity.this);
        //启动线程
        fixedThreadPool.submit(tcpReciver);

        //下拉刷新功能实现，当到达顶部时继续下拉则重新从存储卡中获取最新图片
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.mswipeRefreshLayout_main);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshUI();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    //用于主界面刷新UI
    public void refreshUI(){
        //清空当前保存的图片，并重新加载
        FileUtils.images.clear();
        FileUtils.picture(MainActivity.this.getContentResolver());
        //清空当前图片列表内容
        adapter.clear();
        //重新获取前30张图片
        for (int i = 0; i < 30 && i < FileUtils.images.size(); i++) {
            try {
                adapter.insertBitmap(FileUtils.generateThumbnails(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
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
                    adapter.deleteBitmap(pos);//从adapter中移除图片
                    FileUtils.deleteBitmap(getContentResolver(),pos);//实际删除这个图片
                    adapter.notifyItemRemoved(pos);//在recyclerView中删除这个图片
                    deleted++;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lock.release();
    }
}
