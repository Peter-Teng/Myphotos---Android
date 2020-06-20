package com.example.photos.NetWorking.tcp.receiver;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.photos.Activities.MainActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/*
* Last Editor:sujiaxin
* Last Edit Date:2020/6/10
* */
public class FileReciver implements Runnable{

    Socket socket;//将要传输文件的套接字连接

    private String fileStorePath;//文件保存路径

    private Context context;//保存可能需要进行UI交互的上下文环境

    private boolean ifInform;

    public FileReciver(Socket socket, String fileStorePath, Context context, boolean ifInform){
        this.socket = socket;
        this.fileStorePath = fileStorePath;
        this.context = context;
        this.ifInform = ifInform;//是否通过Toast展示接收文件信息
    }

    public FileReciver(Socket socket, String fileStorePath, Context context){
        this(socket, fileStorePath, context, true);
    }


    /*
    * 执行读取文件操作，并把读取结果展示在指定活动中
    */
    @Override
    public void run() {
        OutputStream out = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        try{
            InputStream in = socket.getInputStream();
            /**
             * 获取文件名长度
             * 文件格式：文件名长度(数字)\r\文件名\r\n文件内容\r\n
             * 获取文件名 - 读到第一个回车换行之前 截取出文件名的长度 接着读取这个长度的字节 就是文件名
             * 读取数据 直到遇到第一个回车换行
             * 每次从流中读取一个字节 转成字符串 拼到line上 只要line还不是\r\n结尾 就重复这个过程
             */
            String line1 = "";
            byte[] by1 = new byte[1];
            while(!line1.endsWith("\r\n")) {
                in.read(by1);
                String str = new String(by1);
                line1 += str;
            }
            /**
             * 1.读到长度，去掉\r\n就是文件名字的长度
             * 2.parseInt():作用是将可分析的字符串转化为整数。
             * 3.substring():返回一个新字符串，它是此字符串的一个子字符串。
             */
            int len1 = Integer.parseInt(line1.substring(0, line1.length() - 2));
            /**
             * 1.读取文件名
             * 2.先创建一个长度和文件名长度相等的字节数组，用来存放文件名
             * 3.read(data)：从输入流中读取一定数量的字节，并将其存储在缓冲区数组 data 中
             *      data数组有多大，就在in输入流里面读取多少内容，并将内容存放在data数组里面
             */
            byte[] data = new byte[len1];
            in.read(data);
            final String fileName = new String(data);

            // 获取文件内容字节长度
            String line2 = "";
            byte[] by2 = new byte[1];
            while(!line2.endsWith("\r\n")) {
                in.read(by2);
                String str = new String(by2);
                line2 += str;
            }
            int len2 = Integer.parseInt(line2.substring(0, line2.length() - 2));
            System.out.println("文件长度"+len2);
            // 创建输文件出流，指定文件输出地址
            final String path = fileStorePath+System.currentTimeMillis()+".JPEG";
            out = new FileOutputStream(path);
            dis = new DataInputStream(in);
            dos = new DataOutputStream(out);
            // 获取文件内容字节
            // 流对接
            byte[] buf = new byte[len2];
            int len = 0;

            while ((len = dis.read(buf)) != -1) {
                dos.write(buf, 0, len);
            }
            dos.flush();
//            展示读取结果
            if (ifInform) {
                context = (MainActivity)context;
                ((MainActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "接收到图片" + fileName + "并保存到" + fileStorePath,
                                Toast.LENGTH_SHORT).show();

                        //这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DATA, path);
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(path)));
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            // 关闭资源
            try {
                if(dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                dis = null;
            }
            try {
                if(dos != null) {
                    dos.flush();
                    dos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                dos = null;
            }
            // 关闭输出流
            try {
                if(out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                out = null;
            }
            // 关闭socket
            try {
                if(socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                socket = null;
            }

        }
    }
}
