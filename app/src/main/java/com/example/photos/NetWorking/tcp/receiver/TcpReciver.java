package com.example.photos.NetWorking.tcp.receiver;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * @author Ticsmyc
 * @date 2020-06-05 16:33
 */
public class TcpReciver implements Runnable{

    private int port;//文件上传监听端口

    private String fileStorePath;//文件存储路径

    private Context context;//显示上传结果的活动

    public TcpReciver(int port, String fileStorePath, Context context){
        this.port = port;
        this.fileStorePath = fileStorePath;
        this.context = context;
    }
    /**
     * 持续阻塞监听文件上传请求，得到请求socket之后即开启线程执行读取上传文件操作
     */
    @Override
    public void run() {
        ServerSocket ss = null;
        Looper.prepare();//通过子线程调用UI处理方法需要先经过此步骤，具体用法及原因尚待探索
        Toast.makeText(context, "文件接收监听服务启动", Toast.LENGTH_SHORT).show();
        Looper.loop();// 进入loop中的循环，查看消息队列
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Socket socket = ss.accept();
                new Thread(new FileReciver(socket, fileStorePath, context)).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
