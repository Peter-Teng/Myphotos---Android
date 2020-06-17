package com.example.photos.NetWorking.tcp.sender;

import android.widget.Toast;

import com.example.photos.Activities.DeviceActivity;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Ticsmyc
 * @date 2020-06-05 16:33
 */
public class TcpSender {

    public static final int SEND_SIZE = 5<<20;//文件传输缓冲区大小

    private static final int CONN_TIME_OUT = 5000;

    public void sendFile(String filePath , String host, int port, final DeviceActivity context)  {
        Scanner scan = null;
        InputStream in = null;
        Socket socket = null;

        try {
            File file = new File(filePath);
            if(file.exists() && file.isFile()) {
                in = new FileInputStream(file);

                socket = new Socket();
                //连接
                socket.connect(new InetSocketAddress(host, port),CONN_TIME_OUT);
                //心跳机制
                socket.setOOBInline(true);
                OutputStream out = socket.getOutputStream();


                // 向服务器发送[文件名字节长度 \r\n]
                out.write((file.getName().getBytes().length + "\r\n").getBytes());
                // 向服务器发送[文件名字节]
                out.write(file.getName().getBytes());
                System.out.println("文件长度"+file.length());
                // 向服务器发送[文件字节长度\r\n]
                out.write((file.length() + "\r\n").getBytes());
                // 发送[文件字节内容]
                byte[] data = new byte[SEND_SIZE];
                int i = 0;
                while((i = in.read(data)) != -1) {
                    out.write(data, 0, i);
                }
                //                            显示结果
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "传输成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "传输失败", Toast.LENGTH_SHORT).show();
                }
            });
        }finally {
            /**
             * 关闭Scanner，文件输入流，套接字
             * 套接字装饰了输出流，所以不用关闭输出流
             */
            if(scan != null) {
                scan.close();
            }
            try {
                if(in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                in = null;
            }
            try {
                if(socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                socket = null;
            }
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    context.finishFinding(true);
                }
            });

        }
    }
}
